package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

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
import vazkii.psi.common.core.handler.ConfigHandler;
import vazkii.psi.common.spell.trick.block.PieceTrickBreakBlock;

import java.util.function.Predicate;

public class PieceTrickBreakBlockFortune extends PieceTrick {

    private static final int MAX_FORTUNE_LEVEL = 100;

    SpellParam<Vector3> position;
    SpellParam<Number> fortuneLevel;

    public PieceTrickBreakBlockFortune(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(150).add(new StatLabel("psi.spellparam.power", true).mul(100)));
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
        if (fortuneVal == null || !isValidFortuneLevel(fortuneVal)) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_VALUE, x, y);
        }

        int fortune = fortuneVal.intValue();
        meta.addStat(EnumSpellStat.POTENCY, Math.addExact(150, Math.multiplyExact(fortune, 100)));
        meta.addStat(EnumSpellStat.COST, Math.addExact(100, Math.multiplyExact(fortune, 100)));
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
        if (fortuneVal == null || !isValidFortuneLevel(fortuneVal.doubleValue())) {
            throw new SpellRuntimeException(SpellRuntimeException.NON_POSITIVE_VALUE);
        }

        int fortune = fortuneVal.intValue();
        BlockPos pos = positionVal.toBlockPos();
        Level level = context.focalPoint.getCommandSenderWorld();

        ItemStack fortuneTool = createFortuneTool(context, fortune);

        Predicate<BlockState> filter = state -> fortuneTool.isCorrectToolForDrops(state)
                || PieceTrickBreakBlock.canHarvest(ConfigHandler.COMMON.cadHarvestLevel.get(), state);

        PieceTrickBreakBlock.removeBlockWithDrops(context, context.caster, level, fortuneTool, pos, filter);

        return null;
    }

    private ItemStack createFortuneTool(SpellContext context, int level) throws SpellRuntimeException {
        ItemStack tool = context.getHarvestTool().copy();
        Holder<Enchantment> fortune = context.caster.level().registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.FORTUNE);
        tool.enchant(fortune, level);
        return tool;
    }

    private static boolean isValidFortuneLevel(double value) {
        return Double.isFinite(value)
                && value >= 1
                && value == Math.rint(value)
                && value <= MAX_FORTUNE_LEVEL;
    }
}
