package com.moratan251.psitweaks.common.cad;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import vazkii.psi.api.cad.EnumCADComponent;
import vazkii.psi.api.cad.ICAD;

final class PsiCadDisassemblyStrategy implements CadDisassemblyStrategy {
    static final PsiCadDisassemblyStrategy INSTANCE = new PsiCadDisassemblyStrategy();

    private PsiCadDisassemblyStrategy() {
    }

    @Override
    public boolean matches(ItemStack cadStack) {
        return cadStack.getItem() instanceof ICAD;
    }

    @Override
    public CadDisassemblyResult collectComponentDrops(ItemStack cadStack) {
        List<ItemStack> drops = new ArrayList<>();

        if (cadStack.getItem() instanceof ICAD cad) {
            for (EnumCADComponent component : EnumCADComponent.values()) {
                ItemStack componentStack = cad.getComponentInSlot(cadStack, component);
                if (!componentStack.isEmpty()) {
                    drops.add(componentStack.copy());
                }
            }
        }

        return CadDisassemblyResult.success(drops);
    }
}
