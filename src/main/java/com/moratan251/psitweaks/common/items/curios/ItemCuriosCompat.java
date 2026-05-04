package com.moratan251.psitweaks.common.items.curios;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.cad.ISocketable;
import vazkii.psi.api.exosuit.IPsiEventArmor;
import vazkii.psi.api.exosuit.PsiArmorEvent;
import vazkii.psi.api.spell.ISpellAcceptor;
import vazkii.psi.common.core.handler.PlayerDataHandler;
import vazkii.psi.common.item.ItemCAD;
import vazkii.psi.common.item.base.ModDataComponents;
import vazkii.psi.common.item.tool.IPsimetalTool;
import vazkii.psi.common.item.tool.ToolSocketable;

public class ItemCuriosCompat extends Item implements ICurioItem, IPsimetalTool, IPsiEventArmor {
    public static final int SLOT_COUNT = 7;

    public ItemCuriosCompat(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId, boolean isSelected) {
        IPsimetalTool.regen(stack, entity);
    }

    public void cast(ItemStack stack, PsiArmorEvent event) {
        if (!ISocketable.isSocketable(stack)) {
            return;
        }

        PlayerDataHandler.PlayerData data = PlayerDataHandler.get(event.getEntity());
        ItemStack playerCad = PsiAPI.getPlayerCAD(event.getEntity());
        ItemStack bullet = ISocketable.socketable(stack).getSelectedBullet();
        if (playerCad.isEmpty() || bullet.isEmpty() || !ISpellAcceptor.hasSpell(bullet)) {
            return;
        }

        int timesCast = stack.getOrDefault(ModDataComponents.TIMES_CAST, 0);
        ItemCAD.cast(event.getEntity().level(), event.getEntity(), data, bullet, playerCad, getCastCooldown(stack), 0,
                getCastVolume(), context -> {
                    context.tool = stack;
                    context.attackingEntity = event.attacker;
                    context.damageTaken = event.damage;
                    context.loopcastIndex = timesCast;
                }, (int) ((double) data.calculateDamageDeduction((float) event.damage) * 0.75D));
        stack.set(ModDataComponents.TIMES_CAST, timesCast + 1);
    }

    @Override
    public void onEvent(ItemStack stack, PsiArmorEvent event) {
        if (event.type.equals(getTrueEvent(stack)) && event.getEntity() != null) {
            cast(stack, event);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Component componentName = ISocketable.getSocketedItemName(stack, "psimisc.none");
        tooltip.add(Component.translatable("psimisc.spell_selected", componentName));
        tooltip.add(Component.translatable(getTrueEvent(stack)));
    }

    public String getEvent(ItemStack stack) {
        return "psi.event.none";
    }

    public String getTrueEvent(ItemStack stack) {
        return getEvent(stack);
    }

    public float getCastVolume() {
        return 0.025F;
    }

    public int getCastCooldown(ItemStack stack) {
        return 1;
    }

    public static class CurioSocketable extends ToolSocketable {
        public CurioSocketable(ItemStack tool) {
            super(tool, SLOT_COUNT);
        }

        @Override
        public void setSelectedSlot(int slot) {
            super.setSelectedSlot(slot);
            this.tool.set(ModDataComponents.TIMES_CAST, 0);
        }

        @Override
        public void setBulletInSocket(int slot, @Nullable ItemStack bullet) {
            super.setBulletInSocket(slot, bullet);
            this.tool.set(ModDataComponents.TIMES_CAST, 0);
        }
    }
}
