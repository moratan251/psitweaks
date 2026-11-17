package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.api.value.BlockValue;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorListNbtRange;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorListNbtRangeBase;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorListNbtRangeOutside;
import com.moratan251.psitweaks.common.spells.wrapper.BlockListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.SpellItemListWrapper;
import com.moratan251.psitweaksqol.api.PsitweaksModeOptions;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.wrapper.EntityListWrapper;

/**
 * Operator: List Keep NBT In Range / Out Of Range compare the numbers at an NBT path with Min and Max: the comparison
 * table of the book, any-match on list paths, elements without numbers, Min above Max, and the modes.
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class NbtRangeGameTests {
    private static final String TEMPLATE = "connector_empty";
    private static final String LIST = SpellParam.GENERIC_NAME_LIST;
    private static final String PATH = PsitweaksSpellParams.NBT_PATH;
    private static final String MIN = SpellParam.GENERIC_NAME_MIN;
    private static final String MAX = SpellParam.GENERIC_NAME_MAX;

    private NbtRangeGameTests() {
    }

    private static PieceOperatorListNbtRangeBase range(boolean inside, Map<String, Object> inputs) {
        if (inside) {
            return new PieceOperatorListNbtRange(new Spell()) {
                @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                    return inputs.get(param.name);
                }
            };
        }
        return new PieceOperatorListNbtRangeOutside(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return inputs.get(param.name);
            }
        };
    }

    private static SpellContext context(GameTestHelper helper) {
        var context = new SpellContext();
        context.focalPoint = new ArmorStand(helper.getLevel(), 0, 0, 0);
        return context;
    }

    private static Set<Entity> entities(Object result) {
        Set<Entity> set = new HashSet<>();
        for (Entity entity : (EntityListWrapper) result) {
            set.add(entity);
        }
        return set;
    }

    private static Set<Item> items(Object result) {
        return ((SpellItemListWrapper) result).asList().stream().map(value -> value.copyStack().getItem())
                .collect(Collectors.toSet());
    }

    private static ArmorStand stand(GameTestHelper helper, float health) {
        var stand = new ArmorStand(helper.getLevel(), 0, 0, 0);
        stand.setHealth(health);
        return stand;
    }

    /** Sets Min and Max (null removes the bound) and returns the two pieces' results as a pair of sets. */
    private static List<Set<Entity>> compare(SpellContext context, Map<String, Object> inputs, PieceOperatorListNbtRangeBase in,
                                             PieceOperatorListNbtRangeBase out, Double min, Double max) throws SpellRuntimeException {
        if (min == null) {
            inputs.remove(MIN);
        } else {
            inputs.put(MIN, min);
        }
        if (max == null) {
            inputs.remove(MAX);
        } else {
            inputs.put(MAX, max);
        }
        return List.of(entities(in.execute(context)), entities(out.execute(context)));
    }

    @GameTest(template = TEMPLATE)
    public static void entityHealthFollowsTheComparisonTable(GameTestHelper helper) throws SpellRuntimeException {
        var context = context(helper);
        var low = stand(helper, 5.0F);
        var mid = stand(helper, 10.0F);
        var high = stand(helper, 15.0F);
        Map<String, Object> inputs = new HashMap<>();
        inputs.put(LIST, EntityListWrapper.make(List.<Entity>of(low, mid, high)));
        inputs.put(PATH, "Health");
        var in = range(true, inputs);
        var out = range(false, inputs);
        helper.assertTrue(in.getModeOption().id().equals(PsitweaksModeOptions.ENTITY.id())
                        && in.getAvailableModeOptions().stream().map(option -> option.id()).collect(Collectors.toList())
                        .equals(List.of(PsitweaksModeOptions.ENTITY.id(), PsitweaksModeOptions.ITEM.id(), PsitweaksModeOptions.BLOCK.id())),
                "The range filters must offer Entity, Item and Block, starting in Entity");

        helper.assertTrue(compare(context, inputs, in, out, 10.0, null).get(0).equals(Set.of(mid, high)), "At least 10 = In Range, Min 10");
        helper.assertTrue(compare(context, inputs, in, out, null, 10.0).get(0).equals(Set.of(low, mid)), "At most 10 = In Range, Max 10");
        helper.assertTrue(compare(context, inputs, in, out, null, 10.0).get(1).equals(Set.of(high)), "Above 10 = Out Of Range, Max 10");
        helper.assertTrue(compare(context, inputs, in, out, 10.0, null).get(1).equals(Set.of(low)), "Below 10 = Out Of Range, Min 10");
        var equal = compare(context, inputs, in, out, 10.0, 10.0);
        helper.assertTrue(equal.get(0).equals(Set.of(mid)) && equal.get(1).equals(Set.of(low, high)),
                "Min and Max 10 must give = and the not-equal");
        var open = compare(context, inputs, in, out, null, null);
        helper.assertTrue(open.get(0).equals(Set.of(low, mid, high)) && open.get(1).isEmpty(),
                "Without bounds everything is in range and nothing out of it");
        var empty = compare(context, inputs, in, out, 12.0, 8.0);
        helper.assertTrue(empty.get(0).isEmpty() && empty.get(1).equals(Set.of(low, mid, high)),
                "Min above Max must be an empty range");

        inputs.put(PATH, "Missing");
        var missing = compare(context, inputs, in, out, 0.0, 1.0);
        helper.assertTrue(missing.get(0).isEmpty() && missing.get(1).isEmpty(), "Elements without a number must be dropped by both");
        helper.succeed();
    }

    private static SpellItemValue item(GameTestHelper helper, Item item, int count, Integer damage, String customData) {
        ItemStack stack = new ItemStack(item, count);
        if (damage != null) {
            stack.set(DataComponents.DAMAGE, damage);
        }
        if (customData != null) {
            try {
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(TagParser.parseTag(customData)));
            } catch (Exception e) {
                helper.fail("Bad SNBT in the test: " + customData);
            }
        }
        return SpellItemValue.snapshot(stack);
    }

    @GameTest(template = TEMPLATE)
    public static void itemsAndListPathsUseAnyNumber(GameTestHelper helper) throws SpellRuntimeException {
        var context = context(helper);
        Map<String, Object> inputs = new HashMap<>();
        var in = range(true, inputs);
        var out = range(false, inputs);
        in.setModeOption(PsitweaksModeOptions.ITEM);
        out.setModeOption(PsitweaksModeOptions.ITEM);

        inputs.put(LIST, SpellItemListWrapper.make(List.of(item(helper, Items.IRON_SWORD, 1, 5, null),
                item(helper, Items.DIAMOND_SWORD, 1, 20, null), item(helper, Items.STONE, 3, null, null))));
        inputs.put(PATH, "components.minecraft:damage");
        inputs.put(MIN, 10.0);
        helper.assertTrue(items(in.execute(context)).equals(Set.of(Items.DIAMOND_SWORD)), "Damage at least 10 must keep the worn sword");
        helper.assertTrue(items(out.execute(context)).equals(Set.of(Items.IRON_SWORD)),
                "Damage below 10 must keep the other sword and drop the stone without damage");
        inputs.put(PATH, "count");
        inputs.put(MIN, 2.0);
        helper.assertTrue(items(in.execute(context)).equals(Set.of(Items.STONE)), "Count at least 2 must keep the stone stack");

        inputs.put(LIST, SpellItemListWrapper.make(List.of(item(helper, Items.APPLE, 1, null, "{vals:[1,9]}"),
                item(helper, Items.BREAD, 1, null, "{vals:[4]}"), item(helper, Items.CARROT, 1, null, "{vals:[\"9\"]}"))));
        inputs.put(PATH, "components.minecraft:custom_data.vals[]");
        inputs.put(MIN, 8.0);
        inputs.remove(MAX);
        helper.assertTrue(items(in.execute(context)).equals(Set.of(Items.APPLE)), "One number in range must be enough; \"9\" is no number");
        inputs.put(MIN, 2.0);
        inputs.put(MAX, 5.0);
        helper.assertTrue(items(out.execute(context)).equals(Set.of(Items.APPLE)), "One number out of range must be enough");
        helper.assertTrue(items(in.execute(context)).equals(Set.of(Items.BREAD)), "Only the bread has a number from 2 to 5");

        var level = helper.getLevel();
        BlockPos a = new BlockPos(1, 1, 1);
        BlockPos b = new BlockPos(3, 1, 1);
        helper.setBlock(a, Blocks.CHEST);
        helper.setBlock(b, Blocks.CHEST);
        ((ChestBlockEntity) helper.getBlockEntity(a)).setItem(0, new ItemStack(Items.STONE, 5));
        ((ChestBlockEntity) helper.getBlockEntity(a)).setItem(1, new ItemStack(Items.STONE, 30));
        ((ChestBlockEntity) helper.getBlockEntity(b)).setItem(0, new ItemStack(Items.STONE, 7));
        in.setModeOption(PsitweaksModeOptions.BLOCK);
        out.setModeOption(PsitweaksModeOptions.BLOCK);
        inputs.put(LIST, BlockListWrapper.make(List.of(BlockValue.snapshot(level, helper.absolutePos(a)),
                BlockValue.snapshot(level, helper.absolutePos(b)))));
        inputs.put(PATH, "Items[].count");
        inputs.put(MIN, 20.0);
        inputs.remove(MAX);
        helper.assertTrue(((BlockListWrapper) in.execute(context)).asList().stream().map(BlockValue::blockPos)
                        .collect(Collectors.toList()).equals(List.of(helper.absolutePos(a))),
                "A chest with a stack of at least 20 must stay");
        inputs.put(MIN, 6.0);
        inputs.put(MAX, 10.0);
        helper.assertTrue(((BlockListWrapper) out.execute(context)).asList().stream().map(BlockValue::blockPos)
                        .collect(Collectors.toList()).equals(List.of(helper.absolutePos(a))),
                "Only the chest with a stack outside 6 to 10 must stay");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void rangeFiltersAreRegisteredAndKeepTheirMode(GameTestHelper helper) {
        for (String id : List.of("operator_list_nbt_range", "operator_list_nbt_range_outside")) {
            var type = PsiAPI.SPELL_PIECE_REGISTRY.get(Psitweaks.location(id));
            helper.assertTrue(type != null, "Not registered: " + id);
            var piece = (PieceOperatorListNbtRangeBase) type.create(new Spell());
            piece.setModeOption(PsitweaksModeOptions.ITEM);
            piece.paramSides.put(piece.params.get(MAX), SpellParam.Side.BOTTOM);
            var tag = new CompoundTag();
            piece.writeToNBT(tag);
            var reloaded = (PieceOperatorListNbtRangeBase) SpellPiece.createFromNBT(new Spell(), tag);
            helper.assertTrue(reloaded.getModeOption().id().equals(PsitweaksModeOptions.ITEM.id())
                            && reloaded.paramSides.get(reloaded.params.get(MAX)) == SpellParam.Side.BOTTOM,
                    "Item mode or the Max wire lost on reload: " + id);
        }
        helper.succeed();
    }
}
