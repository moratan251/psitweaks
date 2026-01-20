package com.moratan251.psitweaks.client.proxy;

import com.moratan251.psitweaks.client.guis.GuiFlashRing;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import com.moratan251.psitweaks.common.proxy.IProxyPsitweaks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.spell.ISpellAcceptor;

public class ClientProxyPsitweaks implements IProxyPsitweaks {

    @OnlyIn(Dist.CLIENT)
    public void initializeClient(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ResourceLocation activeProperty =  ResourceLocation.fromNamespaceAndPath("psitweaks", "active");
            ItemProperties.register(PsitweaksItems.FLASH_RING.get(), activeProperty,
                    (stack, level, entity, seed) -> {
                        // Spell がある場合は 1.0、ない場合は 0.0
                        return stack.getCapability(PsiAPI.SPELL_ACCEPTOR_CAPABILITY)
                                .map(ISpellAcceptor::containsSpell)
                                .orElse(false) ? 1.0F : 0.0F;
                    }
            );
        });
    }

    public void openFlashRingGUI(ItemStack stack) {
        Minecraft.getInstance().setScreen(new GuiFlashRing(stack));
    }


}

