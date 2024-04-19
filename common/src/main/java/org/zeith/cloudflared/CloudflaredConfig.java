package org.zeith.cloudflared;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.ToString;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@ToString
public class CloudflaredConfig
{
	private static final Gson GSON = new GsonBuilder()
			.setPrettyPrinting()
			.create();
	
	@Getter
	public static CloudflaredConfig instance = new CloudflaredConfig();
	
	@SerializedName("Hosting")
	public Hosting hosting = new Hosting();
	
	@SerializedName("Network: Advanced")
	public AdvancedNetwork advancedNetwork = new AdvancedNetwork();
	
	@Getter
	static Path configFile;
	
	public static void load()
			throws IOException
	{
		CloudflaredMod.LOG.info("Loading configs.");
		
		if(!Files.isRegularFile(configFile))
			Files.writeString(configFile, GSON.toJson(instance));
		
		instance = GSON.fromJson(Files.readString(configFile), CloudflaredConfig.class);
		Files.writeString(configFile, GSON.toJson(instance));
		CloudflaredMod.LOG.info("Configs loaded: {}.", instance);
	}
	
	@ToString
	public static class Hosting
	{
		@SerializedName("Custom Port Override")
		public int customPortOverride = 0;
		
		@SerializedName("Enable PvP")
		public boolean enablePvP = true;
		
		@SerializedName("Online Mode")
		public boolean onlineMode = true;
		
		@SerializedName("Start Tunnel")
		public boolean startTunnel = true;
	}
	
	@ToString
	public static class AdvancedNetwork
	{
		@SerializedName("Cloudflare Hostname")
		public String hostname = "";
	}
}