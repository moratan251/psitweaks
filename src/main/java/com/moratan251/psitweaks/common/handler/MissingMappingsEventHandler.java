package com.moratan251.psitweaks.common.handler;

import com.mojang.logging.LogUtils;
import com.moratan251.psitweaks.Psitweaks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;
import org.slf4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Psitweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MissingMappingsEventHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Map<ResourceLocation, ResourceLocation> ITEM_REMAPS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, ResourceLocation> BLOCK_REMAPS = new LinkedHashMap<>();

    static {
        // Example:
        // addItemRemap("old_item_name", "new_item_name");
        // addBlockRemap("old_block_name", "new_block_name");
    }

    private static void addItemRemap(String oldIdOrPath, String newIdOrPath) {
        ITEM_REMAPS.put(parsePsitweaksId(oldIdOrPath), parsePsitweaksId(newIdOrPath));
    }

    private static void addBlockRemap(String oldIdOrPath, String newIdOrPath) {
        BLOCK_REMAPS.put(parsePsitweaksId(oldIdOrPath), parsePsitweaksId(newIdOrPath));
    }

    private static ResourceLocation parsePsitweaksId(String idOrPath) {
        ResourceLocation id = idOrPath.contains(":")
                ? ResourceLocation.tryParse(idOrPath)
                : Psitweaks.location(idOrPath);
        if (id == null) {
            throw new IllegalArgumentException("Invalid id: " + idOrPath);
        }
        return id;
    }

    @SubscribeEvent
    public static void onMissingMappings(MissingMappingsEvent event) {
        remapItems(event);
        remapBlocks(event);
    }

    private static void remapItems(MissingMappingsEvent event) {
        for (MissingMappingsEvent.Mapping<Item> mapping : event.getMappings(ForgeRegistries.Keys.ITEMS, Psitweaks.MOD_ID)) {
            ResourceLocation targetId = ITEM_REMAPS.get(mapping.getKey());
            if (targetId == null) {
                continue;
            }

            Item target = ForgeRegistries.ITEMS.getValue(targetId);
            if (target == null || target == Items.AIR) {
                LOGGER.warn("Missing item remap target not found: {} -> {}", mapping.getKey(), targetId);
                continue;
            }

            mapping.remap(target);
            LOGGER.info("Remapped missing item {} -> {}", mapping.getKey(), targetId);
        }
    }

    private static void remapBlocks(MissingMappingsEvent event) {
        for (MissingMappingsEvent.Mapping<Block> mapping : event.getMappings(ForgeRegistries.Keys.BLOCKS, Psitweaks.MOD_ID)) {
            ResourceLocation targetId = BLOCK_REMAPS.get(mapping.getKey());
            if (targetId == null) {
                continue;
            }

            Block target = ForgeRegistries.BLOCKS.getValue(targetId);
            if (target == null || target == Blocks.AIR) {
                LOGGER.warn("Missing block remap target not found: {} -> {}", mapping.getKey(), targetId);
                continue;
            }

            mapping.remap(target);
            LOGGER.info("Remapped missing block {} -> {}", mapping.getKey(), targetId);
        }
    }
}
