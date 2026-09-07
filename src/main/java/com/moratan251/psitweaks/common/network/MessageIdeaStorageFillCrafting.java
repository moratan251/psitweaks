package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.common.menu.IdeaStorageMenu;
import com.moratan251.psitweaks.common.storage.idea.ItemResourceKey;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

/** Positive references are storage IDs, negative references are -(menu slot + 1), zero is empty. */
public record MessageIdeaStorageFillCrafting(IdeaStorageMenuToken token, List<Reference> references) {
    public static final int CRAFT_SLOT_COUNT = 9;
    public record Reference(long source, int fingerprint) {}

    public MessageIdeaStorageFillCrafting {
        references = List.copyOf(references);
        if (references.size() != CRAFT_SLOT_COUNT) throw new IllegalArgumentException("Expected nine crafting references");
    }

    public static MessageIdeaStorageFillCrafting fromTemplates(IdeaStorageMenu menu, List<ItemStack> templates) {
        List<Reference> references = new ArrayList<>();
        for (ItemStack template : templates) {
            if (template.isEmpty()) {
                references.add(new Reference(0, 0));
                continue;
            }
            ItemResourceKey key = ItemResourceKey.of(template).orElseThrow();
            long source = 0;
            for (var entry : menu.clientStorageEntries()) {
                if (entry.entryId() > 0 && ItemResourceKey.of(entry.template()).filter(key::equals).isPresent()) {
                    source = entry.entryId();
                    break;
                }
            }
            if (source == 0) {
                for (int slot = 0; slot < IdeaStorageMenu.CRAFT_RESULT_SLOT; slot++) {
                    if (ItemResourceKey.of(menu.getSlot(slot).getItem()).filter(key::equals).isPresent()) {
                        source = -1L - slot;
                        break;
                    }
                }
            }
            if (source == 0) throw new IllegalArgumentException("Crafting source no longer available");
            references.add(new Reference(source, key.hashCode()));
        }
        return new MessageIdeaStorageFillCrafting(menu.token(), references);
    }

    public void write(FriendlyByteBuf buf) {
        token.write(buf);
        for (Reference reference : references) {
            buf.writeLong(reference.source());
            buf.writeInt(reference.fingerprint());
        }
    }

    public static MessageIdeaStorageFillCrafting read(FriendlyByteBuf buf) {
        IdeaStorageMenuToken token = IdeaStorageMenuToken.read(buf);
        List<Reference> references = new ArrayList<>(CRAFT_SLOT_COUNT);
        for (int i = 0; i < CRAFT_SLOT_COUNT; i++) references.add(new Reference(buf.readLong(), buf.readInt()));
        return new MessageIdeaStorageFillCrafting(token, references);
    }

    public List<ItemStack> resolve(IdeaStorageMenu menu) {
        if (!token.equals(menu.token())) return List.of();
        List<ItemStack> templates = new ArrayList<>(CRAFT_SLOT_COUNT);
        for (Reference reference : references) {
            if (reference.source() == 0) {
                templates.add(ItemStack.EMPTY);
                continue;
            }
            ItemResourceKey key = null;
            if (reference.source() > 0 && menu.resolveEntry(reference.source()) instanceof ItemResourceKey item) {
                key = item;
            } else if (reference.source() >= -IdeaStorageMenu.CRAFT_RESULT_SLOT && reference.source() < 0) {
                key = ItemResourceKey.of(menu.getSlot((int) (-reference.source() - 1)).getItem()).orElse(null);
            }
            if (key == null || key.hashCode() != reference.fingerprint()) return List.of();
            templates.add(key.template());
        }
        return templates;
    }

    public static void handle(MessageIdeaStorageFillCrafting message, Supplier<NetworkEvent.Context> supplier) {
        var context = supplier.get();
        context.setPacketHandled(true);
        context.enqueueWork(() -> {
            var player = context.getSender();
            IdeaStorageMenu menu = message.token().resolve(player);
            if (menu != null) menu.handleFillCrafting(player, message.resolve(menu));
        });
    }
}
