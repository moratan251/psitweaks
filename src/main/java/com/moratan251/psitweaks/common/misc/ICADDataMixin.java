package com.moratan251.psitweaks.common.misc;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import vazkii.psi.api.cad.ICAD;
import vazkii.psi.api.spell.SpellRuntimeException;

import javax.annotation.Nullable;

public interface ICADDataMixin extends ICAD {
    public Entity psitweaks$getSavedEntity(int memorySlot);
    public void psitweaks$setSavedEntity(int memorySlot, Entity value);
}