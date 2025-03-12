package org.zeith.cloudflared.forge1710.proxy;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerAddress;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.ClientCommandHandler;

import org.zeith.cloudflared.core.CloudflaredAPI;
import org.zeith.cloudflared.core.CloudflaredAPIFactory;
import org.zeith.cloudflared.core.api.IFileDownload;
import org.zeith.cloudflared.core.api.IGameListener;
import org.zeith.cloudflared.core.api.IGameSession;
import org.zeith.cloudflared.core.exceptions.CloudflaredNotFoundException;
import org.zeith.cloudflared.core.process.CFDAccess;
import org.zeith.cloudflared.forge1710.CloudflaredForge;
import org.zeith.cloudflared.forge1710.Configs1710;
import org.zeith.cloudflared.forge1710.MCGameSession1710;
import org.zeith.cloudflared.forge1710.command.CommandCloudflared;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ProgressManager;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;

@SuppressWarnings("deprecation")
public class ClientProxy implements CommonProxy {

    private static final List<IChatComponent> toSend = new ArrayList<>();

    static private final ExecutorService executor;
    static {
        executor = Executors.newSingleThreadExecutor();
    }

    protected final List<IGameListener> listeners = new ArrayList<>();
    private CloudflaredAPI api;
    public IGameSession startedSession;
    protected Thread gameThread;
    public static Field pbMsg;
    public static Field pbLastTime;

    public static void onSharedToLan(IntegratedServer server, int port) {
        // noinspection SimplifyOptionalCallChains
        if (!CloudflaredForge.PROXY.getApi()
            .isPresent()) return;
        server.setAllowPvp(Configs1710.enablePvP);
        server.setOnlineMode(Configs1710.onlineMode);
        server.setCanSpawnAnimals(Configs1710.canSpawnAnimals);
        server.setCanSpawnNPCs(Configs1710.canSpawnNPCs);
        if (Configs1710.startTunnel) {
            Minecraft mc = Minecraft.getMinecraft();
            mc.ingameGUI.getChatGUI()
                .printChatMessage(new ChatComponentTranslation("chat.cloudflared:starting_tunnel"));
            CloudflaredForge.PROXY.startSession(
                new MCGameSession1710(
                    port,
                    mc.thePlayer.getGameProfile()
                        .getId(),
                    mc.thePlayer));
        }
    }

    public void tryCreateApi() {
        try {
            this.api = CloudflaredAPIFactory.builder()
                .gameProxy(this)
                .hostname(() -> Configs1710.hostname)
                .build()
                .createApi();
        } catch (CloudflaredNotFoundException e) {
            CloudflaredForge.LOG.error("Failed to create CloudflaredForge API!", e);
        }
    }

    public IFileDownload pushFileDownload() {
        if (this.gameThread != Thread.currentThread()) return IFileDownload.DUMMY;
        return new IFileDownload() {

            ProgressManager.ProgressBar bar;
            final int totalSteps = 1000;
            int stepsLeft = 1000;
            long lastUpdateLen;
            long lastUpdateMs;

            public void onStart() {
                onEnd();
                this.bar = ProgressManager.push("Downloading CloudflaredForge", totalSteps);
            }

            public void onUpload(long uploaded, long total) {
                long bpp = total / totalSteps;
                if (uploaded - this.lastUpdateLen > bpp) {
                    if (this.stepsLeft > 0) {
                        this.bar.step(ClientProxy.getSizeString(uploaded) + " / " + ClientProxy.getSizeString(total));
                        this.stepsLeft--;
                    }
                    this.lastUpdateLen = uploaded;
                } else if (System.currentTimeMillis() - this.lastUpdateMs > 50L) {
                    ClientProxy.setMessage(
                        this.bar,
                        ClientProxy.getSizeString(uploaded) + " / " + ClientProxy.getSizeString(total));
                    this.lastUpdateMs = System.currentTimeMillis();
                }
            }

            public void onEnd() {
                if (this.bar == null) return;
                while (this.bar.getStep() < this.bar.getSteps()) this.bar.step("");
                ProgressManager.pop(this.bar);
                this.bar = null;
            }
        };
    }

    public void preInit(FMLPreInitializationEvent event) {
        this.gameThread = Thread.currentThread();
        CommonProxy.super.preInit(event);
        if (this.api == null) {
            toSend.add(
                (new ChatComponentTranslation("chat.cloudflared:not_installed")).appendText(" ")
                    .appendSibling(
                        (new ChatComponentTranslation("chat.cloudflared:not_installed.click")).setChatStyle(
                            (new ChatStyle()).setColor(EnumChatFormatting.BLUE)
                                .setUnderlined(true)
                                .setChatHoverEvent(
                                    new HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        new ChatComponentText("/cloudflared install")))
                                .setChatClickEvent(
                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cloudflared install")))));
        }
        ClientCommandHandler.instance.registerCommand(new CommandCloudflared());
    }

    public void serverStarted(FMLServerAboutToStartEvent event) {
        if (event.getServer() instanceof IntegratedServer && this.api != null) {
            this.api.closeAllAccesses();
        }
        for (IChatComponent message : toSend) {
            Minecraft.getMinecraft().ingameGUI.getChatGUI()
                .printChatMessage(message);
        }
    }

    public void serverStop(FMLServerStoppingEvent event) {
        if (this.startedSession != null) {
            for (IGameListener listener : this.listeners) listener.onHostingEnd(this.startedSession);
            this.startedSession = null;
        }
    }

    public void startSession(MCGameSession1710 session) {
        this.startedSession = session;
        for (IGameListener listener : this.listeners) {
            listener.onHostingStart(session);
        }
    }

    public Optional<CloudflaredAPI> getApi() {
        return Optional.ofNullable(this.api);
    }

    public List<IGameListener> getListeners() {
        return Collections.unmodifiableList(this.listeners);
    }

    public ExecutorService getBackgroundExecutor() {
        return ClientProxy.executor;
    }

    public void addListener(IGameListener listener) {
        this.listeners.add(listener);
    }

    public void sendChatMessage(String string) {
        if ((Minecraft.getMinecraft()).thePlayer != null) {
            (Minecraft.getMinecraft()).ingameGUI.getChatGUI()
                .printChatMessage(new ChatComponentTranslation(string));
        } else {
            toSend.add(new ChatComponentTranslation(string));
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public File getExtraDataDir() {
        File f = new File((Minecraft.getMinecraft()).mcDataDir, "asm" + File.separator + "CloudflaredForge");
        if (f.isFile()) f.delete();
        if (!f.isDirectory()) f.mkdirs();
        return f;
    }

    @Nullable
    public static Integer pickPort() {
        if (Configs1710.customPortOverride > 0 && Configs1710.customPortOverride < 65535)
            return Configs1710.customPortOverride;
        return null;
    }

    public static ServerAddress decodeAddress(String input) {
        if (!input.startsWith("cloudflared://")) return null;
        String hostname = input.substring(14);
        CFDAccess tunnel = CloudflaredForge.PROXY.getApi()
            .map(a -> a.getOrOpenAccess(hostname))
            .orElse(null);
        if (tunnel == null) return null;
        int openPort = tunnel.getOpenFuture()
            .join();
        return ServerAddress.func_78860_a("127.0.0.1:" + openPort);
    }

    public static void setMessage(ProgressManager.ProgressBar bar, String msg) {
        if (pbMsg == null) pbMsg = ClientProxy.getField("message");
        if (pbLastTime == null) {
            pbLastTime = ClientProxy.getField("lastTime");
        }
        try {
            msg = FMLCommonHandler.instance()
                .stripSpecialChars(msg);
            pbMsg.set(bar, msg);
            pbLastTime.setLong(bar, System.nanoTime());
            FMLCommonHandler.instance()
                .processWindowMessages();
        } catch (IllegalAccessException e) {
            CloudflaredForge.LOG.error("Failed to set progress bar message!", e);
        }
    }

    private static Field getField(String name) {
        Field field = null;
        try {
            field = ProgressManager.ProgressBar.class.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            CloudflaredForge.LOG
                .error("Failed to get field {} from class {}", name, ProgressManager.ProgressBar.class.getName(), e);
            return null;
        }
        field.setAccessible(true);
        return field;
    }

    private static String getSizeString(long bytes) {
        String[] suffixes = new String[] { "B", "KB", "MB", "GB", "TB", "PB" };
        for (int i = 0; i < 5; i++) {
            if (bytes < Math.pow(1024, i + 1)) {
                String fstring = i > 1 ? "%,.2f %s" : "%,f %s";
                return String.format(fstring, bytes / Math.pow(1024, i), suffixes[i]);
            }
        }
        return String.format("%,.2f %s", bytes / Math.pow(1024, 5), suffixes[5]);
    }
}
