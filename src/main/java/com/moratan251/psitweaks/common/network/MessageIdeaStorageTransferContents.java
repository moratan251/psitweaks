package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.common.menu.IdeaStorageMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

/** カーソル上の容器とFluid/Chemicalストレージ間の右クリック転送要求。 */
public record MessageIdeaStorageTransferContents(int targetKind, FluidStack fluidTemplate,
                                                 @Nullable ResourceLocation chemicalId, boolean bulk) {
    public static final int TARGET_NONE = 0;
    public static final int TARGET_FLUID = 1;
    public static final int TARGET_CHEMICAL = 2;

    public MessageIdeaStorageTransferContents {
        if (targetKind == TARGET_FLUID && !fluidTemplate.isEmpty()) {
            fluidTemplate = new FluidStack(fluidTemplate, 1);
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
        return new MessageIdeaStorageTransferContents(TARGET_FLUID, new FluidStack(template, 1), null, bulk);
    }

    public static MessageIdeaStorageTransferContents chemicalTarget(ResourceLocation chemicalId, boolean bulk) {
        return new MessageIdeaStorageTransferContents(TARGET_CHEMICAL, FluidStack.EMPTY, chemicalId, bulk);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(targetKind);
        if (targetKind == TARGET_FLUID) {
            fluidTemplate.writeToPacket(buf);
        } else if (targetKind == TARGET_CHEMICAL && chemicalId != null) {
            buf.writeResourceLocation(chemicalId);
        }
        buf.writeBoolean(bulk);
    }

    public static MessageIdeaStorageTransferContents read(FriendlyByteBuf buf) {
        int targetKind = buf.readVarInt();
        FluidStack fluidTemplate = targetKind == TARGET_FLUID
                ? FluidStack.readFromPacket(buf)
                : FluidStack.EMPTY;
        ResourceLocation chemicalId = targetKind == TARGET_CHEMICAL
                ? buf.readResourceLocation()
                : null;
        boolean bulk = buf.readBoolean();
        return new MessageIdeaStorageTransferContents(targetKind, fluidTemplate, chemicalId, bulk);
    }

    public static void handle(MessageIdeaStorageTransferContents message, java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> supplier) {
        var context = supplier.get();
        context.setPacketHandled(true);
        context.enqueueWork(() -> {
            ServerPlayer serverPlayer = context.getSender();
            if (serverPlayer != null
                    && serverPlayer.containerMenu instanceof IdeaStorageMenu menu
                    && menu.ownerUuid().equals(serverPlayer.getUUID())) {
                menu.handleTransferContents(serverPlayer, message.targetKind,
                        message.fluidTemplate, message.chemicalId, message.bulk);
            }
        });
    }
}
