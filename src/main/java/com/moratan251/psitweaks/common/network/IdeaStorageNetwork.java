package com.moratan251.psitweaks.common.network;
import com.moratan251.psitweaks.common.handler.NetworkHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import java.util.Optional;
public final class IdeaStorageNetwork {
    private IdeaStorageNetwork() {}
    public static int register(int id) {
        NetworkHandler.CHANNEL.registerMessage(id++, MessageIdeaStorageClearCrafting.class, MessageIdeaStorageClearCrafting::write, MessageIdeaStorageClearCrafting::read, MessageIdeaStorageClearCrafting::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        NetworkHandler.CHANNEL.registerMessage(id++, MessageIdeaStorageCraftToggle.class, MessageIdeaStorageCraftToggle::write, MessageIdeaStorageCraftToggle::read, MessageIdeaStorageCraftToggle::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        NetworkHandler.CHANNEL.registerMessage(id++, MessageIdeaStorageDeposit.class, MessageIdeaStorageDeposit::write, MessageIdeaStorageDeposit::read, MessageIdeaStorageDeposit::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        NetworkHandler.CHANNEL.registerMessage(id++, MessageIdeaStorageExtract.class, MessageIdeaStorageExtract::write, MessageIdeaStorageExtract::read, MessageIdeaStorageExtract::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        NetworkHandler.CHANNEL.registerMessage(id++, MessageIdeaStorageFillBucket.class, MessageIdeaStorageFillBucket::write, MessageIdeaStorageFillBucket::read, MessageIdeaStorageFillBucket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        NetworkHandler.CHANNEL.registerMessage(id++, MessageIdeaStorageFillCrafting.class, MessageIdeaStorageFillCrafting::write, MessageIdeaStorageFillCrafting::read, MessageIdeaStorageFillCrafting::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        NetworkHandler.CHANNEL.registerMessage(id++, MessageIdeaStorageResize.class, MessageIdeaStorageResize::write, MessageIdeaStorageResize::read, MessageIdeaStorageResize::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        NetworkHandler.CHANNEL.registerMessage(id++, MessageIdeaStorageSync.class, MessageIdeaStorageSync::write, MessageIdeaStorageSync::read, MessageIdeaStorageSync::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        NetworkHandler.CHANNEL.registerMessage(id++, MessageIdeaStorageTransferContents.class, MessageIdeaStorageTransferContents::write, MessageIdeaStorageTransferContents::read, MessageIdeaStorageTransferContents::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        return id;
    }
    public static void sendToServer(Object message) { NetworkHandler.CHANNEL.sendToServer(message); }
    public static void sendToPlayer(ServerPlayer player, Object message) {
        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    /** Resource identity requires full NBT including ForgeCaps, not the vanilla share tag. */
    public static void writeItem(net.minecraft.network.FriendlyByteBuf buf, net.minecraft.world.item.ItemStack stack) {
        buf.writeNbt(stack.isEmpty() ? new net.minecraft.nbt.CompoundTag()
                : stack.copyWithCount(1).save(new net.minecraft.nbt.CompoundTag()));
    }

    public static net.minecraft.world.item.ItemStack readItem(net.minecraft.network.FriendlyByteBuf buf) {
        var tag = buf.readNbt();
        return tag == null ? net.minecraft.world.item.ItemStack.EMPTY : net.minecraft.world.item.ItemStack.of(tag);
    }
}
