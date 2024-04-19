package org.zeith.cloudflared.core.util;

import org.apache.logging.log4j.*;
import org.zeith.cloudflared.core.api.*;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Predicate;

public class CloudflaredUtils
{
	static final Logger LOG = LogManager.getLogger("CloudflaredAPI/Utils");
	
	static CompletableFuture<Integer> download(String url, File to, File etag, Executor exe, IFileDownload progress)
	{
		return CompletableFuture.supplyAsync(() ->
		{
			File tmp = new File(to.getAbsolutePath() + ".tmp");
			
			try(HttpRequest r = HttpRequest.get(url)
					.userAgent("Cloudflared Java Agent"))
			{
				int code = r.code();
				URL u = r.url();
				String aLoc = u.getProtocol() + "://" + u.getHost() + u.getPath();
				String urlSha = Hashers.SHA1.hashifyHex(aLoc);
				
				LOG.info("Download URL hash: {}", urlSha);
				
				List<String> lines;
				
				if(to.isFile() && etag.isFile()
				   && (lines = Files.readAllLines(etag.toPath())).size() > 1
				   && lines.get(0).equals(urlSha)
				   && lines.get(1).equals(Hashers.SHA1.hashifyHex(Files.readAllBytes(to.toPath()))))
				{
					tmp.delete();
					if(!to.canExecute()) to.setExecutable(true);
					LOG.info("Skip download of {} from same url as it probably didn't change.", to.getName());
					return 0;
				}
				
				progress.onStart();
				try(IFileDownload ignore = progress)
				{
					r.progress(progress)
							.incrementTotalSize(r.contentLength())
							.receive(tmp);
					
					to.delete();
					if(!to.canExecute()) to.setExecutable(true);
					
					if(tmp.renameTo(to))
						Files.write(etag.toPath(), Arrays.asList(
								urlSha,
								Hashers.SHA1.hashifyHex(Files.readAllBytes(to.toPath()))
						));
				}
				
				return code / 100 < 4 ? 0 : code;
			} catch(IOException e)
			{
				throw new CompletionException(e);
			}
		}, exe);
	}
	
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
	
	public static CompletableFuture<Integer> download(IGameProxy proxy)
	{
		OSArch.InstructionSet instructions = OSArch.getInstructions();
		OSArch.ArchDistro architecture = OSArch.getArchitecture();
		
		String baseUrl = "https://github.com/cloudflare/cloudflared/releases/latest/download/";
		
		File etag = new File(proxy.getExtraDataDir(), "cloudflared.etag");
		
		if(architecture.getType() == OSArch.OSType.WINDOWS)
		{
			String url = baseUrl;
			if(instructions == OSArch.InstructionSet.X86) url += "cloudflared-windows-386.exe";
			else url += "cloudflared-windows-amd64.exe";
			
			return download(url, new File(proxy.getExtraDataDir(), "cloudflared.exe"), etag, proxy.getBackgroundExecutor(), proxy.pushFileDownload());
		}
		
		if(architecture == OSArch.ArchDistro.GNU_LINUX)
		{
			String url = baseUrl;
			if(instructions == OSArch.InstructionSet.X86) url += "cloudflared-linux-386";
			else if(instructions == OSArch.InstructionSet.X86_64) url += "cloudflared-linux-amd64";
			else if(instructions == OSArch.InstructionSet.ARM_32) url += "cloudflared-linux-arm";
			else if(instructions == OSArch.InstructionSet.ARM_64) url += "cloudflared-linux-arm64";
			
			File cfe = new File(proxy.getExtraDataDir(), "cloudflared");
			return download(url, cfe, etag, proxy.getBackgroundExecutor(), proxy.pushFileDownload()).thenApply(i ->
			{
				if(!cfe.canExecute())
					cfe.setExecutable(true);
				return i;
			});
		}
		
		if(architecture.getType() == OSArch.OSType.MACOS)
		{
			String url = baseUrl + "cloudflared-darwin-amd64.tgz";
			File dst = new File(proxy.getExtraDataDir(), "cloudflared.tgz");
			return download(url, dst, etag, proxy.getBackgroundExecutor(), proxy.pushFileDownload()).thenCompose(i ->
			{
				if(!dst.isFile()) return CompletableFuture.completedFuture(i);
				File tmp = new File(proxy.getExtraDataDir(), "cloudflared-tmp");
				tmp.mkdirs();
				return process(new ProcessBuilder("tar", "-xzf", dst.getAbsolutePath()).directory(tmp), null)
						.thenApply(i2 ->
						{
							File dstf = new File(proxy.getExtraDataDir(), "cloudflared");
							File r = new File(tmp, "cloudflared");
							if(r.isFile())
							{
								dstf.delete();
								r.renameTo(dstf);
								File[] fs = tmp.listFiles();
								if(fs != null) for(File file : fs) file.delete();
								tmp.delete();
								if(!dstf.canExecute()) dstf.setExecutable(true);
								return 0;
							}
							
							return i2;
						});
			});
		}
		
		return CompletableFuture.completedFuture(null);
	}
}