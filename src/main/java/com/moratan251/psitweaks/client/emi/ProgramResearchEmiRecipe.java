package com.moratan251.psitweaks.client.emi;

import com.moratan251.psitweaks.common.recipe.ProgramResearchRecipe;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;

/** 9入力、触媒、電力、時間を含むプログラム研究のEMI表示。 */
final class ProgramResearchEmiRecipe extends BasicEmiRecipe {
    private static final int WIDTH = 176;
    private static final int HEIGHT = 88;
    private final RecipeHolder<ProgramResearchRecipe> holder;
    private final List<DisplayInput> displayInputs;

    ProgramResearchEmiRecipe(EmiRecipeCategory category, RecipeHolder<ProgramResearchRecipe> holder) {
        super(category, holder.id(), WIDTH, HEIGHT);
        this.holder = holder;
        this.displayInputs = new ArrayList<>(holder.value().getInputs().size());
        for (ProgramResearchRecipe.RequiredInput input : holder.value().getInputs()) {
            EmiIngredient ingredient = EmiIngredient.of(input.ingredient(), input.count());
            displayInputs.add(new DisplayInput(ingredient, !input.consume()));
            if (input.consume()) {
                inputs.add(ingredient);
            } else {
                catalysts.add(ingredient);
            }
        }
        outputs.add(EmiStack.of(holder.value().getOutput()));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        for (int i = 0; i < ProgramResearchRecipe.MAX_INPUT_SLOTS; i++) {
            int x = 8 + (i % 3) * 18;
            int y = 12 + (i / 3) * 18;
            if (i >= displayInputs.size()) {
                widgets.addSlot(x, y);
                continue;
            }
            DisplayInput input = displayInputs.get(i);
            widgets.addSlot(input.ingredient(), x, y).catalyst(input.catalyst());
        }

        ProgramResearchRecipe recipe = holder.value();
        int animationTime = Math.max(1, Math.min(Integer.MAX_VALUE / 50, recipe.getTime())) * 50;
        widgets.addFillingArrow(90, 28, animationTime);
        widgets.addSlot(outputs.getFirst(), 148, 28).recipeContext(this);

        int totalSeconds = Math.max(1, (recipe.getTime() + 19) / 20);
        widgets.addText(Component.translatable(
                        "jei.psitweaks.program_research.energy", recipe.getEnergyPerTick()),
                8, 68, 0x202020, false);
        widgets.addText(Component.translatable(
                        "jei.psitweaks.program_research.time", totalSeconds / 60, totalSeconds % 60),
                8, 79, 0x202020, false);
    }

    @Override
    public RecipeHolder<?> getBackingRecipe() {
        return holder;
    }

    private record DisplayInput(EmiIngredient ingredient, boolean catalyst) {
    }
}
