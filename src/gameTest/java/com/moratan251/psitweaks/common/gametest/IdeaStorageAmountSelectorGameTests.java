package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.param.ParamSpellItemValue;
import com.moratan251.psitweaks.common.spells.param.ParamString;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorIdeaStorageChemicalAmount;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorIdeaStorageFluidAmount;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorIdeaStorageItemAmount;
import com.moratan251.psitweaks.common.storage.idea.FluidResourceKey;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageSavedData;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageService;
import com.moratan251.psitweaks.common.storage.idea.ItemResourceKey;
import com.moratan251.psitweaksqol.api.PsitweaksModeOptions;
import io.netty.buffer.Unpooled;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;

@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class IdeaStorageAmountSelectorGameTests {
    @GameTest(template = "psi110_empty")
    public static void itemModesAggregateIdsWithoutChangingInventory(GameTestHelper helper) throws Exception {
        var player = player(helper);
        var context = new SpellContext().setPlayer(player);
        var storage = IdeaStorageService.get(player.server, player.getUUID());
        var stone = new ItemStack(Items.STONE);
        var namedStone = stone.copy();
        namedStone.setHoverName(Component.literal("Variant"));
        storage.insert(stone, 130);
        storage.insert(namedStone, 7);
        storage.insert(new ItemStack(Items.DIRT), 900);
        Object[] input = {"minecraft:stone"};
        var selector = new PieceSelectorIdeaStorageItemAmount(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return input[0]; }
        };
        long version = storage.getVersion();
        helper.assertTrue(selector.execute(context).equals(137.0D), "String must sum component variants by ID");
        selector.setModeOption(PsitweaksModeOptions.ITEM);
        input[0] = SpellItemValue.snapshot(namedStone.copyWithCount(64));
        helper.assertTrue(selector.execute(context).equals(137.0D), "Item count/components must not narrow the ID match");
        input[0] = SpellItemValue.EMPTY;
        helper.assertTrue(selector.execute(context).equals(0.0D), "Empty Item must return zero");
        selector.setModeOption(PsitweaksModeOptions.STRING);
        for (String missing : new String[] {"minecraft:apple", "example:stone", "minecraft:*", "INVALID ID", ""}) {
            input[0] = missing;
            helper.assertTrue(selector.execute(context).equals(0.0D), "Expected zero for " + missing);
        }
        input[0] = "minecraft:stone";
        helper.assertTrue(selector.execute(new SpellContext().setPlayer(player(helper))).equals(0.0D),
                "Inventory leaked between owners");
        helper.assertTrue(selector.execute(new SpellContext()).equals(0.0D), "Missing caster must return zero");
        helper.assertTrue(storage.getVersion() == version && storage.itemTypeCount() == 3
                && storage.simulateExtract(ItemResourceKey.of(stone).orElseThrow(), Long.MAX_VALUE) == 130
                && storage.simulateExtract(ItemResourceKey.of(namedStone).orElseThrow(), Long.MAX_VALUE) == 7,
                "Queries mutated or consumed the stored resources");
        storage.extract(ItemResourceKey.of(stone).orElseThrow(), 30);
        helper.assertTrue(selector.execute(context).equals(107.0D), "Selector returned a stale total");
        storage.markLoadFailed();
        helper.assertTrue(selector.execute(context).equals(0.0D), "Unread storage must return zero");
        helper.succeed();
    }

    @GameTest(template = "psi110_empty")
    public static void fluidsAndChemicalsReturnCurrentMbIncludingLargeCounts(GameTestHelper helper) throws Exception {
        var player = player(helper);
        var context = new SpellContext().setPlayer(player);
        var storage = IdeaStorageService.get(player.server, player.getUUID());
        var water = new FluidStack(Fluids.WATER, 1);
        var namedWater = water.copy();
        namedWater.getOrCreateTag().putString("variant", "Variant");
        storage.insertFluid(water, 2000);
        storage.insertFluid(namedWater, 3500);
        storage.insertFluid(new FluidStack(Fluids.LAVA, 1), 9000);
        var hydrogen = ResourceLocation.parse("mekanism:gas/hydrogen");
        long oldCapacity = PsitweaksConfig.COMMON.ideaStorageMaxChemicalPerType.get();
        long chemicalAmount;
        try {
            PsitweaksConfig.COMMON.ideaStorageMaxChemicalPerType.set(Math.max(oldCapacity, 4_000_000_123L));
            chemicalAmount = storage.insertChemical(hydrogen, 4_000_000_123L);
        } finally {
            PsitweaksConfig.COMMON.ideaStorageMaxChemicalPerType.set(oldCapacity);
        }
        helper.assertTrue(chemicalAmount == 4_000_000_123L, "Test requires a count above Integer.MAX_VALUE");
        storage.insertChemical(ResourceLocation.parse("mekanism:gas/oxygen"), 33);
        String[] input = {"minecraft:water"};
        var fluid = new PieceSelectorIdeaStorageFluidAmount(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return input[0]; }
        };
        var chemical = new PieceSelectorIdeaStorageChemicalAmount(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return input[0]; }
        };
        long version = storage.getVersion();
        helper.assertTrue(fluid.execute(context).equals(5500.0D), "Fluid component variants must sum in mB");
        input[0] = hydrogen.toString();
        helper.assertTrue(chemical.execute(context).equals((double) chemicalAmount), "Chemical quantity was truncated");
        input[0] = "mekanism:hydrogen";
        helper.assertTrue(chemical.execute(context).equals((double) chemicalAmount), "Raw registry ID must match Forge chemical key");
        helper.assertTrue(fluid.execute(context).equals(0.0D), "Chemical must not count as fluid");
        for (String missing : new String[] {"missing:resource", "INVALID ID", ""}) {
            input[0] = missing;
            helper.assertTrue(fluid.execute(context).equals(0.0D) && chemical.execute(context).equals(0.0D),
                    "Invalid/missing resource must return zero");
        }
        helper.assertTrue(storage.getVersion() == version
                && storage.simulateExtractFluid(FluidResourceKey.of(water).orElseThrow(), Long.MAX_VALUE) == 2000
                && storage.simulateExtractChemical(hydrogen, Long.MAX_VALUE) == chemicalAmount,
                "Fluid or chemical queries mutated the inventory");
        storage.extractFluid(FluidResourceKey.of(water).orElseThrow(), 500);
        storage.extractChemical(hydrogen, 123);
        input[0] = "minecraft:water";
        helper.assertTrue(fluid.execute(context).equals(5000.0D), "Fluid selector returned stale data");
        input[0] = hydrogen.toString();
        helper.assertTrue(chemical.execute(context).equals(4_000_000_000.0D), "Chemical selector returned stale data");
        storage.markLoadFailed();
        helper.assertTrue(chemical.execute(context).equals(0.0D), "Unread chemicals must return zero");
        input[0] = "minecraft:water";
        helper.assertTrue(fluid.execute(context).equals(0.0D), "Unread fluids must return zero");
        helper.succeed();
    }

    @GameTest(template = "psi110_empty")
    public static void modesSurviveSerializationAndSelectorsHaveCorrectStats(GameTestHelper helper) throws Exception {
        var spell = new Spell();
        for (String suffix : new String[] {"item", "fluid", "chemical"}) {
            var id = Psitweaks.location("selector_idea_storage_" + suffix + "_amount");
            var piece = PsiAPI.getSpellPiece(id).getConstructor(Spell.class).newInstance(spell);
            helper.assertTrue(piece.getEvaluationType() == Double.class && piece.params.size() == 1
                    && piece.params.values().iterator().next() instanceof ParamString
                    && !piece.params.values().iterator().next().canDisable, "Expected one required String input");
            var metadata = new SpellMetadata();
            metadata.setStat(EnumSpellStat.COMPLEXITY, 7);
            metadata.setStat(EnumSpellStat.POTENCY, 15);
            metadata.setStat(EnumSpellStat.COST, 20);
            piece.addToMetadata(metadata);
            helper.assertTrue(metadata.getStat(EnumSpellStat.COMPLEXITY) == 8
                    && metadata.getStat(EnumSpellStat.POTENCY) == 15
                    && metadata.getStat(EnumSpellStat.COST) == 20, "Expected Complexity +1, Potency/Cost +0");
            helper.assertTrue(PsiAPI.getGroupForPiece(piece.getClass()).equals(Psitweaks.location("trick_idea_storage_view")), "Wrong research group");
        }
        var item = (PieceSelectorIdeaStorageItemAmount) new PieceSelectorIdeaStorageItemAmount(spell);
        item.paramSides.replaceAll((param, side) -> SpellParam.Side.RIGHT);
        item.setModeOption(PsitweaksModeOptions.ITEM);
        assertItemMode(helper, item);
        var tag = new CompoundTag();
        item.writeToNBT(tag);
        assertItemMode(helper, (PieceSelectorIdeaStorageItemAmount) SpellPiece.createFromNBT(new Spell(), tag));
        item.setModeOption(PsitweaksModeOptions.STRING);
        helper.assertTrue(item.params.values().iterator().next() instanceof ParamString
                && item.paramSides.values().iterator().next() == SpellParam.Side.RIGHT, "Switching back lost the input");
        tag.putString("psitweaksMode", "unknown:mode");
        item.readFromNBT(tag);
        helper.assertTrue(item.getModeOption().equals(PsitweaksModeOptions.STRING), "Unknown mode must fall back to String");
        helper.succeed();
    }

    @GameTest(template = "psi110_empty")
    public static void totalsAboveLongRangeRemainPositive(GameTestHelper helper) {
        UUID owner = UUID.randomUUID();
        var data = new IdeaStorageSavedData(owner);
        var water = new FluidStack(Fluids.WATER, 1);
        var namedWater = water.copy();
        namedWater.getOrCreateTag().putString("variant", "Variant");
        data.storage().insertFluid(water, 1);
        data.storage().insertFluid(namedWater, 1);
        var registries = helper.getLevel().registryAccess();
        var tag = data.save(new CompoundTag());
        for (var entry : tag.getList("Fluids", 10)) {
            ((CompoundTag) entry).putLong("count", Long.MAX_VALUE);
        }
        var restored = IdeaStorageSavedData.load(owner, tag).storage();
        helper.assertTrue(!restored.isLoadFailed() && restored.fluidAmountById(ResourceLocation.parse("minecraft:water"))
                == 2.0D * Long.MAX_VALUE, "ID aggregate overflowed long or lost a component variant");
        helper.succeed();
    }

    private static void assertItemMode(GameTestHelper helper, PieceSelectorIdeaStorageItemAmount piece) {
        helper.assertTrue(piece.getModeOption().equals(PsitweaksModeOptions.ITEM) && piece.params.size() == 1
                && piece.params.values().iterator().next() instanceof ParamSpellItemValue
                && piece.paramSides.values().iterator().next() == SpellParam.Side.RIGHT, "Item mode/input direction lost");
    }

    private static FakePlayer player(GameTestHelper helper) {
        return new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "amount-reader"));
    }
}
