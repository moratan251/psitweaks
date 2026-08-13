package com.moratan251.psitweaks.common.items.component;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.cad.EnumCADComponent;
import vazkii.psi.common.attribute.base.ModAttributes;
import vazkii.psi.common.item.ItemCAD;

public final class CadBatteryAttributeHandler {
    private static final int CHECK_INTERVAL_TICKS = 10;
    private static final double REACTIVE_BATTERY_REGEN_BONUS = 10.0D;
    private static final double QUANTUM_BATTERY_MAX_PSI_BONUS = 1500.0D;

    private static final ResourceLocation REACTIVE_BATTERY_REGEN_ID = Psitweaks.location("cad_battery_reactive_regen");
    private static final ResourceLocation QUANTUM_BATTERY_MAX_PSI_ID = Psitweaks.location("cad_battery_quantum_max_psi");

    private CadBatteryAttributeHandler() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || player.tickCount % CHECK_INTERVAL_TICKS != 0) {
            return;
        }

        ItemStack battery = getCadBattery(player);
        updateModifier(player.getAttribute(ModAttributes.REGEN), REACTIVE_BATTERY_REGEN_ID,
                battery.is(PsitweaksItems.CAD_BATTERY_REACTIVE.get()) ? REACTIVE_BATTERY_REGEN_BONUS : 0.0D);
        updateModifier(player.getAttribute(ModAttributes.TOTAL_PSI), QUANTUM_BATTERY_MAX_PSI_ID,
                battery.is(PsitweaksItems.CAD_BATTERY_QUANTUM.get()) ? QUANTUM_BATTERY_MAX_PSI_BONUS : 0.0D);
    }

    private static ItemStack getCadBattery(Player player) {
        ItemStack cad = PsiAPI.getPlayerCAD(player);
        return getBattery(cad);
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

    private static ItemStack getBattery(ItemStack cad) {
        if (cad.isEmpty() || !(cad.getItem() instanceof ItemCAD item)) {
            return ItemStack.EMPTY;
        }
        return item.getComponentInSlot(cad, EnumCADComponent.BATTERY);
    }

    private static void updateModifier(AttributeInstance attribute, ResourceLocation id, double bonus) {
        if (attribute == null) {
            return;
        }
        if (bonus > 0.0D) {
            if (attribute.getModifier(id) == null) {
                attribute.addTransientModifier(new AttributeModifier(id, bonus, AttributeModifier.Operation.ADD_VALUE));
            }
        } else if (attribute.getModifier(id) != null) {
            attribute.removeModifier(id);
        }
    }
}
