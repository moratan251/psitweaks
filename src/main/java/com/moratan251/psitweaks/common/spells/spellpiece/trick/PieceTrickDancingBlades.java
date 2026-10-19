package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.entities.combat.EntityDancingBlade;
import com.moratan251.psitweaks.common.items.BladePouches;
import com.moratan251.psitweaks.common.spells.SpellSafetyUtils;
import com.moratan251.psitweaks.common.spells.combat.CombatEffects;
import com.moratan251.psitweaks.common.spells.combat.CombatSpellHelper;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.piece.PieceTrick;

/**
 * Deploys every sword in the caster's Blade Pouch (up to eight) as a spectral blade circling the caster. Each one darts
 * at a hostile creature nearby and cuts it as its sword would, three times, then fades; all of them fade after fifteen
 * seconds at most. The swords stay in the pouch and lose no durability; a sword with no durability left (a broken
 * psimetal sword) is not deployed.
 */
public class PieceTrickDancingBlades extends PieceTrick {
    public static final String ERROR_NO_SWORDS = "psitweaks.spellerror.required_item_missing";
    /** Fixed, since how many swords the pouch holds is only known when the spell runs: sized for a full pouch. */
    public static final int POTENCY = 900;
    public static final int COST = 1500;

    public PieceTrickDancingBlades(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(POTENCY));
        setStatLabel(EnumSpellStat.COST, new StatLabel(COST));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        meta.addStat(EnumSpellStat.POTENCY, POTENCY);
        meta.addStat(EnumSpellStat.COST, COST);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        List<ItemStack> swords = BladePouches.findActive(context.caster).map(BladePouches::deployableSwords).orElse(List.of());
        if (swords.isEmpty()) {
            throw new SpellRuntimeException(ERROR_NO_SWORDS);
        }
        if (!(context.caster.level() instanceof ServerLevel level)) {
            return null;
        }
        boolean safeToPlayers = SpellSafetyUtils.hasSafeToPlayers(context);
        int color = CombatSpellHelper.spellColor(context.caster);
        // Recasting replaces the caster's earlier blades instead of piling more up.
        for (EntityDancingBlade old : EntityDancingBlade.bladesOf(level, context.caster)) {
            old.discard();
        }
        for (int i = 0; i < swords.size(); i++) {
            EntityDancingBlade blade = new EntityDancingBlade(level, context.caster, i, swords.size());
            blade.configure(context.caster, safeToPlayers, 0.0F, color);
            blade.setSword(swords.get(i));
            level.addFreshEntity(blade);
        }
        CombatEffects.sound(level, context.caster.position(), SoundEvents.TRIDENT_RETURN, 0.9F, 1.3F);
        CombatEffects.sound(level, context.caster.position(), SoundEvents.ENCHANTMENT_TABLE_USE, 0.8F, 1.6F);
        return null;
    }
}
