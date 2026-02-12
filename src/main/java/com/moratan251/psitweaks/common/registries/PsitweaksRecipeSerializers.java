package com.moratan251.psitweaks.common.registries;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.recipe.ProgramResearchRecipe;
import com.moratan251.psitweaks.common.recipe.SculkEroderRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PsitweaksRecipeSerializers {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Psitweaks.MOD_ID);

    public static final RegistryObject<RecipeSerializer<SculkEroderRecipe>> SCULK_ERODER =
            RECIPE_SERIALIZERS.register("sculk_eroder", SculkEroderRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<ProgramResearchRecipe>> PROGRAM_RESEARCH =
            RECIPE_SERIALIZERS.register("program_research", ProgramResearchRecipe.Serializer::new);

    private PsitweaksRecipeSerializers() {
    }

    public static void register(IEventBus eventBus) {
        RECIPE_SERIALIZERS.register(eventBus);
    }
}
