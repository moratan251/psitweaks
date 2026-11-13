package com.moratan251.psitweaksqol.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * 設定を開いている間、ピースの代わりに一覧の中へ出す設定項目の並び。
 * <ul>
 * <li>大きさ: 小・中・大 の 3 つから選ぶ</li>
 * <li>入り切りの項目: 検索内容の保持、右クリックで検索欄のクリア、英語名順に並べる</li>
 * </ul>
 * ピースを置く場所（検索欄の下からページ番号の行まで）を不透明に塗って、その上に描く。Psi が先に描いた
 * カーソルの枠とページ番号は、これで隠れる。
 * 画面に載せる口が Button の一覧しかないので Button として作り、クリックの位置で項目を分ける。
 */
public final class PiecePanelSettingsList extends Button {
    private static final String KEY_PREFIX = "gui.psitweaks_qol.spell_programmer.settings.";
    private static final Component TITLE = Component.translatable(KEY_PREFIX + "title");
    private static final Component SIZE_LABEL = Component.translatable(KEY_PREFIX + "size");

    private static final int BACKGROUND_COLOR = 0xFF141414;
    private static final int LABEL_COLOR = 0xD0D0D0;
    private static final int HOVERED_COLOR = 0xFFFFFF;
    private static final int BOX_COLOR = 0xFFA0A0A0;
    private static final int CHECK_COLOR = 0xFFFFFF55;

    private static final int PADDING = 5;
    private static final int LINE_HEIGHT = 10;
    private static final int ITEM_GAP = 5;
    /** 大きさの選択肢 1 つ分の幅。タブと同じ。 */
    private static final int OPTION_WIDTH = PiecePanelTabButton.WIDTH;
    private static final int SIZE_ROW_HEIGHT = 13;
    private static final int CHECK_TEXT_OFFSET = PixelIcon.SIZE + 4;

    /** 入り切りの項目 1 つ。 */
    private record Toggle(Component label, Component tooltip, BooleanSupplier isOn, Runnable flip) {
        static Toggle of(String id, BooleanSupplier isOn, Runnable flip) {
            return new Toggle(Component.translatable(KEY_PREFIX + id),
                    Component.translatable(KEY_PREFIX + id + ".tooltip"), isOn, flip);
        }
    }

    private final List<Toggle> toggles;
    private final Runnable onLayoutChanged;
    /** タブの列の上端。ここに見出しを描く。 */
    private int titleY;

    /** @param onLayoutChanged 一覧の大きさか並び順が変わったあとに呼ぶ（一覧の置き直しと並べ直し） */
    public PiecePanelSettingsList(Runnable onLayoutChanged) {
        super(0, 0, 0, 0, TITLE, button -> { }, DEFAULT_NARRATION);
        this.onLayoutChanged = onLayoutChanged;
        this.toggles = List.of(
                Toggle.of("keep_search", PiecePanelSettings::keepSearchText, this::toggleKeepSearchText),
                Toggle.of("right_click_clear", PiecePanelSettings::rightClickClearsSearch,
                        this::toggleRightClickClearsSearch),
                Toggle.of("english_sort", () -> PiecePanelSettings.sortOrder() == PieceSortOrder.ENGLISH_NAME,
                        this::toggleEnglishSort)
        );
    }

    /** 一覧の本体の位置と大きさから、自分の置き場所を決める。 */
    public void place(int bodyX, int bodyY, int bodyWidth, int bodyHeight, int tabRowY) {
        setRectangle(bodyWidth, bodyHeight - PiecePanelSize.SEARCH_ROW_HEIGHT,
                bodyX, bodyY + PiecePanelSize.SEARCH_ROW_HEIGHT);
        titleY = tabRowY;
    }

    public void selectSize(PiecePanelSize size) {
        if (PiecePanelSettings.size() != size) {
            PiecePanelSettings.setSize(size);
            onLayoutChanged.run();
        }
    }

    public void toggleKeepSearchText() {
        PiecePanelSettings.setKeepSearchText(!PiecePanelSettings.keepSearchText());
    }

    public void toggleRightClickClearsSearch() {
        PiecePanelSettings.setRightClickClearsSearch(!PiecePanelSettings.rightClickClearsSearch());
    }

    public void toggleEnglishSort() {
        PiecePanelSettings.setSortOrder(PiecePanelSettings.sortOrder() == PieceSortOrder.ENGLISH_NAME
                ? PieceSortOrder.NAME
                : PieceSortOrder.ENGLISH_NAME);
        onLayoutChanged.run();
    }

    // --- 並べ方（描くときとクリックのときで同じ計算を使う） ---

    private int sizeRowTop() {
        return getY() + 4;
    }

    private int optionLeft(int index) {
        return getX() + getWidth() - PADDING - (PiecePanelSize.values().length - index) * OPTION_WIDTH;
    }

    private int textWidth() {
        return Math.max(20, getWidth() - PADDING * 2 - CHECK_TEXT_OFFSET);
    }

    private List<FormattedCharSequence> lines(Font font, Toggle toggle) {
        return font.split(toggle.label(), textWidth());
    }

    private int toggleHeight(Font font, Toggle toggle) {
        return Math.max(PixelIcon.SIZE, lines(font, toggle).size() * LINE_HEIGHT - 1);
    }

    /** index 番目の入り切りの上端。上にある項目の高さ（折り返しの行数で変わる）を足していく。 */
    private int toggleTop(Font font, int index) {
        int top = sizeRowTop() + SIZE_ROW_HEIGHT + ITEM_GAP;
        for (int i = 0; i < index; i++) {
            top += toggleHeight(font, toggles.get(i)) + ITEM_GAP;
        }
        return top;
    }

    private static boolean inRow(double mouseY, int top, int height) {
        return mouseY >= top - 2 && mouseY < top + height + 2;
    }

    /** カーソルの下にある大きさの選択肢。なければ null。 */
    private PiecePanelSize sizeAt(double mouseX, double mouseY) {
        if (!inRow(mouseY, sizeRowTop(), SIZE_ROW_HEIGHT)) {
            return null;
        }
        PiecePanelSize[] sizes = PiecePanelSize.values();
        for (int index = 0; index < sizes.length; index++) {
            int left = optionLeft(index);
            if (mouseX >= left && mouseX < left + OPTION_WIDTH) {
                return sizes[index];
            }
        }
        return null;
    }

    /** カーソルの下にある入り切りの番号。なければ -1。 */
    private int toggleAt(Font font, double mouseY) {
        for (int index = 0; index < toggles.size(); index++) {
            if (inRow(mouseY, toggleTop(font, index), toggleHeight(font, toggles.get(index)))) {
                return index;
            }
        }
        return -1;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        PiecePanelSize size = sizeAt(mouseX, mouseY);
        if (size != null) {
            selectSize(size);
            return;
        }
        int toggle = toggleAt(Minecraft.getInstance().font, mouseY);
        if (toggle >= 0) {
            toggles.get(toggle).flip().run();
        }
    }

    @Override
    public void onPress() {
        // キーボードからは操作しない（どの項目を変えるか決められないため）。
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Font font = Minecraft.getInstance().font;
        boolean hovered = isMouseOver(mouseX, mouseY);
        Component tooltip = null;

        guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), BACKGROUND_COLOR);
        guiGraphics.drawString(font, TITLE, getX() + PADDING, titleY + 4, HOVERED_COLOR);

        // 大きさ
        guiGraphics.drawString(font, SIZE_LABEL, getX() + PADDING, sizeRowTop() + 2, LABEL_COLOR);
        PiecePanelSize current = PiecePanelSettings.size();
        PiecePanelSize hoveredSize = hovered ? sizeAt(mouseX, mouseY) : null;
        PiecePanelSize[] sizes = PiecePanelSize.values();
        for (int index = 0; index < sizes.length; index++) {
            PiecePanelSize size = sizes[index];
            int left = optionLeft(index);
            int color;
            if (size == hoveredSize) {
                color = PiecePanelTabButton.HOVERED_COLOR;
            } else {
                color = size == current ? PiecePanelTabButton.SELECTED_COLOR : PiecePanelTabButton.UNSELECTED_COLOR;
            }
            guiGraphics.drawCenteredString(font, size.label(), left + OPTION_WIDTH / 2, sizeRowTop() + 2, color);
            if (size == current) {
                guiGraphics.fill(left + 2, sizeRowTop() + SIZE_ROW_HEIGHT - 1, left + OPTION_WIDTH - 2,
                        sizeRowTop() + SIZE_ROW_HEIGHT, PiecePanelTabButton.SELECTED_UNDERLINE_COLOR);
            }
        }
        if (hoveredSize != null) {
            tooltip = hoveredSize.tooltip();
        }

        // 入り切り
        int hoveredToggle = hovered && hoveredSize == null ? toggleAt(font, mouseY) : -1;
        for (int index = 0; index < toggles.size(); index++) {
            Toggle toggle = toggles.get(index);
            renderToggle(guiGraphics, font, toggle, toggleTop(font, index), index == hoveredToggle);
        }
        if (hoveredToggle >= 0) {
            tooltip = toggles.get(hoveredToggle).tooltip();
        }

        // ツールチップはこのあと（AbstractWidget.render の最後）で出されるので、ここで差し替えれば同じフレームに間に合う。
        setTooltip(tooltip == null ? null : Tooltip.create(tooltip));
    }

    private void renderToggle(GuiGraphics guiGraphics, Font font, Toggle toggle, int top, boolean hovered) {
        int left = getX() + PADDING;
        PixelIcon.draw(guiGraphics, PixelIcon.CHECK_BOX, left, top, hovered ? 0xFF000000 | HOVERED_COLOR : BOX_COLOR);
        if (toggle.isOn().getAsBoolean()) {
            PixelIcon.draw(guiGraphics, PixelIcon.CHECK, left, top, CHECK_COLOR);
        }

        int y = top + 1;
        for (FormattedCharSequence line : lines(font, toggle)) {
            guiGraphics.drawString(font, line, left + CHECK_TEXT_OFFSET, y, hovered ? HOVERED_COLOR : LABEL_COLOR);
            y += LINE_HEIGHT;
        }
    }
}
