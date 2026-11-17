package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.api.value.BlockValue;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.spells.item.SpellItemFilters;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.mode.BlockResourceModes;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorBlockResourceAmount;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorListNameSearch;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorListSearch;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorListSearchExclude;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorListStringFilterBase;
import com.moratan251.psitweaks.common.spells.wrapper.BlockListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.SpellItemListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.StringListWrapper;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageService;
import com.moratan251.psitweaksqol.api.PsitweaksModeOptions;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.wrapper.EntityListWrapper;

/**
 * A pattern starting with # compares tags: in Operator: List Keep Matches / List Remove Matches (Entity, Item and Block
 * modes), in the item filters of the transfer and Ideaspace tricks, and in Operator: Block Resource Amount. The name
 * filters and the String mode keep # as an ordinary character.
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ListTagFilterGameTests {
    private static final String TEMPLATE = "connector_empty";
    private static final String LIST = SpellParam.GENERIC_NAME_LIST;

    private ListTagFilterGameTests() {
    }

    /** A Keep (or Remove) filter that reads its List from {@code list[0]} and its pattern from {@code pattern[0]}. */
    private static PieceOperatorListStringFilterBase filter(boolean keep, Object[] list, String[] pattern) {
        if (keep) {
            return new PieceOperatorListSearch(new Spell()) {
                @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                    return param.name.equals(LIST) ? list[0] : pattern[0];
                }
            };
        }
        return new PieceOperatorListSearchExclude(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return param.name.equals(LIST) ? list[0] : pattern[0];
            }
        };
    }

    private static SpellItemListWrapper itemList(Item... items) {
        return SpellItemListWrapper.make(List.of(items).stream()
                .map(item -> SpellItemValue.snapshot(new ItemStack(item))).collect(Collectors.toList()));
    }

    private static Set<Item> items(Object result) {
        return ((SpellItemListWrapper) result).asList().stream()
                .map(value -> value.copyStack().getItem()).collect(Collectors.toSet());
    }

    private static Set<Entity> entities(Object result) {
        Set<Entity> set = new HashSet<>();
        for (Entity entity : (EntityListWrapper) result) {
            set.add(entity);
        }
        return set;
    }

    /** An item without any tag, found at run time so the test does not depend on the tag files. */
    private static Item taglessItem(GameTestHelper helper) {
        var found = BuiltInRegistries.ITEM.holders()
                .filter(holder -> holder.value() != Items.AIR && holder.tags().findAny().isEmpty())
                .findFirst();
        helper.assertTrue(found.isPresent(), "No item without tags to test with");
        return found.get().value();
    }

    @GameTest(template = TEMPLATE)
    public static void itemListsMatchTagPatterns(GameTestHelper helper) throws SpellRuntimeException {
        var context = new SpellContext();
        Object[] list = {itemList(Items.OAK_LOG, Items.STONE, Items.IRON_ORE)};
        String[] pattern = {"#minecraft:logs"};
        var keep = filter(true, list, pattern);
        var remove = filter(false, list, pattern);
        keep.setModeOption(PsitweaksModeOptions.ITEM);
        remove.setModeOption(PsitweaksModeOptions.ITEM);

        helper.assertTrue(items(keep.execute(context)).equals(Set.of(Items.OAK_LOG)), "#minecraft:logs must keep the oak log only");
        helper.assertTrue(items(remove.execute(context)).equals(Set.of(Items.STONE, Items.IRON_ORE)),
                "#minecraft:logs must remove the oak log only");
        pattern[0] = "#logs";
        helper.assertTrue(items(keep.execute(context)).equals(Set.of(Items.OAK_LOG)), "#logs must match the path in any namespace");
        pattern[0] = "#*_logs";
        helper.assertTrue(items(keep.execute(context)).equals(Set.of(Items.OAK_LOG)), "#*_logs must match minecraft:oak_logs");
        pattern[0] = "#c:ores/*";
        helper.assertTrue(items(keep.execute(context)).equals(Set.of(Items.IRON_ORE)), "#c:ores/* must match c:ores/iron");
        pattern[0] = "minecraft:logs";
        helper.assertTrue(items(keep.execute(context)).isEmpty(), "Without # the pattern must compare item IDs");
        pattern[0] = "#";
        helper.assertTrue(items(keep.execute(context)).isEmpty() && items(remove.execute(context)).size() == 3,
                "# alone must match nothing");

        Item tagless = taglessItem(helper);
        list[0] = itemList(Items.OAK_LOG, tagless);
        pattern[0] = "#*";
        helper.assertTrue(items(keep.execute(context)).equals(Set.of(Items.OAK_LOG)), "#* must keep the items that have a tag");
        helper.assertTrue(items(remove.execute(context)).equals(Set.of(tagless)), "#* must leave only the items without tags");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void blockAndEntityListsMatchTheirOwnTags(GameTestHelper helper) throws SpellRuntimeException {
        var level = helper.getLevel();
        var context = new SpellContext();
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.OAK_LOG);
        helper.setBlock(new BlockPos(2, 1, 1), Blocks.STONE);
        BlockPos log = helper.absolutePos(new BlockPos(1, 1, 1));
        BlockPos stone = helper.absolutePos(new BlockPos(2, 1, 1));
        Object[] list = {BlockListWrapper.make(List.of(BlockValue.snapshot(level, log), BlockValue.snapshot(level, stone)))};
        String[] pattern = {"#minecraft:mineable/axe"};
        var keep = filter(true, list, pattern);
        var remove = filter(false, list, pattern);
        keep.setModeOption(PsitweaksModeOptions.BLOCK);
        remove.setModeOption(PsitweaksModeOptions.BLOCK);
        helper.assertTrue(((BlockListWrapper) keep.execute(context)).asList().stream().map(BlockValue::blockPos)
                        .collect(Collectors.toList()).equals(List.of(log)),
                "#minecraft:mineable/axe must keep the log block only");
        helper.assertTrue(((BlockListWrapper) remove.execute(context)).asList().stream().map(BlockValue::blockPos)
                        .collect(Collectors.toList()).equals(List.of(stone)),
                "#minecraft:mineable/axe must remove the log block only");

        var skeleton = EntityType.SKELETON.create(level);
        var zombie = EntityType.ZOMBIE.create(level);
        list[0] = EntityListWrapper.make(List.<Entity>of(skeleton, zombie));
        pattern[0] = "#minecraft:skeletons";
        keep.setModeOption(PsitweaksModeOptions.ENTITY);
        remove.setModeOption(PsitweaksModeOptions.ENTITY);
        helper.assertTrue(entities(keep.execute(context)).equals(Set.of(skeleton)), "#minecraft:skeletons must keep the skeleton");
        helper.assertTrue(entities(remove.execute(context)).equals(Set.of(zombie)), "#minecraft:skeletons must leave the zombie");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void hashStaysLiteralForStringsAndNames(GameTestHelper helper) throws SpellRuntimeException {
        var context = new SpellContext();
        Object[] list = {StringListWrapper.make(List.of("#x", "x"))};
        String[] pattern = {"#x"};
        var keep = filter(true, list, pattern);
        helper.assertTrue(keep.getModeOption().id().equals(PsitweaksModeOptions.STRING.id()), "A new filter must start in String mode");
        helper.assertTrue(((StringListWrapper) keep.execute(context)).asList().equals(List.of("#x")),
                "In String mode # must be an ordinary character");

        var bob = new ArmorStand(helper.getLevel(), 0, 0, 0);
        bob.setCustomName(Component.literal("#Bob"));
        var alice = new ArmorStand(helper.getLevel(), 0, 0, 0);
        alice.setCustomName(Component.literal("Alice"));
        Object[] names = {EntityListWrapper.make(List.<Entity>of(bob, alice))};
        var byName = new PieceOperatorListNameSearch(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return param.name.equals(LIST) ? names[0] : "#Bob";
            }
        };
        helper.assertTrue(entities(byName.execute(context)).equals(Set.of(bob)), "The name filter must match #Bob as a name");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void resourceFiltersMatchTagPatterns(GameTestHelper helper) throws Exception {
        var items = SpellItemFilters.search("#minecraft:logs");
        helper.assertTrue(items.test(new ItemStack(Items.OAK_LOG)) && !items.test(new ItemStack(Items.STONE)),
                "The item search filter must match #minecraft:logs by tag");
        var ingots = SpellItemFilters.search("#c:ingots");
        helper.assertTrue(ingots.test(new ItemStack(Items.IRON_INGOT)) && !ingots.test(new ItemStack(Items.IRON_ORE)),
                "The item search filter must match #c:ingots by tag");
        helper.assertTrue(SpellItemFilters.search("oak_log").test(new ItemStack(Items.OAK_LOG)),
                "An ID pattern must still match the item ID");

        var amount = new PieceOperatorBlockResourceAmount(new Spell()) {
            long items(IItemHandler handler, String pattern) {
                return readItems(handler, pattern, false);
            }

            long fluids(IFluidHandler handler, String pattern) {
                return readFluids(handler, pattern);
            }
        };
        var chest = new ItemStackHandler(3);
        chest.setStackInSlot(0, new ItemStack(Items.OAK_LOG, 3));
        chest.setStackInSlot(1, new ItemStack(Items.BIRCH_LOG, 2));
        chest.setStackInSlot(2, new ItemStack(Items.STONE, 5));
        helper.assertTrue(amount.items(chest, "#minecraft:logs") == 5 && amount.items(chest, "#*_logs") == 5
                        && amount.items(chest, "stone") == 5,
                "Block Resource Amount must count items by tag: " + amount.items(chest, "#minecraft:logs"));
        var tank = new FluidTank(4000);
        tank.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
        helper.assertTrue(amount.fluids(tank, "#minecraft:water") == 1000 && amount.fluids(tank, "#minecraft:lava") == 0,
                "Block Resource Amount must count fluids by tag");

        // Ideaspace Storage - Deposit in Fluid mode, from a connector holding water.
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "tag-filter"));
        var connector = IdeaLogisticsGameTests.place(helper, new BlockPos(1, 1, 1), UUID.randomUUID());
        connector.setResource(0, IdeaLogisticsGameTests.water());
        connector.storage().insertFluid(new FluidStack(Fluids.WATER, 1), 1000);
        player.setPos(Vec3.atCenterOf(connector.getBlockPos()));
        var context = new SpellContext().setPlayer(player);
        var storage = IdeaStorageService.get(player.server, player.getUUID());
        String[] fluidPattern = {"#minecraft:lava"};
        var deposit = IdeaLogisticsGameTests.trick(true, BlockResourceModes.FLUID,
                param -> IdeaLogisticsGameTests.paramValue(param, connector.getBlockPos(), 0.25, fluidPattern[0]));
        deposit.execute(context);
        helper.assertTrue(IdeaLogisticsGameTests.water().amount(storage) == 0, "#minecraft:lava must not take water");
        fluidPattern[0] = "#minecraft:water";
        deposit.execute(context);
        helper.assertTrue(IdeaLogisticsGameTests.water().amount(storage) == 250, "#minecraft:water must take water");

        if (MekanismCompat.isMekanismLoaded()) {
            ResourceTagChemicalChecks.run(helper);
        }
        helper.succeed();
    }
}
