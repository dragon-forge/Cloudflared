package org.zeith.cloudflared.core;

import lombok.Getter;
import org.apache.logging.log4j.*;
import org.zeith.cloudflared.core.api.*;
import org.zeith.cloudflared.core.exceptions.CloudflaredNotFoundException;
import org.zeith.cloudflared.core.process.*;
import org.zeith.cloudflared.core.util.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;

import static org.zeith.cloudflared.core.util.CloudflaredVersion.CFD_VER_REGEX;

@Getter
public class CloudflaredAPI
{
	private static final Logger LOG = LogManager.getLogger("CloudflaredAPI");
	protected final CloudflaredAPIFactory configs;
	
	private final Map<String, CFDAccess> allAccesses = new ConcurrentHashMap<>();
	private final CloudflaredAPIListeners listeners = new CloudflaredAPIListeners(this);
	protected final CloudflaredVersion version;
	
	static
	{
		Runtime.getRuntime().addShutdownHook(new ShutdownTunnels());
	}
	
	protected CloudflaredAPI(CloudflaredAPIFactory configs)
			throws CloudflaredNotFoundException
	{
		this.configs = configs;
		
		String exe = configs.getExecutable().get();
		
		CloudflaredVersion ver = null;
		try
		{
			LOG.info("Attempting to get current version of {}...", exe);
			ver = getVersionFuture().join();
		} catch(Throwable e)
		{
			LOG.info("Failed to get version of {}.", exe);
			throw new CloudflaredNotFoundException("Cloudflared not found");
		}
		
		if(ver == null && configs.isAutoDownload())
		{
			LOG.info("Attempting to auto-download Cloudflared...");
			CloudflaredUtils.winget().join();
			ver = getVersionFuture().join();
		}
		
		this.version = ver;
		getGame().addListener(listeners);
		LOG.info("API created. Version: {}", ver);
	}
	
	public IGameProxy getGame()
	{
		return configs.getGameProxy();
	}
	
	private CompletableFuture<CloudflaredVersion> getVersionFuture()
	{
		return CloudflaredUtils.processOut(new ProcessBuilder(configs.getExecutable().get(), "--version"), c -> c == 0).thenApply(s ->
		{
			Matcher matcher = CFD_VER_REGEX.matcher(s);
			if(!matcher.find()) throw new RuntimeException("Unable to find cloudflared version pattern.");
			return new CloudflaredVersion(matcher.group("version"), matcher.group("built"));
		});
	}
	
	public static CloudflaredAPI create(CloudflaredAPIFactory configs)
			throws CloudflaredNotFoundException
	{
		return new CloudflaredAPI(configs);
	}
	
	public CFDTunnel createTunnel(IGameSession session, int port, String hostname)
	{
		return new CFDTunnel(session, this, port, hostname);
	}
	
	public CFDAccess getExistingAccess(String hostname)
	{
		hostname = hostname.toLowerCase(Locale.ROOT);
		CFDAccess access = allAccesses.get(hostname);
		if(access != null && !access.isAlive()) allAccesses.remove(hostname);
		return access;
	}
	
	public CFDAccess getOrOpenAccess(String hostname)
	{
		CFDAccess open = getExistingAccess(hostname);
		if(open != null) return open;
		CFDAccess access = new CFDAccess(this, hostname, pickRandomLocalPort());
		access.start();
		allAccesses.put(hostname.toLowerCase(Locale.ROOT), access);
		return access;
	}
	
	public int pickRandomLocalPort()
	{
		int i;
		try(ServerSocket serversocket = new ServerSocket(0))
		{
			i = serversocket.getLocalPort();
		} catch(IOException e)
		{
			throw new RuntimeException(e);
		}
		return i;
	}
	
	public void closeAllAccesses()
	{
		for(CFDAccess value : allAccesses.values())
			value.closeTunnel();
		allAccesses.clear();
	}
}