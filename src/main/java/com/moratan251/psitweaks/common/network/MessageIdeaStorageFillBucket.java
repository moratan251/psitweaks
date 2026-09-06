package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.common.menu.IdeaStorageMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fluids.FluidStack;

/** 保存済み空バケツ1個と対象Fluid 1 Bを、カーソル上の満たされたバケツへ変換する要求。 */
public record MessageIdeaStorageFillBucket(FluidStack fluidTemplate) {

    public MessageIdeaStorageFillBucket {
        fluidTemplate = fluidTemplate.isEmpty() ? FluidStack.EMPTY : new FluidStack(fluidTemplate, 1);
    }

    public void write(FriendlyByteBuf buf) {
        fluidTemplate.writeToPacket(buf);
    }

    public static MessageIdeaStorageFillBucket read(FriendlyByteBuf buf) {
        return new MessageIdeaStorageFillBucket(FluidStack.readFromPacket(buf));
    }

    public static void handle(MessageIdeaStorageFillBucket message, java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> supplier) {
        var context = supplier.get();
        context.setPacketHandled(true);
        context.enqueueWork(() -> {
            ServerPlayer serverPlayer = context.getSender();
            if (serverPlayer != null
                    && serverPlayer.containerMenu instanceof IdeaStorageMenu menu
                    && menu.ownerUuid().equals(serverPlayer.getUUID())
                    && menu.stillValid(serverPlayer)) {
                menu.handleFillStoredBucket(message.fluidTemplate());
            }
        });
    }
}
