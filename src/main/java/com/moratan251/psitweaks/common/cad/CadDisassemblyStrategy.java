package com.moratan251.psitweaks.common.cad;

import net.minecraft.world.item.ItemStack;

interface CadDisassemblyStrategy {
    boolean matches(ItemStack cadStack);

    CadDisassemblyResult collectComponentDrops(ItemStack cadStack);
}
