package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorListValueBase;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorListValueElement;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorListValueKeep;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorListValueRange;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorNbtNumberList;
import com.moratan251.psitweaks.common.spells.wrapper.NumberListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.SpellItemListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.StringListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.VectorListWrapper;
import com.moratan251.psitweaksqol.api.PsitweaksModeOptions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
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

/**
 * Selector: NBT Number List lines its numbers up with the List, and the value-ranked list operators (range, keep,
 * element) rank or filter a List by such a Number List.
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ListValueGameTests {
    private static final String TEMPLATE = "connector_empty";
    private static final String LIST = SpellParam.GENERIC_NAME_LIST;
    private static final String VALUES = PsitweaksSpellParams.VALUES;
    private static final String NUMBER = SpellParam.GENERIC_NAME_NUMBER;
    private static final String MIN = SpellParam.GENERIC_NAME_MIN;
    private static final String MAX = SpellParam.GENERIC_NAME_MAX;
    private static final String PATH = PsitweaksSpellParams.NBT_PATH;
    private static final String FALLBACK = PsitweaksSpellParams.FALLBACK;

    private ListValueGameTests() {
    }

    private static NumberListWrapper numbers(Double... values) {
        return NumberListWrapper.make(List.of(values));
    }

    private static PieceSelectorNbtNumberList numberList(Map<String, Object> inputs) {
        return new PieceSelectorNbtNumberList(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return inputs.get(param.name);
            }
        };
    }

    private static PieceOperatorListValueBase operator(String kind, Map<String, Object> inputs) {
        return switch (kind) {
            case "range" -> new PieceOperatorListValueRange(new Spell()) {
                @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                    return inputs.get(param.name);
                }
            };
            case "keep" -> new PieceOperatorListValueKeep(new Spell()) {
                @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                    return inputs.get(param.name);
                }
            };
            default -> new PieceOperatorListValueElement(new Spell()) {
                @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                    return inputs.get(param.name);
                }
            };
        };
    }

    private static List<String> strings(Object result) {
        return ((StringListWrapper) result).asList();
    }

    private static List<Double> xs(Object result) {
        List<Double> xs = new ArrayList<>();
        for (Vector3 vector : (VectorListWrapper) result) {
            xs.add(vector.x);
        }
        return xs;
    }

    private static boolean failsWith(PieceOperatorListValueBase piece, SpellContext context, String error) {
        try {
            piece.execute(context);
            return false;
        } catch (SpellRuntimeException e) {
            return error.equals(e.getMessage());
        }
    }

    private static List<Entity> entities(Object result) {
        List<Entity> entities = new ArrayList<>();
        for (Entity entity : (EntityListWrapper) result) {
            entities.add(entity);
        }
        return entities;
    }

    private static ArmorStand stand(GameTestHelper helper, float health) {
        var stand = new ArmorStand(helper.getLevel(), 0, 0, 0);
        stand.setHealth(health);
        return stand;
    }

    @GameTest(template = TEMPLATE)
    public static void nbtNumberListLinesUpWithTheList(GameTestHelper helper) throws SpellRuntimeException {
        var context = new SpellContext();
        context.focalPoint = new ArmorStand(helper.getLevel(), 0, 0, 0);
        var weak = stand(helper, 5.0F);
        var strong = stand(helper, 15.0F);
        var middle = stand(helper, 10.0F);
        var list = EntityListWrapper.make(List.<Entity>of(strong, weak, middle));
        Map<String, Object> inputs = new HashMap<>();
        inputs.put(LIST, list);
        inputs.put(PATH, "Health");
        var selector = numberList(inputs);
        helper.assertTrue(selector.getModeOption().id().equals(PsitweaksModeOptions.ENTITY.id()), "NBT Number List must start in Entity mode");

        List<Double> expected = new ArrayList<>();
        for (Entity entity : list) {
            expected.add((double) ((ArmorStand) entity).getHealth());
        }
        var healths = (NumberListWrapper) selector.execute(context);
        helper.assertTrue(healths.asList().equals(expected), "The numbers must follow the List's order: " + healths.asList());
        inputs.put(PATH, "Missing");
        helper.assertTrue(((NumberListWrapper) selector.execute(context)).asList().equals(List.of(0.0, 0.0, 0.0)),
                "A missing path must give 0");
        inputs.put(FALLBACK, 7.0);
        helper.assertTrue(((NumberListWrapper) selector.execute(context)).asList().equals(List.of(7.0, 7.0, 7.0)),
                "A missing path must give the Fallback");

        Map<String, Object> keepInputs = new HashMap<>();
        keepInputs.put(LIST, list);
        keepInputs.put(VALUES, healths);
        keepInputs.put(NUMBER, -1.0);
        var keep = operator("keep", keepInputs);
        keep.setModeOption(PsitweaksModeOptions.ENTITY);
        helper.assertTrue(entities(keep.execute(context)).equals(List.of(strong)),
                "-1 must keep the entity with the most health");
        var element = operator("element", keepInputs);
        element.setModeOption(PsitweaksModeOptions.ENTITY);
        keepInputs.put(NUMBER, 0.0);
        helper.assertTrue(element.execute(context) == weak, "Index 0 must be the entity with the least health");

        ItemStack low = new ItemStack(Items.STONE);
        low.set(DataComponents.CUSTOM_DATA, CustomData.of(level(2)));
        ItemStack high = new ItemStack(Items.DIRT);
        high.set(DataComponents.CUSTOM_DATA, CustomData.of(level(9)));
        inputs.remove(FALLBACK);
        inputs.put(LIST, SpellItemListWrapper.make(List.of(SpellItemValue.snapshot(high), SpellItemValue.snapshot(new ItemStack(Items.APPLE)),
                SpellItemValue.snapshot(low))));
        inputs.put(PATH, "components.minecraft:custom_data.level");
        selector.setModeOption(PsitweaksModeOptions.ITEM);
        helper.assertTrue(((NumberListWrapper) selector.execute(context)).asList().equals(List.of(9.0, 0.0, 2.0)),
                "Item mode must read each item in the List's order");
        helper.succeed();
    }

    private static CompoundTag level(int value) {
        var tag = new CompoundTag();
        tag.putByte("level", (byte) value);
        return tag;
    }

    @GameTest(template = TEMPLATE)
    public static void valueRangeKeepsTheOrderAndOpenBounds(GameTestHelper helper) throws SpellRuntimeException {
        var context = new SpellContext();
        Map<String, Object> inputs = new HashMap<>();
        inputs.put(LIST, StringListWrapper.make(List.of("a", "b", "c", "d")));
        inputs.put(VALUES, numbers(1.0, 5.0, 3.0, 9.0));
        var range = operator("range", inputs);
        helper.assertTrue(range.getModeOption().id().equals(PsitweaksModeOptions.STRING.id()), "The value operators must start in String mode");

        helper.assertTrue(strings(range.execute(context)).equals(List.of("a", "b", "c", "d")), "No bounds must keep everything");
        inputs.put(MIN, 3.0);
        inputs.put(MAX, 5.0);
        helper.assertTrue(strings(range.execute(context)).equals(List.of("b", "c")), "Both bounds must be included, in List order");
        inputs.remove(MAX);
        helper.assertTrue(strings(range.execute(context)).equals(List.of("b", "c", "d")), "An omitted Max must be open");
        inputs.remove(MIN);
        inputs.put(MAX, 3.0);
        helper.assertTrue(strings(range.execute(context)).equals(List.of("a", "c")), "An omitted Min must be open");
        inputs.put(VALUES, numbers(1.0, 2.0));
        helper.assertTrue(failsWith(range, context, PieceOperatorListValueBase.ERROR_SIZE_MISMATCH), "Different sizes must be an error");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void valueKeepAndElementRankLikeDistance(GameTestHelper helper) throws SpellRuntimeException {
        var context = new SpellContext();
        Map<String, Object> inputs = new HashMap<>();
        inputs.put(LIST, VectorListWrapper.make(List.of(new Vector3(10, 0, 0), new Vector3(20, 0, 0), new Vector3(30, 0, 0),
                new Vector3(40, 0, 0))));
        inputs.put(VALUES, numbers(3.0, 1.0, 4.0, 2.0));
        inputs.put(NUMBER, 2.0);
        var keep = operator("keep", inputs);
        var element = operator("element", inputs);
        keep.setModeOption(PsitweaksModeOptions.VECTOR);
        element.setModeOption(PsitweaksModeOptions.VECTOR);
        helper.assertTrue(keep.getEvaluationType() == VectorListWrapper.class && element.getEvaluationType() == Vector3.class,
                "Vector mode must output a Vector List and a Vector");

        helper.assertTrue(xs(keep.execute(context)).equals(List.of(20.0, 40.0)), "2 must keep the two smallest, smallest first");
        inputs.put(NUMBER, -2.0);
        helper.assertTrue(xs(keep.execute(context)).equals(List.of(30.0, 10.0)), "-2 must keep the two largest, largest first");
        inputs.put(NUMBER, 2.9);
        helper.assertTrue(xs(keep.execute(context)).equals(List.of(20.0, 40.0)), "2.9 must count as 2");
        inputs.put(NUMBER, 0.0);
        helper.assertTrue(xs(keep.execute(context)).isEmpty(), "0 must give an empty List");
        inputs.put(NUMBER, 10.0);
        helper.assertTrue(xs(keep.execute(context)).equals(List.of(20.0, 40.0, 10.0, 30.0)), "Over the size must keep all, sorted");
        inputs.put(NUMBER, -10.0);
        helper.assertTrue(xs(keep.execute(context)).equals(List.of(30.0, 10.0, 40.0, 20.0)), "Under minus the size must keep all, largest first");

        inputs.put(NUMBER, 0.0);
        helper.assertTrue(((Vector3) element.execute(context)).x == 20.0, "Index 0 must have the smallest value");
        inputs.put(NUMBER, -1.0);
        helper.assertTrue(((Vector3) element.execute(context)).x == 30.0, "Index -1 must have the largest value");
        inputs.put(NUMBER, 4.0);
        helper.assertTrue(failsWith(element, context, "psi.spellerror.out_of_bounds"), "Index 4 of 4 must be out of bounds");
        inputs.put(NUMBER, -5.0);
        helper.assertTrue(failsWith(element, context, "psi.spellerror.out_of_bounds"), "Index -5 of 4 must be out of bounds");

        inputs.put(VALUES, numbers(2.0, 1.0, 2.0, 1.0));
        inputs.put(NUMBER, 4.0);
        helper.assertTrue(xs(keep.execute(context)).equals(List.of(20.0, 40.0, 10.0, 30.0)), "Equal values must keep the List's order");
        inputs.put(VALUES, numbers(1.0, 2.0, 3.0));
        helper.assertTrue(failsWith(keep, context, PieceOperatorListValueBase.ERROR_SIZE_MISMATCH)
                        && failsWith(element, context, PieceOperatorListValueBase.ERROR_SIZE_MISMATCH),
                "Different sizes must be an error");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void valuePiecesAreRegisteredAndKeepTheirMode(GameTestHelper helper) {
        for (String id : List.of("operator_list_value_range", "operator_list_value_keep", "operator_list_value_element")) {
            var type = PsiAPI.SPELL_PIECE_REGISTRY.get(Psitweaks.location(id));
            helper.assertTrue(type != null, "Not registered: " + id);
            var piece = (PieceOperatorListValueBase) type.create(new Spell());
            piece.setModeOption(PsitweaksModeOptions.VECTOR);
            piece.paramSides.put(piece.params.get(VALUES), SpellParam.Side.LEFT);
            var tag = new CompoundTag();
            piece.writeToNBT(tag);
            var reloaded = (PieceOperatorListValueBase) SpellPiece.createFromNBT(new Spell(), tag);
            helper.assertTrue(reloaded.getModeOption().id().equals(PsitweaksModeOptions.VECTOR.id())
                            && reloaded.paramSides.get(reloaded.params.get(VALUES)) == SpellParam.Side.LEFT,
                    "Vector mode or the Values wire lost on reload: " + id);
        }
        var type = PsiAPI.SPELL_PIECE_REGISTRY.get(Psitweaks.location("selector_nbt_number_list"));
        helper.assertTrue(type != null, "Not registered: selector_nbt_number_list");
        var selector = (PieceSelectorNbtNumberList) type.create(new Spell());
        selector.setModeOption(PsitweaksModeOptions.BLOCK);
        selector.paramSides.put(selector.params.get(PATH), SpellParam.Side.TOP);
        var tag = new CompoundTag();
        selector.writeToNBT(tag);
        var reloaded = (PieceSelectorNbtNumberList) SpellPiece.createFromNBT(new Spell(), tag);
        helper.assertTrue(reloaded.getModeOption().id().equals(PsitweaksModeOptions.BLOCK.id())
                        && reloaded.paramSides.get(reloaded.params.get(PATH)) == SpellParam.Side.TOP,
                "Block mode or the path wire lost on reload");
        helper.succeed();
    }
}
