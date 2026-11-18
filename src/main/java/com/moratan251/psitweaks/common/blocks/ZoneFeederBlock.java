package com.moratan251.psitweaks.common.blocks;

import com.mojang.serialization.MapCodec;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityChargedZone;
import com.moratan251.psitweaks.common.items.PsitweaksDataComponents;
import com.moratan251.psitweaks.common.registries.PsitweaksBlockEntityTypes;
import com.moratan251.psitweaks.common.tile.ZoneFeederBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

/**
 * 領域給電機のブロック。置いた人を所有者として覚え、荷電領域が FE を引いている間は {@link #ACTIVE}（光る）。素手で右クリックすると、
 * ためている量と、引いている荷電領域の数をアクションバーに出す。コンパレーターには、ためている割合を出す。
 */
public class ZoneFeederBlock extends BaseEntityBlock {
    public static final MapCodec<ZoneFeederBlock> CODEC = simpleCodec(ZoneFeederBlock::new);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public ZoneFeederBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(ACTIVE, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ZoneFeederBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, PsitweaksBlockEntityTypes.ZONE_FEEDER.get(),
                (tickLevel, pos, tickState, feeder) -> feeder.serverTick((ServerLevel) tickLevel));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && placer instanceof Player player && level.getBlockEntity(pos) instanceof ZoneFeederBlockEntity feeder) {
            feeder.setOwner(player.getUUID());
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level instanceof ServerLevel serverLevel && level.getBlockEntity(pos) instanceof ZoneFeederBlockEntity feeder) {
            int zones = EntityChargedZone.zonesDrawingFrom(serverLevel, feeder);
            player.displayClientMessage(Component.translatable("message.psitweaks.zone_feeder.status",
                    String.format(Locale.ROOT, "%,d", feeder.stored()),
                    String.format(Locale.ROOT, "%,d", ZoneFeederBlockEntity.capacity()), zones), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** FE が残っているアイテムにだけ、ためている量を出す。 */
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        long stored = stack.getOrDefault(PsitweaksDataComponents.ZONE_FEEDER_ENERGY.get(), 0L);
        if (stored > 0L) {
            tooltip.add(Component.translatable("tooltip.psitweaks.zone_feeder.stored",
                    String.format(Locale.ROOT, "%,d", stored),
                    String.format(Locale.ROOT, "%,d", ZoneFeederBlockEntity.capacity())).withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof ZoneFeederBlockEntity feeder ? feeder.comparatorSignal() : 0;
    }
}
