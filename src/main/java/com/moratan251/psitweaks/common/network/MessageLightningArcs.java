package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.client.combat.CombatEffectsClient;
import io.netty.handler.codec.DecoderException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * The bolts of a Trick: Chain Lightning cast, as a tree: point 0 is where the bolt starts, and an arc is drawn to every
 * other point from its parent ({@code parents}, always an earlier point). An arc lights up a little after the arc that
 * reached its parent.
 */
public record MessageLightningArcs(List<Vec3> points, List<Integer> parents, int color, long seed)
        implements CustomPacketPayload {
    public static final int MAX_POINTS = 16;

    public static final Type<MessageLightningArcs> TYPE = new Type<>(Psitweaks.location("lightning_arcs"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageLightningArcs> STREAM_CODEC =
            CustomPacketPayload.codec(MessageLightningArcs::write, MessageLightningArcs::read);

    public MessageLightningArcs {
        int count = Math.min(Math.min(points.size(), parents.size()), MAX_POINTS);
        points = List.copyOf(points.subList(0, count));
        List<Integer> checked = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            Integer parent = parents.get(i);
            checked.add(i == 0 ? -1 : parent == null ? 0 : Math.max(0, Math.min(parent, i - 1)));
        }
        parents = List.copyOf(checked);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(points.size());
        for (int i = 0; i < points.size(); i++) {
            Vec3 point = points.get(i);
            buf.writeDouble(point.x);
            buf.writeDouble(point.y);
            buf.writeDouble(point.z);
            if (i > 0) {
                buf.writeVarInt(parents.get(i));
            }
        }
        buf.writeInt(color);
        buf.writeLong(seed);
    }

    private static MessageLightningArcs read(RegistryFriendlyByteBuf buf) {
        int count = buf.readVarInt();
        if (count < 0 || count > MAX_POINTS) {
            throw new DecoderException("Too many lightning arc points: " + count);
        }
        List<Vec3> points = new ArrayList<>(count);
        List<Integer> parents = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            points.add(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
            parents.add(i == 0 ? -1 : buf.readVarInt());
        }
        return new MessageLightningArcs(points, parents, buf.readInt(), buf.readLong());
    }

    public static void handle(MessageLightningArcs message, IPayloadContext context) {
        context.enqueueWork(() -> CombatEffectsClient.handleLightningArcs(message));
    }
}
