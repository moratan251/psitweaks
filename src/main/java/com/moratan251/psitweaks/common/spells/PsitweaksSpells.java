package com.moratan251.psitweaks.common.spells;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import vazkii.psi.api.PsiAPI;

@Mod.EventBusSubscriber(modid = "psitweaks", bus = Mod.EventBusSubscriber.Bus.MOD)
public class PsitweaksSpells {


    @SubscribeEvent
    public static void registerPieces(FMLCommonSetupEvent event) {
       // System.out.println("Psitweaks: register spells");
        event.enqueueWork(() -> {
            PsiAPI.registerSpellPieceAndTexture(ResourceLocation.fromNamespaceAndPath("psitweaks", "trick_explode_no_destroy"), PieceTrickExplodeNoDestroy.class);
            PsiAPI.addPieceToGroup(PieceTrickExplodeNoDestroy.class, ResourceLocation.fromNamespaceAndPath("psitweaks", "trick_explode_no_destroy"), false);

            PsiAPI.registerSpellPieceAndTexture(ResourceLocation.fromNamespaceAndPath("psitweaks", "trick_parade"), PieceTrickParade.class);
            PsiAPI.addPieceToGroup(PieceTrickParade.class, ResourceLocation.fromNamespaceAndPath("psitweaks", "trick_parade"), false);

            PsiAPI.registerSpellPieceAndTexture(ResourceLocation.fromNamespaceAndPath("psitweaks", "trick_flight"), PieceTrickFlight.class);
            PsiAPI.addPieceToGroup(PieceTrickFlight.class, ResourceLocation.fromNamespaceAndPath("psitweaks", "trick_flight"), false);

            PsiAPI.registerSpellPieceAndTexture(ResourceLocation.fromNamespaceAndPath("psitweaks", "trick_hardening"), PieceTrickHardening.class);
            PsiAPI.addPieceToGroup(PieceTrickHardening.class, ResourceLocation.fromNamespaceAndPath("psitweaks", "trick_hardening"), false);

            PsiAPI.registerSpellPieceAndTexture(ResourceLocation.fromNamespaceAndPath("psitweaks", "trick_barrier"), PieceTrickBarrier.class);
            PsiAPI.addPieceToGroup(PieceTrickBarrier.class, ResourceLocation.fromNamespaceAndPath("psitweaks", "trick_barrier"), false);

          //  PsiAPI.registerSpellPieceAndTexture(ResourceLocation.fromNamespaceAndPath("psitweaks", "trick_interact_block"), PieceTrickInteractBlock.class);
          //  PsiAPI.addPieceToGroup(PieceTrickInteractBlock.class, ResourceLocation.fromNamespaceAndPath("psitweaks", "trick_interact_block"), false);
        });
    }
}
