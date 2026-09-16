package com.moratan251.psitweaks.client.gui;
import net.minecraft.client.Minecraft;
import com.moratan251.psitweaks.common.menu.IdeaspaceConnectorMenu;
import com.moratan251.psitweaks.common.network.MessageConnectorState;
public final class ConnectorClientHandler {
    private ConnectorClientHandler() { }
    public static void accept(MessageConnectorState message) {
        var player = Minecraft.getInstance().player;
        if (player != null && player.containerMenu instanceof IdeaspaceConnectorMenu menu
                && menu.containerId == message.containerId()) menu.applyState(message);
    }
    public static void accept(com.moratan251.psitweaks.common.network.MessageMenuFragment part) {
        var player = Minecraft.getInstance().player;
        if (player == null || player.containerMenu.containerId != part.containerId()) return;
        com.moratan251.psitweaks.common.network.MenuPayloadBuffer receiver;
        if (part.kind() == 0 && player.containerMenu instanceof IdeaspaceConnectorMenu menu) receiver = menu.payloadBuffer();
        else if (part.kind() == 3 && player.containerMenu instanceof com.moratan251.psitweaks.common.menu.PortableSpellProgrammerMenu menu
                && menu.session().equals(part.session())) receiver = menu.payloadBuffer();
        else return;
        byte[] bytes = receiver.accept(part);
        if (bytes == null) return;
        var buf = new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.wrappedBuffer(bytes));
        try {
            if (part.kind() == 0) {
                var message = MessageConnectorState.read(buf);
                if (!buf.isReadable() && message.session().equals(part.session())) accept(message);
            } else {
                var message = com.moratan251.psitweaks.common.network.MessagePortableSpellProgrammerEdit.read(buf);
                if (!buf.isReadable()) ((com.moratan251.psitweaks.common.menu.PortableSpellProgrammerMenu) player.containerMenu).receiveInitial(message);
            }
        } finally { buf.release(); }
    }
}
