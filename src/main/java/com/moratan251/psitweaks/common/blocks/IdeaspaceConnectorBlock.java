package com.moratan251.psitweaks.common.blocks;

import com.moratan251.psitweaks.common.tile.IdeaspaceConnectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import vazkii.psi.common.block.BlockConjured;

public class IdeaspaceConnectorBlock extends BlockConjured {
    public IdeaspaceConnectorBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(SOLID, true).setValue(LIGHT, false));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new IdeaspaceConnectorBlockEntity(pos, state);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof IdeaspaceConnectorBlockEntity connector) connector.doParticles(random);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Override the conjured block's expiration tick: this block is permanent.
        if (level.getBlockEntity(pos) instanceof IdeaspaceConnectorBlockEntity connector) connector.autoTransfer();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        openConnector(level, pos, player);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                             Player player, InteractionHand hand, BlockHitResult hit) {
        openConnector(level, pos, player);
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    private void openConnector(Level level, BlockPos pos, Player player) {
        // Owner data is server-only. Consume the click on both sides so the client does not
        // send a second, item-use packet while the server is opening the menu.
        player.stopUsingItem();
        if (player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof IdeaspaceConnectorBlockEntity connector
                && connector.canConfigure(player)) serverPlayer.openMenu(connector, pos);
    }
}
