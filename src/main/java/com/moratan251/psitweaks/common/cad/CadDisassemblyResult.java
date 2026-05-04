package com.moratan251.psitweaks.common.cad;

import java.util.List;
import net.minecraft.world.item.ItemStack;

public record CadDisassemblyResult(boolean successful, List<ItemStack> drops, String failureMessageKey) {
    public static CadDisassemblyResult success(List<ItemStack> drops) {
        return new CadDisassemblyResult(true, List.copyOf(drops), null);
    }

    public static CadDisassemblyResult failure(String failureMessageKey) {
        return new CadDisassemblyResult(false, List.of(), failureMessageKey);
    }
}
