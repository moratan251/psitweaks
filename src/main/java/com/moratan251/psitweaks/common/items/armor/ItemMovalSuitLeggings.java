package com.moratan251.psitweaks.common.items.armor;

import com.moratan251.psitweaks.common.effects.PsitweaksEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemMovalSuitLeggings extends ItemMovalSuitArmor {

    private  final ArmorMaterial material;

    public ItemMovalSuitLeggings(ArmorMaterial material, Type type, Properties props) {
        super(material,type, props);
        this.material = material;
    }

    @Override
    public float getToughness() {
        return 4.0f;
    }

    public String getEvent(ItemStack stack) {
        return "psi.event.tick";
    }

    public int getCastCooldown(ItemStack stack) {
        return 0;
    }

    public float getCastVolume() {
        return 0.0F;
    }


    @Override
    public @NotNull ArmorMaterial getMaterial() {
        return material;
    }


    @Override
    public void onInventoryTick(ItemStack stack, Level level, Player player) {

    }

}