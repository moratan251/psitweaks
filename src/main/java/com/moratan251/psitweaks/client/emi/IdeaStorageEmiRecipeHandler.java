package com.moratan251.psitweaks.client.emi;

import com.moratan251.psitweaks.client.gui.IdeaStorageCraftingTransferPlanner;
import com.moratan251.psitweaks.common.menu.IdeaStorageMenu;
import com.moratan251.psitweaks.common.network.MessageIdeaStorageFillCrafting;
import com.moratan251.psitweaks.common.network.MessageIdeaStorageSync;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Widget;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import com.moratan251.psitweaks.common.network.IdeaStorageNetwork;

/** プレイヤーインベントリとイデアストレージを材料源にするEMI作業台レシピ転送。 */
public class IdeaStorageEmiRecipeHandler implements EmiRecipeHandler<IdeaStorageMenu> {
    @Override
    public EmiPlayerInventory getInventory(AbstractContainerScreen<IdeaStorageMenu> screen) {
        IdeaStorageMenu menu = screen.getMenu();
        List<EmiStack> available = new ArrayList<>();
        for (int slot = 0; slot < IdeaStorageMenu.CRAFT_RESULT_SLOT; slot++) {
            ItemStack stack = menu.getSlot(slot).getItem();
            if (!stack.isEmpty()) {
                available.add(EmiStack.of(stack, stack.getCount()));
            }
        }
        for (MessageIdeaStorageSync.Entry entry : menu.clientStorageEntries()) {
            available.add(EmiStack.of(entry.template(), entry.count()));
        }
        return new EmiPlayerInventory(available);
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING
                && recipe.getInputs().size() <= MessageIdeaStorageFillCrafting.CRAFT_SLOT_COUNT;
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<IdeaStorageMenu> context) {
        return context.getScreenHandler().isCraftOpen() && plan(recipe, context.getScreenHandler()).complete();
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<IdeaStorageMenu> context) {
        if (!context.getScreenHandler().isCraftOpen()) {
            return false;
        }
        IdeaStorageCraftingTransferPlanner.Plan plan = plan(recipe, context.getScreenHandler());
        if (!plan.complete()) {
            return false;
        }
        IdeaStorageNetwork.sendToServer(MessageIdeaStorageFillCrafting.fromTemplates(context.getScreenHandler(), plan.templates()));
        return true;
    }

    @Override
    public void render(EmiRecipe recipe, EmiCraftContext<IdeaStorageMenu> context,
                       List<Widget> widgets, GuiGraphics guiGraphics) {
        if (context.getScreenHandler().isCraftOpen()) {
            StandardRecipeHandler.renderMissing(recipe, context.getInventory(), widgets, guiGraphics);
        }
    }

    private static IdeaStorageCraftingTransferPlanner.Plan plan(EmiRecipe recipe, IdeaStorageMenu menu) {
        List<List<ItemStack>> candidates = new ArrayList<>(recipe.getInputs().size());
        for (EmiIngredient input : recipe.getInputs()) {
            if (input.isEmpty()) {
                candidates.add(List.of());
                continue;
            }
            candidates.add(input.getEmiStacks().stream()
                    .map(EmiStack::getItemStack)
                    .filter(stack -> !stack.isEmpty())
                    .map(stack -> stack.copyWithCount(1))
                    .toList());
        }
        return IdeaStorageCraftingTransferPlanner.plan(menu, candidates);
    }
}
