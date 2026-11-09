package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.compat.OptionalSpellPieceCompat;
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
        int expected = 87 + (ModList.get().isLoaded("mekanism") ? 4 : 0) + (ModList.get().isLoaded("sable") ? 1 : 0);
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

    /** Spells saved before a piece was renamed load under its new ID instead of failing the whole player. */
    @GameTest(template = "psi110_empty")
    public static void renamedPiecesLoadUnderTheirNewId(GameTestHelper helper) {
        String[][] renames = {
                {"psitweaks:trick_kamaitachi", "trick_air_bullet"},
                {"psitweaks:trick_gravity_collapse", "trick_micro_black_hole"},
                {"trick_gravity_collapse", "trick_micro_black_hole"}
        };
        for (String[] rename : renames) {
            var tag = new CompoundTag();
            PsiAPI.SPELL_PIECE_REGISTRY.get(Psitweaks.location(rename[1])).create(new Spell()).writeToNBT(tag);
            helper.assertTrue(tag.contains("key"), "Psi no longer saves the piece ID under 'key': " + tag);
            tag.putString("key", rename[0]);
            var restored = SpellPiece.createFromNBT(new Spell(), tag);
            helper.assertTrue(restored != null && Psitweaks.location(rename[1]).equals(restored.getRegistryKey()),
                    rename[0] + " did not load as " + rename[1]);
        }
        helper.succeed();
    }

    /** Spells saved with a piece removed from the mod load with that piece taken out instead of failing the whole player. */
    @GameTest(template = "psi110_empty")
    public static void removedPiecesLoadAsRemovalMarker(GameTestHelper helper) {
        for (String key : new String[] {"psitweaks:trick_set_spellgram_follow_target", "trick_set_spellgram_follow_target"}) {
            helper.assertFalse(PsiAPI.SPELL_PIECE_REGISTRY.containsKey(Psitweaks.location("trick_set_spellgram_follow_target")),
                    "Trick: Set SpellGram Follow Target should no longer be registered");
            var tag = new CompoundTag();
            tag.putString("key", key);
            var restored = SpellPiece.createFromNBT(new Spell(), tag);
            helper.assertTrue(OptionalSpellPieceCompat.isRemovalMarker(restored),
                    key + " should load as a removal marker, got " + restored);
        }
        helper.succeed();
    }

    /** F-037: without Mekanism, its pieces load as a removal marker instead of failing the whole spell. */
    @GameTest(template = "psi110_empty")
    public static void mekanismPiecesAreRemovedWithoutMekanism(GameTestHelper helper) {
        for (String path : new String[] {"trick_radiation_filter", "trick_radiation_injection", "trick_cure_radiation",
                "selector_autonomous_caster_position"}) {
            var id = Psitweaks.location(path);
            var tag = new CompoundTag();
            if (ModList.get().isLoaded("mekanism")) {
                PsiAPI.SPELL_PIECE_REGISTRY.get(id).create(new Spell()).writeToNBT(tag);
                var restored = SpellPiece.createFromNBT(new Spell(), tag);
                helper.assertTrue(restored != null && id.equals(restored.getRegistryKey()), path + " did not load with Mekanism");
            } else {
                tag.putString("key", id.toString());
                var restored = SpellPiece.createFromNBT(new Spell(), tag);
                helper.assertTrue(OptionalSpellPieceCompat.isRemovalMarker(restored),
                        path + " should be removed without Mekanism, got " + restored);
            }
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
