package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.tile.machine.AutonomousCasterBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** 自律詠唱機 GUI の操作。サーバー側で開いている menu・対象・セキュリティを確認してから反映する。 */
public record MessageAutonomousCasterAction(int containerId, Action action, int value) implements CustomPacketPayload {
    public static final Type<MessageAutonomousCasterAction> TYPE = new Type<>(Psitweaks.location("autonomous_caster_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageAutonomousCasterAction> STREAM_CODEC =
            CustomPacketPayload.codec(MessageAutonomousCasterAction::write, MessageAutonomousCasterAction::read);

    public enum Action {
        SELECT_BULLET,
        SELECT_ITEM_SLOT,
        SET_TRIGGER_MODE,
        SET_INTERVAL,
        SET_YAW,
        SET_PITCH,
        RESET_LOOP_COUNTER,
        CLEAR_LOG,
        /** クライアント側の作動ログが差分を反映できなかった時に、全体の再送を頼む。 */
        REQUEST_LOG,
        CAST_NOW
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        buf.writeEnum(action);
        buf.writeVarInt(value);
    }

    private static MessageAutonomousCasterAction read(RegistryFriendlyByteBuf buf) {
        return new MessageAutonomousCasterAction(buf.readVarInt(), buf.readEnum(Action.class), buf.readVarInt());
    }

    public static void handle(MessageAutonomousCasterAction message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (MekanismCompat.isMekanismLoaded()) {
                AutonomousCasterBlockEntity.handleAction(context.player(), message.containerId(), message.action(), message.value());
            }
        });
    }
}
