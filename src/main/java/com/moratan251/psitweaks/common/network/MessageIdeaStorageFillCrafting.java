package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.common.menu.IdeaStorageMenu;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** JEI/EMIで選択した9スロット分のクラフト材料配置要求。実移動はサーバー側で再検証する。 */
public record MessageIdeaStorageFillCrafting(List<ItemStack> templates) {
    public static final int CRAFT_SLOT_COUNT = 9;

    public MessageIdeaStorageFillCrafting {
        templates = List.copyOf(templates);
    }

    public void write(FriendlyByteBuf buf) {
        if (templates.size() != CRAFT_SLOT_COUNT) {
            throw new IllegalArgumentException("Ideaspace crafting transfer requires exactly 9 templates");
        }
        for (ItemStack template : templates) {
            IdeaStorageNetwork.writeItem(buf, template.isEmpty() ? ItemStack.EMPTY : template.copyWithCount(1));
        }
    }

    public static MessageIdeaStorageFillCrafting read(FriendlyByteBuf buf) {
        List<ItemStack> templates = new ArrayList<>(CRAFT_SLOT_COUNT);
        for (int i = 0; i < CRAFT_SLOT_COUNT; i++) {
            ItemStack template = IdeaStorageNetwork.readItem(buf);
            templates.add(template.isEmpty() ? ItemStack.EMPTY : template.copyWithCount(1));
        }
        return new MessageIdeaStorageFillCrafting(templates);
    }

    public static void handle(MessageIdeaStorageFillCrafting message, java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> supplier) {
        var context = supplier.get();
        context.setPacketHandled(true);
        context.enqueueWork(() -> {
            Player player = context.getSender();
            if (player instanceof ServerPlayer serverPlayer
                    && serverPlayer.containerMenu instanceof IdeaStorageMenu menu
                    && menu.stillValid(serverPlayer)) {
                menu.handleFillCrafting(serverPlayer, message.templates());
            }
        });
    }
}
