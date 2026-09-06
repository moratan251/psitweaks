package com.moratan251.psitweaks.client.jei;
import com.moratan251.psitweaks.client.gui.IdeaStorageScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import java.util.List;
import java.util.Optional;
@JeiPlugin
public final class IdeaStorageJeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() { return ResourceLocation.fromNamespaceAndPath("psitweaks", "idea_storage"); }
    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        IIngredientManager ingredientManager = registration.getJeiHelpers().getIngredientManager();
        registration.addGuiContainerHandler(IdeaStorageScreen.class, new IGuiContainerHandler<>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(IdeaStorageScreen screen) {
                if (screen.getMenu().isCraftOpen()) {
                    return List.of(screen.getCraftPanelArea(), screen.getSideButtonArea());
                }
                return List.of(screen.getSideButtonArea());
            }

            @Override
            public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(
                    IdeaStorageScreen screen, double mouseX, double mouseY) {
                return screen.getStorageItemUnderMouse(mouseX, mouseY)
                        .flatMap(reference -> ingredientManager.createClickableIngredient(
                                VanillaTypes.ITEM_STACK, reference.stack(), reference.area(), false))
                        .map(ingredient -> ingredient);
            }
        });
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(
                new IdeaStorageJeiRecipeTransferHandler(registration.getTransferHelper()), RecipeTypes.CRAFTING);
    }

}
