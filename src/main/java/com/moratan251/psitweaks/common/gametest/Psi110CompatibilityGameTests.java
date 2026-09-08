package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.GameType;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.common.core.handler.PlayerDataHandler;

@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class Psi110CompatibilityGameTests {
    @GameTest(template = "psi110_empty")
    public static void groupsResolveRegisteredPieces(GameTestHelper helper) {
        var groups = helper.getLevel().registryAccess().registryOrThrow(PsiAPI.SPELL_PIECE_GROUP_REGISTRY_KEY);
        int count = 0;
        for (var id : groups.keySet()) {
            if (!id.getNamespace().equals(Psitweaks.MOD_ID)) {
                continue;
            }
            count++;
            var group = groups.get(id);
            helper.assertTrue(PsiAPI.SPELL_PIECE_REGISTRY.containsKey(group.main()), "Unknown main piece in " + id);
            for (var member : group.pieces()) {
                helper.assertTrue(PsiAPI.SPELL_PIECE_REGISTRY.containsKey(member), "Unknown member " + member);
                helper.assertTrue(PsiAPI.getPieceGroup(helper.getLevel().registryAccess(), member)
                        .orElseThrow().key().location().equals(id), "Wrong group for " + member);
            }
        }
        int expected = 75 + (ModList.get().isLoaded("mekanism") ? 3 : 0) + (ModList.get().isLoaded("sable") ? 1 : 0);
        helper.assertTrue(count == expected, "Expected " + expected + " groups, got " + count);
        helper.succeed();
    }

    @GameTest(template = "psi110_empty")
    public static void registeredPiecesRoundTrip(GameTestHelper helper) {
        for (var id : PsiAPI.SPELL_PIECE_REGISTRY.keySet()) {
            if (!id.getNamespace().equals(Psitweaks.MOD_ID)) {
                continue;
            }
            var piece = PsiAPI.SPELL_PIECE_REGISTRY.get(id).create(new Spell());
            helper.assertTrue(id.equals(piece.getRegistryKey()), "Factory lost ID " + id);
            var tag = new CompoundTag();
            piece.writeToNBT(tag);
            var restored = SpellPiece.createFromNBT(new Spell(), tag);
            helper.assertTrue(restored != null && id.equals(restored.getRegistryKey()), "NBT lost ID " + id);
            helper.assertTrue(piece.getClass().equals(restored.getClass()), "NBT changed type " + id);
        }
        helper.succeed();
    }

    @GameTest(template = "psi110_empty")
    public static void researchUnlockUsesPsiEvents(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var data = PlayerDataHandler.get(player);
        var flight = Psitweaks.location("trick_flight");
        helper.assertTrue(data.isPieceGroupUnlocked(flight, flight) == !PsitweaksConfig.COMMON.requireSpellUnlocks.get(),
                "Research lock listener is not registered on PsiEvents");
        player.addTag("psitweaks.unlock.trick_flight");
        helper.assertTrue(data.isPieceGroupUnlocked(flight, flight), "Research unlock tag is ignored");
        helper.assertTrue(data.isPieceGroupUnlocked(Psitweaks.location("string"), Psitweaks.location("constant_string")),
                "Unrestricted group became locked");
        helper.succeed();
    }
}
