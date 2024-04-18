package org.zeith.cloudflared.forge1122.proxy;

import net.minecraftforge.fml.common.event.*;
import org.zeith.cloudflared.core.api.*;
import org.zeith.cloudflared.forge1122.MCGameSession1122;

public interface CommonProxy1122
		extends IGameProxy
{
	void preInit(FMLPreInitializationEvent e);
	
	void serverStarted(FMLServerAboutToStartEvent e);
	
	void serverStop(FMLServerStoppingEvent e);
	
	void startSession(MCGameSession1122 session);
}