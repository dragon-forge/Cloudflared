package org.zeith.cloudflared.forge;

import org.zeith.cloudflared.CloudflaredMod;
import org.zeith.cloudflared.proxy.ServerProxy;

public class CloudflaredForgeServer
{
	static void setup()
	{
		CloudflaredMod.PROXY = new ServerProxy();
	}
}