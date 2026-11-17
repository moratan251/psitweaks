package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.caster.CasterIds;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramCircle;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIndex;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import com.moratan251.psitweaks.common.spells.param.ParamEntityOrEntityList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.param.ParamEntity;
import vazkii.psi.api.spell.piece.PieceTrick;

import java.util.ArrayList;
import java.util.List;

/**
 * 作動式: サークル追従。自分のサークル（{@link SpellGramCircle}）を、追従先の位置へ毎 tick 移るようにする。追従先を入れなければ
 * 追従をやめ、その場に残す（放す）。
 * <ul>
 *   <li>対象: 自分のサークル（エンティティかリスト）。省くと、このディメンションの術者の一番新しいサークル。サークルでないもの・
 *   自分のものでないものが 1 つでも混ざるとエラーで、何も変えない（自分のものの決まりは作動式: 魔法式回収・延長と同じ）。サークル
 *   自体はどれだけ遠くてもよい。</li>
 *   <li>追従先: 術者か、生き物でないエンティティ。魔法式オブジェクトと、術者以外の生き物が乗っている乗り物は選べない。術者以外は
 *   術式の射程の中でなければならない。省くと放す。</li>
 * </ul>
 * 追従を続けられなくなったとき（追従先が消えた・遠すぎるなど）と、動いた距離のぶん耐久値を使うのは {@link SpellGramCircle}。
 */
public class PieceTrickCircleFollow extends PieceTrick {
    public static final String ERROR_INVALID_FOLLOW_TARGET = "psitweaks.spellerror.invalid_follow_target";
    public static final String ERROR_FOLLOW_TARGET_RIDDEN = "psitweaks.spellerror.follow_target_ridden";
    /** 入力「追従先」の名前。 */
    public static final String PARAM_FOLLOW_TARGET = "psitweaks.spellparam.follow_target";

    // 仮の数値
    private static final int POTENCY = 40;
    private static final int COST = 150;

    private SpellParam<?> target;
    private SpellParam<Entity> followTarget;

    public PieceTrickCircleFollow(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(POTENCY));
        setStatLabel(EnumSpellStat.COST, new StatLabel(COST));
    }

    @Override
    public void initParams() {
        addParam(target = new ParamEntityOrEntityList(SpellParam.GENERIC_NAME_TARGET, SpellParam.YELLOW, true));
        addParam(followTarget = new ParamEntity(PARAM_FOLLOW_TARGET, SpellParam.BLUE, true, false));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        meta.addStat(EnumSpellStat.POTENCY, POTENCY);
        meta.addStat(EnumSpellStat.COST, COST);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        if (context.caster == null) {
            return null;
        }
        List<SpellGramCircle> circles = circles(context);
        Entity destination = getParamValue(context, followTarget);
        if (destination == null) {
            for (SpellGramCircle circle : circles) {
                circle.stopFollowing();
            }
            return null;
        }
        checkDestination(context, destination);
        for (SpellGramCircle circle : circles) {
            circle.startFollowing(destination);
            Vec3 at = circle.position();
            circle.level().playSound(null, at.x, at.y, at.z, SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.4F, 1.8F);
        }
        return null;
    }

    /** 対象のサークル。省けばこのディメンションの術者の一番新しいサークル（なければエラー）。全部確かめてから返す。 */
    private List<SpellGramCircle> circles(SpellContext context) throws SpellRuntimeException {
        Object value = getParamValue(context, target);
        if (value == null) {
            SpellGramCircle newest = newestCircle(context.caster);
            if (newest == null) {
                throw new SpellRuntimeException(SpellRuntimeException.NULL_TARGET);
            }
            return List.of(newest);
        }
        List<SpellGramCircle> circles = new ArrayList<>();
        for (Entity entity : ParamEntityOrEntityList.entities(value)) {
            circles.add(SpellGramTargets.ownCircle(context, entity));
        }
        return circles;
    }

    /** このディメンションの {@code caster} の一番新しい（エンティティの番号が一番大きい）サークル。なければ null。 */
    @Nullable
    public static SpellGramCircle newestCircle(LivingEntity caster) {
        SpellGramCircle newest = null;
        for (SpellGramObject object : SpellGramIndex.ofCaster(caster.level(), CasterIds.idOf(caster))) {
            if (object instanceof SpellGramCircle circle && !circle.isEnded()
                    && (newest == null || circle.getId() > newest.getId())) {
                newest = circle;
            }
        }
        return newest;
    }

    /** 追従先の確かめ。術者ならよい。術者以外は、生き物でも魔法式オブジェクトでもなく、術者以外の生き物が乗っておらず、射程の中のもの。 */
    private static void checkDestination(SpellContext context, Entity entity) throws SpellRuntimeException {
        if (entity == context.caster) {
            return;
        }
        if (entity instanceof LivingEntity || entity instanceof SpellGramObject) {
            throw new SpellRuntimeException(ERROR_INVALID_FOLLOW_TARGET);
        }
        context.verifyEntity(entity);
        if (!context.isInRadius(entity)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }
        if (SpellGramCircle.isRiddenByOthers(entity, CasterIds.idOf(context.caster))) {
            throw new SpellRuntimeException(ERROR_FOLLOW_TARGET_RIDDEN);
        }
    }
}
