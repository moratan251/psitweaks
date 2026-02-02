package com.moratan251.psitweaks.common.spells;

import com.moratan251.psitweaks.common.items.PsitweaksItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import vazkii.psi.api.recipe.ITrickRecipe;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.piece.PieceCraftingTrick;
import vazkii.psi.common.spell.trick.infusion.PieceTrickEbonyIvory;

public class PieceTrickSupremeInfusion extends PieceTrickEbonyIvory {

    public PieceTrickSupremeInfusion(Spell spell) {
        super(spell);
        // EbonyIvoryよりさらに高いコスト設定
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(500));
        setStatLabel(EnumSpellStat.COST, new StatLabel(7000));
    }

    @Override
    protected void addPotencyAndCost(SpellMetadata meta) throws SpellCompilationException {
        meta.addStat(EnumSpellStat.POTENCY, 500);
        meta.addStat(EnumSpellStat.COST, 7000);
    }

    @Override
    public boolean canCraft(PieceCraftingTrick trick) {
        return trick instanceof PieceTrickSupremeInfusion;
    }
}