package com.moratan251.psitweaks.common.items.armor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import vazkii.psi.api.exosuit.IExosuitSensor;
import vazkii.psi.api.exosuit.ISensorHoldable;

import javax.annotation.Nonnull;

public class ItemMovalSuitHelmet extends ItemMovalSuitArmor implements ISensorHoldable {
    private final ArmorMaterial material;
    private static final String TAG_SENSOR = "sensor";

    public ItemMovalSuitHelmet(ArmorMaterial material, Type type, Properties props) {
        super(material, type, props);
        this.material = material;
    }

    @Override
    public float getToughness() {
        return 4.0f;
    }

    public String getEvent(ItemStack stack) {
        ItemStack sensor = this.getAttachedSensor(stack);
        return !sensor.isEmpty() && sensor.getItem() instanceof IExosuitSensor ? ((IExosuitSensor)sensor.getItem()).getEventType(sensor) : super.getEvent(stack);
    }

    public int getCastCooldown(ItemStack stack) {
        return 40;
    }

    @OnlyIn(Dist.CLIENT)
    public int getColor(@Nonnull ItemStack stack) {
        ItemStack sensor = this.getAttachedSensor(stack);
        return !sensor.isEmpty() && sensor.getItem() instanceof IExosuitSensor ? ((IExosuitSensor)sensor.getItem()).getColor(sensor) : super.getColor(stack);
    }

    public ItemStack getAttachedSensor(ItemStack stack) {
        CompoundTag cmp = stack.getOrCreateTag().getCompound(TAG_SENSOR);
        return ItemStack.of(cmp);
    }



    public void attachSensor(ItemStack stack, ItemStack sensor) {
        CompoundTag cmp = new CompoundTag();
        if (!sensor.isEmpty()) {
            sensor.save(cmp);
        }

        stack.getOrCreateTag().put(TAG_SENSOR, cmp);
    }


    @Override
    public @NotNull ArmorMaterial getMaterial() {
        return material;
    }
}
