package org.zeith.cloudflared.core.process;

import com.google.common.base.Suppliers;
import lombok.Getter;
import org.apache.logging.log4j.*;
import org.zeith.cloudflared.core.CloudflaredAPI;
import org.zeith.cloudflared.core.api.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.regex.*;

public class CFDTunnel
		extends Thread
		implements ITunnel
{
	public static final Pattern URL_REGEX = Pattern.compile("https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)");
	private static final Logger LOG = LogManager.getLogger("CloudflaredTunnel");
	
	@Getter
	protected final CloudflaredAPI api;
	
	@Getter
	protected final IGameSession session;
	
	private final Supplier<Process> process;
	private Process startedProcess;
	
	@Getter
	private String generatedHostname;
	
	public CFDTunnel(IGameSession session, CloudflaredAPI api, int port, String hostname)
	{
		super(TunnelThreadGroup.GROUP, "CFDTunnelThread[Ingress=" + port + "->Egress=" + hostname + "]");
		this.session = session;
		this.api = api;
		this.process = Suppliers.memoize(() ->
		{
			String localAddr = "tcp://127.0.0.1:" + port;
			
			LOG.info("Starting tunnel pointing to {}...", localAddr);
			try
			{
				List<String> args = new ArrayList<>();
				
				args.add(api.getConfigs().getExecutable().get());
				args.add("tunnel");
				if(hostname != null && !hostname.isEmpty())
				{
					args.add("--hostname");
					args.add(hostname);
				}
				args.add("--url");
				args.add(localAddr);
				
				Process p = new ProcessBuilder(args.toArray(new String[0]))
						.redirectInput(ProcessBuilder.Redirect.INHERIT)
						.start();
				
				this.startedProcess = p;
				
				LOG.info("Tunnel to {} started.", localAddr);
				
				return p;
			} catch(IOException e)
			{
				throw new RuntimeException(e);
			}
		});
	}
	
	void markOpen()
	{
		session.onTunnelOpen(this);
	}
	
	@Override
	public void run()
	{
		startedProcess = this.process.get();
		try(Scanner in = new Scanner(startedProcess.getErrorStream()))
		{
			boolean qtf = false;
			boolean registered = false;
			
			while(in.hasNextLine())
			{
				String ln = in.nextLine();
				
				if(!registered && ln.contains("Registered tunnel connection"))
				{
					markOpen();
					registered = true;
				}
				
				if(generatedHostname == null)
				{
					if(ln.contains("Visit it at"))
					{
						Matcher m = URL_REGEX.matcher(ln);
						if(m.find())
						{
							generatedHostname = m.group();
							qtf = false;
							continue;
						}
						qtf = true;
					}
					
					if(qtf)
					{
						Matcher m = URL_REGEX.matcher(ln);
						if(m.find())
						{
							generatedHostname = m.group();
							qtf = false;
							continue;
						}
					}
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