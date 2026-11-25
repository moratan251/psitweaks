package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.api.value.BlockValue;
import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorListNbtFilterBase;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorListNbtSearch;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorListNbtSearchExclude;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorListSearch;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorStringPartialMatch;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorNbt;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorNbtExists;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorNbtKeys;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorNbtValue;
import com.moratan251.psitweaks.common.spells.util.NbtPathHelper;
import com.moratan251.psitweaks.common.spells.util.NbtTargetReader;
import com.moratan251.psitweaks.common.spells.util.SnbtText;
import com.moratan251.psitweaks.common.spells.util.WildcardBudget;
import com.moratan251.psitweaks.common.spells.wrapper.BlockListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.SpellItemListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.SpellListLimits;
import com.moratan251.psitweaks.common.spells.wrapper.StringListWrapper;
import com.moratan251.psitweaksqol.api.PsitweaksModeOptions;
import com.moratan251.psitweaksqol.api.StringSpellHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
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

    @GameTest(template = TEMPLATE)
    public static void deepPathsFindNothingWithoutOverflowing(GameTestHelper helper) throws SpellRuntimeException {
        CompoundTag root = snbt(helper, "{list:[{a:{b:1b}},{a:{b:2b}}]}");
        for (int depth : List.of(511, 513, 2050, 8189)) {
            String open = "{a:" + "[".repeat(depth);
            helper.assertTrue(NbtPathHelper.find(root, open).isEmpty(), "An unclosed path of depth " + depth + " must find nothing");
            helper.assertTrue(NbtPathHelper.find(root, open + "]".repeat(depth) + "}").isEmpty(),
                    "A closed path of depth " + depth + " must find nothing");
        }

        Map<String, Object> inputs = new HashMap<>();
        inputs.put(TARGET, item(helper, Items.STONE, "{foo:\"bar\"}"));
        inputs.put(PATH, "{a:" + "[".repeat(8189));
        helper.assertTrue("".equals(value(inputs).execute(context(helper))), "NBT Value must treat a deep path as not found");
        var keep = filter(true, inputs);
        keep.setModeOption(PsitweaksModeOptions.ITEM);
        inputs.put(LIST, SpellItemListWrapper.make(List.of(item(helper, Items.STONE, "{foo:\"bar\"}"))));
        inputs.put(PATTERN, "*");
        helper.assertTrue(items(keep.execute(context(helper))).isEmpty(), "A list filter must treat a deep path as not found");

        helper.assertTrue(texts(NbtPathHelper.find(root, "list[{a:{b:2b}}].a.b")).equals(List.of("2")),
                "A nested filter within the limit must still match");
        String bracketKey = "[".repeat(600);
        CompoundTag outer = new CompoundTag();
        outer.putInt(bracketKey, 5);
        root.put("outer", outer);
        helper.assertTrue(texts(NbtPathHelper.find(root, "outer.\"" + bracketKey + "\"")).equals(List.of("5")),
                "Brackets inside a quoted key must not count as nesting");

        var compiled = NbtPathHelper.compile("list[].a.b");
        helper.assertTrue(texts(compiled.find(root)).equals(List.of("1", "2")) && compiled.find(new CompoundTag()).isEmpty()
                        && compiled.find(null).isEmpty() && texts(compiled.find(root)).equals(List.of("1", "2")),
                "A compiled path must work on every root it is given");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void listsShareTheirElementsAndReadNbtOncePerCast(GameTestHelper helper) throws SpellRuntimeException {
        var level = helper.getLevel();
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.CHEST);
        helper.setBlock(new BlockPos(3, 1, 1), Blocks.CHEST);
        BlockPos first = helper.absolutePos(new BlockPos(1, 1, 1));
        BlockPos second = helper.absolutePos(new BlockPos(3, 1, 1));
        BlockListWrapper blocks = BlockListWrapper.make(List.of(BlockValue.snapshot(level, second),
                BlockValue.snapshot(level, first), BlockValue.snapshot(level, second)));
        helper.assertTrue(blocks.asList().stream().map(BlockValue::blockPos).collect(Collectors.toList()).equals(List.of(second, first)),
                "A Block List must drop duplicates and keep its order");
        helper.assertTrue(blocks.get(0) == blocks.get(0), "A Block List must hand out the same element each time");

        SpellItemListWrapper itemList = SpellItemListWrapper.make(List.of(item(helper, Items.STONE, "{a:1b}"),
                item(helper, Items.STONE, "{a:1b}"), item(helper, Items.STONE, "{a:2b}"),
                SpellItemValue.snapshot(new ItemStack(Items.STONE, 2)), SpellItemValue.EMPTY));
        helper.assertTrue(itemList.size() == 3, "An Item List must drop empty and equivalent items: " + itemList.size());
        helper.assertTrue(itemList.get(0) == itemList.get(0), "An Item List must hand out the same element each time");
        itemList.get(0).copyStack().setCount(5);
        helper.assertTrue(itemList.get(0).copyStack().getCount() == 1, "Changing a copy must not change the element");

        var chest = (ChestBlockEntity) level.getBlockEntity(first);
        chest.setItem(0, new ItemStack(Items.APPLE));
        Map<String, Object> inputs = new HashMap<>();
        inputs.put(LIST, blocks);
        inputs.put(PATH, "Items[].id");
        inputs.put(PATTERN, "minecraft:apple");
        var keep = filter(true, inputs);
        keep.setModeOption(PsitweaksModeOptions.BLOCK);
        var context = context(helper);
        helper.assertTrue(((BlockListWrapper) keep.execute(context)).size() == 1, "The chest with the apple must stay");
        chest.setItem(0, new ItemStack(Items.BREAD));
        helper.assertTrue(((BlockListWrapper) keep.execute(context)).size() == 1, "One cast must reuse the NBT it already read");
        helper.assertTrue(((BlockListWrapper) keep.execute(context(helper))).size() == 0, "A new cast must read the NBT again");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void oneCastReadsTheNbtOfALimitedNumberOfTargets(GameTestHelper helper) throws SpellRuntimeException {
        int limit = PsitweaksConfig.COMMON.nbtMaxTargetsPerCast.get();
        List<SpellItemValue> values = new ArrayList<>();
        for (int i = 0; i <= limit; i++) {
            values.add(item(helper, Items.STONE, "{i:" + i + "}"));
        }
        Map<String, Object> inputs = new HashMap<>();
        inputs.put(LIST, SpellItemListWrapper.make(values.subList(0, limit)));
        inputs.put(PATH, "id");
        inputs.put(PATTERN, "*");
        var keep = filter(true, inputs);
        keep.setModeOption(PsitweaksModeOptions.ITEM);
        var context = context(helper);
        helper.assertTrue(((SpellItemListWrapper) keep.execute(context)).size() == limit, "Reading up to the limit must work");
        helper.assertTrue(((SpellItemListWrapper) keep.execute(context)).size() == limit, "Reading the same targets again must not count");

        inputs.put(LIST, SpellItemListWrapper.make(values));
        try {
            keep.execute(context(helper));
            helper.fail("Reading past the limit must stop the spell");
        } catch (SpellRuntimeException e) {
            helper.assertTrue(NbtTargetReader.ERROR_TOO_MANY_TARGETS.equals(e.getMessage()), "Wrong error: " + e.getMessage());
        }

        List<BlockValue> blocksWithoutNbt = new ArrayList<>();
        for (int i = 0; i <= limit; i++) {
            blocksWithoutNbt.add(BlockValue.snapshot(helper.getLevel(), helper.absolutePos(new BlockPos(i % 16, 10 + i / 256, (i / 16) % 16))));
        }
        inputs.put(LIST, BlockListWrapper.make(blocksWithoutNbt));
        keep.setModeOption(PsitweaksModeOptions.BLOCK);
        helper.assertTrue(((BlockListWrapper) keep.execute(context(helper))).size() == 0, "Targets without NBT must not count");
        helper.succeed();
    }

    /** SNBT の書き出しは、どこで切っても、バニラの SNBT を術式の文字列と同じく整えて切ったものと同じになる。 */
    @GameTest(template = TEMPLATE)
    public static void snbtTextIsVanillaSnbtCutWherever(GameTestHelper helper) {
        Random random = new Random(20261125L);
        for (int round = 0; round < 300; round++) {
            Tag tag = randomTag(random, 0);
            String whole = tag.toString();
            for (int cut = 0; cut <= whole.length() + 2; cut++) {
                String written = SnbtText.write(tag, cut).text();
                String expected = StringSpellHelper.sanitize(whole, cut);
                if (!written.equals(expected)) {
                    helper.fail("Cut " + cut + " of " + whole + ": wrote " + written + ", expected " + expected);
                }
            }
        }
        helper.succeed();
    }

    /** 大きなまとまりも、残す文字数のぶんしか書かない。キーの多い複合タグも、並べ替えるのは書くキーのぶんだけ。 */
    @GameTest(template = TEMPLATE)
    public static void snbtTextStopsWritingAtTheCut(GameTestHelper helper) {
        int max = StringSpellHelper.MAX_STRING_LENGTH;
        ListTag books = new ListTag();
        for (int i = 0; i < 100_000; i++) {
            CompoundTag book = new CompoundTag();
            book.putString("id", "minecraft:written_book");
            book.putString("text", "page ".repeat(20));
            books.add(book);
        }
        CompoundTag root = new CompoundTag();
        root.put("Items", books);
        long start = System.nanoTime();
        SnbtText snbt = SnbtText.write(root, max);
        long elapsed = System.nanoTime() - start;
        helper.assertTrue(snbt.text().equals(root.toString().substring(0, max)), "The text must be vanilla's SNBT, cut");
        helper.assertTrue(snbt.work() < 3L * max, "Writing must stop at the cut, took " + snbt.work() + " steps");
        helper.assertTrue(elapsed < 100_000_000L, "Writing took " + elapsed / 1_000_000 + " ms");
        helper.assertTrue(NbtPathHelper.text(root).equals(snbt.text()), "Selector: NBT Value must use the same text");

        CompoundTag manyKeys = new CompoundTag();
        for (int i = 0; i < 100_000; i++) {
            manyKeys.putInt("k" + i, i);
        }
        SnbtText keys = SnbtText.write(manyKeys, 64);
        helper.assertTrue(keys.text().equals(StringSpellHelper.sanitize(manyKeys.toString(), 64)),
                "The keys must come in vanilla's order: " + keys.text());
        // A heap puts in order only the keys it writes; sorting all of them would take more steps than this.
        helper.assertTrue(keys.work() < 100_000L * 16, "Only the written keys should be sorted, took " + keys.work() + " steps");
        helper.succeed();
    }

    /**
     * NBT一致抽出は、まとまりを文字列にする手間も詠唱の予算から引く。パターンが 1 文字で照らし合わせが 0 回でも、大きな
     * まとまりを並べれば予算が尽きる。文字列と数は手間に数えない。
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void nbtMatchesCountTheWritingOfSnbt(GameTestHelper helper) throws SpellRuntimeException {
        ListTag books = new ListTag();
        for (int i = 0; i < 2000; i++) {
            CompoundTag book = new CompoundTag();
            book.putString("id", "minecraft:written_book");
            book.putInt("n", i);
            books.add(book);
        }
        SpellItemValue first = chestOfBooks(0, books);
        Map<String, Object> inputs = new HashMap<>();
        inputs.put(LIST, SpellItemListWrapper.make(List.of(first)));
        inputs.put(PATTERN, "a");
        var keep = filter(true, inputs);
        keep.setModeOption(PsitweaksModeOptions.ITEM);
        int max = StringSpellHelper.MAX_STRING_LENGTH;
        long budget = PsitweaksConfig.COMMON.wildcardMaxComparisonsPerCast.get();
        var context = context(helper);
        CompoundTag firstNbt = NbtTargetReader.read(context, first).orElseThrow();

        inputs.put(PATH, DATA + ".s");
        keep.execute(context);
        long usedByString = budget - WildcardBudget.forCast(context).remaining();
        helper.assertTrue(usedByString == 0, "A String value has no SNBT to write, used " + usedByString);

        inputs.put(PATH, DATA + ".list");
        long perList = SnbtText.write(NbtPathHelper.find(firstNbt, DATA + ".list").get(0), max).work() * NbtPathHelper.SNBT_STEP_COST;
        keep.execute(context);
        long usedByList = budget - WildcardBudget.forCast(context).remaining();
        helper.assertTrue(perList > 0 && usedByList == perList, "Writing the list must cost its steps, used " + usedByList + " for " + perList);
        helper.assertTrue(perList < 3L * max * NbtPathHelper.SNBT_STEP_COST, "A long list must be written only up to the cut, cost " + perList);

        inputs.put(PATH, DATA + ".list[]");
        long perChest = 0;
        for (Tag book : NbtPathHelper.find(firstNbt, DATA + ".list[]")) {
            perChest += SnbtText.write(book, max).work() * NbtPathHelper.SNBT_STEP_COST;
        }
        long chests = WildcardBudget.forCast(context).remaining() / perChest + 1;
        helper.assertTrue(chests < PsitweaksConfig.COMMON.nbtMaxTargetsPerCast.get(), "The test needs " + chests + " chests");
        List<SpellItemValue> many = new ArrayList<>();
        for (int i = 0; i < chests; i++) {
            SpellItemValue chest = chestOfBooks(i, books);
            NbtTargetReader.read(context, chest);
            many.add(chest);
        }
        inputs.put(LIST, SpellItemListWrapper.make(many));
        long start = System.nanoTime();
        try {
            keep.execute(context);
            helper.fail("Writing SNBT must stop at the budget");
        } catch (SpellRuntimeException e) {
            helper.assertTrue(WildcardBudget.ERROR_TOO_MANY_COMPARISONS.equals(e.getMessage()), "Wrong error: " + e.getMessage());
        }
        long elapsed = System.nanoTime() - start;
        helper.assertTrue(elapsed < 1_000_000_000L, "Stopping at the budget took " + elapsed / 1_000_000 + " ms");
        helper.succeed();
    }

    /** A chest whose custom data is {i:index, s:"short", list:books}. */
    private static SpellItemValue chestOfBooks(int index, ListTag books) {
        CompoundTag data = new CompoundTag();
        data.putInt("i", index);
        data.putString("s", "short");
        data.put("list", books);
        ItemStack stack = new ItemStack(Items.CHEST);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        return SpellItemValue.snapshot(stack);
    }

    private static Tag randomTag(Random random, int depth) {
        return switch (random.nextInt(depth >= 4 ? 10 : 13)) {
            case 0 -> ByteTag.valueOf((byte) random.nextInt());
            case 1 -> ShortTag.valueOf((short) random.nextInt());
            case 2 -> IntTag.valueOf(random.nextInt());
            case 3 -> LongTag.valueOf(random.nextLong());
            case 4 -> FloatTag.valueOf(switch (random.nextInt(4)) {
                case 0 -> Float.NaN;
                case 1 -> -0.0F;
                default -> (random.nextFloat() - 0.5F) * (float) Math.pow(10, random.nextInt(40) - 20);
            });
            case 5 -> DoubleTag.valueOf(switch (random.nextInt(4)) {
                case 0 -> Double.POSITIVE_INFINITY;
                case 1 -> 1.0E-300;
                default -> (random.nextDouble() - 0.5) * Math.pow(10, random.nextInt(60) - 30);
            });
            case 6, 7 -> StringTag.valueOf(randomText(random));
            case 8 -> {
                byte[] values = new byte[random.nextInt(4)];
                random.nextBytes(values);
                yield new ByteArrayTag(values);
            }
            case 9 -> random.nextBoolean()
                    ? new IntArrayTag(random.ints(random.nextInt(4)).toArray())
                    : new LongArrayTag(random.longs(random.nextInt(4)).toArray());
            case 10, 11 -> {
                CompoundTag compound = new CompoundTag();
                int size = random.nextInt(6);
                for (int i = 0; i < size; i++) {
                    compound.put(randomText(random), randomTag(random, depth + 1));
                }
                yield compound;
            }
            default -> {
                ListTag list = new ListTag();
                Tag first = randomTag(random, depth + 1);
                list.add(first);
                int size = random.nextInt(5);
                for (int i = 1; i < size; i++) {
                    Tag next = randomTag(random, depth + 1);
                    if (next.getId() == first.getId()) {
                        list.add(next);
                    }
                }
                yield list;
            }
        };
    }

    /** Short text full of the characters SNBT quotes or escapes and the spell strings drop or change. */
    private static String randomText(Random random) {
        String tricky = "aZ09._+-\"'\\\r\n\t\u0000\u0007\u007f\u0085\u00e9\u6f22\uD83D\uDE00 :,{}[]";
        StringBuilder out = new StringBuilder();
        int length = random.nextInt(8);
        for (int i = 0; i < length; i++) {
            out.append(random.nextInt(3) == 0 ? (char) ('a' + random.nextInt(26)) : tricky.charAt(random.nextInt(tricky.length())));
        }
        return out.toString();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void patternMatchingOfOneCastStopsAtTheBudget(GameTestHelper helper) throws SpellRuntimeException {
        long budget = PsitweaksConfig.COMMON.wildcardMaxComparisonsPerCast.get();
        int count = Math.min(PsitweaksConfig.COMMON.nbtMaxTargetsPerCast.get(), 1024);
        int length = 8000;
        String longText = "a".repeat(length);
        List<SpellItemValue> values = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            values.add(item(helper, Items.STONE, "{i:" + i + ",s:\"" + longText + "\"}"));
        }
        Map<String, Object> inputs = new HashMap<>();
        inputs.put(LIST, SpellItemListWrapper.make(values));
        inputs.put(PATH, DATA + ".s");
        inputs.put(PATTERN, "*b*");
        var keep = filter(true, inputs);
        keep.setModeOption(PsitweaksModeOptions.ITEM);

        var context = context(helper);
        helper.assertTrue(((SpellItemListWrapper) keep.execute(context)).size() == 0, "A short pattern over long values should fit the budget");
        long used = budget - WildcardBudget.forCast(context).remaining();
        helper.assertTrue(used == (long) count * length, "Each start of the search should count one test, used " + used);

        long runsThatFit = budget / used;
        for (long run = 1; run < runsThatFit; run++) {
            keep.execute(context);
        }
        try {
            keep.execute(context);
            helper.fail("The pieces of one cast must share the budget");
        } catch (SpellRuntimeException e) {
            helper.assertTrue(WildcardBudget.ERROR_TOO_MANY_COMPARISONS.equals(e.getMessage()), "Wrong error: " + e.getMessage());
        }

        Map<String, Object> texts = new HashMap<>();
        texts.put(PsitweaksSpellParams.STRING1, "abc");
        texts.put(PsitweaksSpellParams.STRING2, "b");
        texts.put(LIST, StringListWrapper.make(List.of("abc")));
        texts.put(PATTERN, "a*");
        var partial = new PieceOperatorStringPartialMatch(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return texts.get(param.name);
            }
        };
        var search = new PieceOperatorListSearch(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return texts.get(param.name);
            }
        };
        for (SpellPiece piece : List.<SpellPiece>of(partial, search)) {
            try {
                piece.execute(context);
                helper.fail(piece.getClass().getSuperclass().getSimpleName() + " must use the budget of the cast");
            } catch (SpellRuntimeException e) {
                helper.assertTrue(WildcardBudget.ERROR_TOO_MANY_COMPARISONS.equals(e.getMessage()), "Wrong error: " + e.getMessage());
            }
        }
        var fresh = context(helper);
        helper.assertTrue(((Number) partial.execute(fresh)).doubleValue() == 1.0, "A new cast must start with a new budget");
        helper.assertTrue(strings(search.execute(fresh)).equals(List.of("abc")), "A new cast must start with a new budget");

        inputs.put(PATTERN, "*" + "?".repeat(4000) + "b*");
        long start = System.nanoTime();
        try {
            keep.execute(context(helper));
            helper.fail("A long pattern over long values must stop at the budget");
        } catch (SpellRuntimeException e) {
            helper.assertTrue(WildcardBudget.ERROR_TOO_MANY_COMPARISONS.equals(e.getMessage()), "Wrong error: " + e.getMessage());
        }
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        helper.assertTrue(elapsedMs < 2_000, "Stopping at the budget took " + elapsedMs + " ms");
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

    /** 取得子: NBT キー一覧は、大きい配列でも String List の要素数の上限までの番号を、0 から順に出す。 */
    @GameTest(template = TEMPLATE)
    public static void nbtKeysStopAtTheListLimitForLargeArrays(GameTestHelper helper) throws SpellRuntimeException {
        var context = context(helper);
        Map<String, Object> inputs = new HashMap<>();
        CompoundTag data = new CompoundTag();
        data.put("big", new IntArrayTag(new int[SpellListLimits.MAX_ELEMENTS * 4]));
        ItemStack stack = new ItemStack(Items.STONE);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        inputs.put(TARGET, SpellItemValue.snapshot(stack));
        inputs.put(PATH, DATA + ".big");
        var keys = new PieceSelectorNbtKeys(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return inputs.get(param.name);
            }
        };

        List<String> indexes = strings(keys.execute(context));
        helper.assertTrue(indexes.size() == SpellListLimits.MAX_ELEMENTS && "0".equals(indexes.get(0))
                        && Integer.toString(SpellListLimits.MAX_ELEMENTS - 1).equals(indexes.get(indexes.size() - 1)),
                "NBT Keys on a large array must give the indexes 0 to " + (SpellListLimits.MAX_ELEMENTS - 1) + ", got "
                        + indexes.size() + " indexes");
        helper.succeed();
    }

    /**
     * 取得子: NBT は、長い値も多い要素も、前と同じ所で切った String List を出す（SNBT 全体を作らずに）。長い値は 1 つの
     * 文字列の上限で、要素は String List の要素数と文字数の上限で切れる。
     */
    @GameTest(template = TEMPLATE)
    public static void nbtEntriesOfLargeTagsAreCutAsBefore(GameTestHelper helper) throws SpellRuntimeException {
        CompoundTag data = new CompoundTag();
        data.putString("long", "x".repeat(5000) + "\r\n\ty" + "z".repeat(5000));
        ListTag pages = new ListTag();
        for (int i = 0; i < 20; i++) {
            CompoundTag page = new CompoundTag();
            page.putString("text", "p".repeat(5000));
            pages.add(page);
        }
        data.put("pages", pages);
        CompoundTag many = new CompoundTag();
        for (int i = 0; i < 10_000; i++) {
            many.putInt("k" + i, i);
        }
        data.put("many", many);
        ListTag books = new ListTag();
        for (int i = 0; i < 100_000; i++) {
            CompoundTag book = new CompoundTag();
            book.putString("id", "minecraft:written_book");
            book.putInt("n", i);
            books.add(book);
        }
        data.put("books", books);
        ItemStack stack = new ItemStack(Items.CHEST);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        Map<String, Object> inputs = new HashMap<>();
        inputs.put(TARGET, SpellItemValue.snapshot(stack));
        var nbt = new PieceSelectorNbt(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return inputs.get(param.name);
            }
        };
        var context = context(helper);
        CompoundTag root = NbtTargetReader.read(context, inputs.get(TARGET)).orElseThrow();

        for (String path : List.of(DATA, DATA + ".pages", DATA + ".many", DATA + ".books")) {
            inputs.put(PATH, path);
            Tag tag = NbtPathHelper.find(root, path).get(0);
            List<String> whole = new ArrayList<>();
            if (tag instanceof CompoundTag compound) {
                compound.getAllKeys().stream().sorted().forEach(key -> whole.add(key + ":" + compound.get(key)));
            } else {
                ListTag list = (ListTag) tag;
                for (int i = 0; i < list.size(); i++) {
                    whole.add(i + ":" + list.get(i));
                }
            }
            List<String> expected = StringListWrapper.make(whole).asList();
            long start = System.nanoTime();
            List<String> got = strings(nbt.execute(context));
            long elapsed = System.nanoTime() - start;
            helper.assertTrue(got.equals(expected), "NBT at " + path + " must cut where the whole SNBT was cut: "
                    + got.size() + " entries for " + expected.size());
            helper.assertTrue(elapsed < 100_000_000L, "Listing " + path + " took " + elapsed / 1_000_000 + " ms");
        }
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
