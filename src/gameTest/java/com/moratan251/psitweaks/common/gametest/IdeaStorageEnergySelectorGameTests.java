package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.handler.SpellUnlockHandler;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import com.moratan251.psitweaks.common.spells.mode.BlockResourceModes;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorIdeaStorageAmount;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.common.core.handler.PlayerDataHandler;

/**
 * Ported from 1.21.1 (commit cdda5834): the FE mode of the Ideaspace Storage amount selector, and the research group
 * and unlock program it shares with the other ideaspace pieces.
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class IdeaStorageEnergySelectorGameTests {
    @GameTest(template = "psi110_empty")
    public static void readsCurrentOwnerBalanceWithoutConsuming(GameTestHelper helper) throws Exception {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "energy-reader"));
        var storage = IdeaStorageService.get(player.server, player.getUUID());
        var piece = new PieceSelectorIdeaStorageAmount(new Spell());
        piece.setModeOption(BlockResourceModes.ENERGY);
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
        var id = Psitweaks.location("selector_idea_storage_amount");
        var piece = (PieceSelectorIdeaStorageAmount) SpellPiece.create(PsiAPI.getSpellPiece(id), new Spell());
        piece.setModeOption(BlockResourceModes.ENERGY);
        helper.assertTrue(piece.params.isEmpty(), "FE mode must have no inputs");
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
        // Psi 1.20.1 has no piece group registry: a group is just an ID. The Ideaspace Storage group is
        // psitweaks:trick_idea_storage_view on 1.20.1 (psitweaks:idea_storage on 1.21.1).
        ResourceLocation group = PsiAPI.getGroupForPiece(piece.getClass());
        helper.assertTrue(Psitweaks.location("trick_idea_storage_view").equals(group),
                "Selector must share the Ideaspace Storage group");
        var data = PlayerDataHandler.get(player);
        // Every group member, and also a piece ID with no definition of its own, must be judged by the group-wide unlock.
        List<ResourceLocation> ids = new ArrayList<>();
        // 1.21.1 listed the group's main piece (the view trick) and then its nine other pieces. PsitweaksSpells on
        // 1.20.1 adds ten pieces to the group, the view trick included, all with main = false, so there is no main
        // piece and the ten come from the member list: ten pieces plus the unregistered ID still make 11.
        Class<? extends SpellPiece> main = PsiAPI.getMainPieceForGroup(group);
        if (main != null) {
            ids.add(PsiAPI.getSpellPieceKey(main));
        }
        for (Class<? extends SpellPiece> member : PsiAPI.getPiecesInAdvancementGroup(group)) {
            ResourceLocation key = PsiAPI.getSpellPieceKey(member);
            if (!ids.contains(key)) {
                ids.add(key);
            }
        }
        ids.add(Psitweaks.location("unregistered_idea_storage_piece"));
        helper.assertTrue(ids.size() == 11, "Ideaspace Storage group membership changed: " + ids);
        // On 1.21.1 the view trick was in the list as the main piece; here it must be a plain member.
        helper.assertTrue(ids.contains(Psitweaks.location("trick_idea_storage_view")), "Group is missing trick_idea_storage_view");
        for (String added : IdeaLogisticsPlusGameTests.GROUP_PIECES) {
            helper.assertTrue(ids.contains(Psitweaks.location(added)), "Group is missing " + added);
        }
        for (ResourceLocation pieceId : ids) {
            helper.assertTrue(data.isPieceGroupUnlocked(group, pieceId)
                    == !PsitweaksConfig.COMMON.requireSpellUnlocks.get(), "Unexpected initial unlock: " + pieceId);
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(PsitweaksItems.PROGRAM_IDEA_STORAGE.get()));
        var event = new PlayerInteractEvent.RightClickItem(player, InteractionHand.MAIN_HAND);
        SpellUnlockHandler.onRightClickUnlockItem(event);
        helper.assertTrue(event.isCanceled() && player.getTags().contains("psitweaks.unlock.idea_storage"),
                "Program did not grant the shared unlock");
        for (ResourceLocation pieceId : ids) {
            helper.assertTrue(data.isPieceGroupUnlocked(group, pieceId),
                    "Program did not unlock " + pieceId);
        }
        helper.succeed();
    }
}
