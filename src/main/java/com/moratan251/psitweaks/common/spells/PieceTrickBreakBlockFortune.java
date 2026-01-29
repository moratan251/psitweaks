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
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;
import vazkii.psi.common.spell.trick.block.PieceTrickBreakBlock;

import java.util.List;
import java.util.function.Predicate;

public class PieceTrickBreakBlockFortune extends PieceTrick {

    SpellParam<Vector3> position;
    SpellParam<Number> fortuneLevel;

    public PieceTrickBreakBlockFortune(Spell spell) {
        super(spell);
        // 幸運レベルに応じてコストが変動
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(80).add(new StatLabel("psi.spellparam.power", true).mul(50)));
        setStatLabel(EnumSpellStat.COST, new StatLabel(100).add(new StatLabel("psi.spellparam.power", true).mul(100)));
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
        addParam(fortuneLevel = new ParamNumber("psi.spellparam.power", SpellParam.GREEN, false, true));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);

        Double fortuneVal = this.<Double>getParamEvaluation(fortuneLevel);
        if (fortuneVal == null || fortuneVal < 1 || fortuneVal > 3) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_VALUE, x, y);
        }

        int fortune = fortuneVal.intValue();
        meta.addStat(EnumSpellStat.POTENCY, 80 + fortune * 50);
        meta.addStat(EnumSpellStat.COST, 100 + fortune * 100);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 positionVal = this.getParamValue(context, position);
        Number fortuneVal = this.getParamValue(context, fortuneLevel);

        if (positionVal == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }
        if (!context.isInRadius(positionVal)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }

        int fortune = Math.min(3, Math.max(1, fortuneVal.intValue())); // 1-3に制限
        BlockPos pos = positionVal.toBlockPos();
        Level world = context.focalPoint.getCommandSenderWorld();

        // 幸運付きのダイヤモンドピッケルを作成
        ItemStack fortuneTool = createFortuneTool(fortune);

        removeBlockWithDropsFortune(context, context.caster, world, fortuneTool, pos,
                (state) -> fortuneTool.isCorrectToolForDrops(state) ||
                        PieceTrickBreakBlock.canHarvest(4, state));

        return null;
    }

    /**
     * 幸運エンチャント付きのツールを作成
     */
    private ItemStack createFortuneTool(int level) {
        ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
        tool.enchant(Enchantments.BLOCK_FORTUNE, level);
        return tool;
    }

    /**
     * 幸運でブロックを破壊してドロップを得る
     */
    public static void removeBlockWithDropsFortune(SpellContext context, Player player, Level world,
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

            // ブロック破壊エフェクトを送信
            ((ServerPlayer) player).connection.send(
                    new ClientboundLevelEventPacket(LevelEvent.PARTICLES_DESTROY_BLOCK, pos,
                            Block.getId(blockstate), false));

            // 幸運でドロップを取得
            List<ItemStack> drops = Block.getDrops(blockstate, (ServerLevel) world, pos,
                    world.getBlockEntity(pos), player, stack);

            // ドロップをスポーン
            for (ItemStack drop : drops) {
                Block.popResource(world, pos, drop);
            }

            // ブロックを削除
            world.removeBlock(pos, false);
        }
    }
}