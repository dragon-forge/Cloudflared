package org.zeith.cloudflared.forge1710;

import net.minecraftforge.common.config.Configuration;

public class Configs1710 {

    public static int customPortOverride;
    public static boolean enablePvP;
    public static boolean onlineMode;
    public static boolean canSpawnAnimals;
    public static boolean canSpawnNPCs;
    public static String hostname = "";
    public static boolean startTunnel = true;

    public static void load(Configuration config) {
        customPortOverride = config.getInt(
            "Custom Port Override",
            "Hosting",
            0,
            0,
            65534,
            "Which port should be forced when opening world to LAN? Keep at 0 to retain Vanilla behavior.");
        enablePvP = config
            .getBoolean("Enable PvP", "Hosting", true, "Should PvP be enabled on the shared to LAN server?");
        onlineMode = config
            .getBoolean("Online Mode", "Hosting", true, "Should online mode be active when hosting a LAN server?");
        canSpawnAnimals = config.getBoolean(
            "Can Spawn Animals",
            "Hosting",
            true,
            "Should animals be allowed to spawn on a hosted a LAN server?");
        canSpawnNPCs = config
            .getBoolean("Can Spawn NPCs", "Hosting", true, "Should NPCs be allowed to spawn on a hosted a LAN server?");
        hostname = config.getString(
            "Cloudflare Hostname",
            "Network: Advanced",
            "",
            "Which host should the cloudflared tunnel be configured to?\nIf your cloudflared is not authorized, this won't work.");
        startTunnel = config.getBoolean(
            "Start Tunnel",
            "Hosting",
            true,
            "Should Argo Tunnel be started whenever the hosting session starts?");
        if (config.hasChanged()) {
            config.save();
        }
    }
}
