package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import vazkii.psi.api.internal.PsiRenderHelper;
import vazkii.psi.common.Psi;

public record MessageFlightPsiCastEffect(int entityId, int color) implements CustomPacketPayload {
    private static final int PARTICLE_COUNT = 25;
    private static final double DIRECTION_VARIANCE = 0.25;
    private static final double PARTICLE_SPEED = 0.15;
    private static final float PARTICLE_SIZE = 0.3F;
    private static final int PARTICLE_LIFETIME = 5;

    public static final Type<MessageFlightPsiCastEffect> TYPE =
            new Type<>(Psitweaks.location("flight_psi_cast_effect"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageFlightPsiCastEffect> STREAM_CODEC =
            CustomPacketPayload.codec(MessageFlightPsiCastEffect::write, MessageFlightPsiCastEffect::read);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeInt(color);
    }

    private static MessageFlightPsiCastEffect read(RegistryFriendlyByteBuf buf) {
        return new MessageFlightPsiCastEffect(buf.readVarInt(), buf.readInt());
    }

    public static void handle(MessageFlightPsiCastEffect message, IPayloadContext context) {
        context.enqueueWork(() -> {
            Entity entity = context.player().level().getEntity(message.entityId());
            if (entity instanceof Player player) {
                spawnParticles(player, message.color());
            }
        });
    }

    private static void spawnParticles(Player player, int color) {
        float red = PsiRenderHelper.r(color) / 255.0F;
        float green = PsiRenderHelper.g(color) / 255.0F;
        float blue = PsiRenderHelper.b(color) / 255.0F;
        double x = player.getX();
        double y = player.getY() + player.getEyeHeight() - 0.1;
        double z = player.getZ();
        Vec3 look = player.getLookAngle();
        RandomSource random = player.getRandom();

        for (int i = 0; i < PARTICLE_COUNT; i++) {
            Vec3 direction = look.add(
                            (random.nextDouble() - 0.5) * DIRECTION_VARIANCE,
                            (random.nextDouble() - 0.5) * DIRECTION_VARIANCE,
                            (random.nextDouble() - 0.5) * DIRECTION_VARIANCE)
                    .normalize()
                    .scale(PARTICLE_SPEED);
            Psi.proxy.sparkleFX(
                    x,
                    y,
                    z,
                    red,
                    green,
                    blue,
                    (float) direction.x,
                    (float) direction.y,
                    (float) direction.z,
                    PARTICLE_SIZE,
                    PARTICLE_LIFETIME
            );
        }
    }
}
