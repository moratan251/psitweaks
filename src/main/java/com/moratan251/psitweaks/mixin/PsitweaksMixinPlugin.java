package com.moratan251.psitweaks.mixin;

import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public final class PsitweaksMixinPlugin implements IMixinConfigPlugin {
    private static final String RADIATION_MIXIN =
            "com.moratan251.psitweaks.mixin.RadiationUtilMixin";
    private static final String RADIATION_TARGET =
            "mekanism.common.lib.radiation.RadiationUtil";
    private static final String WIND_GENERATOR_MIXIN =
            "com.moratan251.psitweaks.mixin.WindGeneratorTurbulenceMixin";
    private static final String WIND_GENERATOR_TARGET =
            "mekanism.generators.common.tile.TileEntityWindGenerator";

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (RADIATION_MIXIN.equals(mixinClassName)) {
            return isClassPresent(RADIATION_TARGET);
        }
        if (WIND_GENERATOR_MIXIN.equals(mixinClassName)) {
            return isClassPresent(WIND_GENERATOR_TARGET);
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    /**
     * Looks for the class file without loading the class. Loading it here (Class.forName) would load the target
     * before Mixin transforms it, so the mixin would silently never apply.
     */
    private static boolean isClassPresent(String className) {
        String classResource = className.replace('.', '/') + ".class";
        ClassLoader[] classLoaders = {
                Thread.currentThread().getContextClassLoader(),
                PsitweaksMixinPlugin.class.getClassLoader()
        };
        for (ClassLoader classLoader : classLoaders) {
            if (classLoader != null && classLoader.getResource(classResource) != null) {
                return true;
            }
        }
        return false;
    }
}
