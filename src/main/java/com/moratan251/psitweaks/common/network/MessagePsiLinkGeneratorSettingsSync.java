package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.tile.machine.PsionicGeneratorBlockEntity;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MessagePsiLinkGeneratorSettingsSync(int consumePsiPerTick, boolean linkActive) implements CustomPacketPayload {
    public static final Type<MessagePsiLinkGeneratorSettingsSync> TYPE =
            new Type<>(Psitweaks.location("psi_link_generator_settings_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessagePsiLinkGeneratorSettingsSync> STREAM_CODEC =
            CustomPacketPayload.codec(MessagePsiLinkGeneratorSettingsSync::write, MessagePsiLinkGeneratorSettingsSync::read);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(consumePsiPerTick);
        buf.writeBoolean(linkActive);
    }

    private static MessagePsiLinkGeneratorSettingsSync read(RegistryFriendlyByteBuf buf) {
        return new MessagePsiLinkGeneratorSettingsSync(buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(MessagePsiLinkGeneratorSettingsSync message, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (!(player.containerMenu instanceof MekanismTileContainer<?> menu)
                    || !(menu.getTileEntity() instanceof PsionicGeneratorBlockEntity generator)
                    || !menu.stillValid(player)
                    || !menu.canPlayerAccess(player)
                    || !generator.isOwnedBy(player)) {
                return;
            }
            generator.applySettings(message.consumePsiPerTick, message.linkActive);
        });
    }
}
