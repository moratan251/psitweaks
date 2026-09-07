package com.moratan251.psitweaks.client.jei;

import com.moratan251.psitweaks.client.gui.IdeaStorageCraftingTransferPlanner;
import com.moratan251.psitweaks.common.menu.IdeaStorageMenu;
import com.moratan251.psitweaks.client.gui.machine.ModMenuTypes;
import com.moratan251.psitweaks.common.network.MessageIdeaStorageFillCrafting;
import java.util.ArrayList;
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
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import com.moratan251.psitweaks.common.network.IdeaStorageNetwork;
import org.jetbrains.annotations.Nullable;

/** プレイヤーインベントリとイデアストレージを材料源にするJEI作業台レシピ転送。 */
public class IdeaStorageJeiRecipeTransferHandler
        implements IRecipeTransferHandler<IdeaStorageMenu, CraftingRecipe> {
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
    public RecipeType<CraftingRecipe> getRecipeType() {
        return RecipeTypes.CRAFTING;
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(IdeaStorageMenu container, CraftingRecipe recipe,
            IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
        if (!container.isCraftOpen()) {
            return null;
        }
        List<IRecipeSlotView> inputSlots = recipeSlots.getSlotViews(RecipeIngredientRole.INPUT);
        if (inputSlots.size() > MessageIdeaStorageFillCrafting.CRAFT_SLOT_COUNT) {
            return helper.createInternalError();
        }

        List<List<ItemStack>> candidates = new ArrayList<>(inputSlots.size());
        for (IRecipeSlotView inputSlot : inputSlots) {
            candidates.add(inputSlot.isEmpty()
                    ? List.of()
                    : inputSlot.getItemStacks()
                            .filter(stack -> !stack.isEmpty())
                            .map(stack -> stack.copyWithCount(1))
                            .toList());
        }
        IdeaStorageCraftingTransferPlanner.Plan plan =
                IdeaStorageCraftingTransferPlanner.plan(container, candidates);
        if (!plan.complete()) {
            List<IRecipeSlotView> missing = plan.missingSlots().stream()
                    .filter(index -> index >= 0 && index < inputSlots.size())
                    .map(inputSlots::get)
                    .toList();
            return missing.isEmpty()
                    ? helper.createInternalError()
                    : helper.createUserErrorForMissingSlots(
                            Component.translatable("gui.psitweaks.idea_storage.transfer_missing"), missing);
        }
        if (doTransfer) {
            IdeaStorageNetwork.sendToServer(MessageIdeaStorageFillCrafting.fromTemplates(container, plan.templates()));
        }
        return null;
    }
}
