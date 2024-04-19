package org.zeith.cloudflared.core.process;

import com.google.common.base.Suppliers;
import lombok.Getter;
import org.apache.logging.log4j.*;
import org.zeith.cloudflared.core.CloudflaredAPI;
import org.zeith.cloudflared.core.api.TunnelThreadGroup;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class CFDAccess
		extends Thread
		implements ITunnel
{
	private static final Logger LOG = LogManager.getLogger("CloudflaredAccess");
	
	private final Supplier<Process> process;
	private Process startedProcess;
	
	@Getter
	protected final CloudflaredAPI api;
	
	@Getter
	protected final int localPort;
	
	@Getter
	protected CompletableFuture<Integer> openFuture = new CompletableFuture<>();
	
	public CFDAccess(CloudflaredAPI api, String hostname, int localPort)
	{
		super(TunnelThreadGroup.GROUP, "CFDAccessThread[Ingress=" + hostname + "->OnPort=" + localPort + "]");
		this.api = api;
		this.localPort = localPort;
		this.process = Suppliers.memoize(() ->
		{
			String localAddr = "127.0.0.1:" + localPort;
			
			LOG.info("Starting access point from {} to {}...", hostname, localAddr);
			try
			{
				List<String> args = new ArrayList<>();
				
				args.add(api.getExecutable().get());
				args.add("access");
				args.add("tcp");
				args.add("--hostname");
				args.add(hostname);
				args.add("--url");
				args.add(localAddr);
				
				Process p = new ProcessBuilder(args.toArray(new String[0]))
						.redirectInput(ProcessBuilder.Redirect.INHERIT)
						.redirectOutput(ProcessBuilder.Redirect.INHERIT)
						.start();
				
				this.startedProcess = p;
				
				LOG.info("Access to {} on port {} started.", hostname, localAddr);
				
				return p;
			} catch(IOException e)
			{
				throw new RuntimeException(e);
			}
		});
	}
	
	@Override
	public void run()
	{
		startedProcess = this.process.get();
		
		try(Scanner in = new Scanner(startedProcess.getErrorStream()))
		{
			boolean qtf = false;
			
			while(in.hasNextLine())
			{
				String ln = in.nextLine();
				
				if(!qtf)
				{
					qtf = true;
					openFuture.complete(localPort);
				}
				
				System.out.println(ln.split("\\s", 3)[2]);
			}
			
			startedProcess.waitFor();
			startedProcess = null;
		} catch(InterruptedException ignored)
		{
			LOG.error("Access forcefully interrupted.");
		} catch(Exception e)
		{
			LOG.error("Failed to launch access:", e);
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
				}
			} catch(Exception e)
			{
				LOG.error("Failed to wait until tunnel shutdown. Sending force-destroy instruction.");
				startedProcess.destroyForcibly();
				startedProcess = null;
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