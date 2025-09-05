package com.moratan251.psitweaks.common.items.curios;


import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.cad.ISocketable;
import vazkii.psi.api.cad.ISocketableController;
import vazkii.psi.common.core.handler.PsiSoundHandler;

public class ItemCuriosController extends Item implements ISocketableController {
    private static final String TAG_SELECTED_CONTROL_SLOT = "selectedControlSlot";

    public ItemCuriosController(Item.Properties properties) {
        super(properties.stacksTo(1));
    }

    @Nonnull
    public InteractionResultHolder<ItemStack> use(@NotNull Level worldIn, Player playerIn, @Nonnull InteractionHand hand) {
        ItemStack itemStackIn = playerIn.getItemInHand(hand);
        if (!playerIn.isShiftKeyDown()) {
            return new InteractionResultHolder<>(InteractionResult.PASS, itemStackIn);
        } else {
            if (!worldIn.isClientSide) {
                worldIn.playSound((Player)null, playerIn.getX(), playerIn.getY(), playerIn.getZ(), PsiSoundHandler.compileError, SoundSource.PLAYERS, 0.25F, 1.0F);
            } else {
                playerIn.swing(hand);
            }

            ItemStack[] stacks = this.getControlledStacks(playerIn, itemStackIn);

            for(ItemStack stack : stacks) {
                stack.getCapability(PsiAPI.SOCKETABLE_CAPABILITY).ifPresent((c) -> c.setSelectedSlot(3));
            }

            return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStackIn);
        }
    }

    public ItemStack[] getControlledStacks(Player player, ItemStack stack) {
        List<ItemStack> stacks = new ArrayList<>();

        for(int i = 0; i < 4; ++i) {
            ItemStack curio = CuriosSlotChecker.getItemFromSelectedMagicSlot(player, i);
            if (!curio.isEmpty() && ISocketable.isSocketable(curio)) {
                stacks.add(curio);
            }
        }

        return (ItemStack[])stacks.toArray(new ItemStack[0]);
    }

    public int getDefaultControlSlot(ItemStack stack) {
        return stack.getOrCreateTag().getInt("selectedControlSlot");
    }

    public void setSelectedSlot(Player player, ItemStack stack, int controlSlot, int slot) {
        stack.getOrCreateTag().putInt("selectedControlSlot", controlSlot);
        ItemStack[] stacks = this.getControlledStacks(player, stack);
        if (controlSlot < stacks.length && !stacks[controlSlot].isEmpty()) {
            stacks[controlSlot].getCapability(PsiAPI.SOCKETABLE_CAPABILITY).ifPresent((cap) -> cap.setSelectedSlot(slot));
        }

    }
}
