package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorGetId;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorGetIdList;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorListNameSearch;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorListNameSearchExclude;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorListSearch;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorListSearchExclude;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorTagList;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorDisplayName;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorDisplayNameList;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorEffectList;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorEntitySlotItem;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorHeldItem;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorHeldItems;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorNbtKeys;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorNbtValue;
import com.moratan251.psitweaks.common.spells.wrapper.StringListWrapper;
import com.moratan251.psitweaksqol.api.PsitweaksModeOptions;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.spell.ISpellImmune;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.wrapper.EntityListWrapper;

/**
 * Pieces that only read an entity (list filters, ID, tags, display name, NBT, effects, held items) must not stop on a
 * boss or a creature riding something, which Psi's {@code verifyEntity} reports as immune. Psi's own reading operators
 * (Entity Health, Entity Position) do not stop on them either.
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ReadOnlyTargetGameTests {
    private static final String TEMPLATE = "connector_empty";
    private static final String BATCH = "psitweaks_read_only_targets";
    private static final String TARGET = SpellParam.GENERIC_NAME_TARGET;
    private static final String LIST = SpellParam.GENERIC_NAME_LIST;
    private static final String STRING = PsitweaksSpellParams.STRING;
    private static final String PSI_TARGET = "psi.spellparam.target";
    private static final String PSI_NUMBER = "psi.spellparam.number";

    private ReadOnlyTargetGameTests() {
    }

    /** Runs the piece and fails the test with Psi's error key if it stops. */
    private static Object read(String what, SpellPiece piece, SpellContext context) {
        try {
            return piece.execute(context);
        } catch (SpellRuntimeException e) {
            throw new GameTestAssertException(what + " must not stop on a boss or a rider: " + e.getMessage());
        }
    }

    private static Set<Entity> entities(Object result) {
        Set<Entity> set = new HashSet<>();
        for (Entity entity : (EntityListWrapper) result) {
            set.add(entity);
        }
        return set;
    }

    @GameTest(template = TEMPLATE, batch = BATCH)
    public static void readingPiecesAcceptBossesAndRiders(GameTestHelper helper) {
        var wither = helper.spawnWithNoFreeWill(EntityType.WITHER, new BlockPos(2, 0, 2));
        var mount = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(4, 0, 4));
        var rider = helper.spawnWithNoFreeWill(EntityType.HUSK, new BlockPos(4, 0, 4));
        var stand = helper.spawn(EntityType.ARMOR_STAND, new BlockPos(0, 0, 4));
        try {
            rider.startRiding(mount, true);
            wither.setCustomName(Component.literal("Boss Wither"));
            rider.setCustomName(Component.literal("Rider"));
            stand.setCustomName(Component.literal("Stand"));
            rider.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            rider.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200));
            helper.assertTrue(ISpellImmune.isImmune(wither) && ISpellImmune.isImmune(rider),
                    "Psi itself must treat the Wither and the rider as immune, or this test checks nothing");

            var context = new SpellContext();
            Map<String, Object> in = new HashMap<>();
            in.put(LIST, EntityListWrapper.make(List.<Entity>of(wither, rider, stand)));

            // Operator: List Search / List Search Exclude (registry ID)
            var search = new PieceOperatorListSearch(new Spell()) {
                @Override public Object getRawParamValue(SpellContext c, SpellParam<?> p) { return in.get(p.name); }
            };
            var searchExclude = new PieceOperatorListSearchExclude(new Spell()) {
                @Override public Object getRawParamValue(SpellContext c, SpellParam<?> p) { return in.get(p.name); }
            };
            search.setModeOption(PsitweaksModeOptions.ENTITY);
            searchExclude.setModeOption(PsitweaksModeOptions.ENTITY);
            in.put(STRING, "wither");
            helper.assertTrue(entities(read("List Search", search, context)).equals(Set.of(wither)),
                    "List Search must keep the Wither by its ID");
            helper.assertTrue(entities(read("List Search Exclude", searchExclude, context)).equals(Set.of(rider, stand)),
                    "List Search Exclude must leave the rider and the stand");

            // Operator: List Name Search / List Name Search Exclude (display name)
            var nameSearch = new PieceOperatorListNameSearch(new Spell()) {
                @Override public Object getRawParamValue(SpellContext c, SpellParam<?> p) { return in.get(p.name); }
            };
            var nameExclude = new PieceOperatorListNameSearchExclude(new Spell()) {
                @Override public Object getRawParamValue(SpellContext c, SpellParam<?> p) { return in.get(p.name); }
            };
            nameSearch.setModeOption(PsitweaksModeOptions.ENTITY);
            nameExclude.setModeOption(PsitweaksModeOptions.ENTITY);
            in.put(STRING, "Boss*");
            helper.assertTrue(entities(read("List Name Search", nameSearch, context)).equals(Set.of(wither)),
                    "List Name Search must keep the Wither by its name");
            in.put(STRING, "Rider");
            helper.assertTrue(entities(read("List Name Search Exclude", nameExclude, context)).equals(Set.of(wither, stand)),
                    "List Name Search Exclude must remove only the rider");

            // Operator: Get ID / Get ID List / Tag List
            in.put(TARGET, wither);
            var getId = new PieceOperatorGetId(new Spell()) {
                @Override public Object getRawParamValue(SpellContext c, SpellParam<?> p) { return in.get(p.name); }
            };
            helper.assertTrue("minecraft:wither".equals(read("Get ID", getId, context)), "Get ID must read the Wither");
            var getIdList = new PieceOperatorGetIdList(new Spell()) {
                @Override public Object getRawParamValue(SpellContext c, SpellParam<?> p) { return in.get(p.name); }
            };
            helper.assertTrue(Set.copyOf(((StringListWrapper) read("Get ID List", getIdList, context)).asList())
                            .equals(Set.of("minecraft:wither", "minecraft:husk", "minecraft:armor_stand")),
                    "Get ID List must read every entity");
            var tags = new PieceOperatorTagList(new Spell()) {
                @Override public Object getRawParamValue(SpellContext c, SpellParam<?> p) { return in.get(p.name); }
            };
            helper.assertTrue(((StringListWrapper) read("Tag List", tags, context)).asList().contains("c:bosses"),
                    "Tag List must read the Wither's tags");

            // Selector: Display Name / Display Name List
            var displayName = new PieceSelectorDisplayName(new Spell()) {
                @Override public Object getRawParamValue(SpellContext c, SpellParam<?> p) { return in.get(p.name); }
            };
            helper.assertTrue("Boss Wither".equals(read("Display Name", displayName, context)),
                    "Display Name must read the Wither");
            in.put(TARGET, in.get(LIST));
            var displayNames = new PieceSelectorDisplayNameList(new Spell()) {
                @Override public Object getRawParamValue(SpellContext c, SpellParam<?> p) { return in.get(p.name); }
            };
            helper.assertTrue(Set.copyOf(((StringListWrapper) read("Display Name List", displayNames, context)).asList())
                            .equals(Set.of("Boss Wither", "Rider", "Stand")),
                    "Display Name List must read every entity");

            // Selector: NBT Keys / NBT Value
            in.put(TARGET, wither);
            var nbtKeys = new PieceSelectorNbtKeys(new Spell()) {
                @Override public Object getRawParamValue(SpellContext c, SpellParam<?> p) { return in.get(p.name); }
            };
            helper.assertTrue(((StringListWrapper) read("NBT Keys", nbtKeys, context)).asList().contains("Health"),
                    "NBT Keys must read the Wither");
            in.put(TARGET, rider);
            in.put(STRING, "Health");
            var nbtValue = new PieceSelectorNbtValue(new Spell()) {
                @Override public Object getRawParamValue(SpellContext c, SpellParam<?> p) { return in.get(p.name); }
            };
            helper.assertTrue(!((String) read("NBT Value", nbtValue, context)).isEmpty(), "NBT Value must read the rider");

            // Selector: Effect List / Held Item / Held Items / Entity Slot Item
            in.put(PSI_TARGET, rider);
            in.put(PSI_NUMBER, 0.0);
            var effects = new PieceSelectorEffectList(new Spell()) {
                @Override public Object getRawParamValue(SpellContext c, SpellParam<?> p) { return in.get(p.name); }
            };
            helper.assertTrue(((StringListWrapper) read("Effect List", effects, context)).asList()
                            .contains("minecraft:slowness"),
                    "Effect List must read the rider's effects");
            var held = new PieceSelectorHeldItem(new Spell()) {
                @Override public Object getRawParamValue(SpellContext c, SpellParam<?> p) { return in.get(p.name); }
            };
            helper.assertTrue(((SpellItemValue) read("Held Item", held, context)).snapshot().is(Items.IRON_SWORD),
                    "Held Item must read the rider's sword");
            var heldItems = new PieceSelectorHeldItems(new Spell()) {
                @Override public Object getRawParamValue(SpellContext c, SpellParam<?> p) { return in.get(p.name); }
            };
            read("Held Items", heldItems, context);
            var slotItem = new PieceSelectorEntitySlotItem(new Spell()) {
                @Override public Object getRawParamValue(SpellContext c, SpellParam<?> p) { return in.get(p.name); }
            };
            read("Entity Slot Item", slotItem, context);
            in.put(PSI_TARGET, wither);
            read("Effect List on the Wither", effects, context);
        } finally {
            wither.discard();
        }
        helper.succeed();
    }
}
