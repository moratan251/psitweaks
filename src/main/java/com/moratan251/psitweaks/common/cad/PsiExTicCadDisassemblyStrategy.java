package com.moratan251.psitweaks.common.cad;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

final class PsiExTicCadDisassemblyStrategy implements CadDisassemblyStrategy {
    static final PsiExTicCadDisassemblyStrategy INSTANCE = new PsiExTicCadDisassemblyStrategy();

    private static final String TIC_CAD_CLASS_NAME = "com.hutuneko.psi_ex.item.TiCCAD";
    private static final ResourceLocation TIC_CAD_ID = ResourceLocation.fromNamespaceAndPath("psi_ex", "tic_cad");

    private PsiExTicCadDisassemblyStrategy() {
    }

    @Override
    public boolean matches(ItemStack cadStack) {
        Item item = cadStack.getItem();
        return item.getClass().getName().equals(TIC_CAD_CLASS_NAME)
                || TIC_CAD_ID.equals(BuiltInRegistries.ITEM.getKey(item));
    }

    @Override
    public CadDisassemblyResult collectComponentDrops(ItemStack cadStack) {
        // PsiEX/TiC is not available on 1.21.1 yet. Keep detection isolated here so the
        // future material-to-part restoration can be reimplemented without touching the event flow.
        return CadDisassemblyResult.failure(CadDisassemblyService.UNSUPPORTED_TIC_CAD_MESSAGE);
    }
}
