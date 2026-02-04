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

            PsiAPI.registerSpellPieceAndTexture(ResourceLocation.fromNamespaceAndPath("psitweaks", "trick_interact_block"), PieceTrickInteractBlock.class);
            PsiAPI.addPieceToGroup(PieceTrickInteractBlock.class, ResourceLocation.fromNamespaceAndPath("psitweaks", "trick_interact_block"), false);

            PsiAPI.registerSpellPieceAndTexture(ResourceLocation.fromNamespaceAndPath("psitweaks", "trick_store_entity"), PieceTrickStoreEntityUUID.class);
            PsiAPI.addPieceToGroup(PieceTrickStoreEntityUUID.class, ResourceLocation.fromNamespaceAndPath("psitweaks", "trick_store_entity"), false);

            PsiAPI.registerSpellPieceAndTexture(ResourceLocation.fromNamespaceAndPath("psitweaks", "selector_stored_entity"), PieceSelectorStoredEntity.class);
            PsiAPI.addPieceToGroup(PieceSelectorStoredEntity.class, ResourceLocation.fromNamespaceAndPath("psitweaks", "selector_stored_entity"), false);

            PsiAPI.registerSpellPieceAndTexture(ResourceLocation.fromNamespaceAndPath("psitweaks", "trick_dispel"), PieceTrickDispel.class);
            PsiAPI.addPieceToGroup(PieceTrickDispel.class, ResourceLocation.fromNamespaceAndPath("psitweaks", "trick_dispel"), false);

            PsiAPI.registerSpellPieceAndTexture(ResourceLocation.fromNamespaceAndPath("psitweaks", "trick_dispel_beneficial"), PieceTrickDispelBeneficial.class);
            PsiAPI.addPieceToGroup(PieceTrickDispelBeneficial.class, ResourceLocation.fromNamespaceAndPath("psitweaks", "trick_dispel_beneficial"), false);

            PsiAPI.registerSpellPieceAndTexture(ResourceLocation.fromNamespaceAndPath("psitweaks", "trick_dispel_non_beneficial"), PieceTrickDispelNonBeneficial.class);
            PsiAPI.addPieceToGroup(PieceTrickDispelNonBeneficial.class, ResourceLocation.fromNamespaceAndPath("psitweaks", "trick_dispel_non_beneficial"), false);

            PsiAPI.registerSpellPieceAndTexture(ResourceLocation.fromNamespaceAndPath("psitweaks", "trick_cocytus"), PieceTrickCocytus.class);
            PsiAPI.addPieceToGroup(PieceTrickCocytus.class, ResourceLocation.fromNamespaceAndPath("psitweaks", "trick_cocytus"), false);

            PsiAPI.registerSpellPieceAndTexture(ResourceLocation.fromNamespaceAndPath("psitweaks", "trick_supply_fe"), PieceTrickSupplyFE.class);
            PsiAPI.addPieceToGroup(PieceTrickSupplyFE.class, ResourceLocation.fromNamespaceAndPath("psitweaks", "trick_supply_fe"), false);

            PsiAPI.registerSpellPieceAndTexture(ResourceLocation.fromNamespaceAndPath("psitweaks", "trick_time_accelerate"), PieceTrickTimeAccelerate.class);
            PsiAPI.addPieceToGroup(PieceTrickTimeAccelerate.class, ResourceLocation.fromNamespaceAndPath("psitweaks", "trick_time_accelerate"), false);

            PsiAPI.registerSpellPieceAndTexture(ResourceLocation.fromNamespaceAndPath("psitweaks", "trick_phonon_maser"), PieceTrickPhononMaser.class);
            PsiAPI.addPieceToGroup(PieceTrickPhononMaser.class, ResourceLocation.fromNamespaceAndPath("psitweaks", "trick_phonon_maser"), false);

            PsiAPI.registerSpellPieceAndTexture(ResourceLocation.fromNamespaceAndPath("psitweaks", "trick_supreme_infusion"), PieceTrickSupremeInfusion.class);
            PsiAPI.addPieceToGroup(PieceTrickSupremeInfusion.class, ResourceLocation.fromNamespaceAndPath("psitweaks", "trick_supreme_infusion"), false);

            PsiAPI.registerSpellPieceAndTexture(ResourceLocation.fromNamespaceAndPath("psitweaks", "trick_molecular_divider"), PieceTrickMolecularDivider.class);
            PsiAPI.addPieceToGroup(PieceTrickMolecularDivider.class, ResourceLocation.fromNamespaceAndPath("psitweaks", "trick_molecular_divider"), false);


        });
    }
}
