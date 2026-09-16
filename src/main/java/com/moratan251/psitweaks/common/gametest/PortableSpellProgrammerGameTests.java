package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.items.ItemPortableSpellProgrammer;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import com.moratan251.psitweaks.common.menu.PortableSpellProgrammerMenu;
import com.moratan251.psitweaks.common.network.MessagePortableSpellProgrammerEdit;
import io.netty.buffer.Unpooled;
import java.nio.file.Files;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.cad.ISocketable;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.common.item.ItemCAD;
import vazkii.psi.common.item.ItemSpellDrive;
import vazkii.psi.common.item.base.ModItems;
import vazkii.psi.common.spell.SpellCompiler;

@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PortableSpellProgrammerGameTests {
    @GameTest(template = "psi110_empty")
    public static void failedRegistrationExplainsReasonInActionBar(GameTestHelper helper) {
        var player = new FeedbackPlayer(helper.getLevel());
        player.setGameMode(GameType.CREATIVE);
        player.setShiftKeyDown(true);
        var programmer = programmer("Feedback");
        player.setItemInHand(InteractionHand.MAIN_HAND, programmer);
        var food = new ItemStack(Items.APPLE, 3);
        player.setItemInHand(InteractionHand.OFF_HAND, food);
        var result = programmer.getItem().use(player.level(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(result.getResult().consumesAction() && food.getCount() == 3 && !player.isUsingItem(),
                "Failure must still consume the interaction and not use the offhand");
        player.assertFeedback(helper, "no_target");

        var bullet = new ItemStack(ModItems.spellBullet.get());
        player.setItemInHand(InteractionHand.OFF_HAND, bullet);
        ItemPortableSpellProgrammer.saveSpell(programmer, new Spell());
        helper.assertTrue(!ItemPortableSpellProgrammer.registerOffhandSpell(player, programmer), "Empty spell was registered");
        player.assertFeedback(helper, "empty_spell");
        ItemPortableSpellProgrammer.saveSpell(programmer, spell(""));
        helper.assertTrue(!ItemPortableSpellProgrammer.registerOffhandSpell(player, programmer), "Invalid spell was registered");
        player.assertFeedback(helper, "compile_failed");
        helper.assertTrue(ItemSpellDrive.getSpell(bullet) == null, "Failed registration changed the bullet");

        player.lastMessage = null;
        ItemPortableSpellProgrammer.saveSpell(programmer, spell("Valid"));
        helper.assertTrue(ItemPortableSpellProgrammer.registerOffhandSpell(player, programmer) && player.lastMessage == null,
                "Successful registration reported an error");
        helper.succeed();
    }

    private static final class FeedbackPlayer extends FakePlayer {
        private Component lastMessage;
        private boolean actionBar;

        private FeedbackPlayer(ServerLevel level) {
            super(level, new GameProfile(UUID.randomUUID(), "programmer-feedback"));
        }

        @Override
        public void displayClientMessage(Component message, boolean actionBar) {
            this.lastMessage = message;
            this.actionBar = actionBar;
        }

        private void assertFeedback(GameTestHelper helper, String reason) {
            helper.assertTrue(actionBar && Component.translatable("message.psitweaks.portable_spell_programmer." + reason)
                    .equals(lastMessage), "Missing action-bar reason: " + reason);
        }
    }

    @GameTest(template = "psi110_empty")
    public static void itemCopiesAndDiskReloadRetainIndependentSpells(GameTestHelper helper) throws Exception {
        var first = programmer("First");
        var second = first.copy();
        var edit = ItemPortableSpellProgrammer.getSpellCopy(second);
        edit.name = "Second";
        ItemPortableSpellProgrammer.saveSpell(second, edit);
        edit.name = "Unsaved mutation";
        helper.assertTrue(ItemPortableSpellProgrammer.getSpellCopy(first).name.equals("First"), "Editing a copy changed the original");
        helper.assertTrue(ItemPortableSpellProgrammer.getSpellCopy(second).name.equals("Second"), "Save retained a mutable editor reference");
        var registries = helper.getLevel().registryAccess();
        var file = Files.createTempFile("psitweaks-portable-programmer-", ".dat");
        try {
            CompoundTag root = new CompoundTag();
            root.put("Item", second.save(registries));
            NbtIo.writeCompressed(root, file);
            var restored = ItemStack.parse(registries,
                    NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap()).getCompound("Item")).orElseThrow();
            var recipient = player(helper, "recipient");
            recipient.setItemInHand(InteractionHand.MAIN_HAND, restored);
            Spell loaded = ItemPortableSpellProgrammer.getSpellCopy(recipient.getMainHandItem());
            helper.assertTrue(loaded.name.equals("Second") && loaded.grid.gridData[4][4] != null
                    && loaded.grid.gridData[4][4].comment.equals("Portable persistence"), "Disk reload or transfer lost spell data");
            helper.assertTrue(new SpellCompiler().compile(loaded, registries, recipient).left().isPresent(), "Reloaded spell cannot compile");
        } finally {
            Files.deleteIfExists(file);
        }
        helper.succeed();
    }

    @GameTest(template = "psi110_empty")
    public static void editingRequiresCurrentMenuSessionAndOriginalHeldItem(GameTestHelper helper) {
        var player = player(helper, "editing");
        var original = programmer("Original");
        player.setItemInHand(InteractionHand.MAIN_HAND, original);
        var session = UUID.randomUUID();
        var menu = new PortableSpellProgrammerMenu(7, player.getInventory(), player.getInventory().selected, session,
                ItemPortableSpellProgrammer.getSpellCopy(original));
        player.containerMenu = menu;
        var edit = new MessagePortableSpellProgrammerEdit(7, session, spell("Edited"));
        helper.assertTrue(menu.handleEdit(player, edit), "Valid session rejected");
        helper.assertTrue(ItemPortableSpellProgrammer.getSpellCopy(original).name.equals("Edited"), "Edit not saved immediately");
        helper.assertTrue(!menu.handleEdit(player, new MessagePortableSpellProgrammerEdit(8, session, spell("Wrong menu"))), "Wrong menu ID accepted");
        helper.assertTrue(!menu.handleEdit(player, new MessagePortableSpellProgrammerEdit(7, UUID.randomUUID(), spell("Stale"))), "Wrong session accepted");
        helper.assertTrue(!menu.handleEdit(player(helper, "outsider"), edit), "Another player could edit");
        player.getInventory().selected = 1;
        helper.assertTrue(!menu.handleEdit(player, edit), "Changed hotbar selection still edits old item");
        player.getInventory().selected = 0;
        var replacement = original.copy();
        player.setItemInHand(InteractionHand.MAIN_HAND, replacement);
        helper.assertTrue(!menu.handleEdit(player, edit), "Identical replacement accepted an old edit");
        player.setItemInHand(InteractionHand.MAIN_HAND, original);
        player.containerMenu = player.inventoryMenu;
        helper.assertTrue(!menu.handleEdit(player, edit), "Closed menu accepted an edit");
        var reopened = new PortableSpellProgrammerMenu(7, player.getInventory(), 0, UUID.randomUUID(), spell("Edited"));
        player.containerMenu = reopened;
        helper.assertTrue(!reopened.handleEdit(player, edit), "Previous session edited a reopened menu");
        helper.assertTrue(reopened.handleEdit(player, new MessagePortableSpellProgrammerEdit(7, reopened.session(), new Spell())),
                "Incomplete/empty edits must be saved");
        helper.assertTrue(ItemPortableSpellProgrammer.getSpellCopy(original).grid.isEmpty(), "Clear was not persisted");
        player.containerMenu = player.inventoryMenu;
        helper.succeed();
    }

    @GameTest(template = "psi110_empty")
    public static void offhandSingleAndStackedBulletsUsePsiRegistration(GameTestHelper helper) {
        var player = player(helper, "bullets");
        var programmer = programmer("Registered");
        var bullet = new ItemStack(ModItems.spellBullet.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, programmer);
        player.setItemInHand(InteractionHand.OFF_HAND, bullet);
        helper.assertTrue(ItemPortableSpellProgrammer.registerOffhandSpell(player, programmer), "Single bullet registration failed");
        helper.assertTrue(ItemSpellDrive.getSpell(bullet).name.equals("Registered"), "Bullet did not receive the spell");
        var previousUuid = ItemSpellDrive.getSpell(bullet).uuid;
        ItemPortableSpellProgrammer.registerOffhandSpell(player, programmer);
        helper.assertTrue(!ItemSpellDrive.getSpell(bullet).uuid.equals(previousUuid), "Registration did not renew Psi spell identity");

        var stacked = new ItemStack(ModItems.spellBullet.get(), 3);
        player.setItemInHand(InteractionHand.OFF_HAND, stacked);
        helper.assertTrue(ItemPortableSpellProgrammer.registerOffhandSpell(player, programmer), "Stacked registration failed");
        helper.assertTrue(stacked.getCount() == 2 && ItemSpellDrive.getSpell(stacked) == null, "Whole stack was written or consumed");
        int written = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.spellBullet.get()) && ItemSpellDrive.getSpell(stack) != null) {
                written += stack.getCount();
            }
        }
        helper.assertTrue(written == 1, "Psi should split exactly one programmed bullet into inventory");
        helper.succeed();
    }

    @GameTest(template = "psi110_empty")
    public static void cadOnlyWritesSelectedBulletAndInvalidSpellsDoNotOverwrite(GameTestHelper helper) {
        var player = player(helper, "cad");
        var programmer = programmer("CAD spell");
        var cad = ItemCAD.makeCAD(player.registryAccess(), new ItemStack(ModItems.cadAssemblyPsimetal.get()),
                new ItemStack(ModItems.cadSocketHuge.get()));
        var sockets = ISocketable.socketable(cad);
        sockets.setBulletInSocket(0, new ItemStack(ModItems.spellBullet.get()));
        sockets.setBulletInSocket(1, new ItemStack(ModItems.spellBullet.get()));
        sockets.setSelectedSlot(1);
        player.setItemInHand(InteractionHand.MAIN_HAND, programmer);
        player.setItemInHand(InteractionHand.OFF_HAND, cad);
        helper.assertTrue(ItemPortableSpellProgrammer.registerOffhandSpell(player, programmer), "CAD registration failed");
        helper.assertTrue(ItemSpellDrive.getSpell(sockets.getBulletInSocket(0)) == null, "Unselected bullet was changed");
        helper.assertTrue(ItemSpellDrive.getSpell(sockets.getBulletInSocket(1)).name.equals("CAD spell"), "Selected bullet was not written");
        ItemStack expected = cad.copy();
        ItemPortableSpellProgrammer.saveSpell(programmer, spell(""));
        helper.assertTrue(!ItemPortableSpellProgrammer.registerOffhandSpell(player, programmer) && ItemStack.matches(cad, expected),
                "Compilation error overwrote a CAD bullet");
        ItemPortableSpellProgrammer.saveSpell(programmer, spell("Valid"));
        player.setShiftKeyDown(false);
        helper.assertTrue(!ItemPortableSpellProgrammer.registerOffhandSpell(player, programmer), "Non-sneaking registration accepted");
        player.setShiftKeyDown(true);
        var food = new ItemStack(Items.APPLE, 3);
        player.setItemInHand(InteractionHand.OFF_HAND, food);
        var result = programmer.getItem().use(player.level(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(result.getResult().consumesAction() && food.getCount() == 3 && !player.isUsingItem(),
                "Failed registration let the offhand item be used");
        helper.succeed();
    }

    @GameTest(template = "psi110_empty")
    public static void openingAndEditingPayloadsRetainSpellConfiguration(GameTestHelper helper) {
        var player = player(helper, "codec");
        var held = programmer("Network");
        player.setItemInHand(InteractionHand.OFF_HAND, held);
        UUID session = UUID.randomUUID();
        var buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.registryAccess());
        try {
            buf.writeVarInt(40);
            buf.writeUUID(session);
            Spell.STREAM_CODEC.encode(buf, ItemPortableSpellProgrammer.getSpellCopy(held));
            var menu = PortableSpellProgrammerMenu.fromNetwork(9, player.getInventory(), buf);
            helper.assertTrue(menu.stillValid(player) && menu.session().equals(session), "Offhand menu lost its identity");
            helper.assertTrue(menu.initialSpell().grid.gridData[4][4].comment.equals("Portable persistence"), "Opening lost piece comments");
            buf.clear();
            var message = new MessagePortableSpellProgrammerEdit(9, session, menu.initialSpell());
            MessagePortableSpellProgrammerEdit.STREAM_CODEC.encode(buf, message);
            var decoded = MessagePortableSpellProgrammerEdit.STREAM_CODEC.decode(buf);
            helper.assertTrue(decoded.containerId() == 9 && decoded.session().equals(session) && decoded.spell().equals(message.spell()),
                    "Edit payload round trip changed the spell");
            helper.assertTrue(new SpellCompiler().compile(decoded.spell(), player.registryAccess(), player).left().isPresent(),
                    "Network transport lost parameter directions");
        } finally {
            buf.release();
        }
        helper.succeed();
    }

    private static FakePlayer player(GameTestHelper helper, String name) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), name));
        player.setGameMode(GameType.CREATIVE);
        player.setShiftKeyDown(true);
        return player;
    }

    private static ItemStack programmer(String name) {
        var item = new ItemStack(PsitweaksItems.PORTABLE_SPELL_PROGRAMMER.get());
        ItemPortableSpellProgrammer.saveSpell(item, spell(name));
        return item;
    }

    private static Spell spell(String name) {
        var spell = new Spell();
        spell.name = name;
        var trick = PsiAPI.SPELL_PIECE_REGISTRY.get(PsiAPI.location("trick_die")).create(spell);
        var number = PsiAPI.SPELL_PIECE_REGISTRY.get(PsiAPI.location("constant_number")).create(spell);
        trick.x = 4;
        trick.y = 4;
        trick.isInGrid = true;
        trick.comment = "Portable persistence";
        number.x = 5;
        number.y = 4;
        number.isInGrid = true;
        trick.paramSides.replaceAll((param, side) -> SpellParam.Side.RIGHT);
        spell.grid.gridData[4][4] = trick;
        spell.grid.gridData[5][4] = number;
        return spell;
    }
}
