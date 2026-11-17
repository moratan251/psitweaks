package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.api.value.BlockValue;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorListNbtFilterBase;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorListNbtSearch;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorListNbtSearchExclude;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorNbt;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorNbtExists;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorNbtKeys;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorNbtValue;
import com.moratan251.psitweaks.common.spells.util.NbtPathHelper;
import com.moratan251.psitweaks.common.spells.wrapper.SpellItemListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.StringListWrapper;
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
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.wrapper.EntityListWrapper;

/**
 * The NBT pieces: lenient paths that never throw, Selector: NBT Value's String and Number modes with the Fallback,
 * Selector: NBT / NBT Keys on compounds and lists, Selector: NBT Exists, and the NBT list filters.
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class NbtPiecesGameTests {
    private static final String TEMPLATE = "connector_empty";
    private static final String TARGET = SpellParam.GENERIC_NAME_TARGET;
    private static final String LIST = SpellParam.GENERIC_NAME_LIST;
    private static final String PATH = PsitweaksSpellParams.NBT_PATH;
    private static final String FALLBACK = PsitweaksSpellParams.FALLBACK;
    private static final String PATTERN = PsitweaksSpellParams.STRING;
    private static final String DATA = "components.minecraft:custom_data";

    private NbtPiecesGameTests() {
    }

    private static CompoundTag snbt(GameTestHelper helper, String text) {
        try {
            return TagParser.parseTag(text);
        } catch (Exception e) {
            helper.fail("Bad SNBT in the test: " + text);
            return new CompoundTag();
        }
    }

    private static SpellItemValue item(GameTestHelper helper, Item item, String customData) {
        ItemStack stack = new ItemStack(item);
        if (customData != null) {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(snbt(helper, customData)));
        }
        return SpellItemValue.snapshot(stack);
    }

    /** A context whose focal point lets Item values save their NBT. */
    private static SpellContext context(GameTestHelper helper) {
        var context = new SpellContext();
        context.focalPoint = new ArmorStand(helper.getLevel(), 0, 0, 0);
        return context;
    }

    private static List<String> texts(List<Tag> tags) {
        return tags.stream().map(NbtPathHelper::text).collect(Collectors.toList());
    }

    private static List<String> strings(Object result) {
        return ((StringListWrapper) result).asList();
    }

    @GameTest(template = TEMPLATE)
    public static void pathsAreLenientAndNeverThrow(GameTestHelper helper) {
        CompoundTag root = snbt(helper, "{Inventory:[{id:\"minecraft:stone\",Slot:0b},{id:\"minecraft:dirt\",Slot:1b}],"
                + "Items:[{Slot:0b,id:\"minecraft:apple\",count:5}],"
                + "\"a.b\":7,outer:{\"inner key\":1,\"it's\":2},components:{\"minecraft:custom_data\":{foo:\"bar\"}},Health:20.0f}");
        helper.assertTrue(NbtPathHelper.find(root, "").equals(List.of(root)) && NbtPathHelper.find(root, null).equals(List.of(root)),
                "An empty path must be the root");
        helper.assertTrue(texts(NbtPathHelper.find(root, "a.b")).equals(List.of("7")), "An exact top-level key must win");
        helper.assertTrue(texts(NbtPathHelper.find(root, "outer.inner key")).equals(List.of("1")), "Keys with spaces must be quoted");
        helper.assertTrue(texts(NbtPathHelper.find(root, "outer.it's")).equals(List.of("2")), "Keys with a quote must be quoted");
        helper.assertTrue(texts(NbtPathHelper.find(root, DATA + ".foo")).equals(List.of("bar")), "A key with a colon needs no quotes");
        helper.assertTrue(texts(NbtPathHelper.find(root, "components.\"minecraft:custom_data\".foo")).equals(List.of("bar")),
                "Quoted keys must keep working");
        helper.assertTrue(texts(NbtPathHelper.find(root, "Inventory[0].id")).equals(List.of("minecraft:stone"))
                        && texts(NbtPathHelper.find(root, "Inventory[-1].id")).equals(List.of("minecraft:dirt"))
                        && texts(NbtPathHelper.find(root, "Inventory[].id")).equals(List.of("minecraft:stone", "minecraft:dirt"))
                        && texts(NbtPathHelper.find(root, "Inventory[{Slot:1b}].id")).equals(List.of("minecraft:dirt")),
                "[n], [-1], [] and [{...}] must work");
        helper.assertTrue(texts(NbtPathHelper.find(root, "Inventory[1.0].id")).equals(texts(NbtPathHelper.find(root, "Inventory[1].id")))
                        && texts(NbtPathHelper.find(root, "Inventory[-1.0].id")).equals(List.of("minecraft:dirt"))
                        && texts(NbtPathHelper.find(root, "Items[{Slot:0.0b}].count")).equals(List.of("5"))
                        && texts(NbtPathHelper.find(root, "Inventory[{id:\"minecraft:stone\"}].Slot")).equals(List.of("0")),
                "Whole numbers printed with a fraction must work as indexes and integer tags");
        helper.assertTrue(NbtPathHelper.find(root, "Inventory[1.5].id").isEmpty()
                        && NbtPathHelper.find(root, "Items[{Slot:0.5b}].count").isEmpty(),
                "Real fractions must not become indexes");
        for (String bad : List.of("Missing", "Inventory[5]", "Inventory[", "Health.x", "outer.\"inner", "Inventory[0].id trailing")) {
            helper.assertTrue(NbtPathHelper.find(root, bad).isEmpty(), "Must find nothing for " + bad);
        }
        helper.assertTrue(NbtPathHelper.find(null, "Health").isEmpty(), "No NBT must find nothing");

        helper.assertTrue(NbtPathHelper.text(StringTag.valueOf("bar")).equals("bar")
                        && NbtPathHelper.text(ByteTag.valueOf((byte) 3)).equals("3")
                        && NbtPathHelper.text(IntTag.valueOf(-4)).equals("-4")
                        && NbtPathHelper.text(LongTag.valueOf(9007199254740993L)).equals("9007199254740993")
                        && NbtPathHelper.text(FloatTag.valueOf(1.5F)).equals("1.5")
                        && NbtPathHelper.text(DoubleTag.valueOf(2.0)).equals("2.0")
                        && NbtPathHelper.text(snbt(helper, "{a:1b}")).equals("{a:1b}"),
                "Text must drop quotes and number suffixes and keep SNBT for compounds");
        helper.assertTrue(NbtPathHelper.number(ByteTag.valueOf((byte) 3)).getAsDouble() == 3.0
                        && NbtPathHelper.number(StringTag.valueOf("5")).isEmpty()
                        && NbtPathHelper.number(new ListTag()).isEmpty(),
                "Only number tags have a number");
        helper.succeed();
    }

    private static PieceSelectorNbtValue value(Map<String, Object> inputs) {
        return new PieceSelectorNbtValue(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return inputs.get(param.name);
            }
        };
    }

    @GameTest(template = TEMPLATE)
    public static void nbtValueHasStringAndNumberModes(GameTestHelper helper) throws SpellRuntimeException {
        var context = context(helper);
        Map<String, Object> inputs = new HashMap<>();
        inputs.put(TARGET, item(helper, Items.STONE, "{foo:\"bar\",level:3b,ratio:0.5d}"));
        var piece = value(inputs);
        helper.assertTrue(piece.getModeOption().id().equals(PsitweaksModeOptions.STRING.id())
                        && piece.getEvaluationType() == String.class, "A new NBT Value must start in String mode");

        inputs.put(PATH, DATA + ".foo");
        helper.assertTrue("bar".equals(piece.execute(context)), "A String must come out without quotes");
        inputs.put(PATH, DATA + ".level");
        helper.assertTrue("3".equals(piece.execute(context)), "1b-style numbers must lose their suffix");
        inputs.put(PATH, DATA + ".missing");
        helper.assertTrue("".equals(piece.execute(context)), "Not found without a Fallback must be empty");
        inputs.put(FALLBACK, "none");
        helper.assertTrue("none".equals(piece.execute(context)), "Not found must give the Fallback");
        inputs.put(PATH, "count");
        helper.assertTrue("1".equals(piece.execute(context)), "The item count must be readable");

        piece.setModeOption(PsitweaksModeOptions.NUMBER);
        helper.assertTrue(piece.getEvaluationType() == Double.class && piece.params.get(FALLBACK) instanceof ParamNumber,
                "Number mode must output a Number and take a Number Fallback");
        inputs.remove(FALLBACK);
        inputs.put(PATH, DATA + ".level");
        helper.assertTrue(Double.valueOf(3.0).equals(piece.execute(context)), "Number mode must read 3b as 3");
        inputs.put(PATH, DATA + ".ratio");
        helper.assertTrue(Double.valueOf(0.5).equals(piece.execute(context)), "Number mode must read 0.5d");
        inputs.put(PATH, DATA + ".foo");
        helper.assertTrue(Double.valueOf(0.0).equals(piece.execute(context)), "A String must be 0 without a Fallback");
        inputs.put(FALLBACK, 7.0);
        helper.assertTrue(Double.valueOf(7.0).equals(piece.execute(context)), "A String must give the Number Fallback");

        var registered = (PieceSelectorNbtValue) PsiAPI.SPELL_PIECE_REGISTRY.get(Psitweaks.location("selector_nbt_value"))
                .create(new Spell());
        registered.setModeOption(PsitweaksModeOptions.NUMBER);
        registered.paramSides.put(registered.params.get(PATH), SpellParam.Side.LEFT);
        registered.paramSides.put(registered.params.get(FALLBACK), SpellParam.Side.TOP);
        var tag = new CompoundTag();
        registered.writeToNBT(tag);
        var reloaded = (PieceSelectorNbtValue) SpellPiece.createFromNBT(new Spell(), tag);
        helper.assertTrue(reloaded.getModeOption().id().equals(PsitweaksModeOptions.NUMBER.id())
                        && reloaded.params.get(FALLBACK) instanceof ParamNumber
                        && reloaded.paramSides.get(reloaded.params.get(PATH)) == SpellParam.Side.LEFT
                        && reloaded.paramSides.get(reloaded.params.get(FALLBACK)) == SpellParam.Side.TOP,
                "Number mode or its wires lost on reload");

        // A piece saved before the path input was renamed keeps its wire.
        var old = new CompoundTag();
        PsiAPI.SPELL_PIECE_REGISTRY.get(Psitweaks.location("selector_nbt_value")).create(new Spell()).writeToNBT(old);
        CompoundTag params = old.getCompound("params");
        params.putInt(PsitweaksSpellParams.STRING, SpellParam.Side.RIGHT.asInt());
        params.remove(PATH);
        old.remove("psitweaksMode");
        var migrated = (PieceSelectorNbtValue) SpellPiece.createFromNBT(new Spell(), old);
        helper.assertTrue(migrated.getModeOption().id().equals(PsitweaksModeOptions.STRING.id())
                        && migrated.paramSides.get(migrated.params.get(PATH)) == SpellParam.Side.RIGHT,
                "An old NBT Value must load in String mode with its path wire");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void nbtListsKeysAndExistsFollowThePath(GameTestHelper helper) throws SpellRuntimeException {
        var context = context(helper);
        Map<String, Object> inputs = new HashMap<>();
        inputs.put(TARGET, item(helper, Items.STONE, "{b:2b,a:\"x\",list:[\"p\",\"q\",\"r\"]}"));
        var nbt = new PieceSelectorNbt(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return inputs.get(param.name);
            }
        };
        var keys = new PieceSelectorNbtKeys(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return inputs.get(param.name);
            }
        };
        var exists = new PieceSelectorNbtExists(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return inputs.get(param.name);
            }
        };

        helper.assertTrue(strings(keys.execute(context)).equals(List.of("components", "count", "id")),
                "Without a path NBT Keys must give the top-level keys: " + strings(keys.execute(context)));
        inputs.put(PATH, DATA);
        helper.assertTrue(strings(nbt.execute(context)).equals(List.of("a:\"x\"", "b:2b", "list:[\"p\",\"q\",\"r\"]")),
                "NBT must list a compound by key: " + strings(nbt.execute(context)));
        helper.assertTrue(strings(keys.execute(context)).equals(List.of("a", "b", "list")), "NBT Keys must list a compound");
        inputs.put(PATH, DATA + ".list");
        helper.assertTrue(strings(nbt.execute(context)).equals(List.of("0:\"p\"", "1:\"q\"", "2:\"r\"")),
                "NBT must list a list by index: " + strings(nbt.execute(context)));
        helper.assertTrue(strings(keys.execute(context)).equals(List.of("0", "1", "2")), "NBT Keys must give list indexes");
        inputs.put(PATH, DATA + ".b");
        helper.assertTrue(strings(nbt.execute(context)).isEmpty() && strings(keys.execute(context)).isEmpty(),
                "A number must give empty Lists");

        helper.assertTrue(Double.valueOf(1.0).equals(exists.execute(context)), "An existing path must give 1");
        inputs.put(PATH, DATA + ".list[2]");
        helper.assertTrue(Double.valueOf(1.0).equals(exists.execute(context)), "An existing list element must give 1");
        inputs.put(PATH, DATA + ".list[3]");
        helper.assertTrue(Double.valueOf(0.0).equals(exists.execute(context)), "A missing list element must give 0");
        inputs.put(PATH, "");
        helper.assertTrue(Double.valueOf(1.0).equals(exists.execute(context)), "The root of an item must exist");
        inputs.put(TARGET, BlockValue.snapshot(helper.getLevel(), helper.absolutePos(new BlockPos(1, 1, 1))));
        helper.assertTrue(Double.valueOf(0.0).equals(exists.execute(context)), "A block without a block entity has no NBT");
        helper.succeed();
    }

    private static PieceOperatorListNbtFilterBase filter(boolean keep, Map<String, Object> inputs) {
        if (keep) {
            return new PieceOperatorListNbtSearch(new Spell()) {
                @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                    return inputs.get(param.name);
                }
            };
        }
        return new PieceOperatorListNbtSearchExclude(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return inputs.get(param.name);
            }
        };
    }

    private static Set<Item> items(Object result) {
        return ((SpellItemListWrapper) result).asList().stream().map(value -> value.copyStack().getItem())
                .collect(Collectors.toSet());
    }

    private static Set<Entity> entities(Object result) {
        Set<Entity> set = new HashSet<>();
        for (Entity entity : (EntityListWrapper) result) {
            set.add(entity);
        }
        return set;
    }

    @GameTest(template = TEMPLATE)
    public static void nbtFiltersMatchValuesAtThePath(GameTestHelper helper) throws SpellRuntimeException {
        var context = context(helper);
        Map<String, Object> inputs = new HashMap<>();
        var keep = filter(true, inputs);
        var remove = filter(false, inputs);
        helper.assertTrue(keep.getModeOption().id().equals(PsitweaksModeOptions.ENTITY.id())
                        && keep.getAvailableModeOptions().stream().map(option -> option.id()).collect(Collectors.toList())
                        .equals(List.of(PsitweaksModeOptions.ENTITY.id(), PsitweaksModeOptions.ITEM.id(), PsitweaksModeOptions.BLOCK.id())),
                "NBT filters must offer Entity, Item and Block, starting in Entity");

        keep.setModeOption(PsitweaksModeOptions.ITEM);
        remove.setModeOption(PsitweaksModeOptions.ITEM);
        inputs.put(LIST, SpellItemListWrapper.make(List.of(item(helper, Items.STONE, "{tier:\"gold\",tags:[\"a\",\"b\"]}"),
                item(helper, Items.DIRT, "{tier:\"iron\",tags:[\"c\"]}"), item(helper, Items.APPLE, null))));
        inputs.put(PATH, DATA + ".tier");
        inputs.put(PATTERN, "g*");
        helper.assertTrue(items(keep.execute(context)).equals(Set.of(Items.STONE)), "Keep must leave the gold tier");
        helper.assertTrue(items(remove.execute(context)).equals(Set.of(Items.DIRT, Items.APPLE)),
                "Remove must keep the items whose path reaches nothing");
        inputs.put(PATH, DATA + ".tags[]");
        inputs.put(PATTERN, "b");
        helper.assertTrue(items(keep.execute(context)).equals(Set.of(Items.STONE)), "One matching list value must be enough");
        inputs.put(PATTERN, "minecraft:*");
        inputs.put(PATH, "id");
        helper.assertTrue(items(keep.execute(context)).size() == 3, "Item ids must match as plain text");
        inputs.put(PATTERN, "stone");
        helper.assertTrue(items(keep.execute(context)).isEmpty(), "There must be no namespace rule");

        var boss = new ArmorStand(helper.getLevel(), 0, 0, 0);
        boss.addTag("boss");
        var plain = new ArmorStand(helper.getLevel(), 0, 0, 0);
        keep.setModeOption(PsitweaksModeOptions.ENTITY);
        inputs.put(LIST, EntityListWrapper.make(List.<Entity>of(boss, plain)));
        inputs.put(PATH, "Tags[]");
        inputs.put(PATTERN, "boss");
        helper.assertTrue(entities(keep.execute(context)).equals(Set.of(boss)), "Entity mode must read the entity's Tags");
        helper.assertTrue(entities(keep.execute(context)).equals(Set.of(boss)), "The cached NBT must give the same result");
        plain.addTag("boss");
        helper.assertTrue(entities(keep.execute(context(helper))).equals(Set.of(boss, plain)),
                "A new cast must read the NBT again");

        for (String id : List.of("selector_nbt_exists", "operator_list_nbt_search", "operator_list_nbt_search_exclude")) {
            helper.assertTrue(PsiAPI.SPELL_PIECE_REGISTRY.get(Psitweaks.location(id)) != null, "Not registered: " + id);
        }
        var registered = (PieceOperatorListNbtFilterBase) PsiAPI.SPELL_PIECE_REGISTRY
                .get(Psitweaks.location("operator_list_nbt_search_exclude")).create(new Spell());
        registered.setModeOption(PsitweaksModeOptions.ITEM);
        var saved = new CompoundTag();
        registered.writeToNBT(saved);
        var reloaded = (PieceOperatorListNbtFilterBase) SpellPiece.createFromNBT(new Spell(), saved);
        helper.assertTrue(reloaded.getModeOption().id().equals(PsitweaksModeOptions.ITEM.id()), "Item mode lost on reload");
        helper.succeed();
    }
}
