package com.moratan251.psitweaks.common.recipe;

import com.moratan251.psitweaks.common.registries.PsitweaksIngredientTypes;
import com.mojang.serialization.MapCodec;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

/**
 * An enchanted book that stores {@code enchantment} at any level, whatever else it stores. Vanilla and NeoForge
 * ingredients can only match a book's enchantments exactly. In JSON:
 * {@code {"type": "psitweaks:enchanted_book", "enchantment": "minecraft:loyalty"}}.
 */
public record EnchantedBookIngredient(Holder<Enchantment> enchantment) implements ICustomIngredient {
    public static final MapCodec<EnchantedBookIngredient> CODEC = Enchantment.CODEC.fieldOf("enchantment")
            .xmap(EnchantedBookIngredient::new, EnchantedBookIngredient::enchantment);
    public static final StreamCodec<RegistryFriendlyByteBuf, EnchantedBookIngredient> STREAM_CODEC =
            Enchantment.STREAM_CODEC.map(EnchantedBookIngredient::new, EnchantedBookIngredient::enchantment);

    @Override
    public boolean test(ItemStack stack) {
        return stack.is(Items.ENCHANTED_BOOK)
                && stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).getLevel(enchantment) > 0;
    }

    /** A book for each level, for recipe viewers to cycle through. */
    @Override
    public Stream<ItemStack> getItems() {
        Enchantment value = enchantment.value();
        return IntStream.rangeClosed(value.getMinLevel(), value.getMaxLevel())
                .mapToObj(level -> EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, level)));
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return PsitweaksIngredientTypes.ENCHANTED_BOOK.get();
    }
}
