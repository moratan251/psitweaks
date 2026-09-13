package com.moratan251.psitweaks.client.jei;

import com.moratan251.psitweaks.client.gui.IdeaspaceConnectorScreen;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.storage.connector.ConnectorResource;
import com.moratan251.psitweaks.common.storage.idea.FluidResourceKey;
import com.moratan251.psitweaks.common.storage.idea.ItemResourceKey;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

final class ConnectorGhostIngredientHandler implements IGhostIngredientHandler<IdeaspaceConnectorScreen> {
    @Override
    public <I> List<Target<I>> getTargetsTyped(IdeaspaceConnectorScreen screen, ITypedIngredient<I> ingredient, boolean doStart) {
        ConnectorResource resource = resource(ingredient.getIngredient());
        if (resource.kind() == ConnectorResource.Kind.EMPTY) return List.of();
        List<Target<I>> targets = new ArrayList<>();
        List<Rect2i> areas = screen.publishedSlotAreas();
        for (int i = 0; i < areas.size(); i++) {
            int slot = i;
            Rect2i area = areas.get(i);
            targets.add(new Target<>() {
                @Override public Rect2i getArea() { return area; }
                @Override public void accept(I ignored) { screen.acceptGhostResource(slot, resource); }
            });
        }
        return targets;
    }

    private static ConnectorResource resource(Object ingredient) {
        if (ingredient instanceof ItemStack stack)
            return ItemResourceKey.of(stack).map(ConnectorResource::item).orElse(ConnectorResource.EMPTY);
        if (ingredient instanceof FluidStack stack)
            return FluidResourceKey.of(stack).map(ConnectorResource::fluid).orElse(ConnectorResource.EMPTY);
        return MekanismCompat.isMekanismLoaded() ? PsitweaksMekanismJeiPlugin.connectorResource(ingredient) : ConnectorResource.EMPTY;
    }

    @Override public void onComplete() { }
}
