package com.moratan251.psitweaks.client.proxy;

import com.moratan251.psitweaks.client.gui.machine.GuiAutoCasterCustomTick;
import com.moratan251.psitweaks.client.gui.machine.GuiFlashRing;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import com.moratan251.psitweaks.common.proxy.IProxyPsitweaks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.spell.ISpellAcceptor;
import vazkii.psi.client.model.ModelCAD;
import vazkii.psi.common.Psi;
import vazkii.psi.common.item.base.ModItems;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(
        value = {Dist.CLIENT},
        modid = "psitweaks",
        bus = Mod.EventBusSubscriber.Bus.MOD
)

@SuppressWarnings("removal")
public class ClientProxyPsitweaks implements IProxyPsitweaks {
    public void registerHandlers() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::modelBake);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::addCADModels);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerItemColors);
    }


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

    @Override
    public void openAutoCasterCustomTickGUI(ItemStack stack, InteractionHand hand) {
        Minecraft.getInstance().setScreen(new GuiAutoCasterCustomTick(stack, hand));
    }

    public void modelBake(ModelEvent.ModifyBakingResult event) {
        event.getModels().computeIfPresent(new ModelResourceLocation(ForgeRegistries.ITEMS.getKey(ModItems.cad), "inventory"), (k, oldModel) -> new ModelCAD());
    }

    public void addCADModels(ModelEvent.RegisterAdditional event) {
        event.register(ResourceLocation.fromNamespaceAndPath("psi", "item/cad_alloy_psion"));
        event.register(ResourceLocation.fromNamespaceAndPath("psi", "item/cad_chaotic_psimetal"));
        event.register(ResourceLocation.fromNamespaceAndPath("psi", "item/cad_flashmetal"));
        event.register(ResourceLocation.fromNamespaceAndPath("psi", "item/cad_heavy_psimetal_alpha"));
        event.register(ResourceLocation.fromNamespaceAndPath("psi", "item/cad_heavy_psimetal_beta"));
        event.register(ResourceLocation.fromNamespaceAndPath("psi", "item/cad_psycheonicmetal"));
        event.register(ResourceLocation.fromNamespaceAndPath("psi", "item/cad_inline_1"));
        event.register(ResourceLocation.fromNamespaceAndPath("psi", "item/cad_inline_2"));
        event.register(ResourceLocation.fromNamespaceAndPath("psi", "item/cad_inline_3"));


    }

    public void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> {
            if (tintIndex != 1) {
                return -1;
            }

            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null) {
                return 1295871;
            }

            ItemStack cadStack = PsiAPI.getPlayerCAD(minecraft.player);
            return cadStack.isEmpty() ? 1295871 : Psi.proxy.getColorForCAD(cadStack);
        }, PsitweaksItems.INLINE_CASTER.get(), PsitweaksItems.SECONDARY_CASTER.get(), PsitweaksItems.PARALLEL_CASTER.get());
    }


}

