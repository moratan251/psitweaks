package com.moratan251.psitweaks.common.spells.combat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Marks a mob frozen by Trick: Cocytus, so it is drawn encased in ice while its AI stays off.
 *
 * @param wasSilent whether the mob was already silent before Cocytus silenced it; restored when it thaws
 */
public record CocytusFreeze(boolean wasSilent) {
    public static final CocytusFreeze DEFAULT = new CocytusFreeze(false);

    public static final Codec<CocytusFreeze> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("was_silent", false).forGetter(CocytusFreeze::wasSilent)
    ).apply(instance, CocytusFreeze::new));

    public static final StreamCodec<ByteBuf, CocytusFreeze> STREAM_CODEC =
            ByteBufCodecs.BOOL.map(CocytusFreeze::new, CocytusFreeze::wasSilent);
}
