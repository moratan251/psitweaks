package com.moratan251.psitweaks.client.emi;

import com.moratan251.psitweaks.common.menu.IdeaStorageMenu;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import java.util.List;
import net.minecraft.world.inventory.Slot;

/**
 * イデアストレージのクラフトウィンドウへのレシピ転送(EMI)。
 * 転送元はプレイヤーインベントリ(スロット0..35)のみ。ストレージからの直接引き出しは行わない。
 * 実際の移動は StandardRecipeHandler の標準実装(スロットクリックのシミュレート)に任せる。
 */
public class IdeaStorageEmiRecipeHandler implements StandardRecipeHandler<IdeaStorageMenu> {
    @Override
    public List<Slot> getInputSources(IdeaStorageMenu menu) {
        return menu.slots.subList(0, IdeaStorageMenu.CRAFT_MATRIX_START);
    }

    @Override
    public List<Slot> getCraftingSlots(IdeaStorageMenu menu) {
        return menu.slots.subList(IdeaStorageMenu.CRAFT_MATRIX_START, IdeaStorageMenu.CRAFT_RESULT_SLOT);
    }

    @Override
    public Slot getOutputSlot(IdeaStorageMenu menu) {
        return menu.slots.get(IdeaStorageMenu.CRAFT_RESULT_SLOT);
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING;
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<IdeaStorageMenu> context) {
        // クラフトウィンドウが閉じている時は転送しない
        return context.getScreenHandler().isCraftOpen() && StandardRecipeHandler.super.canCraft(recipe, context);
    }
}
