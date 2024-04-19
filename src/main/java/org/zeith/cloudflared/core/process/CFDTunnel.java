package org.zeith.cloudflared.core.process;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zeith.cloudflared.core.CloudflaredAPI;
import org.zeith.cloudflared.core.api.IGameSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CFDTunnel
		extends BaseTunnel
{
	public static final Pattern URL_REGEX = Pattern.compile("https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)");
	private static final Logger LOG = LogManager.getLogger("CloudflaredTunnel");
	
	@Getter
	protected final IGameSession session;
	
	protected final String hostname;
	protected final int port;
	
	@Getter
	private String generatedHostname;
	
	private boolean waitingForHostname = false;
	private boolean registered = false;
	
	public CFDTunnel(IGameSession session, CloudflaredAPI api, int port, String hostname)
	{
		super(api, "CFDTunnelThread[Ingress=" + port + "->Egress=" + hostname + "]");
		this.hostname = hostname;
		this.port = port;
		this.session = session;
	}
	
	@Override
	protected Process createProcess()
	{
		String localAddr = "tcp://127.0.0.1:" + port;
		
		LOG.info("Starting tunnel pointing to {}...", localAddr);
		try
		{
			List<String> args = new ArrayList<>();
			
			args.add(api.getExecutable().get());
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
					.redirectOutput(ProcessBuilder.Redirect.INHERIT)
					.start();
			
			this.startedProcess = p;
			
			LOG.info("Tunnel to {} started.", localAddr);
			
			return p;
		} catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	@Override
	protected void markOpen()
	{
		session.onTunnelOpen(this);
		super.markOpen();
	}
	
	@Override
	protected void processLn(String ln)
	{
		if(!registered)
		{
			if(ln.contains("Registered"))
			{
				markOpen();
				registered = true;
			}
			
			if(ln.contains("Failed"))
			{
				api.getGame().sendChatMessage(ln.replaceAll("\\d+\\.\\d+\\.\\d+\\.\\d+", "*.*.*.*"));
			}
			
			if(ln.contains("Retrying"))
			{
				api.getGame().sendChatMessage(ln.replaceAll("\\d+\\.\\d+\\.\\d+\\.\\d+", "*.*.*.*"));
			}
		}
		
		if(generatedHostname == null)
		{
			if(ln.contains("Visit it at"))
			{
				Matcher m = URL_REGEX.matcher(ln);
				if(m.find())
				{
					generatedHostname = m.group();
					waitingForHostname = false;
					return;
				}
				waitingForHostname = true;
			}
			
			if(waitingForHostname)
			{
				Matcher m = URL_REGEX.matcher(ln);
				if(m.find())
				{
					generatedHostname = m.group();
					waitingForHostname = false;
					return;
				}
			}
		}
		
		System.out.println(ln);
	}
}