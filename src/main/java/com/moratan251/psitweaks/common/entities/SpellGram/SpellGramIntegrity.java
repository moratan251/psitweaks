package com.moratan251.psitweaks.common.entities.SpellGram;

import com.moratan251.psitweaks.api.event.SpellGramIntegrityEvent;
import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

/**
 * 魔法式オブジェクトの耐久値（{@link SpellGramObject#getIntegrity}）。体力ではなく Psitweaks の独自の値で、ふつうの攻撃では
 * 減らず、術式解体などの術式からだけ減る。0 になると魔法式オブジェクトは {@link SpellGramObject.EndReason#DESTROYED} で消える。
 * <ul>
 *   <li>置くとき: 固有値 × 置いた時点の事象干渉力 × 威力補正 × 設定の倍率（{@link #initial}）</li>
 *   <li>減らすとき: 術式の量 × 減らす側の事象干渉力（{@link #reduce}）。減る直前に {@link SpellGramIntegrityEvent} を出す</li>
 * </ul>
 */
public final class SpellGramIntegrity {
    private SpellGramIntegrity() {
    }

    /** 置くときの耐久値。{@code powerFactor} は作動式の威力による補正（威力のない作動式は 1）。 */
    public static float initial(float base, @Nullable LivingEntity caster, double powerFactor) {
        double value = base * SpellGramObject.interferenceOf(caster) * Math.max(0.0D, powerFactor)
                * PsitweaksConfig.COMMON.spellGramIntegrityMultiplier.get();
        return (float) Math.max(0.0D, value);
    }

    /** 術式で {@code amount} だけ削るときに実際に減る量（イベントの前）。減らす側の事象干渉力を掛ける。 */
    public static float reduction(float amount, @Nullable LivingEntity reducer) {
        return (float) Math.max(0.0D, amount * SpellGramObject.interferenceOf(reducer));
    }

    /**
     * {@code reducer} の術式で {@code object} の耐久値を {@code amount}（減らす側の事象干渉力を掛ける前の量）だけ削る。
     * サーバー側だけ。
     *
     * @return 実際に減った量（取り消された、すでに消えているなどで減らなかったときは 0）
     */
    public static float reduce(SpellGramObject object, float amount, @Nullable LivingEntity reducer) {
        if (object.isEnded() || object.level().isClientSide || amount <= 0.0F) {
            return 0.0F;
        }
        SpellGramIntegrityEvent event = new SpellGramIntegrityEvent(object, reducer, reduction(amount, reducer));
        if (NeoForge.EVENT_BUS.post(event).isCanceled() || event.getAmount() <= 0.0F) {
            return 0.0F;
        }
        float removed = Math.min(object.getIntegrity(), event.getAmount());
        object.reduceIntegrity(event.getAmount());
        return removed;
    }
}
