package org.zeith.cloudflared.proxy;

import net.minecraft.Util;
import net.minecraft.server.MinecraftServer;
import org.zeith.cloudflared.architectury.MCArchGameSession;
import org.zeith.cloudflared.core.CloudflaredAPI;
import org.zeith.cloudflared.core.api.IGameProxy;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

public interface CommonProxy
		extends IGameProxy
{
	void tryCreateApi();
	
	default void setup()
	{
		tryCreateApi();
	}
	
	default void serverStarting(MinecraftServer server) {}
	
	void serverStarted(MinecraftServer server);
	
	void serverStop();
	
	void startSession(MCArchGameSession session);
	
	Optional<CloudflaredAPI> getApi();
	
	default File getLatestLogFile()
	{
		return new File("logs");
	}
	
	@Override
	default ExecutorService getBackgroundExecutor()
	{
		return Util.backgroundExecutor();
	}
}