package com.moratan251.psitweaks.common.spells;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;

import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;
import vazkii.psi.common.spell.trick.block.PieceTrickBreakBlock;

import java.util.List;
import java.util.function.Predicate;

public class PieceTrickBreakBlockSilk extends PieceTrick {

    private static final int POTENCY = 250;
    private static final int COST = 250;

    SpellParam<Vector3> position;

    public PieceTrickBreakBlockSilk(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(POTENCY));
        setStatLabel(EnumSpellStat.COST, new StatLabel(COST));
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        meta.addStat(EnumSpellStat.POTENCY, POTENCY);
        meta.addStat(EnumSpellStat.COST, COST);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 positionVal = this.getParamValue(context, position);

        if (positionVal == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }
        if (!context.isInRadius(positionVal)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }

        BlockPos pos = positionVal.toBlockPos();
        Level world = context.focalPoint.getCommandSenderWorld();

        ItemStack silkTool = createSilkTool();

        removeBlockWithDropsSilk(context, context.caster, world, silkTool, pos,
                (state) -> silkTool.isCorrectToolForDrops(state) ||
                        PieceTrickBreakBlock.canHarvest(4, state));

        return null;
    }

    private ItemStack createSilkTool() {
        ItemStack tool = new ItemStack(Items.NETHERITE_PICKAXE);
        tool.enchant(Enchantments.SILK_TOUCH, 1);
        return tool;
    }

    public static void removeBlockWithDropsSilk(SpellContext context, Player player, Level world,
                                                ItemStack stack, BlockPos pos, Predicate<BlockState> filter) {

        if (stack.isEmpty()) {
            stack = PsiAPI.getPlayerCAD(player);
        }

        if (!world.hasChunkAt(pos)) {
            return;
        }

        BlockState blockstate = world.getBlockState(pos);
        boolean unminable = blockstate.getDestroySpeed(world, pos) == -1;

        if (!world.isClientSide && !unminable && filter.test(blockstate) && !blockstate.isAir()) {
            if (!world.mayInteract(player, pos)) {
                return;
            }

            ((ServerPlayer) player).connection.send(
                    new ClientboundLevelEventPacket(LevelEvent.PARTICLES_DESTROY_BLOCK, pos,
                            Block.getId(blockstate), false));

            List<ItemStack> drops = Block.getDrops(blockstate, (ServerLevel) world, pos,
                    world.getBlockEntity(pos), player, stack);

            for (ItemStack drop : drops) {
                Block.popResource(world, pos, drop);
            }

            world.removeBlock(pos, false);
        }
    }
}
