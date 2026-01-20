package com.moratan251.psitweaks.common.misc;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import vazkii.psi.api.cad.ICAD;
import vazkii.psi.api.spell.SpellRuntimeException;

import javax.annotation.Nullable;

public interface IItemCADMixin extends ICAD {
    void psitweaks$setStoredEntity(ItemStack stack, int memorySlot, @Nullable Entity entity) throws SpellRuntimeException;
    @Nullable Entity psitweaks$getStoredEntity(ItemStack stack, int memorySlot, Level level) throws SpellRuntimeException;
}