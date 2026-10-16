package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.client.combat.CombatEffectsClient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * A short-lived spherical flash drawn on clients: the honeycomb shield of Trick: Barrier and the shock spheres of
 * the combat tricks. When {@code entityId} is set the flash follows that entity; otherwise it stays at the position.
 */
public record MessageSphereFlash(int entityId, Vec3 position, float radius, int color, Style style,
                                 Vec3 impactDirection, float strength) implements CustomPacketPayload {
    public static final int NO_ENTITY = -1;

    public static final Type<MessageSphereFlash> TYPE = new Type<>(Psitweaks.location("sphere_flash"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageSphereFlash> STREAM_CODEC =
            CustomPacketPayload.codec(MessageSphereFlash::write, MessageSphereFlash::read);

    public enum Style {
        /** The shield is raised: the sphere grows into place. */
        BARRIER_RAISE,
        /** The shield took a hit: a bright flash, strongest on the side the hit came from. */
        BARRIER_HIT,
        /** The shield ran out: the sphere bursts outward and shatters. */
        BARRIER_BREAK,
        /** The shield timed out: a faint fade. */
        BARRIER_FADE,
        /** An expanding plain shock sphere (Burst, Starfall impact). */
        SHOCKWAVE,
        /** A contracting sphere followed by a flash (Gravity Collapse). */
        IMPLOSION;

        private static final Style[] VALUES = values();

        static Style byId(int id) {
            return id >= 0 && id < VALUES.length ? VALUES[id] : SHOCKWAVE;
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeDouble(position.x);
        buf.writeDouble(position.y);
        buf.writeDouble(position.z);
        buf.writeFloat(radius);
        buf.writeInt(color);
        buf.writeVarInt(style.ordinal());
        buf.writeFloat((float) impactDirection.x);
        buf.writeFloat((float) impactDirection.y);
        buf.writeFloat((float) impactDirection.z);
        buf.writeFloat(strength);
    }

    private static MessageSphereFlash read(RegistryFriendlyByteBuf buf) {
        int entityId = buf.readVarInt();
        Vec3 position = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        float radius = buf.readFloat();
        int color = buf.readInt();
        Style style = Style.byId(buf.readVarInt());
        Vec3 direction = new Vec3(buf.readFloat(), buf.readFloat(), buf.readFloat());
        float strength = buf.readFloat();
        return new MessageSphereFlash(entityId, position, radius, color, style, direction, strength);
    }

    public static void handle(MessageSphereFlash message, IPayloadContext context) {
        context.enqueueWork(() -> CombatEffectsClient.handleSphereFlash(message));
    }
}
