package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.menu.IdeaStorageMenu;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** JEI/EMIで選択した9スロット分のクラフト材料配置要求。実移動はサーバー側で再検証する。 */
public record MessageIdeaStorageFillCrafting(List<ItemStack> templates) implements CustomPacketPayload {
    public static final int CRAFT_SLOT_COUNT = 9;

    public static final Type<MessageIdeaStorageFillCrafting> TYPE =
            new Type<>(Psitweaks.location("idea_storage_fill_crafting"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageIdeaStorageFillCrafting> STREAM_CODEC =
            CustomPacketPayload.codec(MessageIdeaStorageFillCrafting::write, MessageIdeaStorageFillCrafting::read);

    public MessageIdeaStorageFillCrafting {
        templates = List.copyOf(templates);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private void write(RegistryFriendlyByteBuf buf) {
        if (templates.size() != CRAFT_SLOT_COUNT) {
            throw new IllegalArgumentException("Ideaspace crafting transfer requires exactly 9 templates");
        }
        for (ItemStack template : templates) {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, template.isEmpty() ? ItemStack.EMPTY : template.copyWithCount(1));
        }
    }

    private static MessageIdeaStorageFillCrafting read(RegistryFriendlyByteBuf buf) {
        List<ItemStack> templates = new ArrayList<>(CRAFT_SLOT_COUNT);
        for (int i = 0; i < CRAFT_SLOT_COUNT; i++) {
            ItemStack template = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
            templates.add(template.isEmpty() ? ItemStack.EMPTY : template.copyWithCount(1));
        }
        return new MessageIdeaStorageFillCrafting(templates);
    }

    public static void handle(MessageIdeaStorageFillCrafting message, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player instanceof ServerPlayer serverPlayer
                    && serverPlayer.containerMenu instanceof IdeaStorageMenu menu
                    && menu.stillValid(serverPlayer)) {
                menu.handleFillCrafting(serverPlayer, message.templates());
            }
        });
    }
}
