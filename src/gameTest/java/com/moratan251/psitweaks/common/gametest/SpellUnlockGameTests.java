package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.moratan251.psitweaks.Psitweaks;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/**
 * Ported from 1.21.1: the spellunlock command tests. The sync payload round trip is covered by
 * {@link SpellUnlockSyncGameTests}.
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SpellUnlockGameTests {
    private static final int SHIPPED_DEFINITIONS = 25;

    @GameTest(template = "psi110_empty")
    public static void allCommandCountsDefinitionsLikeStatus(GameTestHelper helper) throws Exception {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "unlock-all"));
        helper.assertTrue(run(player, "all status @s") == 0, "Fresh player must have no unlocks");
        int granted = run(player, "all grant @s");
        helper.assertTrue(granted == SHIPPED_DEFINITIONS, "Grant counted shared-tag aliases inconsistently: " + granted);
        helper.assertTrue(run(player, "all status @s") == granted, "Status disagrees with grant count");
        helper.assertTrue(run(player, "all revoke @s") == granted, "Revoke counted shared-tag aliases inconsistently");
        helper.assertTrue(run(player, "all status @s") == 0, "Revoke left unlocks behind");
        helper.succeed();
    }

    @GameTest(template = "psi110_empty")
    public static void revokeRemovesLegacyTagOnlyUnlock(GameTestHelper helper) throws Exception {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "unlock-legacy"));
        player.addTag("psitweaks.unlock.trick_cocytus");
        helper.assertTrue(run(player, "cocytus status @s") == 1, "Legacy tag must count as unlocked");
        helper.assertTrue(run(player, "cocytus revoke @s") == 1, "Legacy-only unlock was not revoked");
        helper.assertTrue(!player.getTags().contains("psitweaks.unlock.trick_cocytus"), "Legacy tag remained");
        helper.assertTrue(run(player, "cocytus status @s") == 0, "Status still unlocked after revoke");
        helper.succeed();
    }

    @GameTest(template = "psi110_empty")
    public static void repairingLegacyTagIsNotReportedAsUnlock(GameTestHelper helper) throws Exception {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "unlock-repair"));
        helper.assertTrue(run(player, "cocytus grant @s") == 1, "First grant must report a change");
        player.removeTag("psitweaks.unlock.trick_cocytus");
        helper.assertTrue(run(player, "cocytus grant @s") == 0, "Restoring only the legacy tag was reported as a new unlock");
        helper.assertTrue(player.getTags().contains("psitweaks.unlock.trick_cocytus"), "Legacy tag was not restored");
        helper.succeed();
    }

    @GameTest(template = "psi110_empty")
    public static void commandsResolveDefinitionsAtExecution(GameTestHelper helper) throws Exception {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "unlock-ids"));
        // Definitions come from the reloaded snapshot when the command runs, not from registration time.
        helper.assertTrue(run(player, "idea_storage_free_capacity status @s") == 0, "Reloaded definition was not resolved");
        boolean rejected = false;
        try {
            run(player, "no_such_spell status @s");
        } catch (CommandSyntaxException e) {
            rejected = true;
        }
        helper.assertTrue(rejected, "Unknown spell unlock id must be a command error");
        helper.succeed();
    }

    private static int run(FakePlayer player, String arguments) throws Exception {
        var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
        return player.server.getCommands().getDispatcher().execute("psitweaks spellunlock " + arguments, source);
    }
}
