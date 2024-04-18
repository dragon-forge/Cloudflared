package org.zeith.cloudflared.core.util;

import com.zeitheron.hammercore.utils.java.OSArch;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.function.Predicate;

public class CloudflaredUtils
{
	public static CompletableFuture<Integer> process(ProcessBuilder pb, byte[] out)
	{
		Process proc;
		try
		{
			proc = pb.start();
		} catch(IOException e)
		{
			throw new RuntimeException(e);
		}
		
		if(out != null && out.length > 0)
			new Thread(() ->
			{
				OutputStream stream = proc.getOutputStream();
				try
				{
					stream.write(out);
					stream.flush();
				} catch(IOException e)
				{
				}
			}).start();
		
		return CompletableFuture.supplyAsync(() ->
		{
			try
			{
				return proc.waitFor();
			} catch(InterruptedException e)
			{
				throw new RuntimeException(e);
			}
		});
	}
	
	public static CompletableFuture<String> processOut(ProcessBuilder pb, Predicate<Integer> exitCodeFilter)
	{
		Process proc;
		try
		{
			proc = pb.start();
		} catch(IOException e)
		{
			throw new RuntimeException(e);
		}
		
		return CompletableFuture.supplyAsync(() ->
		{
			StringBuilder sb = new StringBuilder();
			try(Scanner in = new Scanner(proc.getInputStream()))
			{
				while(in.hasNextLine()) sb.append(in.nextLine()).append(System.lineSeparator());
				int ex = proc.waitFor();
				if(!exitCodeFilter.test(ex))
					throw new IOException("Exit code " + ex + " did not pass the expected check.");
			} catch(Exception e)
			{
				throw new CompletionException(e);
			}
			return sb.toString();
		});
	}
	
	public static CompletableFuture<Integer> winget()
	{
		if(OSArch.getArchitecture().getType() == OSArch.OSType.WINDOWS)
			return process(new ProcessBuilder("winget install --accept-package-agreements --accept-source-agreements --id Cloudflare.cloudflared".split(" ")).inheritIO(), null);
		
		if(OSArch.getArchitecture().getType() == OSArch.OSType.MACOS)
			return process(new ProcessBuilder("brew install cloudflared".split(" ")).inheritIO(), null);
		
		return CompletableFuture.completedFuture(0);
	}
}