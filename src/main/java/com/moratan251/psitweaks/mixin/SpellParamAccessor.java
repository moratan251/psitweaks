package com.moratan251.psitweaks.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import vazkii.psi.api.spell.SpellParam;

/** Whether a spell parameter takes only constants ({@code SpellParam#requiresConstant} is protected). */
@Mixin(value = SpellParam.class, remap = false)
public interface SpellParamAccessor {
    @Invoker("requiresConstant")
    boolean psitweaks$requiresConstant();
}
