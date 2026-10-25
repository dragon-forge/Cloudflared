package org.zeith.cloudflared.core.process;

import org.zeith.cloudflared.core.api.TunnelThreadGroup;

public class ShutdownTunnels
		extends Thread
{
	@Override
	public void run()
	{
		TunnelThreadGroup group = TunnelThreadGroup.GROUP;
		Thread[] ats = new Thread[group.activeCount()];
		group.enumerate(ats);
		for(Thread at : ats)
		{
			if(at instanceof ITunnel)
			{
				((ITunnel) at).closeTunnel();
			}
		}
	}
}