package org.zeith.cloudflared.core.process;

import com.google.common.base.Suppliers;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zeith.cloudflared.core.CloudflaredAPI;
import org.zeith.cloudflared.core.api.IGameListener;
import org.zeith.cloudflared.core.api.TunnelThreadGroup;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public abstract class BaseTunnel
		extends Thread
		implements ITunnel
{
	private static final Logger LOG = LogManager.getLogger("CloudflaredTunnels");
	protected final Supplier<Process> process = Suppliers.memoize(this::createProcess);
	
	protected Process startedProcess;
	
	@Getter
	protected final CloudflaredAPI api;
	
	public BaseTunnel(CloudflaredAPI api, String name)
	{
		super(TunnelThreadGroup.GROUP, name);
		this.api = api;
	}
	
	protected void markClosed()
	{
		if(startedProcess == null) return;
		startedProcess = null;
		for(IGameListener listener : api.getGame().getListeners())
			listener.onTunnelClosed(this);
	}
	
	protected void markOpen()
	{
		for(IGameListener listener : api.getGame().getListeners())
			listener.onTunnelOpened(this);
	}
	
	protected abstract Process createProcess();
	
	protected String preprocessLn(String ln)
	{
		return ln.split("\\s", 3)[2];
	}
	
	protected abstract void processLn(String ln);
	
	@Override
	public void run()
	{
		startedProcess = this.process.get();
		try(Scanner in = new Scanner(startedProcess.getErrorStream()))
		{
			while(in.hasNextLine())
				processLn(preprocessLn(in.nextLine()));
			
			if(startedProcess != null)
				startedProcess.waitFor();
			markClosed();
		} catch(InterruptedException ignored)
		{
			LOG.error("Access forcefully interrupted.");
		} catch(Exception e)
		{
			LOG.error("Failed to launch tunnel:", e);
		}
	}
	
	@Override
	public void interrupt()
	{
		if(startedProcess != null)
		{
			startedProcess.destroy();
			try
			{
				if(startedProcess.waitFor(10L, TimeUnit.SECONDS))
				{
					int code = startedProcess.exitValue();
					LOG.info("Tunnel stopped with exit value {}.", code);
					markClosed();
				}
			} catch(Exception e)
			{
				LOG.error("Failed to wait until tunnel shutdown. Sending force-destroy instruction.");
				startedProcess.destroyForcibly();
				markClosed();
			}
		}
		
		super.interrupt();
	}
	
	@Override
	public void closeTunnel()
	{
		interrupt();
	}
}