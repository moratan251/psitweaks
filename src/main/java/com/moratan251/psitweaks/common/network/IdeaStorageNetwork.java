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
        NetworkHandler.CHANNEL.registerMessage(id++, MessageIdeaStorageSyncPart.class, MessageIdeaStorageSyncPart::write, MessageIdeaStorageSyncPart::read, MessageIdeaStorageSyncPart::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        NetworkHandler.CHANNEL.registerMessage(id++, MessageIdeaStorageTransferContents.class, MessageIdeaStorageTransferContents::write, MessageIdeaStorageTransferContents::read, MessageIdeaStorageTransferContents::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        NetworkHandler.CHANNEL.registerMessage(id++, MessageConnectorAction.class, MessageConnectorAction::write, MessageConnectorAction::read, MessageConnectorAction::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        NetworkHandler.CHANNEL.registerMessage(id++, MessageConnectorExportSettings.class, MessageConnectorExportSettings::write, MessageConnectorExportSettings::read, MessageConnectorExportSettings::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        NetworkHandler.CHANNEL.registerMessage(id++, MessageMenuFragment.class, MessageMenuFragment::write, MessageMenuFragment::read, MessageMenuFragment::handle);
        return id;
    }
    public static void sendToServer(Object message) {
        if (message instanceof MessageConnectorTemplate m) MessageMenuFragment.send(m.containerId(), m.session(), MessageMenuFragment.CONNECTOR_TEMPLATE, m::write, NetworkHandler.CHANNEL::sendToServer);
        else if (message instanceof MessagePortableSpellProgrammerEdit m) MessageMenuFragment.send(m.containerId(), m.session(), MessageMenuFragment.SPELL_EDIT, m::write, NetworkHandler.CHANNEL::sendToServer);
        else NetworkHandler.CHANNEL.sendToServer(message);
    }
    public static void sendToPlayer(ServerPlayer player, Object message) {
        if (message instanceof MessageConnectorState m) MessageMenuFragment.send(m.containerId(), m.session(), MessageMenuFragment.CONNECTOR_STATE, m::write,
                part -> NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), part));
        else NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
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

    /** Only for authenticated server-to-client warehouse records, after bounded reassembly. */
    public static net.minecraft.world.item.ItemStack readStorageItem(net.minecraft.network.FriendlyByteBuf buf) {
        var tag = buf.readNbt(new net.minecraft.nbt.NbtAccounter(
                com.moratan251.psitweaks.common.storage.idea.IdeaStorageNbtLimits.MAX_ITEM_NBT_BYTES));
        return tag == null ? net.minecraft.world.item.ItemStack.EMPTY : net.minecraft.world.item.ItemStack.of(tag);
    }
}
