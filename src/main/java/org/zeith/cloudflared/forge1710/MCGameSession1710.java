package org.zeith.cloudflared.forge1710;

import java.util.UUID;

import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import org.zeith.cloudflared.core.api.MCGameSession;
import org.zeith.cloudflared.core.process.CFDTunnel;

public class MCGameSession1710 extends MCGameSession {

    public MCGameSession1710(int serverPort, UUID host, ICommandSender owner) {
        super(serverPort, host);
        this.owner = owner;
    }

    protected final ICommandSender owner;

    public void onTunnelOpen(CFDTunnel tunnel) {
        String hostnameSTR = tunnel.getGeneratedHostname();
        if (hostnameSTR == null) {
            hostnameSTR = tunnel.getApi()
                .getConfigs()
                .getHostname()
                .get();
        }

        if (hostnameSTR != null && hostnameSTR.isEmpty()) hostnameSTR = null;
        if (hostnameSTR == null) {
            IChatComponent txt = (new ChatComponentTranslation("chat.cloudflared:game_logs")).setChatStyle(
                (new ChatStyle()).setColor(EnumChatFormatting.BLUE)
                    .setUnderlined(true)
                    .setChatHoverEvent(
                        new HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            new ChatComponentTranslation("chat.cloudflared:click_to_open")))
                    .setChatClickEvent(
                        new ClickEvent(
                            ClickEvent.Action.OPEN_FILE,
                            CloudflaredForge.PROXY.getLatestLogFile()
                                .getAbsolutePath())));
            this.owner.addChatMessage(new ChatComponentTranslation("chat.cloudflared:tunnel_open_unknown", txt));
            return;
        }
        if (hostnameSTR.contains("://")) {
            hostnameSTR = "cloudflared://" + hostnameSTR.substring(hostnameSTR.indexOf("://") + 3);
        }
        IChatComponent hostname = (new ChatComponentText(hostnameSTR)).setChatStyle(
            (new ChatStyle()).setColor(EnumChatFormatting.BLUE)
                .setUnderlined(true)
                .setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, hostnameSTR))
                .setChatHoverEvent(
                    new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        new ChatComponentTranslation("chat.cloudflared:click_to_suggest"))));
        this.owner.addChatMessage(new ChatComponentTranslation("chat.cloudflared:tunnel_open", hostname));
        CloudflaredForge.LOG.warn("Game tunnel open: {}", hostnameSTR);
    }
}
