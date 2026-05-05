package com.moratan251.psitweaks.common.registries;

import com.google.common.collect.ImmutableSet;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.blocks.PsitweaksBlocks;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.BasicItemListing;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import vazkii.psi.common.block.base.ModBlocks;
import vazkii.psi.common.item.base.ModItems;

@EventBusSubscriber(modid = Psitweaks.MOD_ID)
public final class PsitweaksVillagers {
    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, Psitweaks.MOD_ID);
    public static final DeferredRegister<VillagerProfession> PROFESSIONS =
            DeferredRegister.create(Registries.VILLAGER_PROFESSION, Psitweaks.MOD_ID);

    public static final String SPELLCASTER_ID = "spellcaster";
    public static final ResourceKey<PoiType> CAD_ASSEMBLER_POI_KEY =
            ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, Psitweaks.location("cad_assembler"));
    public static final ResourceKey<VillagerProfession> SPELLCASTER_KEY =
            ResourceKey.create(Registries.VILLAGER_PROFESSION, Psitweaks.location(SPELLCASTER_ID));

    public static final DeferredHolder<PoiType, PoiType> CAD_ASSEMBLER_POI =
            POI_TYPES.register(CAD_ASSEMBLER_POI_KEY.location().getPath(), () -> new PoiType(
                    ImmutableSet.copyOf(ModBlocks.cadAssembler.get().getStateDefinition().getPossibleStates()),
                    1,
                    1
            ));
    public static final DeferredHolder<VillagerProfession, VillagerProfession> SPELLCASTER =
            PROFESSIONS.register(SPELLCASTER_ID, () -> new VillagerProfession(
                    SPELLCASTER_ID,
                    holder -> holder.is(CAD_ASSEMBLER_POI_KEY),
                    holder -> holder.is(CAD_ASSEMBLER_POI_KEY),
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    SoundEvents.BEACON_ACTIVATE
            ));

    private PsitweaksVillagers() {
    }

    public static void register(IEventBus eventBus) {
        POI_TYPES.register(eventBus);
        PROFESSIONS.register(eventBus);
    }

    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() != SPELLCASTER.get()) {
            return;
        }

        Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();

        addTrade(trades, 1, new BasicItemListing(new ItemStack(ModItems.psidust.get(), 8), new ItemStack(Items.EMERALD, 1), 16, 2, 0.05F));
        addTrade(trades, 1, new BasicItemListing(3, new ItemStack(ModItems.spellBullet.get(), 1), 8, 2, 0.05F));
        addTrade(trades, 1, new BasicItemListing(4, new ItemStack(ModItems.projectileSpellBullet.get(), 1), 8, 2, 0.05F));

        addTrade(trades, 2, new BasicItemListing(new ItemStack(ModItems.psimetal.get(), 3), new ItemStack(Items.EMERALD, 1), 12, 5, 0.05F));
        addTrade(trades, 2, new BasicItemListing(4, new ItemStack(Items.ENDER_PEARL, 1), 12, 5, 0.05F));
        addTrade(trades, 2, new BasicItemListing(14, new ItemStack(PsitweaksBlocks.CAD_DISASSEMBLER.get(), 1), 4, 12, 0.05F));

        addTrade(trades, 3, new BasicItemListing(new ItemStack(ModItems.psigem.get(), 1), new ItemStack(Items.EMERALD, 1), 12, 5, 0.05F));
        addTrade(trades, 3, new BasicItemListing(14, new ItemStack(PsitweaksItems.ADVANCED_SPELL_BULLET.get(), 1), 8, 12, 0.05F));
        addTrade(trades, 3, new BasicItemListing(6, new ItemStack(PsitweaksItems.PSIONIC_CONTROL_CIRCUIT.get(), 1), 12, 5, 0.05F));

        addTrade(trades, 4, new BasicItemListing(2, new ItemStack(Items.ECHO_SHARD, 1), 12, 3, 0.1F));
        addTrade(trades, 4, new BasicItemListing(15, new ItemStack(PsitweaksItems.ADVANCED_SPELL_BULLET_PROJECTILE.get(), 1), 8, 15, 0.1F));
        addTrade(trades, 4, new BasicItemListing(9, new ItemStack(PsitweaksItems.PSIMETAL_BOW.get(), 1), 4, 10, 0.1F));

        addTrade(trades, 5, new BasicItemListing(22, new ItemStack(ModItems.flashRing.get(), 1), 2, 24, 0.2F));
        addTrade(trades, 5, new BasicItemListing(29, new ItemStack(PsitweaksItems.AUTO_CASTER_TICK.get(), 1), 2, 30, 0.2F));
        addTrade(trades, 4, new BasicItemListing(1, new ItemStack(ModItems.ebonySubstance.get(), 3), 16, 2, 0.1F));
        addTrade(trades, 4, new BasicItemListing(1, new ItemStack(ModItems.ivorySubstance.get(), 3), 16, 2, 0.1F));
    }

    private static void addTrade(Int2ObjectMap<List<VillagerTrades.ItemListing>> trades, int level, VillagerTrades.ItemListing listing) {
        List<VillagerTrades.ItemListing> entries = trades.get(level);
        if (entries != null) {
            entries.add(listing);
        }
    }
}
