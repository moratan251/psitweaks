package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import io.netty.handler.codec.DecoderException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import vazkii.psi.common.client.PsiClientRuntime;

/**
 * The outline of Trick: Show Region: Psi's sparkles along each edge ({@code from} to {@code to}), in the spell colour.
 * <p>
 * This replaces Psi's own particle trail message, which hands the colour to the sparkle as 0-255 instead of 0-1. The
 * vertex colour then wraps around, so pure colours (a white or red colorizer) came out black and vanished in the
 * additive blending of the sparkle.
 */
public record MessageRegionOutline(List<Vec3> from, List<Vec3> to, int color, int lifetime) implements CustomPacketPayload {
    public static final int MAX_EDGES = 12;
    /** Sparkles per block, as in Psi's particle trail. */
    private static final double STEPS_PER_BLOCK = 4.0;
    /** The longest edge drawn, so a huge region cannot spawn an unbounded number of sparkles. */
    private static final double MAX_EDGE_LENGTH = 256.0;

    public static final Type<MessageRegionOutline> TYPE = new Type<>(Psitweaks.location("region_outline"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageRegionOutline> STREAM_CODEC =
            CustomPacketPayload.codec(MessageRegionOutline::write, MessageRegionOutline::read);

    public MessageRegionOutline {
        int count = Math.min(Math.min(from.size(), to.size()), MAX_EDGES);
        from = List.copyOf(from.subList(0, count));
        to = List.copyOf(to.subList(0, count));
        lifetime = Math.max(1, lifetime);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(from.size());
        for (int i = 0; i < from.size(); i++) {
            writeVec(buf, from.get(i));
            writeVec(buf, to.get(i));
        }
        buf.writeInt(color);
        buf.writeVarInt(lifetime);
    }

    private static MessageRegionOutline read(RegistryFriendlyByteBuf buf) {
        int count = buf.readVarInt();
        if (count < 0 || count > MAX_EDGES) {
            throw new DecoderException("Too many region outline edges: " + count);
        }
        List<Vec3> from = new ArrayList<>(count);
        List<Vec3> to = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            from.add(readVec(buf));
            to.add(readVec(buf));
        }
        return new MessageRegionOutline(from, to, buf.readInt(), buf.readVarInt());
    }

    private static void writeVec(RegistryFriendlyByteBuf buf, Vec3 vec) {
        buf.writeDouble(vec.x);
        buf.writeDouble(vec.y);
        buf.writeDouble(vec.z);
    }

    private static Vec3 readVec(RegistryFriendlyByteBuf buf) {
        return new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public static void handle(MessageRegionOutline message, IPayloadContext context) {
        context.enqueueWork(() -> message.spawn(context.player()));
    }

    private void spawn(Player player) {
        Level level = player.level();
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        for (int i = 0; i < from.size(); i++) {
            Vec3 start = from.get(i);
            Vec3 line = to.get(i).subtract(start);
            double length = Math.min(line.length(), MAX_EDGE_LENGTH);
            if (length < 1.0E-6) {
                continue;
            }
            Vec3 step = line.normalize().scale(1.0 / STEPS_PER_BLOCK);
            // One more sparkle than steps, so the far corner is drawn too.
            int steps = (int) Math.floor(length * STEPS_PER_BLOCK) + 1;
            for (int s = 0; s < steps; s++) {
                PsiClientRuntime.sparkle(level, start.x + step.x * s, start.y + step.y * s, start.z + step.z * s,
                        r, g, b, 0.0F, 0.0F, 0.0F, 1.0F, lifetime);
            }
        }
    }
}
