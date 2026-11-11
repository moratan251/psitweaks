package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.param.ParamBlockListWrapper;
import com.moratan251.psitweaks.common.spells.param.ParamSpellItemListWrapper;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorListNameFilterBase;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorListNameSearch;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorListNameSearchExclude;
import com.moratan251.psitweaks.common.spells.wrapper.BlockListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.SpellItemListWrapper;
import com.moratan251.psitweaksqol.api.PsitweaksModeOption;
import com.moratan251.psitweaksqol.api.PsitweaksModeOptions;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
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
import vazkii.psi.api.spell.param.ParamEntityListWrapper;
import vazkii.psi.api.spell.wrapper.EntityListWrapper;

/**
 * Operator: List Keep Name Matches and Operator: List Remove Name Matches filter Entity, Item and Block Lists by
 * display name. Only custom names are used here, so the result does not depend on the language files.
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ListNameFilterGameTests {
    private static final String TEMPLATE = "psi110_empty";
    private static final String LIST = SpellParam.GENERIC_NAME_LIST;
    private static final String STRING = PsitweaksSpellParams.STRING;

    private ListNameFilterGameTests() {
    }

    /** A filter that reads its List from {@code list[0]} and its pattern from {@code pattern[0]}. */
    private static PieceOperatorListNameFilterBase filter(boolean keep, Object[] list, String[] pattern) {
        if (keep) {
            return new PieceOperatorListNameSearch(new Spell()) {
                @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                    return param.name.equals(LIST) ? list[0] : pattern[0];
                }
            };
        }
        return new PieceOperatorListNameSearchExclude(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return param.name.equals(LIST) ? list[0] : pattern[0];
            }
        };
    }

    private static ArmorStand named(GameTestHelper helper, String name) {
        var stand = new ArmorStand(helper.getLevel(), 0, 0, 0);
        stand.setCustomName(Component.literal(name));
        return stand;
    }

    private static SpellItemValue namedItem(ItemStack stack, String name) {
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        return SpellItemValue.snapshot(stack);
    }

    private static Set<Entity> entities(Object result) {
        var set = new java.util.HashSet<Entity>();
        for (Entity entity : (EntityListWrapper) result) {
            set.add(entity);
        }
        return set;
    }

    private static List<String> modeIds(PieceOperatorListNameFilterBase piece) {
        return piece.getAvailableModeOptions().stream().map(option -> option.id().toString()).collect(Collectors.toList());
    }

    @GameTest(template = TEMPLATE)
    public static void nameFiltersKeepOrRemoveByDisplayName(GameTestHelper helper) throws SpellRuntimeException {
        var context = new SpellContext();
        Object[] list = {null};
        String[] pattern = {""};
        var keep = filter(true, list, pattern);
        var remove = filter(false, list, pattern);

        helper.assertTrue(modeIds(keep).equals(List.of(PsitweaksModeOptions.ENTITY.id().toString(),
                        PsitweaksModeOptions.ITEM.id().toString(), PsitweaksModeOptions.BLOCK.id().toString())),
                "Only Entity, Item and Block have display names: " + modeIds(keep));
        helper.assertTrue(keep.getModeOption().id().equals(PsitweaksModeOptions.ENTITY.id())
                        && keep.params.get(LIST) instanceof ParamEntityListWrapper
                        && keep.params.get(STRING) != null
                        && keep.getEvaluationType() == EntityListWrapper.class,
                "A new name filter must start in Entity mode with a List and a String input");

        var bob = named(helper, "Bob");
        var bobby = named(helper, "Bobby");
        var alice = named(helper, "alice");
        list[0] = EntityListWrapper.make(List.<Entity>of(bob, bobby, alice));
        pattern[0] = "Bob*";
        helper.assertTrue(entities(keep.execute(context)).equals(Set.of(bob, bobby)),
                "Keep must leave the names that match Bob*");
        helper.assertTrue(entities(remove.execute(context)).equals(Set.of(alice)),
                "Remove must leave the names that do not match Bob*");
        pattern[0] = "Bob";
        helper.assertTrue(entities(keep.execute(context)).equals(Set.of(bob)), "The whole name must match");
        pattern[0] = "bob*";
        helper.assertTrue(entities(keep.execute(context)).isEmpty(), "Case must be kept");
        pattern[0] = "?lic[ae]";
        helper.assertTrue(entities(keep.execute(context)).equals(Set.of(alice)), "? and [ae] must work on names");

        PsitweaksModeOption item = PsitweaksModeOptions.ITEM;
        keep.setModeOption(item);
        remove.setModeOption(item);
        helper.assertTrue(keep.params.get(LIST) instanceof ParamSpellItemListWrapper
                        && keep.getEvaluationType() == SpellItemListWrapper.class,
                "Item mode must take and return an Item List");
        list[0] = SpellItemListWrapper.make(List.of(namedItem(new ItemStack(Items.APPLE), "Magic Apple"),
                namedItem(new ItemStack(Items.BREAD), "Plain Bread")));
        pattern[0] = "Magic *";
        var kept = ((SpellItemListWrapper) keep.execute(context)).asList();
        var left = ((SpellItemListWrapper) remove.execute(context)).asList();
        helper.assertTrue(kept.size() == 1 && kept.get(0).copyStack().is(Items.APPLE),
                "Keep must leave the item named Magic Apple: " + kept);
        helper.assertTrue(left.size() == 1 && left.get(0).copyStack().is(Items.BREAD),
                "Remove must leave the item named Plain Bread: " + left);

        keep.setModeOption(PsitweaksModeOptions.BLOCK);
        helper.assertTrue(keep.params.get(LIST) instanceof ParamBlockListWrapper
                        && keep.getEvaluationType() == BlockListWrapper.class,
                "Block mode must take and return a Block List");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void nameFiltersAreRegisteredAndKeepTheirMode(GameTestHelper helper) {
        for (String id : List.of("operator_list_name_search", "operator_list_name_search_exclude")) {
            var type = PsiAPI.SPELL_PIECE_REGISTRY.get(Psitweaks.location(id));
            helper.assertTrue(type != null, "Not registered: " + id);
            var piece = (PieceOperatorListNameFilterBase) type.create(new Spell());
            piece.setModeOption(PsitweaksModeOptions.ITEM);
            piece.paramSides.put(piece.params.get(LIST), SpellParam.Side.RIGHT);
            var tag = new CompoundTag();
            piece.writeToNBT(tag);
            var reloaded = (PieceOperatorListNameFilterBase) SpellPiece.createFromNBT(new Spell(), tag);
            helper.assertTrue(reloaded.getModeOption().id().equals(PsitweaksModeOptions.ITEM.id())
                            && reloaded.params.get(LIST) instanceof ParamSpellItemListWrapper
                            && reloaded.paramSides.get(reloaded.params.get(LIST)) == SpellParam.Side.RIGHT,
                    "Item mode or its wire lost on reload: " + id);
        }
        helper.succeed();
    }
}
