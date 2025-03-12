package org.zeith.cloudflared.forge1710;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;

@SuppressWarnings("unused")
@MCVersion("1.7.10")
public class CloudflaredEarlyMixinLoader implements IFMLLoadingPlugin, IEarlyMixinLoader {

    public String[] getASMTransformerClass() {
        return null;
    }

    public String getModContainerClass() {
        return null;
    }

    @Nullable
    public String getSetupClass() {
        return null;
    }

    public void injectData(Map<String, Object> data) {}

    public String getAccessTransformerClass() {
        return null;
    }

    @Override
    public String getMixinConfig() {
        return "mixins.cloudflared.early.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedCoreMods) {
        List<String> mixins = new ArrayList<>();
        mixins.add("MixinHttpUtil");
        mixins.add("MixinIntegratedServer");
        mixins.add("MixinServerAddress");

        return mixins;
    }
}
