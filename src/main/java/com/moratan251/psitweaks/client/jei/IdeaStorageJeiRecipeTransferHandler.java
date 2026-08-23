package com.moratan251.psitweaks.client.jei;

import com.moratan251.psitweaks.common.menu.IdeaStorageMenu;
import com.moratan251.psitweaks.common.menu.ModMenuTypes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

/**
 * イデアストレージのクラフトウィンドウへのレシピ転送(JEI)。
 * 転送元はプレイヤーインベントリ(スロット0..35)のみ。ストレージからの直接引き出しは行わない。
 * 実際の移動はスロットクリックのシミュレート(handleInventoryMouseClick)で行う。
 */
public class IdeaStorageJeiRecipeTransferHandler
        implements IRecipeTransferHandler<IdeaStorageMenu, RecipeHolder<CraftingRecipe>> {
    private final IRecipeTransferHandlerHelper helper;

    public IdeaStorageJeiRecipeTransferHandler(IRecipeTransferHandlerHelper helper) {
        this.helper = helper;
    }

    @Override
    public Class<? extends IdeaStorageMenu> getContainerClass() {
        return IdeaStorageMenu.class;
    }

    @Override
    public Optional<MenuType<IdeaStorageMenu>> getMenuType() {
        return Optional.of(ModMenuTypes.IDEA_STORAGE.get());
    }

    @Override
    public RecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
        return RecipeTypes.CRAFTING;
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(IdeaStorageMenu container, RecipeHolder<CraftingRecipe> recipe,
            IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
        // クラフトウィンドウが閉じている時は転送しない
        if (!container.isCraftOpen()) {
            return null;
        }
        List<IRecipeSlotView> inputSlots = recipeSlots.getSlotViews(RecipeIngredientRole.INPUT);
        // 3x3 マトリクス分だけ扱う(スロット36..44 に対応)
        int craftSlotCount = IdeaStorageMenu.CRAFT_RESULT_SLOT - IdeaStorageMenu.CRAFT_MATRIX_START;
        int inputCount = Math.min(inputSlots.size(), craftSlotCount);
        // 供給元スロット(0..35)の割り当て計画。-1 は未割り当て
        int[] sources = new int[inputCount];
        Arrays.fill(sources, -1);
        boolean[] reserved = new boolean[IdeaStorageMenu.CRAFT_MATRIX_START];
        List<IRecipeSlotView> missing = new ArrayList<>();
        for (int i = 0; i < inputCount; i++) {
            IRecipeSlotView slotView = inputSlots.get(i);
            if (slotView.isEmpty()) {
                continue;
            }
            ItemStack current = container.getSlot(IdeaStorageMenu.CRAFT_MATRIX_START + i).getItem();
            if (!current.isEmpty() && matches(current, slotView)) {
                // 既に同じ材料が置かれている
                continue;
            }
            int source = findSourceSlot(container, slotView, reserved);
            if (source >= 0) {
                sources[i] = source;
            } else {
                missing.add(slotView);
            }
        }
        if (!doTransfer) {
            return missing.isEmpty()
                    ? null
                    : helper.createUserErrorForMissingSlots(
                            Component.translatable("gui.psitweaks.idea_storage.transfer_missing"), missing);
        }
        MultiPlayerGameMode gameMode = Minecraft.getInstance().gameMode;
        if (gameMode == null) {
            return helper.createInternalError();
        }
        for (int i = 0; i < inputCount; i++) {
            int source = sources[i];
            if (source < 0) {
                continue;
            }
            int craftSlotIndex = IdeaStorageMenu.CRAFT_MATRIX_START + i;
            // 別のアイテムが残っているマスは先に shift クリックでインベントリへ退避する
            Slot craftSlot = container.getSlot(craftSlotIndex);
            if (!craftSlot.getItem().isEmpty() && !matches(craftSlot.getItem(), inputSlots.get(i))) {
                gameMode.handleInventoryMouseClick(container.containerId, craftSlotIndex, 0, ClickType.QUICK_MOVE, player);
            }
            if (!container.getSlot(craftSlotIndex).getItem().isEmpty()) {
                // 退避しきれなかった(インベントリ満杯等)場合はそのマスを諦める
                continue;
            }
            // 拾う → マトリクスに1個置く → 残りを元のスロットへ戻す
            gameMode.handleInventoryMouseClick(container.containerId, source, 0, ClickType.PICKUP, player);
            gameMode.handleInventoryMouseClick(container.containerId, craftSlotIndex, 1, ClickType.PICKUP, player);
            gameMode.handleInventoryMouseClick(container.containerId, source, 0, ClickType.PICKUP, player);
        }
        return null;
    }

    /** スロットのスタックがレシピスロットのいずれかの候補と一致するか(コンポーネント一致を優先)。 */
    private static boolean matches(ItemStack stack, IRecipeSlotView slotView) {
        return slotView.getItemStacks().anyMatch(candidate ->
                ItemStack.isSameItemSameComponents(stack, candidate) || ItemStack.isSameItem(stack, candidate));
    }

    /**
     * プレイヤーインベントリ(スロット0..35)から供給元を探す。
     * コンポーネント完全一致を優先し、無ければアイテム一致に緩和する。見つかったスロットは予約済みにする。
     */
    private static int findSourceSlot(IdeaStorageMenu container, IRecipeSlotView slotView, boolean[] reserved) {
        int looseMatch = -1;
        for (int i = 0; i < IdeaStorageMenu.CRAFT_MATRIX_START; i++) {
            if (reserved[i]) {
                continue;
            }
            ItemStack stack = container.getSlot(i).getItem();
            if (stack.isEmpty()) {
                continue;
            }
            List<ItemStack> candidates = slotView.getItemStacks().toList();
            boolean exact = false;
            boolean sameItem = false;
            for (ItemStack candidate : candidates) {
                if (ItemStack.isSameItemSameComponents(stack, candidate)) {
                    exact = true;
                    break;
                }
                if (ItemStack.isSameItem(stack, candidate)) {
                    sameItem = true;
                }
            }
            if (exact) {
                reserved[i] = true;
                return i;
            }
            if (sameItem && looseMatch < 0) {
                looseMatch = i;
            }
        }
        if (looseMatch >= 0) {
            reserved[looseMatch] = true;
        }
        return looseMatch;
    }
}
