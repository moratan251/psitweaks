package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.handler.MaterialMutationRecipeHandler;
import io.netty.handler.codec.DecoderException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** サーバーの作動式: 物質変換の表（変換前のブロック → できるアイテム）をクライアントへ送る（JEI・EMI に出すため）。 */
public record MessageMaterialMutationSync(List<Entry> entries) implements CustomPacketPayload {
    public static final Type<MessageMaterialMutationSync> TYPE = new Type<>(Psitweaks.location("material_mutation_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageMaterialMutationSync> STREAM_CODEC =
            CustomPacketPayload.codec(MessageMaterialMutationSync::write, MessageMaterialMutationSync::read);
    /** 1 回に送る項目の上限。 */
    public static final int MAX_ENTRIES = 16384;

    private static final StreamCodec<RegistryFriendlyByteBuf, Block> BLOCK_CODEC = ByteBufCodecs.registry(Registries.BLOCK);

    public record Entry(Block input, ItemStack output) {
    }

    public MessageMaterialMutationSync {
        entries = List.copyOf(entries);
    }

    public static MessageMaterialMutationSync of(Map<Block, ItemStack> recipes) {
        List<Entry> entries = new ArrayList<>(recipes.size());
        for (Map.Entry<Block, ItemStack> recipe : recipes.entrySet()) {
            if (entries.size() >= MAX_ENTRIES) {
                break;
            }
            if (!recipe.getValue().isEmpty()) {
                entries.add(new Entry(recipe.getKey(), recipe.getValue()));
            }
        }
        return new MessageMaterialMutationSync(entries);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(entries.size());
        for (Entry entry : entries) {
            BLOCK_CODEC.encode(buf, entry.input());
            ItemStack.STREAM_CODEC.encode(buf, entry.output());
        }
    }

    private static MessageMaterialMutationSync read(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        if (size < 0 || size > MAX_ENTRIES) {
            throw new DecoderException("Too many material mutation entries: " + size);
        }
        List<Entry> entries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Block input = BLOCK_CODEC.decode(buf);
            ItemStack output = ItemStack.STREAM_CODEC.decode(buf);
            entries.add(new Entry(input, output));
        }
        return new MessageMaterialMutationSync(entries);
    }

    public static void handle(MessageMaterialMutationSync message, IPayloadContext context) {
        context.enqueueWork(() -> {
            Map<Block, ItemStack> recipes = new LinkedHashMap<>();
            for (Entry entry : message.entries()) {
                recipes.put(entry.input(), entry.output());
            }
            MaterialMutationRecipeHandler.applyClientRecipes(recipes);
        });
    }
}
