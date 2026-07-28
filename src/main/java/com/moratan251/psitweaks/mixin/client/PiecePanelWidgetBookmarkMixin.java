package com.moratan251.psitweaks.mixin.client;

import com.moratan251.psitweaks.client.gui.PieceBookmarkButton;
import com.moratan251.psitweaks.client.gui.PieceBookmarkManager;
import com.moratan251.psitweaks.client.gui.PiecePanelWidgetBookmarkExtension;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.psi.client.gui.GuiProgrammer;
import vazkii.psi.client.gui.button.GuiButtonPage;
import vazkii.psi.client.gui.button.GuiButtonSpellPiece;
import vazkii.psi.client.gui.widget.PiecePanelWidget;

import java.util.List;
import java.util.Set;

@Mixin(value = PiecePanelWidget.class, remap = false)
public abstract class PiecePanelWidgetBookmarkMixin implements PiecePanelWidgetBookmarkExtension {
    @Unique
    private static final int PSITWEAKS$PANEL_WIDTH = 100;
    @Unique
    private static final int PSITWEAKS$BASE_HEIGHT = 125;
    @Unique
    private static final int PSITWEAKS$BOOKMARK_ROW_HEIGHT = 18;
    @Unique
    private static final int PSITWEAKS$EXTENDED_HEIGHT =
            PSITWEAKS$BASE_HEIGHT + PSITWEAKS$BOOKMARK_ROW_HEIGHT;
    @Unique
    private static final int PSITWEAKS$PIECES_PER_PAGE = 25;

    @Shadow
    @Final
    public GuiProgrammer parent;
    @Shadow
    @Final
    public List<Button> panelButtons;
    @Shadow
    @Final
    public List<GuiButtonSpellPiece> visibleButtons;
    @Shadow
    public boolean panelEnabled;
    @Shadow
    public int page;

    @Shadow
    public abstract int getPageCount();

    @Shadow
    public abstract void updatePanelButtons();

    @Unique
    private boolean psitweaks$bookmarkMode;
    @Unique
    private PieceBookmarkButton psitweaks$bookmarkButton;

    @Inject(method = "populatePanelButtons", at = @At("RETURN"))
    private void psitweaks$addBookmarkButton(CallbackInfo callback) {
        psitweaks$bookmarkButton = new PieceBookmarkButton(button -> psitweaks$toggleBookmarkMode());
        psitweaks$bookmarkButton.visible = false;
        psitweaks$bookmarkButton.active = false;
        panelButtons.add(psitweaks$bookmarkButton);
        parent.addButtons(List.of(psitweaks$bookmarkButton));
        psitweaks$syncBookmarkButton();
    }

    @Inject(method = "updatePanelButtons", at = @At("HEAD"))
    private void psitweaks$restoreBasePanelGeometry(CallbackInfo callback) {
        if (panelEnabled) {
            PiecePanelWidget self = (PiecePanelWidget) (Object) this;
            self.setRectangle(
                    PSITWEAKS$PANEL_WIDTH,
                    PSITWEAKS$BASE_HEIGHT,
                    self.getX(),
                    parent.gridTop
            );
        }
    }

    @Inject(method = "updatePanelButtons", at = @At("RETURN"))
    private void psitweaks$filterBookmarkedPieces(CallbackInfo callback) {
        if (psitweaks$bookmarkMode) {
            Set<String> bookmarkIds = PieceBookmarkManager.getBookmarkIds();
            visibleButtons.removeIf(button ->
                    !PieceBookmarkManager.isBookmarked(button.getPiece(), bookmarkIds));
            psitweaks$layoutBookmarkedPage();
        }

        if (panelEnabled) {
            psitweaks$extendPanel();
        }
        psitweaks$syncBookmarkButton();
    }

    @Inject(method = "openPanel", at = @At("RETURN"))
    private void psitweaks$openBookmarkRow(CallbackInfo callback) {
        psitweaks$extendPanel();
        psitweaks$syncBookmarkButton();
    }

    @Inject(method = "closePanel", at = @At("RETURN"))
    private void psitweaks$closeBookmarkRow(CallbackInfo callback) {
        if (psitweaks$bookmarkButton != null) {
            psitweaks$bookmarkButton.visible = false;
            psitweaks$bookmarkButton.active = false;
        }
    }

    @Override
    public boolean psitweaks$isBookmarkMode() {
        return psitweaks$bookmarkMode;
    }

    @Unique
    private void psitweaks$toggleBookmarkMode() {
        psitweaks$bookmarkMode = !psitweaks$bookmarkMode;
        page = 0;
        updatePanelButtons();
    }

    @Unique
    private void psitweaks$layoutBookmarkedPage() {
        for (Button button : panelButtons) {
            if (button instanceof GuiButtonSpellPiece || button instanceof GuiButtonPage) {
                button.visible = false;
                button.active = false;
            }
        }

        int pageCount = getPageCount();
        page = Math.max(0, Math.min(page, Math.max(0, pageCount - 1)));
        int firstIndex = page * PSITWEAKS$PIECES_PER_PAGE;
        int lastIndex = Math.min(firstIndex + PSITWEAKS$PIECES_PER_PAGE, visibleButtons.size());

        PiecePanelWidget self = (PiecePanelWidget) (Object) this;
        for (int index = firstIndex; index < lastIndex; index++) {
            int pageIndex = index - firstIndex;
            GuiButtonSpellPiece button = visibleButtons.get(index);
            button.setX(self.getX() + 5 + pageIndex % 5 * 18);
            button.setY(parent.gridTop + 20 + pageIndex / 5 * 18);
            button.visible = true;
            button.active = true;
        }

        for (Button button : panelButtons) {
            if (!(button instanceof GuiButtonPage pageButton)) {
                continue;
            }

            boolean shouldShow = pageButton.isRight()
                    ? page < pageCount - 1
                    : page > 0;
            if (!shouldShow) {
                continue;
            }

            pageButton.setX(pageButton.isRight()
                    ? self.getX() + PSITWEAKS$PANEL_WIDTH - 22
                    : self.getX() + 4);
            pageButton.setY(parent.gridTop + PSITWEAKS$BASE_HEIGHT - 15);
            pageButton.visible = true;
            pageButton.active = true;
        }
    }

    @Unique
    private void psitweaks$extendPanel() {
        PiecePanelWidget self = (PiecePanelWidget) (Object) this;
        self.setRectangle(
                PSITWEAKS$PANEL_WIDTH,
                PSITWEAKS$EXTENDED_HEIGHT,
                self.getX(),
                parent.gridTop - PSITWEAKS$BOOKMARK_ROW_HEIGHT
        );
    }

    @Unique
    private void psitweaks$syncBookmarkButton() {
        if (psitweaks$bookmarkButton == null) {
            return;
        }

        PiecePanelWidget self = (PiecePanelWidget) (Object) this;
        psitweaks$bookmarkButton.setX(self.getX() + 1);
        psitweaks$bookmarkButton.setY(parent.gridTop - PSITWEAKS$BOOKMARK_ROW_HEIGHT + 1);
        psitweaks$bookmarkButton.setSelected(psitweaks$bookmarkMode);
        psitweaks$bookmarkButton.setTooltip(Tooltip.create(Component.translatable(
                psitweaks$bookmarkMode
                        ? "gui.psitweaks.spell_programmer.bookmarks.show_all"
                        : "gui.psitweaks.spell_programmer.bookmarks.show"
        )));
        psitweaks$bookmarkButton.visible = panelEnabled;
        psitweaks$bookmarkButton.active = panelEnabled;
    }
}
