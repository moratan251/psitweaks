package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.client.combat.CombatEffectsClient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * The night of Trick: Meteor Line ran out of integrity: clients draw its dark sphere shattering into shards
 * (2026-11-14). It carries what the night looked like at that moment, so the effect does not need the entity, which
 * is removed right after: the centre, the full and the current radius, how strong the night was ({@code strength},
 * 0..1), its age in ticks, the seed of its stars and the caster's CAD colour.
 */
public record MessageMeteorNightShatter(Vec3 center, float radius, float currentRadius, float strength, float age,
                                        long seed, int color) implements CustomPacketPayload {
    public static final Type<MessageMeteorNightShatter> TYPE = new Type<>(Psitweaks.location("meteor_night_shatter"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageMeteorNightShatter> STREAM_CODEC =
            CustomPacketPayload.codec(MessageMeteorNightShatter::write, MessageMeteorNightShatter::read);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeDouble(center.x);
        buf.writeDouble(center.y);
        buf.writeDouble(center.z);
        buf.writeFloat(radius);
        buf.writeFloat(currentRadius);
        buf.writeFloat(strength);
        buf.writeFloat(age);
        buf.writeLong(seed);
        buf.writeInt(color);
    }

    private static MessageMeteorNightShatter read(RegistryFriendlyByteBuf buf) {
        Vec3 center = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        return new MessageMeteorNightShatter(center, buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(),
                buf.readLong(), buf.readInt());
    }

    public static void handle(MessageMeteorNightShatter message, IPayloadContext context) {
        context.enqueueWork(() -> CombatEffectsClient.handleMeteorNightShatter(message));
    }
}
