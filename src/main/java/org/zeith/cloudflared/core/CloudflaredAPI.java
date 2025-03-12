package org.zeith.cloudflared.core;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.regex.Matcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zeith.cloudflared.core.api.IGameProxy;
import org.zeith.cloudflared.core.api.IGameSession;
import org.zeith.cloudflared.core.exceptions.CloudflaredNotFoundException;
import org.zeith.cloudflared.core.process.CFDAccess;
import org.zeith.cloudflared.core.process.CFDTunnel;
import org.zeith.cloudflared.core.process.ShutdownTunnels;
import org.zeith.cloudflared.core.util.CloudflaredUtils;
import org.zeith.cloudflared.core.util.CloudflaredVersion;
import org.zeith.cloudflared.core.util.OSArch;

public class CloudflaredAPI {

    private static final Logger LOG = LogManager.getLogger("CloudflaredAPI");
    protected final CloudflaredAPIFactory configs;

    public CloudflaredAPIFactory getConfigs() {
        return this.configs;
    }

    private final Map<String, CFDAccess> allAccesses = new ConcurrentHashMap<>();

    protected CloudflaredVersion version;
    protected final Supplier<String> executable;
    protected final File executableFilePath;

    public Supplier<String> getExecutable() {
        return this.executable;
    }

    public File getExecutableFilePath() {
        return this.executableFilePath;
    }

    static {
        Runtime.getRuntime()
            .addShutdownHook(new ShutdownTunnels());
    }

    @SuppressWarnings({ "ResultOfMethodCallIgnored", "LoggingSimilarMessage" })
    protected CloudflaredAPI(CloudflaredAPIFactory configs) throws CloudflaredNotFoundException {
        this.configs = configs;

        File extraDataDir = getGame().getExtraDataDir();
        if (extraDataDir.isFile()) extraDataDir.delete();
        extraDataDir.mkdirs();

        this.executableFilePath = new File(
            extraDataDir,
            "cloudflared" + ((OSArch.getArchitecture()
                .getType() == OSArch.OSType.WINDOWS) ? ".exe" : ""));

        this.executable = this.executableFilePath::getAbsolutePath;

        CloudflaredVersion ver = null;
        if (this.executableFilePath.isFile()) {

            try {
                LOG.info("Attempting to get current version of cloudflared...");
                ver = getVersionFuture().join();
            } catch (Throwable e) {

                LOG.info("Failed to get version of cloudflared.");
            }
        }
        if (ver == null) {

            LOG.info("Attempting to auto-download CloudflaredForge...");

            try {
                CloudflaredUtils.download(configs.getGameProxy())
                    .join();
            } catch (Throwable e) {

                LOG.info("Failed to download cloudflared.");
                throw new CloudflaredNotFoundException("CloudflaredForge not found", e);
            }

            try {
                ver = getVersionFuture().join();
            } catch (Throwable e) {

                LOG.info("Failed to get version of cloudflared.");
                throw new CloudflaredNotFoundException("CloudflaredForge not found", e);
            }
        } else {

            this.version = ver;
            CloudflaredUtils.download(configs.getGameProxy())
                .thenCompose(i -> getVersionFuture())
                .thenAccept(ver2 -> {
                    if (!Objects.equals(this.version, ver2)) {
                        LOG.info("CloudflaredForge has been updated from {} to {}.", this.version, ver2);

                        this.version = ver2;
                    }
                });
        }

        this.version = ver;
        CloudflaredAPIListeners listeners = new CloudflaredAPIListeners(this);
        getGame().addListener(listeners);
        LOG.info("API created. Version: {}", ver);
    }

    public IGameProxy getGame() {
        return this.configs.getGameProxy();
    }

    private CompletableFuture<CloudflaredVersion> getVersionFuture() {
        return CloudflaredUtils.processOut(new ProcessBuilder(getExecutable().get(), "--version"), c -> (c == 0))
            .thenApply(s -> {
                Matcher matcher = CloudflaredVersion.CFD_VER_REGEX.matcher(s);
                if (!matcher.find()) {
                    throw new RuntimeException("Unable to find cloudflared version pattern.");
                }
                return new CloudflaredVersion(matcher.group("version"), matcher.group("built"));
            });
    }

    public static CloudflaredAPI create(CloudflaredAPIFactory configs) throws CloudflaredNotFoundException {
        return new CloudflaredAPI(configs);
    }

    public CFDTunnel createTunnel(IGameSession session, int port, String hostname) {
        return new CFDTunnel(session, this, port, hostname);
    }

    public CFDAccess getExistingAccess(String hostname) {
        hostname = hostname.toLowerCase(Locale.ROOT);
        CFDAccess access = this.allAccesses.get(hostname);
        if (access != null && !access.isAlive()) this.allAccesses.remove(hostname);
        return access;
    }

    public CFDAccess getOrOpenAccess(String hostname) {
        CFDAccess open = getExistingAccess(hostname);
        if (open != null) return open;
        CFDAccess access = new CFDAccess(this, hostname, pickRandomLocalPort());
        access.start();
        this.allAccesses.put(hostname.toLowerCase(Locale.ROOT), access);
        return access;
    }

    public int pickRandomLocalPort() {
        int i;
        try (ServerSocket serversocket = new ServerSocket(0)) {

            i = serversocket.getLocalPort();
        } catch (IOException e) {

            throw new RuntimeException(e);
        }
        return i;
    }

    public void closeAllAccesses() {
        for (CFDAccess value : this.allAccesses.values()) value.closeTunnel();
        this.allAccesses.clear();
    }
}
