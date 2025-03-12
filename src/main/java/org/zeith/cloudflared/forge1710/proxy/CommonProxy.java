package org.zeith.cloudflared.forge1710.proxy;

import java.io.File;
import java.util.Optional;

import net.minecraftforge.common.config.Configuration;

import org.zeith.cloudflared.core.CloudflaredAPI;
import org.zeith.cloudflared.core.api.IGameProxy;
import org.zeith.cloudflared.forge1710.Configs1710;
import org.zeith.cloudflared.forge1710.MCGameSession1710;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;

public interface CommonProxy extends IGameProxy {

    void tryCreateApi();

    default void preInit(FMLPreInitializationEvent event) {
        Configs1710.load(new Configuration(event.getSuggestedConfigurationFile()));
        tryCreateApi();
    }

    default void serverStarting(FMLServerStartingEvent event) {}

    void serverStarted(FMLServerAboutToStartEvent event);

    void serverStop(FMLServerStoppingEvent event);

    void startSession(MCGameSession1710 event);

    Optional<CloudflaredAPI> getApi();

    default File getLatestLogFile() {
        return new File("logs");
    }
}
