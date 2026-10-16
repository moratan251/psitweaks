package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.client.combat.CombatEffectsClient;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** The path of a Trick: Chain Lightning cast: arcs are drawn from each point to the next, one jump at a time. */
public record MessageLightningArcs(List<Vec3> points, int color, long seed) implements CustomPacketPayload {
    public static final int MAX_POINTS = 16;

    public static final Type<MessageLightningArcs> TYPE = new Type<>(Psitweaks.location("lightning_arcs"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageLightningArcs> STREAM_CODEC =
            CustomPacketPayload.codec(MessageLightningArcs::write, MessageLightningArcs::read);

    public MessageLightningArcs {
        points = List.copyOf(points.subList(0, Math.min(points.size(), MAX_POINTS)));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(points.size());
        for (Vec3 point : points) {
            buf.writeDouble(point.x);
            buf.writeDouble(point.y);
            buf.writeDouble(point.z);
        }
        buf.writeInt(color);
        buf.writeLong(seed);
    }

    private static MessageLightningArcs read(RegistryFriendlyByteBuf buf) {
        int count = Math.min(buf.readVarInt(), MAX_POINTS);
        List<Vec3> points = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            points.add(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
        }
        return new MessageLightningArcs(points, buf.readInt(), buf.readLong());
    }

    public static void handle(MessageLightningArcs message, IPayloadContext context) {
        context.enqueueWork(() -> CombatEffectsClient.handleLightningArcs(message));
    }
}
