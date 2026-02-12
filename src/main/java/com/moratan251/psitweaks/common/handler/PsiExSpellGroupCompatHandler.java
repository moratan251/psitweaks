package com.moratan251.psitweaks.common.handler;

import com.moratan251.psitweaks.Psitweaks;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.spell.SpellPiece;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = Psitweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PsiExSpellGroupCompatHandler {

    private static final String PSI_EX_NAMESPACE = "psi_ex";

    @SubscribeEvent
    public static void onLoadComplete(FMLLoadCompleteEvent event) {
        event.enqueueWork(() -> addMissingPsiExPieceGroups(PsiAPI.getSpellPieceRegistry()));
    }

    static void addMissingPsiExPieceGroups(MappedRegistry<Class<? extends SpellPiece>> registry) {
        for (ResourceLocation pieceId : registry.keySet()) {
            if (!PSI_EX_NAMESPACE.equals(pieceId.getNamespace())) {
                continue;
            }

            Optional<Class<? extends SpellPiece>> pieceClass = registry.getOptional(pieceId);
            if (pieceClass.isEmpty()) {
                continue;
            }
            if (PsiAPI.getGroupForPiece(pieceClass.get()) != null) {
                continue;
            }

            PsiAPI.addPieceToGroup(pieceClass.get(), pieceId, false);
        }
    }
}
