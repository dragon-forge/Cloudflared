package org.zeith.cloudflared.forge1122.proxy;

import net.minecraftforge.fml.common.event.*;
import org.zeith.cloudflared.core.CloudflaredAPI;
import org.zeith.cloudflared.core.api.*;
import org.zeith.cloudflared.forge1122.MCGameSession1122;

import java.io.File;
import java.util.Optional;

public interface CommonProxy1122
		extends IGameProxy
{
	void tryCreateApi();
	
	default void preInit(FMLPreInitializationEvent e)
	{
		tryCreateApi();
	}
	
	default void serverStarting(FMLServerStartingEvent e) {}
	
	void serverStarted(FMLServerAboutToStartEvent e);
	
	void serverStop(FMLServerStoppingEvent e);
	
	void startSession(MCGameSession1122 session);
	
	Optional<CloudflaredAPI> getApi();
	
	default File getLatestLogFile()
	{
		return new File("logs");
	}
}