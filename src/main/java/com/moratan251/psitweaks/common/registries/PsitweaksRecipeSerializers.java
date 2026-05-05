package com.moratan251.psitweaks.common.registries;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.recipe.ProgramDuplicationRecipe;
import com.moratan251.psitweaks.common.recipe.ProgramResearchRecipe;
import com.moratan251.psitweaks.common.recipe.SculkEroderRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PsitweaksRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Psitweaks.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<SculkEroderRecipe>> SCULK_ERODER =
            RECIPE_SERIALIZERS.register("sculk_eroder", SculkEroderRecipe.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ProgramResearchRecipe>> PROGRAM_RESEARCH =
            RECIPE_SERIALIZERS.register("program_research", ProgramResearchRecipe.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<ProgramDuplicationRecipe>> PROGRAM_DUPLICATION =
            RECIPE_SERIALIZERS.register("crafting_special_program_duplication",
                    () -> new SimpleCraftingRecipeSerializer<>(ProgramDuplicationRecipe::new));

    private PsitweaksRecipeSerializers() {
    }

    public static void register(IEventBus eventBus) {
        RECIPE_SERIALIZERS.register(eventBus);
    }
}
