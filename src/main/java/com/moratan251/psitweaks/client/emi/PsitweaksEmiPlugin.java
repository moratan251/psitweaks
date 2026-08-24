package com.moratan251.psitweaks.client.emi;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.client.gui.IdeaStorageScreen;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.handler.MaterialMutationRecipeHandler;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import com.moratan251.psitweaks.common.menu.ModMenuTypes;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.api.widget.Bounds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

/**
 * Psitweaks の EMI プラグイン。JEI 側(PsitweaksJeiPlugin)と同等の占有領域通知とレシピ転送を登録する。
 */
@EmiEntrypoint
public class PsitweaksEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        registerIdeaStorageIntegration(registry);
        registerCommonRecipes(registry);
        if (MekanismCompat.isMekanismLoaded()) {
            PsitweaksMekanismEmiPlugin.register(registry);
        }
    }

    private static void registerIdeaStorageIntegration(EmiRegistry registry) {
        // サイドボタン列は常時、クラフトパネルは開いている間だけ占有領域として通知する。
        registry.addExclusionArea(IdeaStorageScreen.class, (screen, consumer) -> {
            Rect2i sideButtons = screen.getSideButtonArea();
            consumer.accept(new Bounds(sideButtons.getX(), sideButtons.getY(), sideButtons.getWidth(), sideButtons.getHeight()));
            if (screen.getMenu().isCraftOpen()) {
                Rect2i panel = screen.getCraftPanelArea();
                consumer.accept(new Bounds(panel.getX(), panel.getY(), panel.getWidth(), panel.getHeight()));
            }
        });
        registry.addStackProvider(IdeaStorageScreen.class, (screen, mouseX, mouseY) ->
                screen.getStorageItemUnderMouse(mouseX, mouseY)
                        .<EmiStackInteraction>map(reference ->
                                // Recipe/Usageキーからは参照できるが、通常クリックは画面側へ渡す。
                                new EmiStackInteraction(EmiStack.of(reference.stack()), null, false))
                        .orElse(EmiStackInteraction.EMPTY));
        registry.addRecipeHandler(ModMenuTypes.IDEA_STORAGE.get(), new IdeaStorageEmiRecipeHandler());
    }

    private static void registerCommonRecipes(EmiRegistry registry) {
        EmiRecipeCategory materialMutation = new PsitweaksEmiRecipeCategory(
                Psitweaks.location("material_mutation"),
                PsitweaksItems.PROGRAM_MATERIAL_MUTATION.get(),
                "jei.psitweaks.material_mutation"
        );
        registry.addCategory(materialMutation);
        registry.addWorkstation(materialMutation, EmiStack.of(PsitweaksItems.PROGRAM_MATERIAL_MUTATION.get()));
        for (var entry : MaterialMutationRecipeHandler.getAllMutationOutputs().entrySet()) {
            Block inputBlock = entry.getKey();
            ItemStack input = new ItemStack(inputBlock);
            ItemStack output = entry.getValue();
            if (input.isEmpty() || input.is(Items.AIR) || output.isEmpty()) {
                continue;
            }
            ResourceLocation inputId = BuiltInRegistries.BLOCK.getKey(inputBlock);
            ResourceLocation recipeId = Psitweaks.location(
                    "material_mutation/" + inputId.getNamespace() + "/" + inputId.getPath());
            registry.addRecipe(new MaterialMutationEmiRecipe(
                    materialMutation, recipeId, input, output));
        }

        EmiRecipeCategory gravitonFactorAcquisition = new PsitweaksEmiRecipeCategory(
                Psitweaks.location("graviton_factor_acquisition"),
                PsitweaksItems.GRAVITON_FACTOR.get(),
                "jei.psitweaks.graviton_factor_acquisition"
        );
        registry.addCategory(gravitonFactorAcquisition);
        registry.addRecipe(new GravitonFactorEmiRecipe(
                gravitonFactorAcquisition,
                Psitweaks.location("graviton_factor_acquisition"),
                new ItemStack(PsitweaksItems.QUANTUM_FACTOR.get()),
                new ItemStack(PsitweaksItems.GRAVITON_FACTOR.get())
        ));
    }
}
