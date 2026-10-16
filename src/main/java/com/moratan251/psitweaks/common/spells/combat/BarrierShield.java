package com.moratan251.psitweaks.common.spells.combat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * The shield raised by Trick: Barrier. Immutable; every change replaces the attachment so it is saved and synced.
 *
 * @param amount    shield points left; incoming damage is taken from here first
 * @param maxAmount shield points when it was raised (HUD bar and flash brightness)
 * @param expiresAt game time at which the shield fades
 * @param color     RGB colour of the honeycomb effect (the caster's CAD colour)
 */
public record BarrierShield(float amount, float maxAmount, long expiresAt, int color) {
    public static final BarrierShield NONE = new BarrierShield(0.0F, 0.0F, 0L, 0);

    public static final Codec<BarrierShield> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("amount").forGetter(BarrierShield::amount),
            Codec.FLOAT.fieldOf("max").forGetter(BarrierShield::maxAmount),
            Codec.LONG.fieldOf("expires_at").forGetter(BarrierShield::expiresAt),
            Codec.INT.fieldOf("color").forGetter(BarrierShield::color)
    ).apply(instance, BarrierShield::new));

    public static final StreamCodec<ByteBuf, BarrierShield> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, BarrierShield::amount,
            ByteBufCodecs.FLOAT, BarrierShield::maxAmount,
            ByteBufCodecs.VAR_LONG, BarrierShield::expiresAt,
            ByteBufCodecs.INT, BarrierShield::color,
            BarrierShield::new);

    public boolean isPresent() {
        return amount > 0.0F;
    }

    public boolean isActive(long gameTime) {
        return amount > 0.0F && gameTime < expiresAt;
    }

    public BarrierShield withAmount(float newAmount) {
        return new BarrierShield(newAmount, maxAmount, expiresAt, color);
    }

    /** Remaining shield as a fraction of the amount it was raised with. */
    public float fraction() {
        return maxAmount <= 0.0F ? 0.0F : Math.min(1.0F, amount / maxAmount);
    }
}
