package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.caster.CasterLog;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.tile.machine.AutonomousCasterBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** 自律詠唱機の作動ログ。GUI を開いている閲覧者へだけ、開いた時の全体と以降の差分を送る。 */
public record MessageAutonomousCasterLog(int containerId, CasterLog.Delta delta) implements CustomPacketPayload {
    public static final Type<MessageAutonomousCasterLog> TYPE = new Type<>(Psitweaks.location("autonomous_caster_log"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageAutonomousCasterLog> STREAM_CODEC =
            CustomPacketPayload.codec(MessageAutonomousCasterLog::write, MessageAutonomousCasterLog::read);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        CasterLog.Delta.STREAM_CODEC.encode(buf, delta);
    }

    private static MessageAutonomousCasterLog read(RegistryFriendlyByteBuf buf) {
        return new MessageAutonomousCasterLog(buf.readVarInt(), CasterLog.Delta.STREAM_CODEC.decode(buf));
    }

    public static void handle(MessageAutonomousCasterLog message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (MekanismCompat.isMekanismLoaded()
                    && !AutonomousCasterBlockEntity.applyClientLog(context.player(), message.containerId(), message.delta())) {
                context.reply(new MessageAutonomousCasterAction(message.containerId(),
                        MessageAutonomousCasterAction.Action.REQUEST_LOG, 0));
            }
        });
    }
}
