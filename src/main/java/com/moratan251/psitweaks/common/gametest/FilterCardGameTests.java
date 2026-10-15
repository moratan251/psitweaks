package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.items.FilterCards;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import com.moratan251.psitweaks.common.items.curios.CuriosSlotChecker;
import com.moratan251.psitweaks.common.menu.FilterCardMenu;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorFilterCard;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import top.theillusivec4.curios.api.CuriosApi;
import vazkii.psi.api.spell.SpellRuntimeException;

@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FilterCardGameTests {
    @GameTest(template = "psi110_empty")
    public static void filterCardLookupPrefersCuriosThenInventory(GameTestHelper helper) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "filter-card-order"));
        ItemStack inventoryCard = card();
        ItemStack offhandCard = card();
        ItemStack mainHandCard = card();
        player.getInventory().selected = 0;
        player.getInventory().setItem(5, inventoryCard);
        player.setItemInHand(InteractionHand.OFF_HAND, offhandCard);
        player.setItemInHand(InteractionHand.MAIN_HAND, mainHandCard);

        List<ItemStack> cards = FilterCards.findCards(player);
        helper.assertTrue(cards.size() == 3 && cards.get(0) == mainHandCard && cards.get(1) == offhandCard
                && cards.get(2) == inventoryCard, "Inventory lookup order is main hand, offhand, then inventory");
        helper.assertTrue(!FilterCards.isIdle(player, mainHandCard) && FilterCards.isIdle(player, offhandCard)
                && FilterCards.isIdle(player, inventoryCard), "Only the first card is in use");
        helper.assertTrue(!FilterCards.isIdle(player, mainHandCard.copy()), "A copy of a carried card is not idle");

        ItemStack curioCard = card();
        var curios = CuriosApi.getCuriosInventory(player).orElseThrow();
        curios.setEquippedCurio(CuriosSlotChecker.MAGIC_CALCULATION_AREA, 2, curioCard);
        ItemStack equipped = CuriosSlotChecker.getItemFromSelectedMagicSlot(player, 2);
        helper.assertTrue(FilterCards.findActive(player).orElseThrow() == equipped,
                "A card in a Curios slot comes before the inventory");
        helper.assertTrue(FilterCards.isIdle(player, mainHandCard), "The main-hand card is idle behind the Curios card");
        helper.assertTrue(curioCard.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath("curios", "curio"))),
                "The card is in curios:curio, which every tag-validated Curios slot accepts");
        helper.succeed();
    }

    @GameTest(template = "psi110_empty")
    public static void filterCardPieceReadsTemplatesAndReportsErrors(GameTestHelper helper) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "filter-card-piece"));
        assertError(helper, player, 1, PieceSelectorFilterCard.ERROR_NO_CARD);

        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("Sample"));
        ItemStack card = card();
        FilterCards.setTemplates(card, List.of(ItemStack.EMPTY, sword, new ItemStack(Items.DIAMOND, 16), card()));
        player.setItemInHand(InteractionHand.MAIN_HAND, card);

        List<ItemStack> templates = FilterCards.templates(card);
        helper.assertTrue(templates.size() == FilterCards.SLOT_COUNT && templates.get(2).getCount() == 1
                && templates.get(3).isEmpty(), "Templates are count-1 copies and a card is never a template");

        assertError(helper, player, 1, PieceSelectorFilterCard.ERROR_EMPTY_SLOT);
        assertError(helper, player, 9, PieceSelectorFilterCard.ERROR_EMPTY_SLOT);
        assertError(helper, player, 0, PieceSelectorFilterCard.ERROR_SLOT_RANGE);
        assertError(helper, player, 10, PieceSelectorFilterCard.ERROR_SLOT_RANGE);
        assertError(helper, player, 1.5D, PieceSelectorFilterCard.ERROR_SLOT_RANGE);
        try {
            SpellItemValue value = PieceSelectorFilterCard.resolve(player, 2);
            helper.assertTrue(ItemStack.isSameItemSameComponents(value.snapshot(), sword)
                            && value.snapshot().getCount() == 1 && value.source().isEmpty(),
                    "Slot 2 returns the registered sword without a source");
        } catch (SpellRuntimeException e) {
            helper.fail("Slot 2 failed: " + e.getMessage());
        }
        helper.succeed();
    }

    @GameTest(template = "psi110_empty")
    public static void filterCardMenuRegistersCopiesWithoutTakingItems(GameTestHelper helper) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "filter-card-menu"));
        ItemStack card = card();
        player.getInventory().selected = 0;
        player.setItemInHand(InteractionHand.MAIN_HAND, card);
        ItemStack diamonds = new ItemStack(Items.DIAMOND, 16);
        player.getInventory().setItem(9, diamonds);
        var menu = new FilterCardMenu(0, player.getInventory(), InteractionHand.MAIN_HAND);

        menu.setCarried(new ItemStack(Items.EMERALD, 5));
        menu.clicked(4, 0, ClickType.PICKUP, player);
        helper.assertTrue(FilterCards.template(card, 4).is(Items.EMERALD) && FilterCards.template(card, 4).getCount() == 1
                && menu.getCarried().getCount() == 5, "Clicking a slot registers a copy and keeps the cursor stack");

        menu.setCarried(ItemStack.EMPTY);
        menu.clicked(4, 0, ClickType.PICKUP, player);
        helper.assertTrue(FilterCards.template(card, 4).isEmpty(), "An empty cursor clears the slot");

        // Menu slot 9 is the first main-inventory slot (inventory index 9).
        menu.quickMoveStack(player, 9);
        helper.assertTrue(FilterCards.template(card, 0).is(Items.DIAMOND) && diamonds.getCount() == 16,
                "Shift-click registers the item in the first empty slot without moving it");

        int lockedMenuSlot = FilterCards.SLOT_COUNT + 27;
        menu.clicked(lockedMenuSlot, 0, ClickType.PICKUP, player);
        menu.clicked(FilterCards.SLOT_COUNT + 9, 0, ClickType.SWAP, player);
        helper.assertTrue(player.getMainHandItem() == card && menu.getCarried().isEmpty(),
                "The edited card cannot be picked up or swapped away");
        helper.assertTrue(menu.stillValid(player), "The menu stays open while the card is held");
        helper.succeed();
    }

    private static ItemStack card() {
        return new ItemStack(PsitweaksItems.FILTER_CARD.get());
    }

    private static void assertError(GameTestHelper helper, Player player, Number slot, String key) {
        try {
            PieceSelectorFilterCard.resolve(player, slot);
            helper.fail("Expected " + key + " for slot " + slot);
        } catch (SpellRuntimeException e) {
            helper.assertTrue(key.equals(e.getMessage()), "Expected " + key + " but got " + e.getMessage());
        }
    }
}
