package org.zeith.cloudflared.core.process;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zeith.cloudflared.core.CloudflaredAPI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CFDAccess
		extends BaseTunnel
{
	private static final Logger LOG = LogManager.getLogger("CloudflaredAccess");
	
	@Getter
	protected final int localPort;
	
	@Getter
	protected CompletableFuture<Integer> openFuture = new CompletableFuture<>();
	
	protected final String hostname;
	
	public CFDAccess(CloudflaredAPI api, String hostname, int localPort)
	{
		super(api, "CFDAccessThread[Ingress=" + hostname + "->OnPort=" + localPort + "]");
		this.hostname = hostname;
		this.localPort = localPort;
	}
	
	@Override
	protected Process createProcess()
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
	}
	
	protected boolean hasBeenOpened = false;
	
	@Override
	protected void processLn(String ln)
	{
		if(!hasBeenOpened)
		{
			hasBeenOpened = true;
			openFuture.complete(localPort);
			markOpen();
		}
		
		System.out.println(ln);
	}
}