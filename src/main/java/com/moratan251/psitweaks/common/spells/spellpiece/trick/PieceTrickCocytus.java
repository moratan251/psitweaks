package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import com.moratan251.psitweaks.common.spells.combat.CocytusFreezes;
import com.moratan251.psitweaks.common.spells.combat.CombatEffects;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.piece.PieceTrick;

/**
 * 作動式: コキュートス。入力なし。術者の目から視線の方向へ吹雪を放ち、作動式: 術式解体と同じ円錐
 * （{@link PieceTrickGramDemolition#creaturesInCone}、ブロックは貫通する）の中のモブの精神を永久に凍結させる
 * （{@link CocytusFreezes#freeze}: AI を止めて氷に閉じ込める）。凍結するのは {@link CocytusFreezes#canFreeze} が許すモブだけで、
 * プレイヤー・術者の側（ペット・味方）は凍らない。ボスも凍るが、凍らせると倒せなくなる間（召喚直後に力を溜めているウィザー、
 * 倒されて息絶える途中のエンダードラゴン）は凍らない。術式の射程（{@link SpellContext#isInRadius(net.minecraft.world.entity.Entity)}）
 * の外のモブも凍らないので、術者より事象干渉力の高い他者の領域干渉の中のモブは凍らない。ダメージは与えない。
 */
public class PieceTrickCocytus extends PieceTrick {
    private static final int POTENCY = 1250;
    private static final int COST = 10_000;

    public PieceTrickCocytus(Spell spell) {
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
        if (!(context.caster.level() instanceof ServerLevel level)) {
            return null;
        }
        LivingEntity caster = context.caster;
        Vec3 eye = caster.getEyePosition();
        Vec3 look = caster.getLookAngle().normalize();
        freezeInCone(level, context, eye, look);

        CombatEffects.coneBlizzard(level, eye, look, (float) PieceTrickGramDemolition.RANGE, 1.0F);
        CombatEffects.sound(level, eye, SoundEvents.BREEZE_WIND_CHARGE_BURST, 1.2F, 0.6F);
        CombatEffects.sound(level, eye, SoundEvents.POWDER_SNOW_BREAK, 1.2F, 0.6F);
        CombatEffects.sound(level, eye, SoundEvents.PLAYER_HURT_FREEZE, 0.8F, 0.8F);
        return null;
    }

    /**
     * 目 {@code eye} から {@code look} の方向への円錐の中の、凍らせてよい（{@link CocytusFreezes#canFreeze}）、術式の射程の中の
     * モブをすべて凍結させ、凍らせたモブを返す。
     */
    public static List<Mob> freezeInCone(ServerLevel level, SpellContext context, Vec3 eye, Vec3 look) {
        LivingEntity caster = context.caster;
        AttackRules.Options options = AttackRules.Options.from(context);
        List<Mob> frozen = new ArrayList<>();
        for (LivingEntity living : PieceTrickGramDemolition.creaturesInCone(level, caster, eye, look)) {
            if (living instanceof Mob mob && CocytusFreezes.canFreeze(caster, mob, options) && context.isInRadius(mob)) {
                CocytusFreezes.freeze(level, mob);
                frozen.add(mob);
            }
        }
        return frozen;
    }
}
