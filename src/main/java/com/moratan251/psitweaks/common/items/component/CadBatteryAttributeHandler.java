package com.moratan251.psitweaks.common.items.component;

import com.moratan251.psitweaks.common.attributes.PsitweaksAttributes;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.registries.RegistryObject;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.cad.EnumCADComponent;
import vazkii.psi.common.item.ItemCAD;

import java.util.UUID;

public class CadBatteryAttributeHandler {
    private static final int CHECK_INTERVAL_TICKS = 10;
    private static final double REACTIVE_BATTERY_REGEN_BONUS = 10.0D;
    private static final double QUANTUM_BATTERY_MAX_PSI_BONUS = 1500.0D;

    private static final UUID REACTIVE_BATTERY_REGEN_UUID = UUID.fromString("3f2b9c1e-5a7d-4e8b-9c0f-2a1b3d4e5f60");
    private static final UUID QUANTUM_BATTERY_MAX_PSI_UUID = UUID.fromString("8a1c4e27-6b3d-4f59-a1e2-7c9d0b2f4a68");
    private static final String REACTIVE_BATTERY_REGEN_NAME = "psitweaks.cad_battery_reactive_regen";
    private static final String QUANTUM_BATTERY_MAX_PSI_NAME = "psitweaks.cad_battery_quantum_max_psi";

    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Player player = event.player;
        if (player.level().isClientSide() || player.tickCount % CHECK_INTERVAL_TICKS != 0) {
            return;
        }

        ItemStack battery = getCadBattery(player);
        updateModifier(player, PsitweaksAttributes.PSI_REGEN_BONUS, REACTIVE_BATTERY_REGEN_UUID,
                REACTIVE_BATTERY_REGEN_NAME,
                battery.is(PsitweaksItems.CAD_BATTERY_REACTIVE.get()) ? REACTIVE_BATTERY_REGEN_BONUS : 0.0D);
        updateModifier(player, PsitweaksAttributes.MAX_PSI_BONUS, QUANTUM_BATTERY_MAX_PSI_UUID,
                QUANTUM_BATTERY_MAX_PSI_NAME,
                battery.is(PsitweaksItems.CAD_BATTERY_QUANTUM.get()) ? QUANTUM_BATTERY_MAX_PSI_BONUS : 0.0D);
    }

    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        ItemStack battery = stack.getItem() instanceof ItemCAD ? getBattery(stack) : stack;
        if (battery.is(PsitweaksItems.CAD_BATTERY_REACTIVE.get())) {
            event.getToolTip().add(Component.translatable("tooltip.psitweaks.cad_battery_reactive.bonus")
                    .withStyle(ChatFormatting.BLUE));
        } else if (battery.is(PsitweaksItems.CAD_BATTERY_QUANTUM.get())) {
            event.getToolTip().add(Component.translatable("tooltip.psitweaks.cad_battery_quantum.bonus")
                    .withStyle(ChatFormatting.BLUE));
        }
    }

    private static ItemStack getCadBattery(Player player) {
        return getBattery(PsiAPI.getPlayerCAD(player));
    }

    private static ItemStack getBattery(ItemStack cad) {
        if (cad.isEmpty() || !(cad.getItem() instanceof ItemCAD item)) {
            return ItemStack.EMPTY;
        }
        return item.getComponentInSlot(cad, EnumCADComponent.BATTERY);
    }

    private static void updateModifier(Player player, RegistryObject<Attribute> attribute, UUID id, String name, double bonus) {
        AttributeInstance instance = player.getAttribute(attribute.get());
        if (instance == null) {
            return;
        }
        if (bonus > 0.0D) {
            if (instance.getModifier(id) == null) {
                instance.addTransientModifier(new AttributeModifier(id, name, bonus, AttributeModifier.Operation.ADDITION));
            }
        } else if (instance.getModifier(id) != null) {
            instance.removeModifier(id);
        }
    }
}
