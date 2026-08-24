package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.menu.IdeaStorageMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** 保存済み空バケツ1個と対象Fluid 1 Bを、カーソル上の満たされたバケツへ変換する要求。 */
public record MessageIdeaStorageFillBucket(FluidStack fluidTemplate) implements CustomPacketPayload {
    public static final Type<MessageIdeaStorageFillBucket> TYPE =
            new Type<>(Psitweaks.location("idea_storage_fill_bucket"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageIdeaStorageFillBucket> STREAM_CODEC =
            CustomPacketPayload.codec(MessageIdeaStorageFillBucket::write, MessageIdeaStorageFillBucket::read);

    public MessageIdeaStorageFillBucket {
        fluidTemplate = fluidTemplate.isEmpty() ? FluidStack.EMPTY : fluidTemplate.copyWithAmount(1);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private void write(RegistryFriendlyByteBuf buf) {
        FluidStack.STREAM_CODEC.encode(buf, fluidTemplate);
    }

    private static MessageIdeaStorageFillBucket read(RegistryFriendlyByteBuf buf) {
        return new MessageIdeaStorageFillBucket(FluidStack.STREAM_CODEC.decode(buf));
    }

    public static void handle(MessageIdeaStorageFillBucket message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer
                    && serverPlayer.containerMenu instanceof IdeaStorageMenu menu
                    && menu.ownerUuid().equals(serverPlayer.getUUID())
                    && menu.stillValid(serverPlayer)) {
                menu.handleFillStoredBucket(message.fluidTemplate());
            }
        });
    }
}
