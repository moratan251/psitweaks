package com.moratan251.psitweaks.client.emi;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

/** 既存JEI翻訳を共有するPsitweaks固有EMIカテゴリ。 */
final class PsitweaksEmiRecipeCategory extends EmiRecipeCategory {
    private final Component title;

    PsitweaksEmiRecipeCategory(ResourceLocation id, ItemLike icon, String titleKey) {
        super(id, EmiStack.of(icon));
        this.title = Component.translatable(titleKey);
    }

    @Override
    public Component getName() {
        return title;
    }
}
