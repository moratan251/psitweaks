package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.handler.SpellUnlockHandler.SpellUnlockDefinition;
import com.moratan251.psitweaks.common.network.MessageSpellUnlockSync;
import io.netty.buffer.Unpooled;
import java.util.List;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SpellUnlockSyncGameTests {
    @GameTest(template = "psi110_empty")
    public static void encodedSizeMatchesWrittenBytes(GameTestHelper helper) {
        var definitions = List.of(
                new SpellUnlockDefinition("idea_storage", Psitweaks.location("trick_idea_storage_view"),
                        Psitweaks.location("program_idea_storage"), "psitweaks.unlock.idea_storage",
                        Psitweaks.location("trick_idea_storage_view")),
                new SpellUnlockDefinition("cocytus", Psitweaks.location("trick_cocytus"),
                        Psitweaks.location("program_cocytus"), "解禁タグ.マルチバイト", null));
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            var message = new MessageSpellUnlockSync(definitions);
            message.write(buffer);
            int written = buffer.readableBytes();
            helper.assertTrue(MessageSpellUnlockSync.encodedSize(definitions, Integer.MAX_VALUE - 1) == written,
                    "Computed size differs from written bytes: " + written);
            helper.assertTrue(MessageSpellUnlockSync.encodedSize(definitions, 10) == 11, "Size count did not stop at the limit");
            helper.assertTrue(MessageSpellUnlockSync.read(buffer).definitions().equals(definitions), "Sync payload changed definitions");
        } finally {
            buffer.release();
        }
        helper.succeed();
    }
}
