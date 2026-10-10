package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickIdeaStorageDepositSlot;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickIdeaStorageSlotBase;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickIdeaStorageWithdrawSlot;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageService;
import com.moratan251.psitweaks.common.storage.idea.ItemResourceKey;
import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import com.moratan251.psitweaksqol.api.PsitweaksModeConfigurable;
import com.moratan251.psitweaksqol.api.PsitweaksModeOptions;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.common.item.ItemCAD;
import vazkii.psi.common.item.base.ModItems;

/** Backport of the 1.21.1 Ideaspace slot trick tests (source commit bdad494). */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class IdeaStorageSlotGameTests {
    private static final String NUMBER = SpellParam.GENERIC_NAME_NUMBER;

    /** A plain ServerPlayer: FakePlayer casters are deliberately ignored by the slot pieces. */
    static ServerPlayer caster(GameTestHelper helper, boolean spectator, boolean withCad) {
        var level = helper.getLevel();
        var server = level.getServer();
        var player = new ServerPlayer(server, level, new GameProfile(UUID.randomUUID(), "slot-logistics")) {
            @Override public boolean isSpectator() { return spectator; }
        };
        var connection = new Connection(PacketFlow.SERVERBOUND) {
            @Override public void send(Packet<?> packet) { }
        };
        player.connection = new ServerGamePacketListenerImpl(server, connection, player);
        if (withCad) giveCad(player);
        return player;
    }

    static void giveCad(Player player) {
        player.getInventory().setItem(0, ItemCAD.makeCAD(new ItemStack(ModItems.cadAssemblyPsimetal),
                new ItemStack(ModItems.cadSocketHuge)));
    }

    static <T extends PieceTrickIdeaStorageSlotBase> T piece(boolean deposit, double[] count, Object[] filter) {
        @SuppressWarnings("unchecked")
        T piece = (T) (deposit
                ? new PieceTrickIdeaStorageDepositSlot(new Spell()) {
                    @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                        return param.name.equals(NUMBER) ? count[0] : filter[0];
                    }
                }
                : new PieceTrickIdeaStorageWithdrawSlot(new Spell()) {
                    @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                        return param.name.equals(NUMBER) ? count[0] : filter[0];
                    }
                });
        return piece;
    }

    static long stored(PlayerIdeaStorage storage, ItemStack template) {
        return storage.simulateExtract(ItemResourceKey.of(template).orElseThrow(), Long.MAX_VALUE);
    }

    /** 1.20.1 has no item components; a display-name NBT tag makes the same distinct variant. */
    static ItemStack named(int count) {
        var stack = new ItemStack(Items.APPLE, count);
        stack.setHoverName(Component.literal("Slot variant"));
        return stack;
    }

    private static PieceTrickIdeaStorageSlotBase create(String action) throws Exception {
        return (PieceTrickIdeaStorageSlotBase) PsiAPI.getSpellPiece(Psitweaks.location("trick_idea_storage_" + action + "_slot"))
                .getConstructor(Spell.class).newInstance(new Spell());
    }

    @GameTest(template = "connector_empty")
    public static void slotPiecesRegisterWithNumberFilterAndModes(GameTestHelper helper) throws Exception {
        for (String action : new String[] {"deposit", "withdraw"}) {
            var piece = create(action);
            helper.assertTrue(piece.params.size() == 2 && piece.params.containsKey(NUMBER)
                            && piece.params.containsKey(PsitweaksSpellParams.STRING),
                    "Slot spell must take only Number and the filter");
            helper.assertTrue(!piece.params.get(NUMBER).canDisable && piece.params.get(PsitweaksSpellParams.STRING).canDisable,
                    "Number must be required and the filter optional");
            helper.assertTrue(piece instanceof PsitweaksModeConfigurable, "Slot spell must expose the item filter modes");
            var metadata = new SpellMetadata(); piece.addToMetadata(metadata);
            helper.assertTrue(metadata.getStat(EnumSpellStat.POTENCY) == 50 && metadata.getStat(EnumSpellStat.COST) == 100,
                    "Slot spell metadata differs from documented stats");
            piece.paramSides.put(piece.params.get(NUMBER), SpellParam.Side.RIGHT);
            piece.paramSides.put(piece.params.get(PsitweaksSpellParams.STRING), SpellParam.Side.LEFT);
            for (var mode : piece.getAvailableModeOptions()) {
                piece.setModeOption(mode);
                assertSides(helper, piece);
            }
            var tag = new CompoundTag(); piece.writeToNBT(tag);
            var reloaded = create(action);
            reloaded.readFromNBT(tag);
            helper.assertTrue(reloaded.getModeOption().equals(PsitweaksModeOptions.ITEM_STRICT), "NBT lost slot spell mode");
            assertSides(helper, reloaded);
        }
        helper.succeed();
    }

    private static void assertSides(GameTestHelper helper, PieceTrickIdeaStorageSlotBase piece) {
        String filter = piece.params.containsKey(PsitweaksSpellParams.ITEM) ? PsitweaksSpellParams.ITEM : PsitweaksSpellParams.STRING;
        helper.assertTrue(piece.params.size() == 2 && piece.paramSides.get(piece.params.get(NUMBER)) == SpellParam.Side.RIGHT
                && piece.paramSides.get(piece.params.get(filter)) == SpellParam.Side.LEFT, "Mode switch lost slot spell wiring");
    }

    @GameTest(template = "connector_empty")
    public static void depositMovesTargetSlotOnly(GameTestHelper helper) throws Exception {
        var player = caster(helper, false, true);
        var inventory = player.getInventory();
        var context = new SpellContext().setPlayer(player);
        var storage = IdeaStorageService.get(player.server, player.getUUID());
        double[] count = {32.9}; Object[] filter = {null};
        PieceTrickIdeaStorageSlotBase deposit = piece(true, count, filter);
        helper.assertTrue(context.getTargetSlot() == 1, "Default target slot must be right of the CAD");
        inventory.setItem(1, new ItemStack(Items.APPLE, 40));
        inventory.setItem(2, new ItemStack(Items.APPLE, 40));
        deposit.execute(context);
        helper.assertTrue(stored(storage, new ItemStack(Items.APPLE)) == 32 && inventory.getItem(1).getCount() == 8
                && inventory.getItem(2).getCount() == 40, "Deposit must take Number items from the target slot only");
        filter[0] = "stone"; count[0] = 100;
        deposit.execute(context);
        helper.assertTrue(inventory.getItem(1).getCount() == 8, "Non-matching filter must leave the slot alone");
        filter[0] = "app*";
        deposit.execute(context);
        helper.assertTrue(stored(storage, new ItemStack(Items.APPLE)) == 40 && inventory.getItem(1).isEmpty(),
                "Matching filter must deposit the rest of the slot");

        inventory.setItem(1, named(5));
        deposit.setModeOption(PsitweaksModeOptions.ITEM_STRICT); filter[0] = SpellItemValue.snapshot(new ItemStack(Items.APPLE));
        deposit.execute(context);
        helper.assertTrue(inventory.getItem(1).getCount() == 5, "Strict filter ignored NBT");
        deposit.setModeOption(PsitweaksModeOptions.ITEM);
        deposit.execute(context);
        helper.assertTrue(inventory.getItem(1).isEmpty() && stored(storage, named(1)) == 5, "Item filter must match the type only");

        inventory.setItem(1, new ItemStack(Items.APPLE, 3));
        for (double invalid : new double[] {0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            count[0] = invalid; boolean rejected = false;
            try { deposit.execute(context); } catch (SpellRuntimeException expected) { rejected = true; }
            helper.assertTrue(rejected, "Invalid item count accepted");
        }
        count[0] = 0.5; deposit.execute(context);
        helper.assertTrue(inventory.getItem(1).getCount() == 3, "Fractional count must round down to zero");
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void withdrawRefillsOrFillsTargetSlot(GameTestHelper helper) throws Exception {
        var player = caster(helper, false, true);
        var inventory = player.getInventory();
        var context = new SpellContext().setPlayer(player);
        var storage = IdeaStorageService.get(player.server, player.getUUID());
        storage.insert(new ItemStack(Items.APPLE), 200);
        storage.insert(named(1), 10);
        storage.insert(new ItemStack(Items.STONE), 5);
        double[] count = {64}; Object[] filter = {null};
        PieceTrickIdeaStorageSlotBase withdraw = piece(false, count, filter);

        long version = storage.getVersion();
        withdraw.execute(context);
        helper.assertTrue(inventory.getItem(1).isEmpty() && storage.getVersion() == version,
                "Empty slot without filter must do nothing");
        filter[0] = "minecraft:stone";
        withdraw.execute(context);
        helper.assertTrue(inventory.getItem(1).is(Items.STONE) && inventory.getItem(1).getCount() == 5
                && stored(storage, new ItemStack(Items.STONE)) == 0, "Empty slot must take the filtered type");

        inventory.setItem(1, new ItemStack(Items.APPLE, 60));
        filter[0] = null; count[0] = 100;
        withdraw.execute(context);
        helper.assertTrue(inventory.getItem(1).getCount() == 64 && stored(storage, new ItemStack(Items.APPLE)) == 196
                && stored(storage, named(1)) == 10, "Refill must stop at max stack and keep variants apart");

        inventory.setItem(1, named(1));
        count[0] = 3;
        withdraw.execute(context);
        helper.assertTrue(inventory.getItem(1).getCount() == 4 && stored(storage, named(1)) == 7
                && stored(storage, new ItemStack(Items.APPLE)) == 196, "Refill must use the exact NBT in the slot");
        filter[0] = "stone";
        withdraw.execute(context);
        helper.assertTrue(inventory.getItem(1).getCount() == 4, "Filter must be able to veto a refill");

        inventory.setItem(1, ItemStack.EMPTY);
        filter[0] = "*apple"; count[0] = 5;
        withdraw.execute(context);
        var taken = inventory.getItem(1);
        helper.assertTrue(taken.is(Items.APPLE) && taken.getCount() == 5
                        && stored(storage, new ItemStack(Items.APPLE)) + stored(storage, named(1)) == 196 + 7 - 5,
                "Empty slot must take one matching type up to Number");
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void targetSlotRulesProtectCasterEquipment(GameTestHelper helper) throws Exception {
        var player = caster(helper, false, true);
        var inventory = player.getInventory();
        var storage = IdeaStorageService.get(player.server, player.getUUID());
        double[] count = {64}; Object[] filter = {null};
        PieceTrickIdeaStorageSlotBase deposit = piece(true, count, filter);
        PieceTrickIdeaStorageSlotBase withdraw = piece(false, count, filter);

        var context = new SpellContext().setPlayer(player);
        context.customTargetSlot = true; context.targetSlot = 20;
        inventory.setItem(20, new ItemStack(Items.APPLE, 10));
        deposit.execute(context);
        helper.assertTrue(inventory.getItem(20).isEmpty() && stored(storage, new ItemStack(Items.APPLE)) == 10,
                "Custom inventory target slot ignored");
        // Psi 1.20.1 maps custom target slot 36 to the offhand (Inventory.SLOT_OFFHAND == 40), as on 1.21.1.
        context.targetSlot = 36;
        filter[0] = "apple";
        withdraw.execute(context);
        helper.assertTrue(inventory.getItem(Inventory.SLOT_OFFHAND).getCount() == 10 && stored(storage, new ItemStack(Items.APPLE)) == 0,
                "Offhand target slot ignored");

        ItemStack cad = inventory.getItem(0);
        context.targetSlot = 0; filter[0] = null;
        long version = storage.getVersion();
        deposit.execute(context);
        helper.assertTrue(inventory.getItem(0) == cad && cad.getCount() == 1 && storage.getVersion() == version,
                "CAD slot must not be deposited");
        storage.insert(cad, 1);
        version = storage.getVersion();
        withdraw.execute(context);
        helper.assertTrue(inventory.getItem(0) == cad && cad.getCount() == 1 && storage.getVersion() == version,
                "CAD slot must not be refilled");

        var toolContext = new SpellContext().setPlayer(player);
        var tool = new ItemStack(Items.IRON_PICKAXE);
        inventory.setItem(1, tool);
        toolContext.tool = tool;
        deposit.execute(toolContext);
        helper.assertTrue(inventory.getItem(1) == tool && !tool.isEmpty(), "Casting tool must not be deposited");

        var spectator = caster(helper, true, true);
        spectator.getInventory().setItem(1, new ItemStack(Items.APPLE, 4));
        deposit.execute(new SpellContext().setPlayer(spectator));
        helper.assertTrue(spectator.getInventory().getItem(1).getCount() == 4, "Spectator caster must do nothing");

        var fake = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "slot-fake"));
        giveCad(fake);
        fake.getInventory().setItem(1, new ItemStack(Items.APPLE, 4));
        deposit.execute(new SpellContext().setPlayer(fake));
        helper.assertTrue(fake.getInventory().getItem(1).getCount() == 4, "FakePlayer caster must do nothing");

        var noCad = caster(helper, false, false);
        noCad.getInventory().setItem(1, new ItemStack(Items.APPLE, 4));
        boolean failed = false;
        try { deposit.execute(new SpellContext().setPlayer(noCad)); } catch (SpellRuntimeException expected) { failed = true; }
        helper.assertTrue(failed && noCad.getInventory().getItem(1).getCount() == 4, "Casting without a CAD must fail");
        helper.succeed();
    }
}
