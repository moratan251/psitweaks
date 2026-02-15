package com.moratan251.psitweaks.compat.tconstruct;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.behavior.ToolDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import vazkii.psi.common.core.handler.PlayerDataHandler;

public class PsicologicalModifier extends NoLevelsModifier implements InventoryTickModifierHook, ToolDamageModifierHook {
    private static final int REPAIR_PSI_COST = 28;
    private static final int PSI_PER_DURABILITY_PREVENTED = 28;
    private static final int REPAIR_INTERVAL = 20;
    private static final int REPAIR_AMOUNT = 1;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK, ModifierHooks.TOOL_DAMAGE);
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level level, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (level.isClientSide() || !(holder instanceof Player player)) {
            return;
        }
        if (!isHeldOrEquipped(player, stack, isSelected, isCorrectSlot)) {
            return;
        }

        int damage = tool.getDamage();
        if (damage <= 0) {
            return;
        }

        if (player.tickCount % REPAIR_INTERVAL != 0) {
            return;
        }

        PlayerDataHandler.PlayerData playerData = PlayerDataHandler.get(player);
        if (playerData.getAvailablePsi() < REPAIR_PSI_COST) {
            return;
        }

        int repairAmount = Math.min(damage, REPAIR_AMOUNT);
        if (repairAmount <= 0 || playerData.getAvailablePsi() < REPAIR_PSI_COST * repairAmount) {
            return;
        }

        playerData.deductPsi(REPAIR_PSI_COST * repairAmount, 0, true, false);
        tool.setDamage(Math.max(0, damage - repairAmount));
    }

    @Override
    public int onDamageTool(IToolStackView tool, ModifierEntry modifier, int amount, LivingEntity holder) {
        if (amount <= 0 || !(holder instanceof Player player) || player.level().isClientSide()) {
            return amount;
        }

        PlayerDataHandler.PlayerData playerData = PlayerDataHandler.get(player);
        int availablePsi = playerData.getAvailablePsi();
        if (availablePsi < PSI_PER_DURABILITY_PREVENTED) {
            return amount;
        }

        int preventableDamage = Math.min(amount, availablePsi / PSI_PER_DURABILITY_PREVENTED);
        if (preventableDamage <= 0) {
            return amount;
        }

        playerData.deductPsi(preventableDamage * PSI_PER_DURABILITY_PREVENTED, 0, true, false);
        return amount - preventableDamage;
    }

    private static boolean isHeldOrEquipped(Player player, ItemStack stack, boolean isSelected, boolean isCorrectSlot) {
        if (isSelected || isCorrectSlot) {
            return true;
        }
        return player.getMainHandItem() == stack || player.getOffhandItem() == stack;
    }
}
