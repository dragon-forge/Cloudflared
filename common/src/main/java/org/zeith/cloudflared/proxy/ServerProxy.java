package org.zeith.cloudflared.proxy;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import org.zeith.cloudflared.CloudflaredConfig;
import org.zeith.cloudflared.CloudflaredMod;
import org.zeith.cloudflared.architectury.MCArchGameSession;
import org.zeith.cloudflared.core.CloudflaredAPI;
import org.zeith.cloudflared.core.CloudflaredAPIFactory;
import org.zeith.cloudflared.core.api.IGameListener;
import org.zeith.cloudflared.core.api.IGameSession;
import org.zeith.cloudflared.core.api.InfoLevel;
import org.zeith.cloudflared.core.exceptions.CloudflaredNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ServerProxy
        implements CommonProxy {
    protected final List<IGameListener> listeners = new ArrayList<>();
    public IGameSession startedSession;
    protected MinecraftServer server;
    private CloudflaredAPI api;

    @Override
    public void tryCreateApi() {
        try {
            api = CloudflaredAPIFactory.builder()
                    .gameProxy(this)
                    .hostname(() -> CloudflaredConfig.getInstance().advancedNetwork.hostname)
                    .build()
                    .createApi();
        } catch (CloudflaredNotFoundException ex) {
            CloudflaredMod.LOG.fatal("Unable to create communicate with cloudflared. Are you sure you have cloudflared installed?", ex);
        }
    }

    @Override
    public void startSession(MCArchGameSession session) {
        startedSession = session;
        for (IGameListener listener : listeners)
            listener.onHostingStart(session);
    }

    @Override
    public Optional<CloudflaredAPI> getApi() {
        return Optional.ofNullable(api);
    }

    @Override
    public void serverStarted(MinecraftServer server) {
        if (api != null) api.closeAllAccesses();
        if (CloudflaredConfig.getInstance().hosting.startTunnel) {
            server.sendMessage(new TranslatableComponent("chat.cloudflared:starting_tunnel"), UUID.randomUUID());
            startSession(new MCArchGameSession(server.getPort(), UUID.randomUUID(), server));
        }
    }

    @Override
    public void serverStop() {
        server = null;
        if (startedSession != null) {
            for (IGameListener listener : listeners)
                listener.onHostingEnd(startedSession);
            startedSession = null;
        }
    }

    @Override
    public void addListener(IGameListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(IGameListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void sendChatMessage(String message) {
        server.sendMessage(new TranslatableComponent(message), UUID.randomUUID());
    }

    @Override
    public void createToast(InfoLevel level, String title, String subtitle) {
        server.sendMessage(new TranslatableComponent(title), UUID.randomUUID());
    }
}