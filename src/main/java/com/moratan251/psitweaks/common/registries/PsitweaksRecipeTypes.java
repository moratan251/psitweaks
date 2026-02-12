package com.moratan251.psitweaks.common.registries;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.recipe.ProgramResearchRecipe;
import com.moratan251.psitweaks.common.recipe.SculkEroderRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PsitweaksRecipeTypes {

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, Psitweaks.MOD_ID);

    public static final RegistryObject<RecipeType<SculkEroderRecipe>> SCULK_ERODER =
            RECIPE_TYPES.register("sculk_eroder", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return Psitweaks.MOD_ID + ":sculk_eroder";
                }
            });

    public static final RegistryObject<RecipeType<ProgramResearchRecipe>> PROGRAM_RESEARCH =
            RECIPE_TYPES.register("program_research", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return Psitweaks.MOD_ID + ":program_research";
                }
            });

    private PsitweaksRecipeTypes() {
    }

    public static void register(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
    }
}
