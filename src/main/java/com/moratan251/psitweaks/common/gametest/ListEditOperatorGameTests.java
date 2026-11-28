package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorListIndexOf;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorListRemoveDuplicates;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorListReplaceElement;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorListReverse;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorModeListBase;
import com.moratan251.psitweaks.common.spells.wrapper.NumberListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.SpellItemListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.StringListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.VectorListWrapper;
import com.moratan251.psitweaksqol.api.PsitweaksModeOption;
import com.moratan251.psitweaksqol.api.PsitweaksModeOptions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.wrapper.EntityListWrapper;

/** Operators that edit a List: Replace Element, Remove Duplicates, Reverse and Index Of. */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ListEditOperatorGameTests {
    private static final String TEMPLATE = "psi110_empty";
    private static final String LIST = SpellParam.GENERIC_NAME_LIST;
    private static final String ELEMENT = PsitweaksSpellParams.ELEMENT1;
    private static final String NUMBER = SpellParam.GENERIC_NAME_NUMBER;

    private ListEditOperatorGameTests() {
    }

    private static String errorOf(SpellPiece piece) {
        try {
            piece.execute(new SpellContext());
            return null;
        } catch (SpellRuntimeException e) {
            return e.getMessage();
        }
    }

    private static List<String> modeIds(PieceOperatorModeListBase piece) {
        List<String> ids = new ArrayList<>();
        for (PsitweaksModeOption option : piece.getAvailableModeOptions()) {
            ids.add(option.id().getPath());
        }
        return ids;
    }

    private static boolean contains(EntityListWrapper list, Entity entity) {
        for (Entity element : list) {
            if (element == entity) {
                return true;
            }
        }
        return false;
    }

    private static List<String> strings(Object result) {
        return ((StringListWrapper) result).asList();
    }

    private static List<Double> numbers(Object result) {
        return ((NumberListWrapper) result).asList();
    }

    @GameTest(template = TEMPLATE)
    public static void replaceElementWorksByIndex(GameTestHelper helper) throws SpellRuntimeException {
        Map<String, Object> inputs = new HashMap<>();
        var replace = new PieceOperatorListReplaceElement(new Spell()) {
            @Override public Object getRawParamValue(SpellContext context, SpellParam<?> param) {
                return inputs.get(param.name);
            }
        };
        inputs.put(LIST, StringListWrapper.make(List.of("a", "b", "c")));
        inputs.put(ELEMENT, "x");
        inputs.put(NUMBER, 1.0);
        helper.assertTrue(strings(replace.execute(new SpellContext())).equals(List.of("a", "x", "c")), "Index 1 must be replaced");
        inputs.put(NUMBER, -1.0);
        helper.assertTrue(strings(replace.execute(new SpellContext())).equals(List.of("a", "b", "x")), "-1 must replace the last element");
        for (double index : new double[] {3.0, -4.0}) {
            inputs.put(NUMBER, index);
            helper.assertTrue(SpellRuntimeException.OUT_OF_BOUNDS.equals(errorOf(replace)), index + " must be out of bounds");
        }

        replace.setModeOption(PsitweaksModeOptions.VECTOR);
        inputs.put(LIST, VectorListWrapper.make(List.of(new Vector3(1, 0, 0), new Vector3(2, 0, 0))));
        inputs.put(ELEMENT, new Vector3(9, 9, 9));
        inputs.put(NUMBER, 0.0);
        var vectors = ((VectorListWrapper) replace.execute(new SpellContext())).asList();
        helper.assertTrue(vectors.size() == 2 && vectors.get(0).equals(new Vector3(9, 9, 9)) && vectors.get(1).x == 2,
                "Vector mode must replace in place: " + vectors);

        var level = helper.getLevel();
        Vec3 base = helper.absoluteVec(new Vec3(1.5, 1.0, 1.5));
        var first = new ArmorStand(level, base.x, base.y, base.z);
        var second = new ArmorStand(level, base.x, base.y, base.z);
        var third = new ArmorStand(level, base.x, base.y, base.z);
        var entities = EntityListWrapper.make(List.<Entity>of(first, second));
        replace.setModeOption(PsitweaksModeOptions.ENTITY);
        inputs.put(LIST, entities);
        inputs.put(ELEMENT, third);
        var replaced = entities.get(0);
        var result = (EntityListWrapper) replace.execute(new SpellContext());
        helper.assertTrue(result.size() == 2 && contains(result, third) && !contains(result, replaced),
                "Entity mode must swap the entity at the index");
        inputs.put(ELEMENT, entities.get(1));
        helper.assertTrue(((EntityListWrapper) replace.execute(new SpellContext())).size() == 1,
                "An entity already in the List must not be held twice");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void indexOfFindsTheFirstEqualElement(GameTestHelper helper) throws SpellRuntimeException {
        Map<String, Object> inputs = new HashMap<>();
        var indexOf = new PieceOperatorListIndexOf(new Spell()) {
            @Override public Object getRawParamValue(SpellContext context, SpellParam<?> param) {
                return inputs.get(param.name);
            }
        };
        helper.assertTrue(indexOf.getEvaluationType() == Double.class, "Index Of must return a Number");
        indexOf.setModeOption(PsitweaksModeOptions.NUMBER);
        inputs.put(LIST, NumberListWrapper.make(List.of(3.0, 5.0, 3.0)));
        double[][] cases = {{3.0, 0.0}, {5.0, 1.0}, {7.0, -1.0}};
        for (double[] c : cases) {
            inputs.put(ELEMENT, c[0]);
            helper.assertTrue((Double) indexOf.execute(new SpellContext()) == c[1], c[0] + " must be found at " + c[1]);
        }
        inputs.put(LIST, NumberListWrapper.EMPTY);
        helper.assertTrue((Double) indexOf.execute(new SpellContext()) == -1.0, "An empty List must give -1");

        indexOf.setModeOption(PsitweaksModeOptions.VECTOR);
        inputs.put(LIST, VectorListWrapper.make(List.of(new Vector3(1, 2, 3), new Vector3(0, 0, 0))));
        inputs.put(ELEMENT, new Vector3(-0.0, 0, 0));
        helper.assertTrue((Double) indexOf.execute(new SpellContext()) == 1.0, "Vectors must compare like Operator: Equal");

        var level = helper.getLevel();
        Vec3 base = helper.absoluteVec(new Vec3(1.5, 1.0, 1.5));
        var first = new ArmorStand(level, base.x, base.y, base.z);
        var second = new ArmorStand(level, base.x, base.y, base.z);
        var entities = EntityListWrapper.make(List.<Entity>of(first, second));
        indexOf.setModeOption(PsitweaksModeOptions.ENTITY);
        inputs.put(LIST, entities);
        inputs.put(ELEMENT, entities.get(1));
        helper.assertTrue((Double) indexOf.execute(new SpellContext()) == 1.0, "The index must follow the Entity List's order");

        indexOf.setModeOption(PsitweaksModeOptions.ITEM);
        inputs.put(LIST, SpellItemListWrapper.make(List.of(SpellItemValue.snapshot(new ItemStack(Items.STONE)))));
        inputs.put(ELEMENT, SpellItemValue.EMPTY);
        helper.assertTrue((Double) indexOf.execute(new SpellContext()) == -1.0, "An empty Item must match nothing");
        inputs.put(ELEMENT, SpellItemValue.snapshot(new ItemStack(Items.STONE)));
        helper.assertTrue((Double) indexOf.execute(new SpellContext()) == 0.0, "An equal Item must be found");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void reverseFlipsEveryListButEntities(GameTestHelper helper) throws SpellRuntimeException {
        Map<String, Object> inputs = new HashMap<>();
        var reverse = new PieceOperatorListReverse(new Spell()) {
            @Override public Object getRawParamValue(SpellContext context, SpellParam<?> param) {
                return inputs.get(param.name);
            }
        };
        helper.assertTrue(modeIds(reverse).equals(List.of("string", "number", "vector", "item", "block")),
                "Reverse must offer every List type but Entity: " + modeIds(reverse));
        reverse.setModeOption(PsitweaksModeOptions.ENTITY);
        helper.assertTrue(reverse.getModeOption().id().equals(PsitweaksModeOptions.STRING.id()),
                "Entity mode must not be selectable");

        inputs.put(LIST, StringListWrapper.make(List.of("a", "b", "c")));
        helper.assertTrue(strings(reverse.execute(new SpellContext())).equals(List.of("c", "b", "a")), "Strings must be reversed");
        reverse.setModeOption(PsitweaksModeOptions.NUMBER);
        inputs.put(LIST, NumberListWrapper.make(List.of(1.0, 2.0, 3.0)));
        helper.assertTrue(numbers(reverse.execute(new SpellContext())).equals(List.of(3.0, 2.0, 1.0)), "Numbers must be reversed");
        inputs.put(LIST, NumberListWrapper.EMPTY);
        helper.assertTrue(numbers(reverse.execute(new SpellContext())).isEmpty(), "An empty List must stay empty");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void removeDuplicatesKeepsTheFirstOfEach(GameTestHelper helper) throws SpellRuntimeException {
        Map<String, Object> inputs = new HashMap<>();
        var distinct = new PieceOperatorListRemoveDuplicates(new Spell()) {
            @Override public Object getRawParamValue(SpellContext context, SpellParam<?> param) {
                return inputs.get(param.name);
            }
        };
        helper.assertTrue(modeIds(distinct).equals(List.of("string", "number", "vector", "item")),
                "Remove Duplicates must leave out Entity and Block: " + modeIds(distinct));

        inputs.put(LIST, StringListWrapper.make(List.of("a", "b", "a", "c", "b")));
        helper.assertTrue(strings(distinct.execute(new SpellContext())).equals(List.of("a", "b", "c")),
                "Strings must keep their first occurrences in order");
        distinct.setModeOption(PsitweaksModeOptions.NUMBER);
        inputs.put(LIST, NumberListWrapper.make(List.of(2.0, 1.0, 2.0, 2.0, 1.0)));
        helper.assertTrue(numbers(distinct.execute(new SpellContext())).equals(List.of(2.0, 1.0)), "Numbers must be deduplicated");

        distinct.setModeOption(PsitweaksModeOptions.VECTOR);
        inputs.put(LIST, VectorListWrapper.make(List.of(new Vector3(0, 0, 0), new Vector3(1, 0, 0), new Vector3(-0.0, 0, 0))));
        var vectors = ((VectorListWrapper) distinct.execute(new SpellContext())).asList();
        helper.assertTrue(vectors.size() == 2 && vectors.get(1).x == 1, "0 and -0 must count as the same Vector: " + vectors);

        distinct.setModeOption(PsitweaksModeOptions.ITEM);
        inputs.put(LIST, SpellItemListWrapper.make(List.of(
                SpellItemValue.snapshot(new ItemStack(Items.STONE, 2)),
                SpellItemValue.snapshot(new ItemStack(Items.STONE, 2)),
                SpellItemValue.snapshot(new ItemStack(Items.STONE, 3)),
                SpellItemValue.snapshot(new ItemStack(Items.DIRT, 2)))));
        helper.assertTrue(((SpellItemListWrapper) distinct.execute(new SpellContext())).size() == 3,
                "Only the Item equal to an earlier one must be removed");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void listEditOperatorsAreRegisteredAndKeepTheirMode(GameTestHelper helper) {
        for (String id : List.of("operator_list_replace_element", "operator_list_remove_duplicates",
                "operator_list_reverse", "operator_list_index_of")) {
            ResourceLocation location = Psitweaks.location(id);
            var type = PsiAPI.SPELL_PIECE_REGISTRY.get(location);
            helper.assertTrue(type != null, "Not registered: " + id);
            var group = PsiAPI.getPieceGroup(helper.getLevel().registryAccess(), location);
            helper.assertTrue(group.isPresent() && group.get().key().location().equals(Psitweaks.location("string")),
                    id + " must be in the string group");

            var piece = (PieceOperatorModeListBase) type.create(new Spell());
            piece.paramSides.put(piece.params.get(LIST), SpellParam.Side.LEFT);
            piece.setModeOption(PsitweaksModeOptions.VECTOR);
            helper.assertTrue(piece.params.get(LIST).canAccept(vectorListPiece())
                            && piece.paramSides.get(piece.params.get(LIST)) == SpellParam.Side.LEFT,
                    "Vector mode must take a Vector List and keep the wire: " + id);
            var tag = new CompoundTag();
            piece.writeToNBT(tag);
            var reloaded = (PieceOperatorModeListBase) SpellPiece.createFromNBT(new Spell(), tag);
            helper.assertTrue(reloaded.getModeOption().id().equals(PsitweaksModeOptions.VECTOR.id())
                            && reloaded.paramSides.get(reloaded.params.get(LIST)) == SpellParam.Side.LEFT,
                    "Vector mode or its wire lost on reload: " + id);
        }
        helper.succeed();
    }

    /** Any piece whose output is a Vector List. */
    private static SpellPiece vectorListPiece() {
        return PsiAPI.SPELL_PIECE_REGISTRY.get(Psitweaks.location("operator_entity_position_list")).create(new Spell());
    }
}
