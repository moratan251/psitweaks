package com.moratan251.psitweaks.compat.tconstruct;

import com.moratan251.psitweaks.Psitweaks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.behavior.ProcessLootModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.BlockInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.List;

public class GlobalTravelerModifier extends NoLevelsModifier implements BlockInteractionModifierHook, GeneralInteractionModifierHook, ProcessLootModifierHook {
    private static final ResourceLocation LINKED_DIMENSION = ResourceLocation.fromNamespaceAndPath(Psitweaks.MOD_ID, "global_traveler_linked_dimension");
    private static final ResourceLocation LINKED_POS_X = ResourceLocation.fromNamespaceAndPath(Psitweaks.MOD_ID, "global_traveler_linked_pos_x");
    private static final ResourceLocation LINKED_POS_Y = ResourceLocation.fromNamespaceAndPath(Psitweaks.MOD_ID, "global_traveler_linked_pos_y");
    private static final ResourceLocation LINKED_POS_Z = ResourceLocation.fromNamespaceAndPath(Psitweaks.MOD_ID, "global_traveler_linked_pos_z");
    private static final ResourceLocation LINKED_SIDE = ResourceLocation.fromNamespaceAndPath(Psitweaks.MOD_ID, "global_traveler_linked_side");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.BLOCK_INTERACT, ModifierHooks.GENERAL_INTERACT, ModifierHooks.PROCESS_LOOT);
    }

    @Override
    public InteractionResult beforeBlockUse(IToolStackView tool, ModifierEntry modifier, UseOnContext context, InteractionSource source) {
        if (source != InteractionSource.RIGHT_CLICK) {
            return InteractionResult.PASS;
        }

        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        if (!hasItemInventory(level, clickedPos, context.getClickedFace())) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        linkToInventory(tool, level, clickedPos, context.getClickedFace());
        player.sendSystemMessage(Component.translatable(
                "message.psitweaks.global_traveler.linked",
                clickedPos.getX(),
                clickedPos.getY(),
                clickedPos.getZ()
        ));
        return InteractionResult.SUCCESS;
    }

    @Override
    public void processLoot(IToolStackView tool, ModifierEntry modifier, List<ItemStack> generatedLoot, LootContext context) {
        if (generatedLoot.isEmpty()) {
            return;
        }

        IItemHandler linkedInventory = getLinkedInventory(tool, context.getLevel());
        if (linkedInventory == null) {
            return;
        }

        for (int i = 0; i < generatedLoot.size(); i++) {
            ItemStack drop = generatedLoot.get(i);
            if (drop.isEmpty()) {
                continue;
            }

            ItemStack remainder = ItemHandlerHelper.insertItemStacked(linkedInventory, drop.copy(), false);
            generatedLoot.set(i, remainder);
        }
        generatedLoot.removeIf(ItemStack::isEmpty);
    }

    @Override
    public InteractionResult onToolUse(IToolStackView tool, ModifierEntry modifier, Player player, net.minecraft.world.InteractionHand hand, InteractionSource source) {
        if (source != InteractionSource.RIGHT_CLICK || !player.isShiftKeyDown() || !hasLinkedInventory(tool)) {
            return InteractionResult.PASS;
        }

        HitResult hitResult = player.pick(6.0D, 0.0F, false);
        if (hitResult.getType() != HitResult.Type.MISS) {
            return InteractionResult.PASS;
        }

        if (!player.level().isClientSide()) {
            clearLink(tool);
            player.sendSystemMessage(Component.translatable("message.psitweaks.global_traveler.unlinked"));
        }
        return InteractionResult.SUCCESS;
    }

    private static void linkToInventory(IToolStackView tool, Level level, BlockPos pos, Direction side) {
        tool.getPersistentData().putString(LINKED_DIMENSION, level.dimension().location().toString());
        tool.getPersistentData().putInt(LINKED_POS_X, pos.getX());
        tool.getPersistentData().putInt(LINKED_POS_Y, pos.getY());
        tool.getPersistentData().putInt(LINKED_POS_Z, pos.getZ());
        tool.getPersistentData().putString(LINKED_SIDE, side.getName());
    }

    private static void clearLink(IToolStackView tool) {
        tool.getPersistentData().remove(LINKED_DIMENSION);
        tool.getPersistentData().remove(LINKED_POS_X);
        tool.getPersistentData().remove(LINKED_POS_Y);
        tool.getPersistentData().remove(LINKED_POS_Z);
        tool.getPersistentData().remove(LINKED_SIDE);
    }

    private static boolean hasItemInventory(Level level, BlockPos pos, Direction side) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null || blockEntity.isRemoved()) {
            return false;
        }
        return findItemHandlerFromSide(blockEntity, side) != null;
    }

    @Nullable
    private static IItemHandler getLinkedInventory(IToolStackView tool, ServerLevel currentLevel) {
        if (!hasLinkedInventory(tool)) {
            return null;
        }

        String linkedDimension = tool.getPersistentData().getString(LINKED_DIMENSION);
        if (!currentLevel.dimension().location().toString().equals(linkedDimension)) {
            return null;
        }
        Direction linkedSide = Direction.byName(tool.getPersistentData().getString(LINKED_SIDE));
        if (linkedSide == null) {
            return null;
        }

        BlockPos linkedPos = new BlockPos(
                tool.getPersistentData().getInt(LINKED_POS_X),
                tool.getPersistentData().getInt(LINKED_POS_Y),
                tool.getPersistentData().getInt(LINKED_POS_Z)
        );
        if (!currentLevel.isLoaded(linkedPos)) {
            return null;
        }

        BlockEntity blockEntity = currentLevel.getBlockEntity(linkedPos);
        if (blockEntity == null || blockEntity.isRemoved()) {
            return null;
        }
        return findItemHandlerFromSide(blockEntity, linkedSide);
    }

    private static boolean hasLinkedInventory(IToolStackView tool) {
        return tool.getPersistentData().contains(LINKED_DIMENSION)
                && tool.getPersistentData().contains(LINKED_POS_X)
                && tool.getPersistentData().contains(LINKED_POS_Y)
                && tool.getPersistentData().contains(LINKED_POS_Z)
                && tool.getPersistentData().contains(LINKED_SIDE);
    }

    @Nullable
    private static IItemHandler findItemHandlerFromSide(BlockEntity blockEntity, Direction side) {
        LazyOptional<IItemHandler> sided = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, side);
        return sided.orElse(null);
    }
}
