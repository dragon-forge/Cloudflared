package org.zeith.cloudflared.forge1710;

import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;

import javax.annotation.Nullable;
import java.util.*;

@MCVersion("1.7.10")
public class CloudflaredEarlyMixinLoader
		implements IFMLLoadingPlugin, IEarlyMixinLoader
{
	@Override
	public String[] getASMTransformerClass()
	{
		return null;
	}
	
	@Override
	public String getModContainerClass()
	{
		return null;
	}
	
	@Nullable
	public String getSetupClass()
	{
		return null;
	}
	
	@Override
	public void injectData(Map<String, Object> data) {}
	
	@Override
	public String getAccessTransformerClass()
	{
		return null;
	}
	
	@Override
	public String getMixinConfig()
	{
		return "mixins.cloudflared.early.json";
	}
	
	@Override
	public List<String> getMixins(Set<String> loadedCoreMods)
	{
		List<String> mixins = new ArrayList<>();
		mixins.add("MixinHttpUtil");
		mixins.add("MixinIntegratedServer");
		mixins.add("MixinServerAddress");
		
		return mixins;
	}
}