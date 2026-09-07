package com.moratan251.psitweaks.common.network;
import com.moratan251.psitweaks.common.menu.IdeaStorageMenu;
import com.moratan251.psitweaks.common.storage.idea.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
/** Session-scoped request; resource identity is resolved on the server. */
public record MessageIdeaStorageTransferContents(IdeaStorageMenuToken token, long entryId, boolean bulk) {
    public static final int TARGET_NONE = 0;
    public static final int TARGET_FLUID = 1;
    public static final int TARGET_CHEMICAL = 2;
    public void write(FriendlyByteBuf buf) {
        token.write(buf);
        buf.writeLong(entryId); buf.writeBoolean(bulk);
    }
    public static MessageIdeaStorageTransferContents read(FriendlyByteBuf buf) {
        return new MessageIdeaStorageTransferContents(IdeaStorageMenuToken.read(buf), buf.readLong(), buf.readBoolean());
    }
    public static void handle(MessageIdeaStorageTransferContents message, java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> supplier) {
        var context = supplier.get();
        context.setPacketHandled(true);
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            IdeaStorageMenu menu = message.token().resolve(player);
            if (menu == null) return;
            Object target = menu.resolveEntry(message.entryId());
            if (message.entryId() == 0) menu.handleTransferContents(player, TARGET_NONE, FluidStack.EMPTY, null, message.bulk());
            else if (target instanceof FluidResourceKey fluid) menu.handleTransferContents(player, TARGET_FLUID, fluid.template(), null, message.bulk());
            else if (target instanceof ResourceLocation chemical) menu.handleTransferContents(player, TARGET_CHEMICAL, FluidStack.EMPTY, chemical, message.bulk());
            else if (target instanceof ItemResourceKey) menu.handleTransferContents(player, TARGET_NONE, FluidStack.EMPTY, null, message.bulk());
        });
    }
}
