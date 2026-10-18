package com.moratan251.psitweaks.common.registries;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.recipe.EnchantedBookIngredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class PsitweaksIngredientTypes {
    public static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, Psitweaks.MOD_ID);

    public static final DeferredHolder<IngredientType<?>, IngredientType<EnchantedBookIngredient>> ENCHANTED_BOOK =
            INGREDIENT_TYPES.register("enchanted_book",
                    () -> new IngredientType<>(EnchantedBookIngredient.CODEC, EnchantedBookIngredient.STREAM_CODEC));

    private PsitweaksIngredientTypes() {
    }

    public static void register(IEventBus eventBus) {
        INGREDIENT_TYPES.register(eventBus);
    }
}
