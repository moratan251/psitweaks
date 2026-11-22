package com.moratan251.psitweaks.common.effects;

import com.moratan251.psitweaks.Psitweaks;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 鈍化（Torpor）: 減速領域の中の生き物にかける。強さ（レベル = 増幅値 + 1）1 ごとに、移動速度と飛ぶ速さを 1%、攻撃速度
 * （プレイヤー）を 0.6% 下げる。減速領域は中の威力 P に対してレベル P × 5 でかけるので、移動速度は P × 5%、攻撃速度は
 * P × 3% 下がる（{@code EntityDecelerationZone#amplifierFor}）。
 */
public class EffectTorpor extends MobEffect {
    /** レベル 1 あたりの移動速度・飛ぶ速さの下げ幅（合計への掛け算）。 */
    public static final double MOVEMENT_PER_LEVEL = -0.01;
    /** レベル 1 あたりの攻撃速度の下げ幅（合計への掛け算）。 */
    public static final double ATTACK_SPEED_PER_LEVEL = -0.006;
    /** 色: ソウルサンドの茶色。 */
    public static final int COLOR = 0x7A5A3C;

    public EffectTorpor() {
        super(MobEffectCategory.HARMFUL, COLOR);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, Psitweaks.location("effect.torpor.movement_speed"),
                MOVEMENT_PER_LEVEL, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.FLYING_SPEED, Psitweaks.location("effect.torpor.flying_speed"),
                MOVEMENT_PER_LEVEL, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.ATTACK_SPEED, Psitweaks.location("effect.torpor.attack_speed"),
                ATTACK_SPEED_PER_LEVEL, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }
}
