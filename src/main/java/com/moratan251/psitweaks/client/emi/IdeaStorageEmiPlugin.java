package com.moratan251.psitweaks.client.emi;
import com.moratan251.psitweaks.client.gui.IdeaStorageScreen;
import com.moratan251.psitweaks.client.gui.machine.ModMenuTypes;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.api.widget.Bounds;
import net.minecraft.client.renderer.Rect2i;
@EmiEntrypoint
public final class IdeaStorageEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
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

}
