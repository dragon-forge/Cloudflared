package org.zeith.cloudflared.mixin.client;

import com.mojang.datafixers.DataFixer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.*;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.zeith.cloudflared.CloudflaredConfig;
import org.zeith.cloudflared.CloudflaredMod;
import org.zeith.cloudflared.architectury.MCArchGameSession;

import java.net.Proxy;

@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin
	extends MinecraftServer
{
	@Shadow
	@Final
	private Minecraft minecraft;
	
	public IntegratedServerMixin(Thread thread, LevelStorageSource.LevelStorageAccess levelStorageAccess, PackRepository packRepository, WorldStem worldStem, Proxy proxy, DataFixer dataFixer, Services services, ChunkProgressListenerFactory chunkProgressListenerFactory)
	{
		super(thread, levelStorageAccess, packRepository, worldStem, proxy, dataFixer, services, chunkProgressListenerFactory);
	}
	
	@Inject(
			method = "publishServer",
			at = @At("HEAD")
	)
	private void Cloudflared_publishServer(GameType gameType, boolean cheats, int port, CallbackInfoReturnable<Boolean> cir)
	{
		CloudflaredConfig.Hosting hosting = CloudflaredConfig.getInstance().hosting;
		
		if(hosting.startTunnel)
		{
			minecraft.gui.getChat().addMessage(Component.translatable("chat.cloudflared:starting_tunnel"));
			CloudflaredMod.PROXY.startSession(new MCArchGameSession(port, minecraft.player.getUUID(), minecraft.player));
		}
		
		setUsesAuthentication(hosting.onlineMode);
		setPvpAllowed(hosting.enablePvP);
	}
}