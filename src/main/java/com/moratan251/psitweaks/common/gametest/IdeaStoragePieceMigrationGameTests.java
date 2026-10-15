package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.compat.IdeaStoragePieceMigration;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.item.SpellItemFilters;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.item.StrictItemFilter;
import com.moratan251.psitweaks.common.spells.mode.BlockResourceModes;
import com.moratan251.psitweaks.common.spells.mode.IdeaStorageResourceModes;
import com.moratan251.psitweaks.common.spells.param.ParamItemSearch;
import com.moratan251.psitweaks.common.spells.param.ParamSpellItemFilter;
import com.moratan251.psitweaks.common.spells.param.ParamStringOrItem;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorStrictItem;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorIdeaStorageAmount;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorIdeaStorageFreeCapacity;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorIdeaStorageResourceBase;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorIdeaStorageTypeCount;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickIdeaStorageDeposit;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickIdeaStorageResourceBase;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickIdeaStorageSlotBase;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickIdeaStorageWithdraw;
import com.moratan251.psitweaks.common.spells.wrapper.SpellItemListWrapper;
import com.moratan251.psitweaksqol.api.PsitweaksModeOption;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.api.spell.SpellRuntimeException;

/** Spells saved before the ideaspace pieces were merged, and the item search input that replaced the filter modes. */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class IdeaStoragePieceMigrationGameTests {
    private static final String POSITION = "_position";
    private static final String DIRECTION = "_direction";

    /** One old piece: its ID, the class and mode it must load as, and whether it was a trick. */
    private record Legacy(String id, Class<?> type, PsitweaksModeOption mode, boolean trick) {
    }

    private static final List<Legacy> LEGACY = List.of(
            new Legacy("selector_idea_storage_item_amount", PieceSelectorIdeaStorageAmount.class, BlockResourceModes.ITEM, false),
            new Legacy("selector_idea_storage_fluid_amount", PieceSelectorIdeaStorageAmount.class, BlockResourceModes.FLUID, false),
            new Legacy("selector_idea_storage_chemical_amount", PieceSelectorIdeaStorageAmount.class, BlockResourceModes.CHEMICAL, false),
            new Legacy("selector_idea_storage_energy", PieceSelectorIdeaStorageAmount.class, BlockResourceModes.ENERGY, false),
            new Legacy("selector_idea_storage_item_free_capacity", PieceSelectorIdeaStorageFreeCapacity.class, BlockResourceModes.ITEM, false),
            new Legacy("selector_idea_storage_fluid_free_capacity", PieceSelectorIdeaStorageFreeCapacity.class, BlockResourceModes.FLUID, false),
            new Legacy("selector_idea_storage_chemical_free_capacity", PieceSelectorIdeaStorageFreeCapacity.class, BlockResourceModes.CHEMICAL, false),
            new Legacy("selector_idea_storage_energy_free_capacity", PieceSelectorIdeaStorageFreeCapacity.class, BlockResourceModes.ENERGY, false),
            new Legacy("selector_idea_storage_item_type_count", PieceSelectorIdeaStorageTypeCount.class, BlockResourceModes.ITEM, false),
            new Legacy("selector_idea_storage_fluid_type_count", PieceSelectorIdeaStorageTypeCount.class, BlockResourceModes.FLUID, false),
            new Legacy("selector_idea_storage_chemical_type_count", PieceSelectorIdeaStorageTypeCount.class, BlockResourceModes.CHEMICAL, false),
            new Legacy("trick_idea_storage_deposit_item", PieceTrickIdeaStorageDeposit.class, BlockResourceModes.ITEM, true),
            new Legacy("trick_idea_storage_deposit_fluid", PieceTrickIdeaStorageDeposit.class, BlockResourceModes.FLUID, true),
            new Legacy("trick_idea_storage_deposit_chemical", PieceTrickIdeaStorageDeposit.class, BlockResourceModes.CHEMICAL, true),
            new Legacy("trick_idea_storage_absorb_fe", PieceTrickIdeaStorageDeposit.class, BlockResourceModes.ENERGY, true),
            new Legacy("trick_idea_storage_withdraw_item", PieceTrickIdeaStorageWithdraw.class, BlockResourceModes.ITEM, true),
            new Legacy("trick_idea_storage_withdraw_fluid", PieceTrickIdeaStorageWithdraw.class, BlockResourceModes.FLUID, true),
            new Legacy("trick_idea_storage_withdraw_chemical", PieceTrickIdeaStorageWithdraw.class, BlockResourceModes.CHEMICAL, true),
            new Legacy("trick_idea_storage_supply_fe", PieceTrickIdeaStorageWithdraw.class, BlockResourceModes.ENERGY, true));

    @GameTest(template = "psi110_empty")
    public static void everyOldIdLoadsAsTheMergedPieceWithItsModeAndWires(GameTestHelper helper) {
        for (Legacy legacy : LEGACY) {
            helper.assertTrue(PsiAPI.SPELL_PIECE_REGISTRY.get(Psitweaks.location(legacy.id())) == null,
                    "Old piece is still registered: " + legacy.id());
            boolean energy = legacy.mode() == BlockResourceModes.ENERGY;
            boolean typeCount = legacy.type() == PieceSelectorIdeaStorageTypeCount.class;
            boolean item = legacy.mode() == BlockResourceModes.ITEM;
            for (boolean itemInput : item && !typeCount ? new boolean[] {false, true} : new boolean[] {false}) {
                for (boolean exact : legacy.trick() && !energy ? new boolean[] {false, true} : new boolean[] {false}) {
                    var tag = new CompoundTag();
                    tag.putString("key", Psitweaks.location(legacy.id()).toString());
                    var params = new CompoundTag();
                    String oldInput = itemInput ? PsitweaksSpellParams.ITEM : PsitweaksSpellParams.STRING;
                    if (legacy.trick()) {
                        params.putInt(POSITION, SpellParam.Side.TOP.asInt());
                        params.putInt(DIRECTION, SpellParam.Side.BOTTOM.asInt());
                        params.putInt(item ? "_number" : "_power", SpellParam.Side.RIGHT.asInt());
                    }
                    if (!energy && !typeCount) {
                        params.putInt(oldInput, SpellParam.Side.LEFT.asInt());
                    }
                    tag.put("params", params);
                    if (item && !typeCount) {
                        tag.putString("psitweaksMode", itemInput ? "item" : "string");
                    }
                    if (exact) {
                        tag.putString("psitweaksTransferMode", "exact");
                    }
                    var original = tag.copy();
                    SpellPiece piece = SpellPiece.createFromNBT(new Spell(), tag);
                    String label = legacy.id() + (itemInput ? " (Item input)" : "") + (exact ? " (exact)" : "");
                    helper.assertTrue(piece != null && piece.getClass() == legacy.type(), "Wrong piece for " + label);
                    helper.assertTrue(tag.equals(original), "Migration changed the saved tag of " + label);
                    if (piece instanceof PieceTrickIdeaStorageResourceBase trick) {
                        PsitweaksModeOption expected = exact ? exactOf(legacy.mode()) : legacy.mode();
                        helper.assertTrue(trick.getModeOption() == expected && trick.isExactTransfer() == exact,
                                "Wrong mode for " + label);
                        helper.assertTrue(trick.paramSides.get(trick.params.get("psi.spellparam.position")) == SpellParam.Side.TOP
                                        && trick.paramSides.get(trick.params.get("psi.spellparam.direction")) == SpellParam.Side.BOTTOM
                                        && trick.paramSides.get(trick.params.get(item ? "psi.spellparam.number" : "psi.spellparam.power"))
                                        == SpellParam.Side.RIGHT, "Endpoint or quantity wire lost for " + label);
                        helper.assertTrue(energy ? !trick.params.containsKey(PsitweaksSpellParams.FILTER)
                                        : trick.paramSides.get(trick.params.get(PsitweaksSpellParams.FILTER)) == SpellParam.Side.LEFT,
                                "Filter wire lost for " + label);
                    } else {
                        var selector = (PieceSelectorIdeaStorageResourceBase) piece;
                        helper.assertTrue(selector.getModeOption() == legacy.mode(), "Wrong mode for " + label);
                        helper.assertTrue(energy || typeCount ? selector.params.isEmpty()
                                        : selector.paramSides.get(selector.params.get(PsitweaksSpellParams.RESOURCE)) == SpellParam.Side.LEFT,
                                "Resource wire lost for " + label);
                    }
                    var resaved = new CompoundTag();
                    piece.writeToNBT(resaved);
                    helper.assertTrue(IdeaStoragePieceMigration.migrate(resaved) == null && !resaved.contains("psitweaksTransferMode"),
                            "A merged piece must save in the new format: " + label);
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = "psi110_empty")
    public static void legacyQuantityPathOnlyIdsAndSlotTricksMigrate(GameTestHelper helper) {
        // The item tricks once named their quantity Power, and very old spells saved IDs without the namespace.
        for (String oldKey : new String[] {"_power", "psi.spellparam.power"}) {
            var tag = new CompoundTag();
            tag.putString("key", "trick_idea_storage_withdraw_item");
            var params = new CompoundTag();
            params.putInt(oldKey, SpellParam.Side.RIGHT.asInt());
            tag.put("params", params);
            var piece = (PieceTrickIdeaStorageResourceBase) SpellPiece.createFromNBT(new Spell(), tag);
            helper.assertTrue(piece instanceof PieceTrickIdeaStorageWithdraw && piece.getModeOption() == BlockResourceModes.ITEM
                            && piece.paramSides.get(piece.params.get("psi.spellparam.number")) == SpellParam.Side.RIGHT,
                    "Item quantity saved as " + oldKey + " lost its wire");
        }
        for (String action : new String[] {"deposit", "withdraw"}) {
            for (String oldInput : new String[] {PsitweaksSpellParams.STRING, PsitweaksSpellParams.ITEM}) {
                var tag = new CompoundTag();
                tag.putString("key", Psitweaks.location("trick_idea_storage_" + action + "_slot").toString());
                var params = new CompoundTag();
                params.putInt("_number", SpellParam.Side.RIGHT.asInt());
                params.putInt(oldInput, SpellParam.Side.LEFT.asInt());
                tag.put("params", params);
                tag.putString("psitweaksMode", oldInput.equals(PsitweaksSpellParams.ITEM) ? "item_strict" : "string");
                var piece = (PieceTrickIdeaStorageSlotBase) SpellPiece.createFromNBT(new Spell(), tag);
                helper.assertTrue(piece.paramSides.get(piece.params.get(PsitweaksSpellParams.FILTER)) == SpellParam.Side.LEFT
                                && piece.paramSides.get(piece.params.get("psi.spellparam.number")) == SpellParam.Side.RIGHT,
                        "Slot " + action + " lost the wire of " + oldInput);
                var resaved = new CompoundTag();
                piece.writeToNBT(resaved);
                helper.assertTrue(IdeaStoragePieceMigration.migrate(resaved) == null, "Slot tricks must save in the new format");
            }
        }
        var foreign = new CompoundTag();
        foreign.putString("key", "othermod:trick_idea_storage_deposit_item");
        helper.assertTrue(IdeaStoragePieceMigration.migrate(foreign) == null, "Another mod's ID must never be rewritten");
        helper.succeed();
    }

    @GameTest(template = "psi110_empty")
    public static void searchInputMatchesByTheWiredValueAndStrictItemCompareComponents(GameTestHelper helper) throws Exception {
        var apple = new ItemStack(Items.APPLE);
        var named = apple.copy();
        named.set(DataComponents.CUSTOM_NAME, Component.literal("Variant"));
        var ironOre = new ItemStack(Items.IRON_ORE);
        helper.assertTrue(SpellItemFilters.search(null).test(ironOre) && SpellItemFilters.search("").test(ironOre)
                        && !SpellItemFilters.isSearchSet(null) && !SpellItemFilters.isSearchSet(""),
                "No filter and an empty String must match everything");
        helper.assertTrue(SpellItemFilters.search("*_ore").test(ironOre) && !SpellItemFilters.search("*_ore").test(apple)
                        && SpellItemFilters.search("minecraft:app?e").test(named) && SpellItemFilters.isSearchSet("apple"),
                "A String must match item IDs with wildcards");
        helper.assertTrue(SpellItemFilters.search(SpellItemValue.snapshot(apple)).test(named),
                "An Item must match the item type only");
        var list = SpellItemListWrapper.make(List.of(SpellItemValue.snapshot(ironOre), SpellItemValue.EMPTY, SpellItemValue.snapshot(named)));
        helper.assertTrue(SpellItemFilters.search(list).test(apple) && SpellItemFilters.search(list).test(ironOre)
                        && !SpellItemFilters.search(list).test(new ItemStack(Items.STONE)), "An Item List must match any of its types");

        var context = new SpellContext();
        Object[] input = {SpellItemValue.snapshot(named.copyWithCount(5))};
        var operator = new PieceOperatorStrictItem(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return input[0]; }
        };
        var strict = (StrictItemFilter) operator.execute(context);
        helper.assertTrue(SpellItemFilters.search(strict).test(named.copyWithCount(64)) && !SpellItemFilters.search(strict).test(apple)
                        && SpellItemFilters.of(strict, false).test(named), "A strict Item must compare components but never the count");
        input[0] = list;
        var strictList = (StrictItemFilter) operator.execute(context);
        helper.assertTrue(strictList.templates().size() == 2 && strictList.test(named) && strictList.test(ironOre) && !strictList.test(apple),
                "A strict Item List must keep one template per non-empty element");
        input[0] = strict;
        helper.assertTrue(operator.execute(context) == strict, "A strict Item must pass through unchanged");
        input[0] = SpellItemValue.EMPTY;
        boolean rejected = false;
        try { operator.execute(context); } catch (SpellRuntimeException expected) { rejected = true; }
        helper.assertTrue(rejected, "An empty Item must be an error");

        var spell = new Spell();
        SpellPiece strictPiece = PsiAPI.SPELL_PIECE_REGISTRY.get(Psitweaks.location("operator_strict_item")).create(spell);
        SpellPiece stringPiece = PsiAPI.SPELL_PIECE_REGISTRY.get(Psitweaks.location("constant_string")).create(spell);
        var search = new ParamItemSearch(PsitweaksSpellParams.FILTER, 0, true, false);
        var itemFilter = new ParamSpellItemFilter(PsitweaksSpellParams.ITEM, 0, false, false);
        var resource = new ParamStringOrItem(PsitweaksSpellParams.RESOURCE, 0, false, false);
        helper.assertTrue(search.canAccept(strictPiece) && search.canAccept(stringPiece), "The search input must take a strict Item and a String");
        helper.assertTrue(itemFilter.canAccept(strictPiece) && !itemFilter.canAccept(stringPiece),
                "Item filter inputs take a strict Item but not a String");
        helper.assertTrue(resource.canAccept(stringPiece) && !resource.canAccept(strictPiece),
                "Selector resource inputs take a String or an Item only");
        helper.assertTrue(IdeaStorageResourceModes.TRANSFER.size() == 7, "Seven transfer modes expected");
        helper.succeed();
    }

    private static PsitweaksModeOption exactOf(PsitweaksModeOption kind) {
        if (kind == BlockResourceModes.FLUID) return IdeaStorageResourceModes.FLUID_EXACT;
        if (kind == BlockResourceModes.CHEMICAL) return IdeaStorageResourceModes.CHEMICAL_EXACT;
        return IdeaStorageResourceModes.ITEM_EXACT;
    }
}
