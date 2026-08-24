package com.moratan251.psitweaks.client.compat;

import com.moratan251.psitweaks.common.compat.MekanismCompat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Mekanism未導入クライアントからChemical型を隔離する表示用境界。 */
public final class IdeaStorageChemicalClientCompat {
    private IdeaStorageChemicalClientCompat() {
    }

    public static Component displayName(ResourceLocation chemicalId) {
        if (chemicalId != null && MekanismCompat.isMekanismLoaded()) {
            return MekanismClientIntegration.ideaStorageChemicalName(chemicalId);
        }
        return Component.literal(chemicalId == null ? "Unknown Chemical" : chemicalId.toString());
    }

    public static boolean render(GuiGraphics guiGraphics, ResourceLocation chemicalId, int x, int y) {
        return chemicalId != null && MekanismCompat.isMekanismLoaded()
                && MekanismClientIntegration.renderIdeaStorageChemical(guiGraphics, chemicalId, x, y);
    }
}
