package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.handler.SpellUnlockHandler;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorIdeaStorageEnergy;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageService;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.common.core.handler.PlayerDataHandler;

@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class IdeaStorageEnergySelectorGameTests {
    @GameTest(template = "psi110_empty")
    public static void readsCurrentOwnerBalanceWithoutConsuming(GameTestHelper helper) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "energy-reader"));
        var storage = IdeaStorageService.get(player.server, player.getUUID());
        var piece = new PieceSelectorIdeaStorageEnergy(new Spell());
        var context = new SpellContext().setPlayer(player);
        helper.assertTrue(piece.execute(context).equals(0.0D), "Empty storage must return Number zero");
        long inserted = storage.insertEnergy(5_000_000_123L, false);
        long version = storage.getVersion();
        helper.assertTrue(piece.execute(context).equals((double) inserted), "FE was truncated or read from wrong owner");
        helper.assertTrue(storage.energy() == inserted && storage.getVersion() == version, "Reading changed storage");
        long withdrawn = storage.extractEnergy(123, false);
        helper.assertTrue(piece.execute(context).equals((double) (inserted - withdrawn)), "Selector cached stale balance");
        var other = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "other-reader"));
        helper.assertTrue(piece.execute(new SpellContext().setPlayer(other)).equals(0.0D), "Balance leaked between owners");
        helper.assertTrue(piece.execute(new SpellContext()).equals(0.0D), "Missing caster fallback");
        storage.extractEnergy(Long.MAX_VALUE, false);
        helper.succeed();
    }

    @GameTest(template = "psi110_empty")
    public static void noInputsNoCostAndSharedResearch(GameTestHelper helper) throws Exception {
        var id = Psitweaks.location("selector_idea_storage_energy");
        var piece = PsiAPI.SPELL_PIECE_REGISTRY.get(id).create(new Spell());
        helper.assertTrue(piece.params.isEmpty(), "Selector must have no inputs");
        helper.assertTrue(piece.getEvaluationType() == Double.class, "Selector must return Psi Number");
        var metadata = new SpellMetadata();
        piece.addToMetadata(metadata);
        helper.assertTrue(metadata.getStat(EnumSpellStat.COMPLEXITY) == 1, "Complexity must be one");
        helper.assertTrue(metadata.getStat(EnumSpellStat.POTENCY) == 0, "Potency must be zero");
        helper.assertTrue(metadata.getStat(EnumSpellStat.COST) == 0, "Cost must be zero");
        metadata.setStat(EnumSpellStat.COMPLEXITY, 7);
        metadata.setStat(EnumSpellStat.POTENCY, 15);
        metadata.setStat(EnumSpellStat.COST, 20);
        piece.addToMetadata(metadata);
        helper.assertTrue(metadata.getStat(EnumSpellStat.COMPLEXITY) == 8
                && metadata.getStat(EnumSpellStat.POTENCY) == 15
                && metadata.getStat(EnumSpellStat.COST) == 20, "Selector changed other pieces' stats");
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "program-user"));
        var group = PsiAPI.getPieceGroup(helper.getLevel().registryAccess(), id).orElseThrow();
        helper.assertTrue(group.key().location().equals(Psitweaks.location("idea_storage")),
                "Selector must share the Ideaspace Storage group");
        var data = PlayerDataHandler.get(player);
        String[] ids = {"trick_idea_storage_view", "trick_idea_storage_absorb_fe",
                "trick_idea_storage_supply_fe", "selector_idea_storage_energy"};
        for (String pieceId : ids) {
            helper.assertTrue(data.isPieceGroupUnlocked(group.key().location(), Psitweaks.location(pieceId))
                    == !PsitweaksConfig.COMMON.requireSpellUnlocks.get(), "Unexpected initial unlock: " + pieceId);
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(PsitweaksItems.PROGRAM_IDEA_STORAGE.get()));
        var event = new PlayerInteractEvent.RightClickItem(player, InteractionHand.MAIN_HAND);
        SpellUnlockHandler.onRightClickUnlockItem(event);
        helper.assertTrue(event.isCanceled() && player.getTags().contains("psitweaks.unlock.idea_storage"),
                "Program did not grant the shared unlock");
        for (String pieceId : ids) {
            helper.assertTrue(data.isPieceGroupUnlocked(group.key().location(), Psitweaks.location(pieceId)),
                    "Program did not unlock " + pieceId);
        }
        helper.succeed();
    }
}
