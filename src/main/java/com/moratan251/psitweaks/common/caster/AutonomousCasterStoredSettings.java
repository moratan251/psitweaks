package com.moratan251.psitweaks.common.caster;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * 回収した自律詠唱機のブロックアイテムに残す設定。置き直したときに機械へ戻す。
 * 値の範囲は戻すときに機械の側で収めるので、ここでは確かめない。
 *
 * @param bullet      選択中の術式弾スロット（0 始まり）
 * @param itemSlot    選択中のアイテムスロット（0 始まり）
 * @param trigger     トリガーの種類（{@code AutonomousCasterBlockEntity.TriggerMode} の序数）
 * @param interval    詠唱間隔（tick）
 * @param yaw         正面からの左右の角度
 * @param pitch       上下の角度
 */
public record AutonomousCasterStoredSettings(int bullet, int itemSlot, int trigger, int interval, int yaw, int pitch) {
    public static final Codec<AutonomousCasterStoredSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("bullet", 0).forGetter(AutonomousCasterStoredSettings::bullet),
            Codec.INT.optionalFieldOf("item_slot", 0).forGetter(AutonomousCasterStoredSettings::itemSlot),
            Codec.INT.optionalFieldOf("trigger", 0).forGetter(AutonomousCasterStoredSettings::trigger),
            Codec.INT.fieldOf("interval").forGetter(AutonomousCasterStoredSettings::interval),
            Codec.INT.optionalFieldOf("yaw", 0).forGetter(AutonomousCasterStoredSettings::yaw),
            Codec.INT.optionalFieldOf("pitch", 0).forGetter(AutonomousCasterStoredSettings::pitch)
    ).apply(instance, AutonomousCasterStoredSettings::new));

    public static final StreamCodec<ByteBuf, AutonomousCasterStoredSettings> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, AutonomousCasterStoredSettings::bullet,
            ByteBufCodecs.VAR_INT, AutonomousCasterStoredSettings::itemSlot,
            ByteBufCodecs.VAR_INT, AutonomousCasterStoredSettings::trigger,
            ByteBufCodecs.VAR_INT, AutonomousCasterStoredSettings::interval,
            ByteBufCodecs.VAR_INT, AutonomousCasterStoredSettings::yaw,
            ByteBufCodecs.VAR_INT, AutonomousCasterStoredSettings::pitch,
            AutonomousCasterStoredSettings::new);
}
