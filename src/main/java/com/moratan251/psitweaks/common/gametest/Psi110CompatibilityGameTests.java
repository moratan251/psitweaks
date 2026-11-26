package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.compat.OptionalSpellPieceCompat;
import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.cad.RegenPsiEvent;
import vazkii.psi.api.event.PsiEvents;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.common.core.handler.PlayerDataHandler;
import vazkii.psi.common.item.ItemSpellDrive;
import vazkii.psi.common.item.base.ModItems;

@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class Psi110CompatibilityGameTests {
    /** Mekanism があるときだけ登録され、ほかのピースと同じグループに入っているもの。 */
    private static final java.util.Set<String> MEKANISM_GROUP_MEMBERS = java.util.Set.of("trick_contamination_zone");

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
                if (!MekanismCompat.isMekanismLoaded() && MEKANISM_GROUP_MEMBERS.contains(member.getPath())) {
                    // 領域魔法のグループには、Mekanism がないと登録されない領域も入っている（ID だけなので害はない）。
                    continue;
                }
                helper.assertTrue(PsiAPI.SPELL_PIECE_REGISTRY.containsKey(member), "Unknown member " + member);
                helper.assertTrue(PsiAPI.getPieceGroup(helper.getLevel().registryAccess(), member)
                        .orElseThrow().key().location().equals(id), "Wrong group for " + member);
            }
        }
        // サイ・ブリット・術式解体・定数子: 符号ラッパーを含む。放射線注入は止めている間グループも登録しない。
        // Mekanism: 放射線フィルタ・放射線除去・自律詠唱機の位置・除染（放射線注入は止めている間は数えない）。
        int expected = 91 + (ModList.get().isLoaded("mekanism") ? 4 : 0)
                + (MekanismCompat.isRadiationInjectionEnabled() ? 1 : 0) + (ModList.get().isLoaded("sable") ? 1 : 0);
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

    /**
     * 作動式: 浄化領域は登録しない。このピースを含む魔法式を書いた術式弾は、ピースを外して読み込む（プレイヤーの持ち物と同じ
     * {@link ItemStack#parse}）。Mekanism があれば、浄化領域のプログラムの ID はプログラム: 除染として読み込む。
     */
    @GameTest(template = "psi110_empty")
    public static void purificationZoneDataLoadsWithoutIt(GameTestHelper helper) {
        var removedId = Psitweaks.location("trick_purification_zone");
        helper.assertFalse(PsiAPI.SPELL_PIECE_REGISTRY.containsKey(removedId), "Trick: Purification Zone must not be registered");
        var registries = helper.getLevel().registryAccess();

        Spell spell = new Spell();
        spell.name = "old_purification_zone";
        placeForSave(spell, PsiAPI.SPELL_PIECE_REGISTRY.get(PsiAPI.location("constant_number")).create(spell), 3, 4);
        placeForSave(spell, PsiAPI.SPELL_PIECE_REGISTRY.get(PsiAPI.location("trick_debug")).create(spell), 4, 4);
        ItemStack bullet = new ItemStack(ModItems.spellBullet.get());
        ItemSpellDrive.setSpell(bullet, spell);
        Tag saved = bullet.save(registries);
        int rewritten = rewriteStrings(saved, PsiAPI.location("trick_debug").toString(), removedId.toString());
        helper.assertTrue(rewritten == 1, "The saved bullet must hold the trick's ID once: " + saved);

        ItemStack loaded = ItemStack.parse(registries, saved).orElse(ItemStack.EMPTY);
        Spell loadedSpell = ItemSpellDrive.getSpell(loaded);
        helper.assertTrue(loaded.is(ModItems.spellBullet.get()) && loadedSpell != null, "The bullet must keep its spell: " + loaded);
        helper.assertTrue(loadedSpell.grid.gridData[4][4] == null, "Trick: Purification Zone must be taken out");
        helper.assertTrue(loadedSpell.grid.gridData[3][4] != null, "The other pieces must stay");

        if (PsitweaksItems.PROGRAM_DECONTAMINATION != null) {
            CompoundTag program = new CompoundTag();
            program.putString("id", Psitweaks.location("program_purification_zone").toString());
            program.putInt("count", 2);
            ItemStack loadedProgram = ItemStack.parseOptional(registries, program);
            helper.assertTrue(loadedProgram.is(PsitweaksItems.PROGRAM_DECONTAMINATION.get()) && loadedProgram.getCount() == 2,
                    "A saved purification zone program loaded as " + loadedProgram);
        }
        helper.succeed();
    }

    private static void placeForSave(Spell spell, SpellPiece piece, int x, int y) {
        piece.x = x;
        piece.y = y;
        piece.isInGrid = true;
        spell.grid.gridData[x][y] = piece;
    }

    /** {@code tag} の中の文字列 {@code from} をすべて {@code to} に書き換え、書き換えた数を返す。 */
    private static int rewriteStrings(Tag tag, String from, String to) {
        int count = 0;
        if (tag instanceof CompoundTag compound) {
            for (String key : java.util.List.copyOf(compound.getAllKeys())) {
                Tag child = compound.get(key);
                if (child instanceof StringTag string && string.getAsString().equals(from)) {
                    compound.putString(key, to);
                    count++;
                } else {
                    count += rewriteStrings(child, from, to);
                }
            }
        } else if (tag instanceof ListTag list) {
            for (int i = 0; i < list.size(); i++) {
                Tag child = list.get(i);
                if (child instanceof StringTag string && string.getAsString().equals(from)) {
                    list.set(i, StringTag.valueOf(to));
                    count++;
                } else {
                    count += rewriteStrings(child, from, to);
                }
            }
        }
        return count;
    }

    /** F-037: without Mekanism, its pieces load as a removal marker instead of failing the whole spell. */
    @GameTest(template = "psi110_empty")
    public static void mekanismPiecesAreRemovedWithoutMekanism(GameTestHelper helper) {
        for (String path : new String[] {"trick_radiation_filter", "trick_cure_radiation", "selector_autonomous_caster_position"}) {
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

    /**
     * 作動式: 放射線注入は使えなくしているあいだ登録せず、プログラムも作らない。このピースを含む魔法式は、ピースを外して読み込む
     * （Mekanism の有無によらない）。
     */
    @GameTest(template = "psi110_empty")
    public static void radiationInjectionIsRemovedWhileDisabled(GameTestHelper helper) {
        var id = Psitweaks.location("trick_radiation_injection");
        boolean enabled = com.moratan251.psitweaks.common.compat.MekanismCompat.isRadiationInjectionEnabled();
        helper.assertTrue(PsiAPI.SPELL_PIECE_REGISTRY.containsKey(id) == enabled,
                "Trick: Radiation Injection must be registered only while it is enabled");
        helper.assertTrue(net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(Psitweaks.location("program_radiation_injection")) == enabled,
                "Program: Radiation Injection must exist only while the trick is enabled");
        if (!enabled) {
            var tag = new CompoundTag();
            tag.putString("key", id.toString());
            var restored = SpellPiece.createFromNBT(new Spell(), tag);
            helper.assertTrue(OptionalSpellPieceCompat.isRemovalMarker(restored),
                    "Spells with Trick: Radiation Injection must load with it taken out, got " + restored);
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

    /**
     * The disableRegenCooldown config clears Psi's regen cooldown through RegenPsiEvent, so regen resumes the tick after
     * Psi is spent. With the config off, Psi's cooldown counts down as usual.
     */
    @GameTest(template = "psi110_empty")
    public static void regenCooldownConfigUsesRegenPsiEvent(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var data = PlayerDataHandler.get(player);
        boolean old = PsitweaksConfig.COMMON.disableRegenCooldown.get();
        try {
            for (boolean disabled : new boolean[] {true, false}) {
                PsitweaksConfig.COMMON.disableRegenCooldown.set(disabled);
                data.availablePsi = data.getTotalPsi() / 2;
                data.regenCooldown = 0;
                data.deductPsi(10, 40, false);
                helper.assertTrue(data.regenCooldown == 40,
                        "Spending Psi should set Psi's own cooldown, got " + data.regenCooldown);
                var event = PsiEvents.post(new RegenPsiEvent(player, data, ItemStack.EMPTY));
                int expected = disabled ? 0 : 39;
                helper.assertTrue(event.getRegenCooldown() == expected, "With disableRegenCooldown=" + disabled
                        + " the cooldown should be " + expected + ", got " + event.getRegenCooldown());
                if (disabled) {
                    helper.assertTrue(event.getRegenRate() <= 0 || event.getPlayerRegen() > 0,
                            "Regen did not resume after the cooldown was cleared");
                } else {
                    helper.assertTrue(event.getPlayerRegen() == 0, "Psi regenerated during the cooldown");
                }
            }
        } finally {
            PsitweaksConfig.COMMON.disableRegenCooldown.set(old);
        }
        helper.succeed();
    }
}
