package com.moratan251.psitweaks.common.spells;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import vazkii.psi.api.PsiAPI;

@Mod.EventBusSubscriber(modid = "psitweaks", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModSpells {


    @SubscribeEvent
    public static void registerPieces(FMLCommonSetupEvent event) {
        System.out.println("Psitweaks: register spells");
        event.enqueueWork(() -> {
            PsiAPI.registerSpellPieceAndTexture(ResourceLocation.fromNamespaceAndPath("psitweaks", "trick_explode_no_destroy"), PieceTrickExplodeNoDestroy.class);
            PsiAPI.addPieceToGroup(PieceTrickExplodeNoDestroy.class, ResourceLocation.fromNamespaceAndPath("psitweaks", "trick_explode_no_destroy"), false);
        });
    }
}
