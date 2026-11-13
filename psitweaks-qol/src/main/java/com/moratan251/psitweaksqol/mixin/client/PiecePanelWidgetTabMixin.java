package com.moratan251.psitweaksqol.mixin.client;

import com.moratan251.psitweaksqol.client.gui.PiecePanelSettings;
import com.moratan251.psitweaksqol.client.gui.PiecePanelSettingsButton;
import com.moratan251.psitweaksqol.client.gui.PiecePanelSettingsList;
import com.moratan251.psitweaksqol.client.gui.PiecePanelSize;
import com.moratan251.psitweaksqol.client.gui.PiecePanelTab;
import com.moratan251.psitweaksqol.client.gui.PiecePanelTabButton;
import com.moratan251.psitweaksqol.client.gui.ProgrammerOverlayInputGuard;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.psi.client.gui.GuiProgrammer;
import vazkii.psi.client.gui.button.GuiButtonPage;
import vazkii.psi.client.gui.button.GuiButtonSpellPiece;
import vazkii.psi.client.gui.widget.PiecePanelWidget;
import vazkii.psi.common.spell.constant.PieceConstantNumber;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * ピース一覧の上にタブの列（★・最近・種類）と設定ボタンを足す。設定では一覧の大きさ（小・中・大）、
 * 検索内容の保持、右クリックで検索欄のクリアを変えられる。
 * <ul>
 * <li>絞り込みは Psi が検索語で作った並びに後から掛けるので、検索語とタブは重ねて使える。</li>
 * <li>Psi は 5 列 5 行（1 ページ 25 個）を決め打ちで並べる。ここでは Psi の並べたあとに全部を並べ直し、
 * ページ数と、25 が埋め込まれているカーソルの計算を差し替える。</li>
 * <li>設定を開いている間はピースを隠し、同じ場所に設定項目を出す。</li>
 * </ul>
 */
@Mixin(value = PiecePanelWidget.class, remap = false)
public abstract class PiecePanelWidgetTabMixin {
    @Unique
    private static final int PSITWEAKS$TAB_ROW_HEIGHT = 18;

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

    /** 選択中のタブ。null は絞り込みなし。画面を開いている間だけ覚える。 */
    @Unique
    private PiecePanelTab psitweaks$activeTab;
    /** true の間、ピースとタブの代わりに設定項目を出す。一覧を閉じると戻る。 */
    @Unique
    private boolean psitweaks$settingsMode;
    @Unique
    private final List<PiecePanelTabButton> psitweaks$tabButtons = new ArrayList<>();
    @Unique
    private PiecePanelSettingsButton psitweaks$settingsButton;
    @Unique
    private PiecePanelSettingsList psitweaks$settingsList;

    /** いまの一覧の本体（検索欄からページ番号まで。タブの列は含まない）の位置と大きさ。 */
    @Unique
    private int psitweaks$bodyX;
    @Unique
    private int psitweaks$bodyY;
    @Unique
    private PiecePanelSize psitweaks$size = PiecePanelSize.SMALL;

    @Inject(method = "populatePanelButtons", at = @At("RETURN"))
    private void psitweaks$addTabButtons(CallbackInfo callback) {
        psitweaks$tabButtons.clear();
        List<Button> added = new ArrayList<>();
        for (PiecePanelTab tab : PiecePanelTab.values()) {
            PiecePanelTabButton button = new PiecePanelTabButton(tab, pressed -> psitweaks$toggleTab(tab));
            psitweaks$tabButtons.add(button);
            added.add(button);
        }
        psitweaks$settingsList = new PiecePanelSettingsList(() -> psitweaks$onSizeChanged());
        psitweaks$settingsButton = new PiecePanelSettingsButton(pressed -> psitweaks$toggleSettings());
        added.add(psitweaks$settingsList);
        added.add(psitweaks$settingsButton);
        for (Button button : added) {
            button.visible = false;
            button.active = false;
        }
        panelButtons.addAll(added);
        parent.addButtons(added);
    }

    // --- 大きさと位置 ---

    @Inject(method = "openPanel", at = @At("HEAD"))
    private void psitweaks$placePanel(CallbackInfo callback) {
        psitweaks$computeBody();
    }

    @Inject(method = "openPanel", at = @At("RETURN"))
    private void psitweaks$openTabRow(CallbackInfo callback) {
        ProgrammerOverlayInputGuard.resetPsionicUtilitiesConnectorStart(parent);
        psitweaks$placeSearchField();
        psitweaks$extendPanel();
        psitweaks$syncTabRow();
        psitweaks$restoreSearchText();
    }

    /**
     * 一覧の本体をどこに、どの大きさで出すかを決める。
     * 小（Psi の元の広さ）のときは Psi と同じ位置（選択中のマスの右。グリッドの右にはみ出してよい）。
     * 中・大のときはグリッドの中に収める。選択中のマスの右に入らなければ左、それも無理ならマスに重ねる。
     */
    @Unique
    private void psitweaks$computeBody() {
        psitweaks$size = PiecePanelSettings.size();

        int selectedLeft = parent.gridLeft + GuiProgrammer.selectedX * PiecePanelSize.CELL_SIZE;
        int x = selectedLeft + PiecePanelSize.CELL_SIZE;
        if (psitweaks$size != PiecePanelSize.SMALL) {
            int width = psitweaks$size.panelWidth();
            int minX = parent.gridLeft - PiecePanelSize.SIDE_PADDING;
            int maxX = parent.gridLeft + PiecePanelSize.GRID_PIXELS + PiecePanelSize.SIDE_PADDING - width;
            if (x > maxX) {
                int leftOfSelected = selectedLeft - width;
                x = leftOfSelected >= minX ? leftOfSelected : Math.max(minX, Math.min(maxX, x));
            }
        }

        psitweaks$bodyX = x;
        psitweaks$bodyY = parent.gridTop;
    }

    @Unique
    private void psitweaks$placeSearchField() {
        PiecePanelWidget self = (PiecePanelWidget) (Object) this;
        if (self.searchField == null) {
            return;
        }
        self.searchField.setX(psitweaks$bodyX + 18);
        self.searchField.setY(psitweaks$bodyY + 4);
        self.searchField.setWidth(psitweaks$size.panelWidth() - 30);
    }

    /** 設定で大きさが変わったとき。一覧を開いたまま置き直す。 */
    @Unique
    private void psitweaks$onSizeChanged() {
        psitweaks$computeBody();
        psitweaks$placeSearchField();
        page = 0;
        updatePanelButtons();
    }

    // --- 検索内容の保持 ---

    /** 閉じる直前の検索欄の文字を覚える（Psi は開くたびに検索欄を空にする）。 */
    @Inject(method = "closePanel", at = @At("HEAD"))
    private void psitweaks$rememberSearchText(CallbackInfo callback) {
        PiecePanelWidget self = (PiecePanelWidget) (Object) this;
        if (panelEnabled && self.searchField != null) {
            PiecePanelSettings.rememberSearchText(self.searchField.getValue());
        }
    }

    @Unique
    private void psitweaks$restoreSearchText() {
        PiecePanelWidget self = (PiecePanelWidget) (Object) this;
        String text = PiecePanelSettings.searchTextToRestore();
        if (self.searchField == null || text.isEmpty()) {
            return;
        }
        // setValue は Psi の並べ直しを呼ばないので、ここで並べ直す。ページは Psi が覚えているものを使う。
        self.searchField.setValue(text);
        updatePanelButtons();
    }

    // --- Psi の 25 個決め打ちの差し替え ---

    @Inject(method = "getPageCount", at = @At("HEAD"), cancellable = true)
    private void psitweaks$pageCountForSize(CallbackInfoReturnable<Integer> callback) {
        int perPage = psitweaks$size.piecesPerPage();
        callback.setReturnValue((visibleButtons.size() + perPage - 1) / perPage);
    }

    /** カーソルの枠を描く位置（page * 25 + panelCursor）。 */
    @ModifyConstant(method = "renderWidget", constant = @Constant(intValue = 25))
    private int psitweaks$piecesPerPageForCursorHighlight(int original) {
        return psitweaks$size.piecesPerPage();
    }

    /** Tab キーでカーソルが回る範囲（1 ページ分）。 */
    @ModifyConstant(method = "keyPressed", constant = @Constant(intValue = 25))
    private int psitweaks$piecesPerPageForCursorKeys(int original) {
        return psitweaks$size.piecesPerPage();
    }

    // --- 絞り込みと並べ直し ---

    @Inject(method = "updatePanelButtons", at = @At("HEAD"))
    private void psitweaks$restoreBodyGeometry(CallbackInfo callback) {
        if (panelEnabled) {
            PiecePanelWidget self = (PiecePanelWidget) (Object) this;
            self.setRectangle(psitweaks$size.panelWidth(), psitweaks$size.panelHeight(),
                    psitweaks$bodyX, psitweaks$bodyY);
        }
    }

    @Inject(method = "updatePanelButtons", at = @At("RETURN"))
    private void psitweaks$filterAndLayout(CallbackInfo callback) {
        if (psitweaks$activeTab != null) {
            psitweaks$activeTab.filter(visibleButtons);
            psitweaks$keepTypedNumberConstantFirst();
        }
        if (panelEnabled) {
            psitweaks$layoutPage();
            psitweaks$extendPanel();
        }
        psitweaks$syncTabRow();
    }

    @Inject(method = "closePanel", at = @At("RETURN"))
    private void psitweaks$closeTabRow(CallbackInfo callback) {
        psitweaks$settingsMode = false;
        psitweaks$syncTabRow();
    }

    @Unique
    private void psitweaks$toggleTab(PiecePanelTab tab) {
        psitweaks$activeTab = psitweaks$activeTab == tab ? null : tab;
        page = 0;
        updatePanelButtons();
    }

    @Unique
    private void psitweaks$toggleSettings() {
        psitweaks$settingsMode = !psitweaks$settingsMode;
        // 設定中は検索欄を見えなくする（見えない欄は文字も受け取らない）。虫眼鏡の絵とページ番号は設定の面が覆う。
        // 閉じるときは Psi が検索欄を隠し、開くときは Psi が見えるようにするので、ここでは切り替えのときだけ触る。
        PiecePanelWidget self = (PiecePanelWidget) (Object) this;
        if (self.searchField != null && panelEnabled) {
            self.searchField.setVisible(!psitweaks$settingsMode);
        }
        psitweaks$layoutPage();
        psitweaks$syncTabRow();
    }

    /**
     * 数字を打つと定数子が先頭に来る Psi の動きを、タブで絞っている間も保つ。
     * タブは開き直しても残るので、これがないと「数字を打って Enter」で別のピースが置かれてしまう。
     * 判定は Psi の updatePanelButtons と同じ（5 文字以内の数値）。
     */
    @Unique
    private void psitweaks$keepTypedNumberConstantFirst() {
        PiecePanelWidget self = (PiecePanelWidget) (Object) this;
        if (self.searchField == null) {
            return;
        }

        String text = self.searchField.getValue().toLowerCase(Locale.ROOT).trim();
        if (text.isEmpty() || text.length() > 5
                || !(text.matches("^-?\\d+(?:\\.\\d*)?") || text.matches("^-?\\d*(?:\\.\\d+)?"))) {
            return;
        }

        for (Button button : panelButtons) {
            if (button instanceof GuiButtonSpellPiece pieceButton
                    && pieceButton.getPiece() instanceof PieceConstantNumber) {
                visibleButtons.remove(pieceButton);
                visibleButtons.add(0, pieceButton);
                return;
            }
        }
    }

    /** いまのページのピースとページ送りのボタンを、いまの大きさで並べる。設定を開いている間は全部隠す。 */
    @Unique
    private void psitweaks$layoutPage() {
        for (Button button : panelButtons) {
            if (button instanceof GuiButtonSpellPiece || button instanceof GuiButtonPage) {
                button.visible = false;
                button.active = false;
            }
        }
        if (psitweaks$settingsMode) {
            return;
        }

        int columns = psitweaks$size.columns();
        int perPage = psitweaks$size.piecesPerPage();
        int pageCount = getPageCount();
        page = Math.max(0, Math.min(page, Math.max(0, pageCount - 1)));
        int firstIndex = page * perPage;
        int lastIndex = Math.min(firstIndex + perPage, visibleButtons.size());

        for (int index = firstIndex; index < lastIndex; index++) {
            int pageIndex = index - firstIndex;
            GuiButtonSpellPiece button = visibleButtons.get(index);
            button.setX(psitweaks$bodyX + PiecePanelSize.SIDE_PADDING + pageIndex % columns * PiecePanelSize.CELL_SIZE);
            button.setY(psitweaks$bodyY + PiecePanelSize.SEARCH_ROW_HEIGHT
                    + pageIndex / columns * PiecePanelSize.CELL_SIZE);
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
                    ? psitweaks$bodyX + psitweaks$size.panelWidth() - 22
                    : psitweaks$bodyX + 4);
            pageButton.setY(psitweaks$bodyY + psitweaks$size.panelHeight() - PiecePanelSize.PAGE_ROW_HEIGHT);
            pageButton.visible = true;
            pageButton.active = true;
        }
    }

    /** 一覧の枠をタブの列の分だけ上に広げる（背景の描画と、枠の外のクリックで閉じる判定に使われる）。 */
    @Unique
    private void psitweaks$extendPanel() {
        PiecePanelWidget self = (PiecePanelWidget) (Object) this;
        self.setRectangle(
                psitweaks$size.panelWidth(),
                psitweaks$size.panelHeight() + PSITWEAKS$TAB_ROW_HEIGHT,
                psitweaks$bodyX,
                psitweaks$bodyY - PSITWEAKS$TAB_ROW_HEIGHT
        );
    }

    /** タブの列・設定項目・歯車の位置と表示をいまの状態に合わせる。 */
    @Unique
    private void psitweaks$syncTabRow() {
        int rowY = psitweaks$bodyY - PSITWEAKS$TAB_ROW_HEIGHT + 1;
        boolean showTabs = panelEnabled && !psitweaks$settingsMode;
        for (int index = 0; index < psitweaks$tabButtons.size(); index++) {
            PiecePanelTabButton button = psitweaks$tabButtons.get(index);
            boolean selected = button.getTab() == psitweaks$activeTab;
            button.setX(psitweaks$bodyX + 1 + index * PiecePanelTabButton.WIDTH);
            button.setY(rowY);
            button.setSelected(selected);
            button.setTooltip(Tooltip.create(selected
                    ? Component.translatable(PiecePanelTab.SHOW_ALL_KEY)
                    : button.getTab().tooltip()));
            button.visible = showTabs;
            button.active = showTabs;
        }

        if (psitweaks$settingsList != null) {
            boolean showSettings = panelEnabled && psitweaks$settingsMode;
            psitweaks$settingsList.place(psitweaks$bodyX, psitweaks$bodyY, psitweaks$size.panelWidth(),
                    psitweaks$size.panelHeight(), rowY);
            psitweaks$settingsList.visible = showSettings;
            psitweaks$settingsList.active = showSettings;
        }

        if (psitweaks$settingsButton != null) {
            psitweaks$settingsButton.setX(psitweaks$bodyX + psitweaks$size.panelWidth()
                    - PiecePanelTabButton.WIDTH - 1);
            psitweaks$settingsButton.setY(rowY);
            psitweaks$settingsButton.setSelected(psitweaks$settingsMode);
            psitweaks$settingsButton.setTooltip(Tooltip.create(Component.translatable(psitweaks$settingsMode
                    ? PiecePanelSettingsButton.CLOSE_TOOLTIP_KEY
                    : PiecePanelSettingsButton.TOOLTIP_KEY)));
            psitweaks$settingsButton.visible = panelEnabled;
            psitweaks$settingsButton.active = panelEnabled;
        }
    }
}
