package com.moratan251.psitweaks.common.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemPsitweaksProgram extends Item {

    private final boolean blankProgram;

    public ItemPsitweaksProgram(Properties properties, boolean blankProgram) {
        super(properties);
        this.blankProgram = blankProgram;
    }

    public boolean isBlankProgram() {
        return blankProgram;
    }

    public static boolean isBlankProgram(ItemStack stack) {
        return stack.getItem() instanceof ItemPsitweaksProgram program && program.isBlankProgram();
    }

    public static boolean isNonBlankProgram(ItemStack stack) {
        return stack.getItem() instanceof ItemPsitweaksProgram program && !program.isBlankProgram();
    }
}
