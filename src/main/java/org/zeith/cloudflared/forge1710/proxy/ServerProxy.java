package org.zeith.cloudflared.forge1710.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;

import org.zeith.cloudflared.core.CloudflaredAPI;
import org.zeith.cloudflared.core.CloudflaredAPIFactory;
import org.zeith.cloudflared.core.api.IGameListener;
import org.zeith.cloudflared.core.api.IGameSession;
import org.zeith.cloudflared.core.exceptions.CloudflaredNotFoundException;
import org.zeith.cloudflared.forge1710.CloudflaredForge;
import org.zeith.cloudflared.forge1710.Configs1710;
import org.zeith.cloudflared.forge1710.MCGameSession1710;
import org.zeith.cloudflared.forge1710.command.CommandCloudflared;

import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;

@SuppressWarnings("unused")
public class ServerProxy implements CommonProxy {

    static private final ExecutorService executor;
    static {
        executor = Executors.newSingleThreadExecutor();
    }

    protected final List<IGameListener> listeners = new ArrayList<>();
    private CloudflaredAPI api;
    public IGameSession startedSession;
    protected MinecraftServer server;

    public void tryCreateApi() {
        try {
            this.api = CloudflaredAPIFactory.builder()
                .gameProxy(this)
                .hostname(() -> Configs1710.hostname)
                .build()
                .createApi();
        } catch (CloudflaredNotFoundException e) {
            CloudflaredForge.LOG.error("CloudflaredAPI not found!", e);
        }
    }

    public void startSession(MCGameSession1710 session) {
        this.startedSession = session;
        for (IGameListener listener : this.listeners) {
            listener.onHostingStart(session);
        }
    }

    public List<IGameListener> getListeners() {
        return this.listeners;
    }

    public Optional<CloudflaredAPI> getApi() {
        return Optional.ofNullable(this.api);
    }

    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandCloudflared());
    }

    public void serverStarted(FMLServerAboutToStartEvent event) {
        this.server = event.getServer();
        if (this.api != null) this.api.closeAllAccesses();
        if (Configs1710.startTunnel) {
            this.server.addChatMessage(new ChatComponentTranslation("chat.cloudflared:starting_tunnel"));
            startSession(new MCGameSession1710(this.server.getServerPort(), UUID.randomUUID(), this.server));
        }
    }

    public void serverStop(FMLServerStoppingEvent event) {
        this.server = null;
        if (this.startedSession != null) {
            for (IGameListener listener : this.listeners) listener.onHostingEnd(this.startedSession);
            this.startedSession = null;
        }
    }

    public ExecutorService getBackgroundExecutor() {
        return ServerProxy.executor;
    }

    public void addListener(IGameListener listener) {
        this.listeners.add(listener);
    }

    public void sendChatMessage(String string) {
        this.server.addChatMessage(new ChatComponentTranslation(string));
    }
}
