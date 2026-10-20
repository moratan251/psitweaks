package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.handler.SpellUnlockHandler.SpellUnlockDefinition;
import com.moratan251.psitweaks.common.network.MessageSpellUnlockSync;
import com.moratan251.psitweaks.common.recipe.EnchantedBookIngredient;
import com.moratan251.psitweaks.common.recipe.ProgramResearchRecipe;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.common.item.base.ModItems;

@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SpellUnlockGameTests {
    private static final int SHIPPED_DEFINITIONS = 32;
    /** The combat tricks unlocked by research (Parade included); Kamaitachi is free. */
    private static final List<String> COMBAT_UNLOCKS = List.of(
            "chain_lightning", "mine_genesis", "dancing_blades", "starfall", "burst", "gravity_collapse", "parade");

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
        // JSON にのみある定義も、登録時点ではなく実行時の定義から解決される
        helper.assertTrue(run(player, "idea_storage_free_capacity status @s") == 0, "JSON-only definition was not resolved");
        boolean rejected = false;
        try {
            run(player, "no_such_spell status @s");
        } catch (CommandSyntaxException e) {
            rejected = true;
        }
        helper.assertTrue(rejected, "Unknown spell unlock id must be a command error");
        helper.succeed();
    }

    @GameTest(template = "psi110_empty")
    public static void syncPayloadRoundTripsGroupAndNullGroup(GameTestHelper helper) {
        var definitions = List.of(
                new SpellUnlockDefinition("idea_storage", Psitweaks.location("trick_idea_storage_view"),
                        Psitweaks.location("program_idea_storage"), "psitweaks.unlock.idea_storage", Psitweaks.location("idea_storage")),
                new SpellUnlockDefinition("cocytus", Psitweaks.location("trick_cocytus"),
                        Psitweaks.location("program_cocytus"), "psitweaks.unlock.trick_cocytus", null));
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
        MessageSpellUnlockSync.STREAM_CODEC.encode(buffer, new MessageSpellUnlockSync(definitions));
        var decoded = MessageSpellUnlockSync.STREAM_CODEC.decode(buffer);
        helper.assertTrue(decoded.definitions().equals(definitions), "Sync payload changed definitions: " + decoded.definitions());
        helper.assertTrue(buffer.readableBytes() == 0, "Sync payload left unread bytes");
        helper.succeed();
    }

    @GameTest(template = "psi110_empty")
    public static void combatTricksAreUnlockedByTheirResearchedPrograms(GameTestHelper helper) throws Exception {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "unlock-combat"));
        var recipes = helper.getLevel().getRecipeManager();
        for (String id : COMBAT_UNLOCKS) {
            helper.assertTrue(run(player, id + " status @s") == 0, id + " must start locked");
            helper.assertTrue(run(player, id + " grant @s") == 1, id + " must be grantable");
            // The research table needs Mekanism; without it the program is crafted from the fallback recipe.
            String recipe = (MekanismCompat.isMekanismLoaded() ? "program_research/" : "fallback/") + "program_" + id;
            var holder = recipes.byKey(Psitweaks.location(recipe));
            helper.assertTrue(holder.isPresent() && holder.get().value().getResultItem(helper.getLevel().registryAccess())
                    .is(BuiltInRegistries.ITEM.get(Psitweaks.location("program_" + id))), "No recipe " + recipe);
        }
        helper.assertTrue(isNotUnlockDefinition(player, "kamaitachi"), "Kamaitachi must stay free");
        helper.succeed();
    }

    @GameTest(template = "psi110_empty")
    public static void enchantedBookIngredientAcceptsAnyLevelAlongsideOtherEnchantments(GameTestHelper helper) {
        var enchantments = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var loyalty = enchantments.getOrThrow(Enchantments.LOYALTY);
        var sharpness = enchantments.getOrThrow(Enchantments.SHARPNESS);
        Ingredient ingredient = new EnchantedBookIngredient(loyalty).toVanilla();

        var loyaltyOne = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(loyalty, 1));
        var loyaltyThree = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(loyalty, 3));
        var mixed = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(sharpness, 2));
        mixed.update(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY, stored -> {
            var mutable = new ItemEnchantments.Mutable(stored);
            mutable.set(loyalty, 2);
            return mutable.toImmutable();
        });
        var sharpnessOnly = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(sharpness, 2));
        var trident = new ItemStack(Items.TRIDENT);
        trident.enchant(loyalty, 3);

        helper.assertTrue(ingredient.test(loyaltyOne) && ingredient.test(loyaltyThree), "Loyalty books of any level must match");
        helper.assertTrue(ingredient.test(mixed), "A book with Loyalty and other enchantments must match");
        helper.assertTrue(!ingredient.test(sharpnessOnly) && !ingredient.test(new ItemStack(Items.BOOK))
                && !ingredient.test(new ItemStack(Items.ENCHANTED_BOOK)), "Books without Loyalty must not match");
        helper.assertTrue(!ingredient.test(trident), "Only enchanted books count, not enchanted items");
        helper.assertTrue(ingredient.getItems().length == 3, "Recipe viewers must show Loyalty I to III, got " + ingredient.getItems().length);
        helper.succeed();
    }

    @GameTest(template = "psi110_empty")
    public static void recipeViewersSpreadUnstackableResearchInputs(GameTestHelper helper) {
        if (!MekanismCompat.isMekanismLoaded()) {
            // The research table and its recipes need Mekanism.
            helper.succeed();
            return;
        }
        var holder = helper.getLevel().getRecipeManager().byKey(Psitweaks.location("program_research/program_dancing_blades"));
        helper.assertTrue(holder.isPresent() && holder.get().value() instanceof ProgramResearchRecipe, "Dancing Blades research missing");
        var slots = ((ProgramResearchRecipe) holder.get().value()).getDisplaySlots();
        long swordSlots = slots.stream().filter(slot -> slot.input().ingredient().test(new ItemStack(ModItems.psimetalSword.get()))).count();
        helper.assertTrue(swordSlots == 3 && slots.stream().allMatch(slot -> slot.count() <= 64), "Three swords must take three slots: " + slots);
        helper.assertTrue(slots.size() == 7, "Blank, 3 swords, book, heavy psimetal and circuits make 7 slots, got " + slots.size());
        helper.succeed();
    }

    /** Whether {@code id} is not a spell unlock command id (the command rejects it). */
    private static boolean isNotUnlockDefinition(FakePlayer player, String id) throws Exception {
        try {
            run(player, id + " status @s");
            return false;
        } catch (CommandSyntaxException e) {
            return true;
        }
    }

    private static int run(FakePlayer player, String arguments) throws Exception {
        var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
        return player.server.getCommands().getDispatcher().execute("psitweaks spellunlock " + arguments, source);
    }
}
