package org.zeith.cloudflared.core.api;

import org.apache.logging.log4j.*;

public class TunnelThreadGroup
		extends ThreadGroup
{
	private static final Logger LOG = LogManager.getLogger("CloudflaredTunnel");
	public static final TunnelThreadGroup GROUP = new TunnelThreadGroup("CloudflaredTunnels");
	
	public TunnelThreadGroup(String name)
	{
		super(name);
		setMaxPriority(Thread.MIN_PRIORITY);
	}
	
	@Override
	public void uncaughtException(Thread t, Throwable e)
	{
		LOG.error("Uncaught tunnel exception on tunnel {}:", t.getName(), e);
	}
}