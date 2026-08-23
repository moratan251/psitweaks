package com.moratan251.psitweaks.client.gui;

import com.moratan251.psitweaks.common.menu.IdeaStorageMenu;
import com.moratan251.psitweaks.common.network.MessageIdeaStorageClearCrafting;
import com.moratan251.psitweaks.common.network.MessageIdeaStorageCraftToggle;
import com.moratan251.psitweaks.common.network.MessageIdeaStorageDeposit;
import com.moratan251.psitweaks.common.network.MessageIdeaStorageExtract;
import com.moratan251.psitweaks.common.network.MessageIdeaStorageResize;
import com.moratan251.psitweaks.common.network.MessageIdeaStorageSync;
import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

/**
 * イデアストレージ閲覧画面。Minecraft 標準コンテナ風のフレーム + 仮想グリッド + プレイヤーインベントリ。
 * 検索・スクロールはクライアント側のみで完結し、操作は payload でサーバーへ送る。
 * 左端にクラフトウィンドウ開閉・行数 +/− ボタン(QIO のサイドボタン相当)を持つ。
 */
public class IdeaStorageScreen extends AbstractContainerScreen<IdeaStorageMenu> {
    // バニラコンテナ風の配色
    private static final int COLOR_BACKGROUND = 0xFFC6C6C6;
    private static final int COLOR_FRAME_OUTER = 0xFF000000;
    private static final int COLOR_FRAME_LIGHT = 0xFFFFFFFF;
    private static final int COLOR_FRAME_DARK = 0xFF555555;
    private static final int COLOR_SLOT_INNER = 0xFF8B8B8B;
    private static final int COLOR_SLOT_DARK = 0xFF373737;
    private static final int COLOR_SLOT_LIGHT = 0xFFFFFFFF;
    private static final int COLOR_SCROLL_TRACK = 0xFF373737;
    private static final int COLOR_SCROLL_HANDLE = 0xFFC0C0C0;
    private static final int COLOR_SCROLL_HANDLE_LIGHT = 0xFFFFFFFF;
    private static final int COLOR_SCROLL_HANDLE_DARK = 0xFF555555;
    private static final int COLOR_TEXT = 0xFF404040;
    private static final int COLOR_ERROR = 0xFFAA0000;
    private static final int COLOR_ARROW = 0xFF555555;
    private static final int CELL = 18;
    private static final int MIN_HANDLE_HEIGHT = 12;
    private static final int SIDE_BUTTON_WIDTH = 18;
    private static final int SIDE_BUTTON_HEIGHT = 18;
    private static final int SIDE_BUTTON_X = -20;
    private static final int CRAFT_BUTTON_Y = 34;
    private static final int ROWS_ADD_BUTTON_Y = 54;
    private static final int ROWS_REMOVE_BUTTON_Y = 74;
    private static final int SORT_BUTTON_Y = 94;
    private static final int CRAFT_CLEAR_BUTTON_Y = 114;

    private List<MessageIdeaStorageSync.Entry> entries = List.of();
    private boolean loadFailed;
    private String filter = "";
    private int firstRow;
    private boolean draggingScrollBar;
    @Nullable
    private EditBox searchBox;

    public IdeaStorageScreen(IdeaStorageMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = IdeaStorageMenu.SCREEN_WIDTH;
        this.imageHeight = menu.screenHeight();
    }

    private int gridRows() {
        return this.menu.gridRows();
    }

    @Override
    protected void init() {
        super.init();
        String previous = searchBox != null ? searchBox.getValue() : IdeaStorageClientHandler.lastFilter();
        searchBox = new EditBox(this.font, this.leftPos + IdeaStorageMenu.GRID_X, this.topPos + 16,
                this.imageWidth - IdeaStorageMenu.GRID_X * 2, 12,
                Component.translatable("gui.psitweaks.idea_storage.search"));
        searchBox.setMaxLength(64);
        searchBox.setBordered(true);
        searchBox.setCanLoseFocus(true);
        searchBox.setTextColor(0xFFFFFFFF);
        searchBox.setHint(Component.translatable("gui.psitweaks.idea_storage.search"));
        searchBox.setResponder(value -> {
            filter = value;
            firstRow = 0;
        });
        searchBox.setValue(previous);
        filter = previous;
        addRenderableWidget(searchBox);

        // 左端サイドボタン(QIO 相当): クラフトウィンドウ開閉 / 行数 + / 行数 −
        addRenderableWidget(new SideButton(this.leftPos + SIDE_BUTTON_X, this.topPos + CRAFT_BUTTON_Y,
                Component.empty(), () -> new ItemStack(Items.CRAFTING_TABLE),
                Component.translatable("gui.psitweaks.idea_storage.craft_window"), button -> toggleCraftWindow()));
        addRenderableWidget(new SideButton(this.leftPos + SIDE_BUTTON_X, this.topPos + ROWS_ADD_BUTTON_Y,
                Component.literal("+"), null,
                Component.translatable("gui.psitweaks.idea_storage.rows_add"), button -> changeRows(1)));
        addRenderableWidget(new SideButton(this.leftPos + SIDE_BUTTON_X, this.topPos + ROWS_REMOVE_BUTTON_Y,
                Component.literal("-"), null,
                Component.translatable("gui.psitweaks.idea_storage.rows_remove"), button -> changeRows(-1)));
        // ソートモード巡回ボタン(QIO のソートボタン相当)。アイコンとツールチップは現在のモードに追従する
        SideButton sortButton = new SideButton(this.leftPos + SIDE_BUTTON_X, this.topPos + SORT_BUTTON_Y,
                Component.empty(), () -> IdeaStorageClientHandler.sortMode().icon(),
                sortTooltip(IdeaStorageClientHandler.sortMode()), button -> {
                    IdeaStorageSortMode mode = IdeaStorageClientHandler.cycleSortMode();
                    button.setTooltip(Tooltip.create(sortTooltip(mode)));
                });
        addRenderableWidget(sortButton);
        addRenderableWidget(new SideButton(this.leftPos + SIDE_BUTTON_X, this.topPos + CRAFT_CLEAR_BUTTON_Y,
                Component.empty(), () -> new ItemStack(Items.ENDER_CHEST),
                Component.translatable("gui.psitweaks.idea_storage.craft_clear"),
                button -> PacketDistributor.sendToServer(new MessageIdeaStorageClearCrafting())));

        // 行数変更による開き直し直後は、退避してあったカーソル位置を復元する
        IdeaStorageClientHandler.restoreMousePositionIfStashed();
    }

    /** ソートボタンのツールチップ。「ソート: <モード名>」形式で現在のモードを示す。 */
    private static Component sortTooltip(IdeaStorageSortMode mode) {
        return Component.translatable("gui.psitweaks.idea_storage.sort", mode.displayName());
    }

    /** クラフトウィンドウ開閉トグル。クライアントは楽観的に反映し、サーバーへも通知する。 */
    private void toggleCraftWindow() {
        boolean next = !this.menu.isCraftOpen();
        this.menu.setCraftOpen(next);
        PacketDistributor.sendToServer(new MessageIdeaStorageCraftToggle(next));
    }

    /** JEI/EMI の占有領域(exclusion area)通知用。クラフトパネルの矩形(画面絶対座標)。 */
    public Rect2i getCraftPanelArea() {
        return new Rect2i(this.leftPos + IdeaStorageMenu.CRAFT_PANEL_X, this.topPos + IdeaStorageMenu.CRAFT_PANEL_Y,
                IdeaStorageMenu.CRAFT_PANEL_WIDTH, IdeaStorageMenu.CRAFT_PANEL_HEIGHT);
    }

    /** JEI/EMI の占有領域(exclusion area)通知用。左端サイドボタン列の矩形(画面絶対座標)。 */
    public Rect2i getSideButtonArea() {
        return new Rect2i(this.leftPos + SIDE_BUTTON_X, this.topPos + CRAFT_BUTTON_Y,
                SIDE_BUTTON_WIDTH, CRAFT_CLEAR_BUTTON_Y + SIDE_BUTTON_HEIGHT - CRAFT_BUTTON_Y);
    }

    /** 行数変更要求。サーバーが Menu を閉じて新しい行数で開き直すため、ここでは送信のみ。 */
    private void changeRows(int delta) {
        int current = gridRows();
        int next = Mth.clamp(current + delta, PlayerIdeaStorage.GRID_ROWS_MIN, PlayerIdeaStorage.GRID_ROWS_MAX);
        if (next != current) {
            // 開き直しでスクリーン遷移扱いになりカーソルが中央化されるため、現在位置を退避する
            Minecraft minecraft = Minecraft.getInstance();
            MouseHandler mouse = minecraft.mouseHandler;
            double guiScale = minecraft.getWindow().getGuiScale();
            IdeaStorageClientHandler.stashMousePosition(mouse.xpos() / guiScale, mouse.ypos() / guiScale);
            PacketDistributor.sendToServer(new MessageIdeaStorageResize(next));
        }
    }

    @Override
    public void removed() {
        // リサイズで開き直された場合に検索文字列を引き継ぐため退避する
        if (searchBox != null) {
            IdeaStorageClientHandler.stashFilter(searchBox.getValue());
        }
        super.removed();
    }

    public void applySnapshot(List<MessageIdeaStorageSync.Entry> newEntries, boolean loadFailed) {
        this.entries = newEntries;
        this.menu.applyClientStorageEntries(newEntries);
        this.loadFailed = loadFailed;
        clampScroll();
    }

    /** フィルタ適用後、現在のソートモードで並べ替えた表示リストを返す(PORT は元順のまま)。 */
    private List<MessageIdeaStorageSync.Entry> filteredEntries() {
        List<MessageIdeaStorageSync.Entry> filtered;
        if (filter.isEmpty()) {
            filtered = entries;
        } else {
            String needle = filter.toLowerCase(Locale.ROOT);
            filtered = new ArrayList<>();
            for (MessageIdeaStorageSync.Entry entry : entries) {
                String name = entry.template().getHoverName().getString().toLowerCase(Locale.ROOT);
                String id = BuiltInRegistries.ITEM.getKey(entry.template().getItem()).toString();
                if (name.contains(needle) || id.contains(needle)) {
                    filtered.add(entry);
                }
            }
        }
        IdeaStorageSortMode mode = IdeaStorageClientHandler.sortMode();
        if (mode == IdeaStorageSortMode.PORT) {
            return filtered;
        }
        // entries は不変リストの場合があるため、ソートはコピーに対して行う
        List<MessageIdeaStorageSync.Entry> sorted = new ArrayList<>(filtered);
        sorted.sort(mode.comparator());
        return sorted;
    }

    private int maxFirstRow(int filteredSize) {
        int rows = (filteredSize + IdeaStorageMenu.GRID_COLUMNS - 1) / IdeaStorageMenu.GRID_COLUMNS;
        return Math.max(0, rows - gridRows());
    }

    private void clampScroll() {
        firstRow = Math.max(0, Math.min(firstRow, maxFirstRow(filteredEntries().size())));
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;
        drawFrame(guiGraphics, left, top, this.imageWidth, this.imageHeight);

        if (this.menu.isCraftOpen()) {
            drawFrame(guiGraphics, left + IdeaStorageMenu.CRAFT_PANEL_X, top + IdeaStorageMenu.CRAFT_PANEL_Y,
                    IdeaStorageMenu.CRAFT_PANEL_WIDTH, IdeaStorageMenu.CRAFT_PANEL_HEIGHT);
            drawArrow(guiGraphics, left - 75, top + IdeaStorageMenu.CRAFT_RESULT_Y + 4);
        }

        int gridLeft = left + IdeaStorageMenu.GRID_X;
        int gridTop = top + IdeaStorageMenu.GRID_Y;

        for (int row = 0; row < gridRows(); row++) {
            for (int col = 0; col < IdeaStorageMenu.GRID_COLUMNS; col++) {
                drawSlot(guiGraphics, gridLeft + col * CELL, gridTop + row * CELL);
            }
        }
        for (Slot slot : this.menu.slots) {
            if (this.menu.isCraftSlot(slot) && !this.menu.isCraftOpen()) {
                continue;
            }
            drawSlot(guiGraphics, left + slot.x - 1, top + slot.y - 1);
        }
        drawScrollBar(guiGraphics);

        if (loadFailed) {
            Component error = Component.translatable("gui.psitweaks.idea_storage.load_failed");
            int textWidth = this.font.width(error);
            guiGraphics.drawString(this.font, error,
                    gridLeft + (IdeaStorageMenu.GRID_COLUMNS * CELL - textWidth) / 2,
                    gridTop + gridRows() * CELL / 2 - 4, COLOR_ERROR, false);
            return;
        }

        List<MessageIdeaStorageSync.Entry> visible = filteredEntries();
        int start = firstRow * IdeaStorageMenu.GRID_COLUMNS;
        int end = Math.min(visible.size(), start + IdeaStorageMenu.GRID_COLUMNS * gridRows());
        for (int i = start; i < end; i++) {
            int cellIndex = i - start;
            int x = gridLeft + (cellIndex % IdeaStorageMenu.GRID_COLUMNS) * CELL;
            int y = gridTop + (cellIndex / IdeaStorageMenu.GRID_COLUMNS) * CELL;
            MessageIdeaStorageSync.Entry entry = visible.get(i);
            guiGraphics.renderItem(entry.template(), x + 1, y + 1);
            drawCount(guiGraphics, entry.count(), x + 1, y + 1);
        }
    }

    /** バニラコンテナ風のフレーム(明るいグレー背景 + ベベル枠)。 */
    private static void drawFrame(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, COLOR_BACKGROUND);
        // 外周の黒枠
        guiGraphics.fill(x, y, x + width, y + 1, COLOR_FRAME_OUTER);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, COLOR_FRAME_OUTER);
        guiGraphics.fill(x, y, x + 1, y + height, COLOR_FRAME_OUTER);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, COLOR_FRAME_OUTER);
        // 内側のベベル(上左=白、下右=暗灰)
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + 2, COLOR_FRAME_LIGHT);
        guiGraphics.fill(x + 1, y + 1, x + 2, y + height - 1, COLOR_FRAME_LIGHT);
        guiGraphics.fill(x + 1, y + height - 2, x + width - 1, y + height - 1, COLOR_FRAME_DARK);
        guiGraphics.fill(x + width - 2, y + 1, x + width - 1, y + height - 1, COLOR_FRAME_DARK);
    }

    /** バニラ風スロット(内側は灰、上左=暗、下右=白のベベル)。 */
    private static void drawSlot(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x + 1, y + 1, x + CELL - 1, y + CELL - 1, COLOR_SLOT_INNER);
        guiGraphics.fill(x, y, x + CELL, y + 1, COLOR_SLOT_DARK);
        guiGraphics.fill(x, y, x + 1, y + CELL, COLOR_SLOT_DARK);
        guiGraphics.fill(x, y + CELL - 1, x + CELL, y + CELL, COLOR_SLOT_LIGHT);
        guiGraphics.fill(x + CELL - 1, y, x + CELL, y + CELL, COLOR_SLOT_LIGHT);
    }

    /** クラフトマトリクス → 結果スロットの矢印(fill ベース)。 */
    private static void drawArrow(GuiGraphics guiGraphics, int x, int y) {
        // 軸
        guiGraphics.fill(x, y + 3, x + 9, y + 6, COLOR_ARROW);
        // 矢じり(右向き)
        guiGraphics.fill(x + 9, y, x + 11, y + 9, COLOR_ARROW);
        guiGraphics.fill(x + 11, y + 1, x + 13, y + 8, COLOR_ARROW);
        guiGraphics.fill(x + 13, y + 2, x + 15, y + 7, COLOR_ARROW);
        guiGraphics.fill(x + 15, y + 3, x + 16, y + 6, COLOR_ARROW);
    }

    private void drawScrollBar(GuiGraphics guiGraphics) {
        int trackX = this.leftPos + IdeaStorageMenu.SCROLLBAR_X;
        int trackY = this.topPos + IdeaStorageMenu.GRID_Y;
        int trackHeight = gridRows() * CELL;
        guiGraphics.fill(trackX, trackY, trackX + IdeaStorageMenu.SCROLLBAR_WIDTH, trackY + trackHeight, COLOR_SCROLL_TRACK);
        int maxFirst = maxFirstRow(filteredEntries().size());
        if (maxFirst <= 0) {
            return;
        }
        int totalRows = maxFirst + gridRows();
        int handleHeight = Math.max(MIN_HANDLE_HEIGHT, trackHeight * gridRows() / totalRows);
        int handleY = trackY + (trackHeight - handleHeight) * firstRow / maxFirst;
        // ハンドル本体 + ベベル
        guiGraphics.fill(trackX + 1, handleY, trackX + IdeaStorageMenu.SCROLLBAR_WIDTH - 1, handleY + handleHeight, COLOR_SCROLL_HANDLE);
        guiGraphics.fill(trackX + 1, handleY, trackX + IdeaStorageMenu.SCROLLBAR_WIDTH - 1, handleY + 1, COLOR_SCROLL_HANDLE_LIGHT);
        guiGraphics.fill(trackX + 1, handleY, trackX + 2, handleY + handleHeight, COLOR_SCROLL_HANDLE_LIGHT);
        guiGraphics.fill(trackX + 1, handleY + handleHeight - 1, trackX + IdeaStorageMenu.SCROLLBAR_WIDTH - 1, handleY + handleHeight, COLOR_SCROLL_HANDLE_DARK);
        guiGraphics.fill(trackX + IdeaStorageMenu.SCROLLBAR_WIDTH - 2, handleY, trackX + IdeaStorageMenu.SCROLLBAR_WIDTH - 1, handleY + handleHeight, COLOR_SCROLL_HANDLE_DARK);
    }

    private void drawCount(GuiGraphics guiGraphics, long count, int x, int y) {
        if (count <= 1) {
            return;
        }
        String text = formatCount(count);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 300.0F);
        guiGraphics.pose().scale(0.5F, 0.5F, 1.0F);
        int drawX = (x + CELL - 1) * 2 - this.font.width(text);
        int drawY = (y + CELL - 1) * 2 - 9;
        guiGraphics.drawString(this.font, text, drawX, drawY, 0xFFFFFFFF, true);
        guiGraphics.pose().popPose();
    }

    private static String formatCount(long count) {
        if (count < 1_000L) {
            return Long.toString(count);
        }
        if (count < 1_000_000L) {
            return count / 1_000L + "k";
        }
        return count / 1_000_000L + "M";
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, IdeaStorageMenu.GRID_X, 6, COLOR_TEXT, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle,
                IdeaStorageMenu.PLAYER_INVENTORY_X, this.menu.inventoryLabelY(), COLOR_TEXT, false);
    }

    /** クラフトウィンドウを閉じている間はスロット36..45(マトリクス/結果)を描画しない。 */
    @Override
    protected void renderSlot(GuiGraphics guiGraphics, Slot slot) {
        if (this.menu.isCraftSlot(slot) && !this.menu.isCraftOpen()) {
            return;
        }
        super.renderSlot(guiGraphics, slot);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        MessageIdeaStorageSync.Entry hovered = entryAt(mouseX, mouseY);
        if (hovered != null && this.menu.getCarried().isEmpty()) {
            // 独自ツールチップではなく、そのアイテムの通常のツールチップをそのまま表示する
            guiGraphics.renderTooltip(this.font, hovered.template(), mouseX, mouseY);
        }
    }

    @Nullable
    private MessageIdeaStorageSync.Entry entryAt(int mouseX, int mouseY) {
        if (loadFailed) {
            return null;
        }
        // Java の整数除算はゼロ方向切り捨てのため、先に負数を弾かないと
        // グリッド左/上の -17〜-1px が 0 列目/0 行目と誤判定される
        int relX = mouseX - (this.leftPos + IdeaStorageMenu.GRID_X);
        int relY = mouseY - (this.topPos + IdeaStorageMenu.GRID_Y);
        if (relX < 0 || relY < 0) {
            return null;
        }
        int col = relX / CELL;
        int row = relY / CELL;
        if (col >= IdeaStorageMenu.GRID_COLUMNS || row >= gridRows()) {
            return null;
        }
        int index = (firstRow + row) * IdeaStorageMenu.GRID_COLUMNS + col;
        List<MessageIdeaStorageSync.Entry> visible = filteredEntries();
        return index < visible.size() ? visible.get(index) : null;
    }

    private boolean isOverGrid(int mouseX, int mouseY) {
        int relX = mouseX - (this.leftPos + IdeaStorageMenu.GRID_X);
        int relY = mouseY - (this.topPos + IdeaStorageMenu.GRID_Y);
        return relX >= 0 && relX < IdeaStorageMenu.GRID_COLUMNS * CELL
                && relY >= 0 && relY < gridRows() * CELL;
    }

    private boolean isOverScrollBar(int mouseX, int mouseY) {
        int relX = mouseX - (this.leftPos + IdeaStorageMenu.SCROLLBAR_X);
        int relY = mouseY - (this.topPos + IdeaStorageMenu.GRID_Y);
        return relX >= 0 && relX < IdeaStorageMenu.SCROLLBAR_WIDTH
                && relY >= 0 && relY < gridRows() * CELL;
    }

    /** スクロールバーのドラッグ位置から先頭行を更新する。 */
    private void scrollTo(int mouseY) {
        int maxFirst = maxFirstRow(filteredEntries().size());
        if (maxFirst <= 0) {
            return;
        }
        int trackTop = this.topPos + IdeaStorageMenu.GRID_Y;
        int trackHeight = gridRows() * CELL;
        int totalRows = maxFirst + gridRows();
        int handleHeight = Math.max(MIN_HANDLE_HEIGHT, trackHeight * gridRows() / totalRows);
        double ratio = (double) (mouseY - trackTop - handleHeight / 2) / (double) (trackHeight - handleHeight);
        firstRow = Mth.clamp((int) Math.round(ratio * maxFirst), 0, maxFirst);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mx = (int) mouseX;
        int my = (int) mouseY;
        if (isOverScrollBar(mx, my)) {
            draggingScrollBar = true;
            scrollTo(my);
            return true;
        }
        if (isOverGrid(mx, my)) {
            ItemStack carried = this.menu.getCarried();
            if (!carried.isEmpty()) {
                if (!loadFailed) {
                    PacketDistributor.sendToServer(new MessageIdeaStorageDeposit(carried.copyWithCount(1)));
                }
                return true;
            }
            MessageIdeaStorageSync.Entry entry = entryAt(mx, my);
            if (entry != null && !loadFailed) {
                int mode = switch (button) {
                    case 0 -> hasShiftDown()
                            ? MessageIdeaStorageExtract.MODE_INVENTORY_STACK
                            : MessageIdeaStorageExtract.MODE_CURSOR_STACK;
                    case 1 -> hasShiftDown()
                            ? MessageIdeaStorageExtract.MODE_INVENTORY_HALF_STACK
                            : MessageIdeaStorageExtract.MODE_CURSOR_HALF_STACK;
                    default -> 0;
                };
                if (mode == 0) {
                    return super.mouseClicked(mouseX, mouseY, button);
                }
                PacketDistributor.sendToServer(new MessageIdeaStorageExtract(entry.template(), mode));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingScrollBar) {
            scrollTo((int) mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingScrollBar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isOverGrid((int) mouseX, (int) mouseY) || isOverScrollBar((int) mouseX, (int) mouseY)) {
            firstRow = Math.max(0, Math.min(firstRow - (int) Math.signum(scrollY),
                    maxFirstRow(filteredEntries().size())));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchBox != null && searchBox.isFocused()) {
            if (keyCode == 256) {
                searchBox.setFocused(false);
                return true;
            }
            if (searchBox.keyPressed(keyCode, scanCode, modifiers) || searchBox.canConsumeInput()) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (searchBox != null && searchBox.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    /** 左端の小ボタン。アイコン(ItemStack)またはテキストをバニラ風ベベル枠に描画する。 */
    private class SideButton extends Button {
        @Nullable
        private final Supplier<ItemStack> icon;

        SideButton(int x, int y, Component text, @Nullable Supplier<ItemStack> icon,
                   Component tooltip, OnPress onPress) {
            super(x, y, SIDE_BUTTON_WIDTH, SIDE_BUTTON_HEIGHT, text, onPress, DEFAULT_NARRATION);
            this.icon = icon;
            setTooltip(Tooltip.create(tooltip));
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            int x = getX();
            int y = getY();
            guiGraphics.fill(x, y, x + this.width, y + this.height, COLOR_BACKGROUND);
            // ベベル枠(上左=白、下右=暗灰)
            guiGraphics.fill(x, y, x + this.width, y + 1, COLOR_FRAME_OUTER);
            guiGraphics.fill(x, y + this.height - 1, x + this.width, y + this.height, COLOR_FRAME_OUTER);
            guiGraphics.fill(x, y, x + 1, y + this.height, COLOR_FRAME_OUTER);
            guiGraphics.fill(x + this.width - 1, y, x + this.width, y + this.height, COLOR_FRAME_OUTER);
            guiGraphics.fill(x + 1, y + 1, x + this.width - 1, y + 2, COLOR_FRAME_LIGHT);
            guiGraphics.fill(x + 1, y + 1, x + 2, y + this.height - 1, COLOR_FRAME_LIGHT);
            guiGraphics.fill(x + 1, y + this.height - 2, x + this.width - 1, y + this.height - 1, COLOR_FRAME_DARK);
            guiGraphics.fill(x + this.width - 2, y + 1, x + this.width - 1, y + this.height - 1, COLOR_FRAME_DARK);
            if (icon != null) {
                guiGraphics.renderItem(icon.get(), x + 1, y + 1);
            } else {
                guiGraphics.drawCenteredString(IdeaStorageScreen.this.font, getMessage(),
                        x + this.width / 2, y + (this.height - 8) / 2, COLOR_TEXT);
            }
        }
    }
}
