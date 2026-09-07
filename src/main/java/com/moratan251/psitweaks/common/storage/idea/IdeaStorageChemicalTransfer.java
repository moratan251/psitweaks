package com.moratan251.psitweaks.common.storage.idea;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Mekanism固有型をcommon境界へ漏らさないChemical容器転送plan。 */
public record IdeaStorageChemicalTransfer(ItemStack resultContainer, ResourceLocation chemicalId,
                                          long amount, boolean intoStorage) {
}
