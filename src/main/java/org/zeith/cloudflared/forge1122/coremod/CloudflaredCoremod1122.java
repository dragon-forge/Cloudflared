package org.zeith.cloudflared.forge1122.coremod;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.12.2")
public class CloudflaredCoremod1122
		implements IFMLLoadingPlugin
{
	public static boolean runtimeDeobfEnabled = false;
	
	static final Logger LOG = LogManager.getLogger("Cloudflared/ASM");
	
	private final String[] transformers = new String[] {
			HttpUtilTransformer.class.getName(),
			ServerAddressTransformer.class.getName(),
			IntegratedServerTransformer.class.getName()
	};
	
	@Override
	public String[] getASMTransformerClass()
	{
		return transformers;
	}
	
	@Override
	public String getModContainerClass()
	{
		return null;
	}
	
	@Nullable
	@Override
	public String getSetupClass()
	{
		return null;
	}
	
	@Override
	public void injectData(Map<String, Object> data)
	{
		runtimeDeobfEnabled = (Boolean) data.get("runtimeDeobfuscationEnabled");
	}
	
	@Override
	public String getAccessTransformerClass()
	{
		return null;
	}
	
	static void dump(String transformedName, byte[] cls)
	{
		transformedName = transformedName.replace('.', '/');
		int lSlash = transformedName.lastIndexOf('/');
		File dir = new File("asm/dump", transformedName.substring(0, lSlash));
		dir.mkdirs();
		try(FileOutputStream out = new FileOutputStream(new File(dir, transformedName.substring(lSlash + 1) + ".class")))
		{
			out.write(cls);
		} catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}