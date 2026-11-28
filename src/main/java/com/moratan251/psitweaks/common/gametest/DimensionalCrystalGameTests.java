package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.handler.DimensionalCrystalDropHandler;
import com.moratan251.psitweaks.common.handler.PsitweaksAdvancements;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.spell.PreSpellCastEvent;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCastEvent;
import vazkii.psi.api.spell.SpellContext;

/**
 * The Dimensional Crystal always glints. A cast that yields one rings an amethyst chime, says so above the hotbar and awards the
 * hidden advancement.
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DimensionalCrystalGameTests {
    /** Enough Psi that the drop chance reaches 1. */
    private static final int CERTAIN_COST = 2_000_000;

    private DimensionalCrystalGameTests() {
    }

    @GameTest(template = "psi110_empty")
    public static void crystalGlintsAndAnnouncesItsDrop(GameTestHelper helper) {
        helper.assertTrue(new ItemStack(PsitweaksItems.DIMENSIONAL_CRYSTAL.get()).hasFoil(), "The Dimensional Crystal must always glint");

        List<SoundEvent> sounds = new ArrayList<>();
        List<Component> messages = new ArrayList<>();
        List<Boolean> onActionBar = new ArrayList<>();
        FakePlayer player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "dimensional-crystal")) {
            @Override
            public void playNotifySound(SoundEvent sound, SoundSource source, float volume, float pitch) {
                sounds.add(sound);
            }

            @Override
            public void displayClientMessage(Component message, boolean actionBar) {
                messages.add(message);
                onActionBar.add(actionBar);
            }
        };
        Spell spell = new Spell();
        SpellContext context = new SpellContext().setPlayer(player);
        DimensionalCrystalDropHandler.onPreSpellCast(new PreSpellCastEvent(CERTAIN_COST, 0.0F, 0, 0, spell, context, player,
                null, ItemStack.EMPTY, ItemStack.EMPTY));
        DimensionalCrystalDropHandler.onSpellCast(new SpellCastEvent(spell, context, player, null, ItemStack.EMPTY, ItemStack.EMPTY));

        helper.assertTrue(player.getInventory().countItem(PsitweaksItems.DIMENSIONAL_CRYSTAL.get()) == 1,
                "A cast with a certain chance must give one crystal");
        helper.assertTrue(sounds.equals(List.of(SoundEvents.AMETHYST_BLOCK_RESONATE)), "The drop must ring one amethyst chime: " + sounds);
        helper.assertTrue(messages.size() == 1 && messages.get(0).getContents() instanceof TranslatableContents contents
                        && contents.getKey().equals("message.psitweaks.dimensional_crystal.obtained"),
                "The drop must show one message: " + messages);
        helper.assertTrue(onActionBar.equals(List.of(true)), "The message must go above the hotbar, not to chat");
        helper.succeed();
    }

    @GameTest(template = "psi110_empty")
    public static void crystalFromACastAwardsTheHiddenAdvancement(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.inLevel(helper, "dimensional-crystal-advancement", new Vec3(0.5, 1.0, 0.5));
        try {
            var advancement = helper.getLevel().getServer().getAdvancements().get(PsitweaksAdvancements.DIMENSIONAL_CRYSTAL_FROM_CAST);
            helper.assertTrue(advancement != null, "The Dimensional Crystal advancement is missing");
            player.getInventory().add(new ItemStack(PsitweaksItems.DIMENSIONAL_CRYSTAL.get()));
            helper.assertTrue(!player.getAdvancements().getOrStartProgress(advancement).isDone(),
                    "Holding a crystal that no cast gave must not award the advancement");

            castWithCertainDrop(player);
            helper.assertTrue(player.getAdvancements().getOrStartProgress(advancement).isDone(),
                    "A crystal from a cast must award the advancement");
            helper.succeed();
        } finally {
            GameTestPlayers.remove(helper, player);
        }
    }

    private static void castWithCertainDrop(ServerPlayer player) {
        Spell spell = new Spell();
        SpellContext context = new SpellContext().setPlayer(player);
        DimensionalCrystalDropHandler.onPreSpellCast(new PreSpellCastEvent(CERTAIN_COST, 0.0F, 0, 0, spell, context, player,
                null, ItemStack.EMPTY, ItemStack.EMPTY));
        DimensionalCrystalDropHandler.onSpellCast(new SpellCastEvent(spell, context, player, null, ItemStack.EMPTY, ItemStack.EMPTY));
    }
}
