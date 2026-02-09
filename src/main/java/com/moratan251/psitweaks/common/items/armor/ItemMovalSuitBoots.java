package com.moratan251.psitweaks.common.items.armor;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import vazkii.psi.common.core.handler.PlayerDataHandler;
import vazkii.psi.common.item.tool.IPsimetalTool;

public class ItemMovalSuitBoots extends ItemMovalSuitArmor {
    private final ArmorMaterial material;

    public ItemMovalSuitBoots(ArmorMaterial material, Type type, Properties props) {
        super(material, type, props);
        this.material = material;
    }

    public String getEvent(ItemStack stack) {
        return "psi.event.jump";
    }

    @Override
    public @NotNull ArmorMaterial getMaterial() {
        return material;
    }




}