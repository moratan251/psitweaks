package com.moratan251.psitweaks.common.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.network.MessageMaterialMutationSync;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@EventBusSubscriber(modid = Psitweaks.MOD_ID)
public final class MaterialMutationRecipeHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().create();
    private static final String TRICK_RECIPE_DIRECTORY = "material_mutation/trick";
    private static final TrickRecipeReloadListener TRICK_RELOAD_LISTENER = new TrickRecipeReloadListener();

    /**
     * サーバーのデータパックから読む表。クライアントの資源の再読み込み（F3+T、資源パック・言語の切り替え）には登録しない。
     * 同じ表を使っていたため、シングルプレイでは資源の再読み込みでサーバー側の表まで空になっていた（資源側にファイルはない）。
     */
    private static volatile Map<Block, ItemStack> trickRecipesByInputBlock = Map.of();
    /**
     * サーバーから送られた表（クライアント側。JEI・EMI が読む）。専用サーバーではクライアントにデータパックがないので、
     * 参加したときと /reload のときにサーバーが送る（{@link #onDatapackSync}）。切断したら空にする。
     */
    private static volatile Map<Block, ItemStack> clientRecipesByInputBlock = Map.of();

    private MaterialMutationRecipeHandler() {
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(TRICK_RELOAD_LISTENER);
    }

    public static ItemStack getMutationOutput(Block inputBlock) {
        ItemStack output = trickRecipesByInputBlock.get(inputBlock);
        return output == null ? ItemStack.EMPTY : output.copy();
    }

    public static Map<Block, ItemStack> getAllMutationOutputs() {
        return trickRecipesByInputBlock;
    }

    /**
     * 表をクライアントへ送る。レシピの送信（JEI・EMI の読み直しのきっかけ）より前に呼ばれるので、JEI・EMI は届いた表を読む。
     */
    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        MessageMaterialMutationSync message = MessageMaterialMutationSync.of(trickRecipesByInputBlock);
        event.getRelevantPlayers().forEach(player -> PacketDistributor.sendToPlayer(player, message));
    }

    /** サーバーから送られた表（クライアント側）。JEI・EMI 用。 */
    public static Map<Block, ItemStack> getClientMutationOutputs() {
        return clientRecipesByInputBlock;
    }

    /** サーバーから送られた表を、クライアント側の表にする。 */
    public static void applyClientRecipes(Map<Block, ItemStack> recipes) {
        clientRecipesByInputBlock = Map.copyOf(recipes);
    }

    /** 切断したとき、接続先のサーバーから送られた表を捨てる。 */
    public static void clearClientRecipes() {
        clientRecipesByInputBlock = Map.of();
    }

    /**
     * ブロックやアイテムが見つからず項目を飛ばすときのログ。ID の名前空間の mod が入っていないとき（Mekanism Generators の
     * 発電機など、任意の mod 向けの項目）は想定どおりなので DEBUG にし、入っているのに見つからないとき（書き間違いなど）だけ WARN にする。
     */
    static void logMissing(ResourceLocation missingId, String message, Object... args) {
        if (ModList.get().isLoaded(missingId.getNamespace())) {
            LOGGER.warn(message, args);
        } else {
            LOGGER.debug(message, args);
        }
    }


    private static final class TrickRecipeReloadListener extends SimpleJsonResourceReloadListener {
        private TrickRecipeReloadListener() {
            super(GSON, TRICK_RECIPE_DIRECTORY);
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> jsonEntries, ResourceManager resourceManager, ProfilerFiller profiler) {
            Map<Block, ItemStack> loadedRecipes = new LinkedHashMap<>();

            for (Map.Entry<ResourceLocation, JsonElement> entry : jsonEntries.entrySet()) {
                ResourceLocation fileId = entry.getKey();
                JsonElement value = entry.getValue();
                if (!value.isJsonObject()) {
                    LOGGER.warn("Skipping material mutation recipe file {} because it is not a JSON object", fileId);
                    continue;
                }

                JsonObject root = value.getAsJsonObject();
                if (!GsonHelper.isValidNode(root, "mutations")) {
                    LOGGER.warn("Skipping material mutation recipe file {} because 'mutations' array is missing", fileId);
                    continue;
                }

                for (JsonElement mutationElement : GsonHelper.getAsJsonArray(root, "mutations")) {
                    if (!mutationElement.isJsonObject()) {
                        LOGGER.warn("Skipping malformed mutation entry in {}: expected object", fileId);
                        continue;
                    }

                    JsonObject mutation = mutationElement.getAsJsonObject();
                    @Nullable Block inputBlock = parseInputBlock(mutation, fileId);
                    @Nullable ItemStack outputStack = parseOutputStack(mutation, fileId);
                    if (inputBlock != null && outputStack != null && !outputStack.isEmpty()) {
                        loadedRecipes.put(inputBlock, outputStack);
                    }
                }
            }

            trickRecipesByInputBlock = Map.copyOf(loadedRecipes);
            LOGGER.info("Loaded {} trick material mutation entries", trickRecipesByInputBlock.size());
        }
    }


    @Nullable
    private static Block parseInputBlock(JsonObject mutation, ResourceLocation fileId) {
        if (!GsonHelper.isValidNode(mutation, "input")) {
            LOGGER.warn("Skipping mutation entry in {} because 'input' is missing", fileId);
            return null;
        }

        String blockId = GsonHelper.getAsString(GsonHelper.getAsJsonObject(mutation, "input"), "block", "");
        ResourceLocation inputBlockId = ResourceLocation.tryParse(blockId);
        if (inputBlockId == null) {
            LOGGER.warn("Skipping mutation entry in {} because input block id is invalid: {}", fileId, blockId);
            return null;
        }

        Block inputBlock = BuiltInRegistries.BLOCK.get(inputBlockId);
        if (inputBlock == Blocks.AIR && !ResourceLocation.withDefaultNamespace("air").equals(inputBlockId)) {
            logMissing(inputBlockId, "Skipping mutation entry in {} because input block '{}' was not found", fileId, inputBlockId);
            return null;
        }
        return inputBlock;
    }


    @Nullable
    static ItemStack parseOutputStack(JsonObject root, ResourceLocation fileId) {
        if (!GsonHelper.isValidNode(root, "output")) {
            LOGGER.warn("Skipping mutation entry in {} because 'output' is missing", fileId);
            return null;
        }

        JsonObject output = GsonHelper.getAsJsonObject(root, "output");
        int count = Math.max(1, GsonHelper.getAsInt(output, "count", 1));
        if (GsonHelper.isValidNode(output, "item")) {
            return parseOutputItem(GsonHelper.getAsString(output, "item", ""), count, fileId);
        }
        if (GsonHelper.isValidNode(output, "block")) {
            ResourceLocation outputBlockId = ResourceLocation.tryParse(GsonHelper.getAsString(output, "block", ""));
            if (outputBlockId == null) {
                LOGGER.warn("Skipping mutation entry in {} because output block id is invalid", fileId);
                return null;
            }
            Block outputBlock = BuiltInRegistries.BLOCK.get(outputBlockId);
            Item outputItem = outputBlock.asItem();
            if (outputBlock == Blocks.AIR || outputItem == Items.AIR) {
                logMissing(outputBlockId, "Skipping mutation entry in {} because output block '{}' was not found or has no item",
                        fileId, outputBlockId);
                return null;
            }
            return new ItemStack(outputItem, count);
        }

        LOGGER.warn("Skipping mutation entry in {} because output requires either 'item' or 'block'", fileId);
        return null;
    }

    @Nullable
    private static ItemStack parseOutputItem(String itemId, int count, ResourceLocation fileId) {
        ResourceLocation outputItemId = ResourceLocation.tryParse(itemId);
        if (outputItemId == null) {
            LOGGER.warn("Skipping mutation entry in {} because output item id is invalid: {}", fileId, itemId);
            return null;
        }
        Item outputItem = BuiltInRegistries.ITEM.get(outputItemId);
        if (outputItem == Items.AIR && !ResourceLocation.withDefaultNamespace("air").equals(outputItemId)) {
            logMissing(outputItemId, "Skipping mutation entry in {} because output item '{}' was not found", fileId, outputItemId);
            return null;
        }
        return new ItemStack(outputItem, count);
    }
}
