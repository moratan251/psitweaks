package com.moratan251.psitweaks.client.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** 量子ファクターの落下変換をEMIへ公開する情報レシピ。 */
final class GravitonFactorEmiRecipe extends BasicEmiRecipe {
    private static final int WIDTH = 176;
    private static final int HEIGHT = 60;

    GravitonFactorEmiRecipe(EmiRecipeCategory category, ResourceLocation id,
                            ItemStack input, ItemStack output) {
        super(category, id, WIDTH, HEIGHT);
        inputs.add(EmiStack.of(input));
        outputs.add(EmiStack.of(output));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addText(Component.translatable("jei.psitweaks.graviton_factor_acquisition.fall"),
                        WIDTH / 2, 8, 0x202020, false)
                .horizontalAlign(TextWidget.Alignment.CENTER);
        widgets.addSlot(inputs.getFirst(), 32, 34);
        widgets.addFillingArrow(76, 34, 2_000);
        widgets.addSlot(outputs.getFirst(), 132, 34).recipeContext(this);
    }
}
