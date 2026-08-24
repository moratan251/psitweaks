package com.moratan251.psitweaks.client.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** スペルの物質変成をEMIへ公開する1入力1出力レシピ。 */
final class MaterialMutationEmiRecipe extends BasicEmiRecipe {
    private static final int WIDTH = 144;
    private static final int HEIGHT = 54;

    MaterialMutationEmiRecipe(EmiRecipeCategory category, ResourceLocation id,
                              ItemStack input, ItemStack output) {
        super(category, id, WIDTH, HEIGHT);
        inputs.add(EmiStack.of(input));
        outputs.add(EmiStack.of(output));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(inputs.getFirst(), 34, 18);
        widgets.addFillingArrow(62, 18, 2_000);
        widgets.addSlot(outputs.getFirst(), 94, 18).recipeContext(this);
    }
}
