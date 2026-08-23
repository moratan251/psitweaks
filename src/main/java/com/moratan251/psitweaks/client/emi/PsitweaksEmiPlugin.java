package com.moratan251.psitweaks.client.emi;

import com.moratan251.psitweaks.client.gui.IdeaStorageScreen;
import com.moratan251.psitweaks.common.menu.ModMenuTypes;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.widget.Bounds;
import net.minecraft.client.renderer.Rect2i;

/**
 * Psitweaks の EMI プラグイン。JEI 側(PsitweaksJeiPlugin)と同等の占有領域通知とレシピ転送を登録する。
 */
@EmiEntrypoint
public class PsitweaksEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        // クラフトウィンドウが画面左外に出るため、開いている間はパネルとサイドボタン列を占有領域として通知する
        registry.addExclusionArea(IdeaStorageScreen.class, (screen, consumer) -> {
            if (!screen.getMenu().isCraftOpen()) {
                return;
            }
            Rect2i panel = screen.getCraftPanelArea();
            consumer.accept(new Bounds(panel.getX(), panel.getY(), panel.getWidth(), panel.getHeight()));
            Rect2i sideButtons = screen.getSideButtonArea();
            consumer.accept(new Bounds(sideButtons.getX(), sideButtons.getY(), sideButtons.getWidth(), sideButtons.getHeight()));
        });
        registry.addRecipeHandler(ModMenuTypes.IDEA_STORAGE.get(), new IdeaStorageEmiRecipeHandler());
    }
}
