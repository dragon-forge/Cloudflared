package org.zeith.cloudflared.forge1122;

import com.zeitheron.hammercore.cfg.gui.HCConfigGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

import java.util.*;

public class ConfigFactory
		implements IModGuiFactory
{
	@Override
	public void initialize(Minecraft minecraft)
	{
	
	}
	
	@Override
	public boolean hasConfigGui()
	{
		return true;
	}
	
	@Override
	public GuiScreen createConfigGui(GuiScreen parentScreen)
	{
		return new HCConfigGui(parentScreen, Configs1122.cfg, "cloudflared");
	}
	
	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories()
	{
		return Collections.emptySet();
	}
}