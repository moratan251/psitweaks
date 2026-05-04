package com.moratan251.psitweaks.common.items.curios;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import vazkii.psi.api.cad.ISocketable;
import vazkii.psi.api.cad.ISocketableController;
import vazkii.psi.common.core.handler.PsiSoundHandler;

public class ItemCuriosController extends Item implements ISocketableController {
    private static final String TAG_SELECTED_CONTROL_SLOT = "selectedControlSlot";

    public ItemCuriosController(Item.Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) {
            return new InteractionResultHolder<>(InteractionResult.PASS, stack);
        }

        if (!level.isClientSide) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), PsiSoundHandler.compileError,
                    SoundSource.PLAYERS, 0.25F, 1.0F);
        } else {
            player.swing(hand);
        }

        for (ItemStack controlledStack : getControlledStacks(player, stack)) {
            ISocketable.socketable(controlledStack).setSelectedSlot(3);
        }

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    @Override
    public ItemStack[] getControlledStacks(Player player, ItemStack stack) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int slot = 0; slot < 4; slot++) {
            ItemStack curio = CuriosSlotChecker.getItemFromSelectedMagicSlot(player, slot);
            if (!curio.isEmpty() && ISocketable.isSocketable(curio)) {
                stacks.add(curio);
            }
        }
        return stacks.toArray(new ItemStack[0]);
    }

    @Override
    public int getDefaultControlSlot(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.getInt(TAG_SELECTED_CONTROL_SLOT);
    }

    @Override
    public void setSelectedSlot(Player player, ItemStack stack, int controlSlot, int slot) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(TAG_SELECTED_CONTROL_SLOT, controlSlot));
        ItemStack[] stacks = getControlledStacks(player, stack);
        if (controlSlot < stacks.length && !stacks[controlSlot].isEmpty()) {
            ISocketable.socketable(stacks[controlSlot]).setSelectedSlot(slot);
        }
    }
}
