package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.menu.IdeaStorageMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

/** カーソル上の容器とFluid/Chemicalストレージ間の右クリック転送要求。 */
public record MessageIdeaStorageTransferContents(int targetKind, FluidStack fluidTemplate,
                                                 @Nullable ResourceLocation chemicalId, boolean bulk)
        implements CustomPacketPayload {
    public static final int TARGET_NONE = 0;
    public static final int TARGET_FLUID = 1;
    public static final int TARGET_CHEMICAL = 2;

    public static final Type<MessageIdeaStorageTransferContents> TYPE =
            new Type<>(Psitweaks.location("idea_storage_transfer_contents"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageIdeaStorageTransferContents> STREAM_CODEC =
            CustomPacketPayload.codec(MessageIdeaStorageTransferContents::write,
                    MessageIdeaStorageTransferContents::read);

    public MessageIdeaStorageTransferContents {
        if (targetKind == TARGET_FLUID && !fluidTemplate.isEmpty()) {
            fluidTemplate = fluidTemplate.copyWithAmount(1);
            chemicalId = null;
        } else if (targetKind == TARGET_CHEMICAL && chemicalId != null) {
            fluidTemplate = FluidStack.EMPTY;
        } else {
            targetKind = TARGET_NONE;
            fluidTemplate = FluidStack.EMPTY;
            chemicalId = null;
        }
    }

    public static MessageIdeaStorageTransferContents emptyTarget(boolean bulk) {
        return new MessageIdeaStorageTransferContents(TARGET_NONE, FluidStack.EMPTY, null, bulk);
    }

    public static MessageIdeaStorageTransferContents fluidTarget(FluidStack template, boolean bulk) {
        return new MessageIdeaStorageTransferContents(TARGET_FLUID, template.copyWithAmount(1), null, bulk);
    }

    public static MessageIdeaStorageTransferContents chemicalTarget(ResourceLocation chemicalId, boolean bulk) {
        return new MessageIdeaStorageTransferContents(TARGET_CHEMICAL, FluidStack.EMPTY, chemicalId, bulk);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(targetKind);
        if (targetKind == TARGET_FLUID) {
            FluidStack.STREAM_CODEC.encode(buf, fluidTemplate);
        } else if (targetKind == TARGET_CHEMICAL && chemicalId != null) {
            buf.writeResourceLocation(chemicalId);
        }
        buf.writeBoolean(bulk);
    }

    private static MessageIdeaStorageTransferContents read(RegistryFriendlyByteBuf buf) {
        int targetKind = buf.readVarInt();
        FluidStack fluidTemplate = targetKind == TARGET_FLUID
                ? FluidStack.STREAM_CODEC.decode(buf)
                : FluidStack.EMPTY;
        ResourceLocation chemicalId = targetKind == TARGET_CHEMICAL
                ? buf.readResourceLocation()
                : null;
        boolean bulk = buf.readBoolean();
        return new MessageIdeaStorageTransferContents(targetKind, fluidTemplate, chemicalId, bulk);
    }

    public static void handle(MessageIdeaStorageTransferContents message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer
                    && serverPlayer.containerMenu instanceof IdeaStorageMenu menu
                    && menu.ownerUuid().equals(serverPlayer.getUUID())) {
                menu.handleTransferContents(serverPlayer, message.targetKind,
                        message.fluidTemplate, message.chemicalId, message.bulk);
            }
        });
    }
}
