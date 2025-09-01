package com.moratan251.psitweaks.common.items.curios;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.cad.ISocketable;
import vazkii.psi.api.exosuit.PsiArmorEvent;
import vazkii.psi.api.internal.TooltipHelper;
import vazkii.psi.common.core.handler.PlayerDataHandler;
import vazkii.psi.common.item.ItemCAD;
import vazkii.psi.common.item.armor.ItemPsimetalArmor;
import vazkii.psi.common.item.tool.IPsimetalTool;
import vazkii.psi.common.item.tool.ToolSocketable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemCuriosCompat extends Item implements ICurioItem, IPsimetalTool{


    public ItemCuriosCompat(Properties pProperties) {
        super(pProperties);
    }


    @Nonnull
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ItemPsimetalArmor.ArmorSocketable(stack, 3);
    }



    public static class CurioSocketable extends ToolSocketable {
        public CurioSocketable(ItemStack tool, int slots) {
            super(tool, slots);
        }

        public void setSelectedSlot(int slot) {
            super.setSelectedSlot(slot);
            this.tool.getOrCreateTag().putInt("timesCast", 0);
        }

        public void setBulletInSocket(int slot, ItemStack bullet) {
            super.setBulletInSocket(slot, bullet);
            this.tool.getOrCreateTag().putInt("timesCast", 0);
        }
    }

    public void cast(ItemStack stack, PsiArmorEvent event) {
        PlayerDataHandler.PlayerData data = PlayerDataHandler.get(event.getEntity());
        ItemStack playerCad = PsiAPI.getPlayerCAD(event.getEntity());
        if (this.isEnabled(stack) && !playerCad.isEmpty()) {
            int timesCast = stack.getOrCreateTag().getInt("timesCast");
            ItemStack bullet = ISocketable.socketable(stack).getSelectedBullet();
            ItemCAD.cast(event.getEntity().getCommandSenderWorld(), event.getEntity(), data, bullet, playerCad, this.getCastCooldown(stack), 0, this.getCastVolume(), (context) -> {
                context.tool = stack;
                context.attackingEntity = event.attacker;
                context.damageTaken = event.damage;
                context.loopcastIndex = timesCast;
            }, (int)((double)data.calculateDamageDeduction((float)event.damage) * (double)0.75F));
            stack.getOrCreateTag().putInt("timesCast", timesCast + 1);
        }

    }

    public void onEvent(ItemStack stack, PsiArmorEvent event) {
        if (event.type.equals(this.getTrueEvent(stack)) && event.getEntity() != null) {
            this.cast(stack, event);
        }

    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level playerIn, @NotNull List<Component> tooltip, @NotNull TooltipFlag advanced) {
        TooltipHelper.tooltipIfShift(tooltip, () -> {
            Component componentName = ISocketable.getSocketedItemName(stack, "psimisc.none");
            tooltip.add(Component.translatable("psimisc.spell_selected", new Object[]{componentName}));
            tooltip.add(Component.translatable(this.getTrueEvent(stack)));
        });
    }

    public String getEvent(ItemStack stack) {
        return "psi.event.none";
    }

    public String getTrueEvent(ItemStack stack) {
        return stack.getOrCreateTag().getString("PsiEvent").isEmpty() ? this.getEvent(stack) : stack.getOrCreateTag().getString("PsiEvent");
    }

    public float getCastVolume() {
        return 0.025F;
    }

    public int getCastCooldown(ItemStack stack) {
        return 1;
    }





}

