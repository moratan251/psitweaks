package com.moratan251.psitweaks.common.entities.SpellGram.marker;

import com.moratan251.psitweaks.common.attributes.PsitweaksAttributes;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * 目印型の魔法式オブジェクトの基底。術者が最初に魔力を与えたきりで、作動式が発動する場所の目印になる。
 * CAD の枠は使わない。ほかの術式で移動（{@link #setFollowTarget} など）・解体（{@link #destroyBySpell}）できる。
 * <p>
 * 剣や矢などふつうの攻撃では壊れず、強度は魔法式オブジェクト用の受け口 {@link #reduceIntegrity} でだけ減る。
 * 役目を終える・解体される・強度が尽きる・術者がいなくなると {@link #end} で消える。チャンクと一緒には保存しない。
 */
public abstract class SpellGramMarker extends SpellGramObject {
    /** 目印型が消えた理由。領域型の試作（{@code EntitySpellGramRegion.EndReason}）と後でまとめる。 */
    public enum EndReason {
        /** 作動式の役目を終えた。 */
        FINISHED,
        /** ほかの術式で解体された。 */
        DISPELLED,
        /** 強度が尽きた。 */
        DESTROYED,
        /** 術者がいなくなった。 */
        CASTER_GONE
    }

    private AttackRules.Options options = AttackRules.Options.DEFAULT;
    private float maxIntegrity = 1.0F;
    private boolean ended;

    protected SpellGramMarker(EntityType<? extends SpellGramMarker> type, Level level) {
        super(type, level);
    }

    /** 置く前に呼ぶ。強度は置いた時点の値で決まる。 */
    protected void configureMarker(LivingEntity caster, AttackRules.Options options, float integrity) {
        setCaster(caster);
        this.options = options;
        this.maxIntegrity = Math.max(0.0F, integrity);
        setIntegrity(this.maxIntegrity);
    }

    /** 術者の事象干渉力（置いた時点の値を使う）。 */
    public static double interferenceOf(@Nullable LivingEntity caster) {
        return caster != null ? caster.getAttributeValue(PsitweaksAttributes.SPELL_DAMAGE_FACTOR) : 1.0D;
    }

    public float getMaxIntegrity() {
        return maxIntegrity;
    }

    @Override
    protected AttackRules.Options attackOptions() {
        return options;
    }

    public boolean isEnded() {
        return ended || isRemoved();
    }

    /** 目印を消す。何度呼んでも {@link #onEnd} は一度だけ。 */
    public void end(EndReason reason) {
        if (isEnded() || level().isClientSide) {
            return;
        }
        ended = true;
        onEnd(reason);
        discard();
    }

    /** 消える直前（サーバー側）。 */
    protected void onEnd(EndReason reason) {
    }

    @Override
    protected void onIntegrityDepleted() {
        end(EndReason.DESTROYED);
    }

    @Override
    public void destroyBySpell(@Nullable DamageSource source) {
        end(EndReason.DISPELLED);
    }

    @Override
    protected void tickLifetime() {
        int remaining = getRemainingTicks() - 1;
        setRemainingTicks(remaining);
        if (remaining <= 0) {
            end(EndReason.FINISHED);
        }
    }

    /** ふつうの攻撃では壊れない。強度は {@link #reduceIntegrity} でだけ減る。 */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}
