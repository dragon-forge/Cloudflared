package org.zeith.cloudflared;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zeith.cloudflared.proxy.CommonProxy;

import java.io.IOException;
import java.nio.file.Path;

public class CloudflaredMod
{
	public static final Logger LOG = LogManager.getLogger("CloudflaredAPI/Mod");
	public static final String MOD_ID = "cloudflared";
	
	public static CommonProxy PROXY;
	
	public static void init(Path configFile)
	{
		PROXY.setup();
		
		try
		{
			CloudflaredConfig.configFile = configFile;
			CloudflaredConfig.load();
		} catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}