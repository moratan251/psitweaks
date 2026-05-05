package com.moratan251.psitweaks.common.registries;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.recipe.ProgramResearchRecipe;
import com.moratan251.psitweaks.common.recipe.SculkEroderRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PsitweaksRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, Psitweaks.MOD_ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<SculkEroderRecipe>> SCULK_ERODER =
            RECIPE_TYPES.register("sculk_eroder", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return Psitweaks.location("sculk_eroder").toString();
                }
            });
    public static final DeferredHolder<RecipeType<?>, RecipeType<ProgramResearchRecipe>> PROGRAM_RESEARCH =
            RECIPE_TYPES.register("program_research", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return Psitweaks.location("program_research").toString();
                }
            });

    private PsitweaksRecipeTypes() {
    }

    public static void register(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
    }
}
