//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.moratan251.psitweaks.common.proxy;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;


public interface IProxyPsitweaks {

    public void registerHandlers();
    void openFlashRingGUI(ItemStack var1);
    void openAutoCasterCustomTickGUI(ItemStack stack, InteractionHand hand);
}
