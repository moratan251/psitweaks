package com.moratan251.psitweaks.client.gui;

import com.moratan251.psitweaks.client.compat.IdeaStorageChemicalClientCompat;
import com.moratan251.psitweaks.common.menu.IdeaStorageMenu;
import com.moratan251.psitweaks.common.network.MessageIdeaStorageClearCrafting;
import com.moratan251.psitweaks.common.network.MessageIdeaStorageCraftToggle;
import com.moratan251.psitweaks.common.network.MessageIdeaStorageDeposit;
import com.moratan251.psitweaks.common.network.MessageIdeaStorageExtract;
import com.moratan251.psitweaks.common.network.MessageIdeaStorageFillBucket;
import com.moratan251.psitweaks.common.network.MessageIdeaStorageResize;
import com.moratan251.psitweaks.common.network.MessageIdeaStorageSync;
import com.moratan251.psitweaks.common.network.MessageIdeaStorageTransferContents;
import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import com.moratan251.psitweaks.common.network.IdeaStorageNetwork;
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
    private static final float COUNT_SCALE = 0.75F;
    private static final int MIN_HANDLE_HEIGHT = 12;
    private static final int SIDE_BUTTON_WIDTH = 18;
    private static final int SIDE_BUTTON_HEIGHT = 18;
    private static final int SIDE_BUTTON_X = -20;
    private static final int CRAFT_BUTTON_Y = 34;
    private static final int ROWS_ADD_BUTTON_Y = 54;
    private static final int ROWS_REMOVE_BUTTON_Y = 74;
    private static final int SORT_BUTTON_Y = 94;
    private static final int INFO_BUTTON_Y = 114;
    private static final int CRAFT_CLEAR_BUTTON_SIZE = 12;

    private List<IdeaStorageDisplayEntry> entries = List.of();
    private boolean loadFailed;
    private int itemTypeCount;
    private int fluidTypeCount;
    private int chemicalTypeCount;
    private int maxItemTypes;
    private int maxFluidTypes;
    private int maxChemicalTypes;
    private String filter = "";
    private int firstRow;
    private boolean draggingScrollBar;
    @Nullable
    private EditBox searchBox;
    @Nullable
    private Button craftClearButton;

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
        SideButton infoButton = new SideButton(this.leftPos + SIDE_BUTTON_X, this.topPos + INFO_BUTTON_Y,
                Component.literal("i"), null, null, button -> {
                });
        infoButton.active = false;
        addRenderableWidget(infoButton);
        craftClearButton = addRenderableWidget(new CraftClearButton(
                this.leftPos + IdeaStorageMenu.CRAFT_PANEL_X + IdeaStorageMenu.CRAFT_PANEL_WIDTH
                        - CRAFT_CLEAR_BUTTON_SIZE - 3,
                this.topPos + IdeaStorageMenu.CRAFT_PANEL_Y + 3));
        craftClearButton.visible = this.menu.isCraftOpen();
        craftClearButton.active = this.menu.isCraftOpen();

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
        if (craftClearButton != null) {
            craftClearButton.visible = next;
            craftClearButton.active = next;
        }
        IdeaStorageNetwork.sendToServer(new MessageIdeaStorageCraftToggle(next));
    }

    /** JEI/EMI の占有領域(exclusion area)通知用。クラフトパネルの矩形(画面絶対座標)。 */
    public Rect2i getCraftPanelArea() {
        return new Rect2i(this.leftPos + IdeaStorageMenu.CRAFT_PANEL_X, this.topPos + IdeaStorageMenu.CRAFT_PANEL_Y,
                IdeaStorageMenu.CRAFT_PANEL_WIDTH, IdeaStorageMenu.CRAFT_PANEL_HEIGHT);
    }

    /** JEI/EMI の占有領域(exclusion area)通知用。左端サイドボタン列の矩形(画面絶対座標)。 */
    public Rect2i getSideButtonArea() {
        return new Rect2i(this.leftPos + SIDE_BUTTON_X, this.topPos + CRAFT_BUTTON_Y,
                SIDE_BUTTON_WIDTH, INFO_BUTTON_Y + SIDE_BUTTON_HEIGHT - CRAFT_BUTTON_Y);
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
            IdeaStorageNetwork.sendToServer(new MessageIdeaStorageResize(next));
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

    public void applySnapshot(MessageIdeaStorageSync message) {
        List<IdeaStorageDisplayEntry> displayEntries = new ArrayList<>(
                message.entries().size() + message.fluidEntries().size() + message.chemicalEntries().size());
        message.entries().stream().map(IdeaStorageDisplayEntry::item).forEach(displayEntries::add);
        message.fluidEntries().stream().map(IdeaStorageDisplayEntry::fluid).forEach(displayEntries::add);
        message.chemicalEntries().stream().map(IdeaStorageDisplayEntry::chemical).forEach(displayEntries::add);
        this.entries = List.copyOf(displayEntries);
        this.menu.applyClientStorageEntries(message.entries());
        this.itemTypeCount = message.entries().size();
        this.fluidTypeCount = message.fluidEntries().size();
        this.chemicalTypeCount = message.chemicalEntries().size();
        this.maxItemTypes = message.maxItemTypes();
        this.maxFluidTypes = message.maxFluidTypes();
        this.maxChemicalTypes = message.maxChemicalTypes();
        this.loadFailed = message.loadFailed();
        clampScroll();
    }

    /** フィルタ適用後、現在のソートモードで並べ替えた表示リストを返す(PORT は元順のまま)。 */
    private List<IdeaStorageDisplayEntry> filteredEntries() {
        List<IdeaStorageDisplayEntry> filtered;
        if (filter.isEmpty()) {
            filtered = entries;
        } else {
            String needle = filter.toLowerCase(Locale.ROOT);
            filtered = new ArrayList<>();
            for (IdeaStorageDisplayEntry entry : entries) {
                String name = entry.displayName().getString().toLowerCase(Locale.ROOT);
                String id = entry.resourceId().toString();
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
        List<IdeaStorageDisplayEntry> sorted = new ArrayList<>(filtered);
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

        List<IdeaStorageDisplayEntry> visible = filteredEntries();
        int start = firstRow * IdeaStorageMenu.GRID_COLUMNS;
        int end = Math.min(visible.size(), start + IdeaStorageMenu.GRID_COLUMNS * gridRows());
        for (int i = start; i < end; i++) {
            int cellIndex = i - start;
            int x = gridLeft + (cellIndex % IdeaStorageMenu.GRID_COLUMNS) * CELL;
            int y = gridTop + (cellIndex / IdeaStorageMenu.GRID_COLUMNS) * CELL;
            IdeaStorageDisplayEntry entry = visible.get(i);
            renderEntry(guiGraphics, entry, x + 1, y + 1);
            drawCount(guiGraphics, entry, x + 1, y + 1);
        }
        EntryCell hovered = entryCellAt(mouseX, mouseY);
        if (hovered != null) {
            renderSlotHighlight(guiGraphics, hovered.x() + 1, hovered.y() + 1, 0);
        }
    }

    private static void renderEntry(GuiGraphics guiGraphics, IdeaStorageDisplayEntry entry, int x, int y) {
        switch (entry.kind()) {
            case ITEM -> guiGraphics.renderItem(entry.itemTemplate(), x, y);
            case FLUID -> renderFluid(guiGraphics, entry.fluidTemplate(), x, y);
            case CHEMICAL -> {
                if (!IdeaStorageChemicalClientCompat.render(guiGraphics, entry.chemicalId(), x, y)) {
                    guiGraphics.fill(x + 1, y + 1, x + 15, y + 15, 0xFF6A3D8F);
                }
            }
        }
    }

    private static void renderFluid(GuiGraphics guiGraphics, FluidStack fluid, int x, int y) {
        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid.getFluid());
        ResourceLocation stillTexture = extensions.getStillTexture(fluid);
        if (stillTexture == null) {
            guiGraphics.fill(x + 1, y + 1, x + 15, y + 15, 0xFF3A78B8);
            return;
        }
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(stillTexture);
        int color = extensions.getTintColor(fluid);
        guiGraphics.setColor(
                ((color >> 16) & 0xFF) / 255.0F,
                ((color >> 8) & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F,
                ((color >>> 24) & 0xFF) / 255.0F
        );
        guiGraphics.blit(x, y, 300, 16, 16, sprite);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
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

    private void drawCount(GuiGraphics guiGraphics, IdeaStorageDisplayEntry entry, int x, int y) {
        BigDecimal displayAmount = entry.amountInDisplayUnits();
        if (displayAmount.compareTo(BigDecimal.ONE) == 0) {
            return;
        }
        String text = IdeaStorageAmountFormatter.formatGrid(entry.amount(), entry.displayAmountScale());
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 300.0F);
        guiGraphics.pose().scale(COUNT_SCALE, COUNT_SCALE, 1.0F);
        int drawX = Math.round((x + CELL - 1) / COUNT_SCALE) - this.font.width(text);
        int drawY = Math.round((y + CELL - 1) / COUNT_SCALE) - 9;
        guiGraphics.drawString(this.font, text, drawX, drawY, 0xFFFFFFFF, true);
        guiGraphics.pose().popPose();
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, IdeaStorageMenu.GRID_X, 6, COLOR_TEXT, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle,
                IdeaStorageMenu.PLAYER_INVENTORY_X, this.menu.inventoryLabelY(), COLOR_TEXT, false);
    }

    /** クラフトウィンドウを閉じている間はスロット36..45(マトリクス/結果)を描画しない。 */

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        super.renderTooltip(guiGraphics, mouseX, mouseY);
        if (isOverInfoButton(mouseX, mouseY)) {
            guiGraphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("gui.psitweaks.idea_storage.info.item_types",
                            itemTypeCount, maxItemTypes),
                    Component.translatable("gui.psitweaks.idea_storage.info.fluid_types",
                            fluidTypeCount, maxFluidTypes),
                    Component.translatable("gui.psitweaks.idea_storage.info.chemical_types",
                            chemicalTypeCount, maxChemicalTypes)
            ), mouseX, mouseY);
        }
        IdeaStorageDisplayEntry hovered = entryAt(mouseX, mouseY);
        if (hovered != null && this.menu.getCarried().isEmpty()) {
            if (hovered.kind() == IdeaStorageDisplayEntry.Kind.ITEM) {
                renderItemEntryTooltip(guiGraphics, hovered, mouseX, mouseY);
            } else {
                String amountKey = hovered.kind() == IdeaStorageDisplayEntry.Kind.FLUID
                        ? "gui.psitweaks.idea_storage.fluid_amount"
                        : "gui.psitweaks.idea_storage.chemical_amount";
                guiGraphics.renderComponentTooltip(this.font, List.of(
                        hovered.displayName(),
                        Component.translatable(amountKey, IdeaStorageAmountFormatter.formatExact(
                                hovered.amount(), hovered.displayAmountScale())),
                        Component.literal(hovered.resourceId().toString()).withStyle(ChatFormatting.DARK_GRAY)
                ), mouseX, mouseY);
            }
        }
    }

    private void renderItemEntryTooltip(GuiGraphics guiGraphics, IdeaStorageDisplayEntry entry,
                                        int mouseX, int mouseY) {
        ItemStack stack = entry.itemTemplate();
        TooltipFlag flag = this.minecraft.options.advancedItemTooltips
                ? TooltipFlag.ADVANCED
                : TooltipFlag.NORMAL;
        List<Component> lines = new ArrayList<>(stack.getTooltipLines(this.minecraft.player, flag));
        Component amountLine = Component.translatable("gui.psitweaks.idea_storage.item_amount",
                IdeaStorageAmountFormatter.formatExact(entry.amount(), entry.displayAmountScale()))
                .withStyle(ChatFormatting.GRAY);
        String resourceId = entry.resourceId().toString();
        int idLine = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).getString().equals(resourceId)) {
                idLine = i;
                break;
            }
        }
        if (idLine >= 0) {
            lines.add(idLine, amountLine);
        } else {
            lines.add(amountLine);
            lines.add(Component.literal(resourceId).withStyle(ChatFormatting.DARK_GRAY));
        }
        guiGraphics.renderTooltip(this.font, lines, stack.getTooltipImage(), stack, mouseX, mouseY);
    }

    @Nullable
    private IdeaStorageDisplayEntry entryAt(int mouseX, int mouseY) {
        EntryCell cell = entryCellAt(mouseX, mouseY);
        return cell == null ? null : cell.entry();
    }

    @Nullable
    private EntryCell entryCellAt(int mouseX, int mouseY) {
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
        List<IdeaStorageDisplayEntry> visible = filteredEntries();
        if (index >= visible.size()) {
            return null;
        }
        return new EntryCell(visible.get(index),
                this.leftPos + IdeaStorageMenu.GRID_X + col * CELL,
                this.topPos + IdeaStorageMenu.GRID_Y + row * CELL);
    }

    /** JEI/EMIのRecipe/Usageキー対象として公開する仮想Itemエントリ。 */
    public Optional<StorageItemReference> getStorageItemUnderMouse(double mouseX, double mouseY) {
        EntryCell cell = entryCellAt((int) mouseX, (int) mouseY);
        if (cell == null || cell.entry().kind() != IdeaStorageDisplayEntry.Kind.ITEM) {
            return Optional.empty();
        }
        return Optional.of(new StorageItemReference(cell.entry().itemTemplate().copyWithCount(1),
                new Rect2i(cell.x() + 1, cell.y() + 1, 16, 16)));
    }

    private boolean isOverGrid(int mouseX, int mouseY) {
        int relX = mouseX - (this.leftPos + IdeaStorageMenu.GRID_X);
        int relY = mouseY - (this.topPos + IdeaStorageMenu.GRID_Y);
        return relX >= 0 && relX < IdeaStorageMenu.GRID_COLUMNS * CELL
                && relY >= 0 && relY < gridRows() * CELL;
    }

    private boolean isOverInfoButton(int mouseX, int mouseY) {
        int relX = mouseX - (this.leftPos + SIDE_BUTTON_X);
        int relY = mouseY - (this.topPos + INFO_BUTTON_Y);
        return relX >= 0 && relX < SIDE_BUTTON_WIDTH && relY >= 0 && relY < SIDE_BUTTON_HEIGHT;
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
            if (searchBox != null) {
                searchBox.setFocused(false);
            }
            ItemStack carried = this.menu.getCarried();
            if (!carried.isEmpty()) {
                if (!loadFailed) {
                    if (button == 0) {
                        IdeaStorageNetwork.sendToServer(new MessageIdeaStorageDeposit(carried.copyWithCount(1)));
                    } else if (button == 1) {
                        IdeaStorageNetwork.sendToServer(contentTransferMessage(entryAt(mx, my), hasShiftDown()));
                    } else {
                        return super.mouseClicked(mouseX, mouseY, button);
                    }
                }
                return true;
            }
            IdeaStorageDisplayEntry entry = entryAt(mx, my);
            if (entry != null && !loadFailed
                    && entry.kind() == IdeaStorageDisplayEntry.Kind.FLUID && button == 0) {
                IdeaStorageNetwork.sendToServer(new MessageIdeaStorageFillBucket(entry.fluidTemplate()));
                return true;
            }
            if (entry != null && !loadFailed && entry.kind() == IdeaStorageDisplayEntry.Kind.ITEM) {
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
                IdeaStorageNetwork.sendToServer(new MessageIdeaStorageExtract(entry.itemTemplate(), mode));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private static MessageIdeaStorageTransferContents contentTransferMessage(
            @Nullable IdeaStorageDisplayEntry target, boolean bulk) {
        if (target == null || target.kind() == IdeaStorageDisplayEntry.Kind.ITEM) {
            return MessageIdeaStorageTransferContents.emptyTarget(bulk);
        }
        if (target.kind() == IdeaStorageDisplayEntry.Kind.FLUID) {
            return MessageIdeaStorageTransferContents.fluidTarget(target.fluidTemplate(), bulk);
        }
        return MessageIdeaStorageTransferContents.chemicalTarget(target.chemicalId(), bulk);
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
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        if (isOverGrid((int) mouseX, (int) mouseY) || isOverScrollBar((int) mouseX, (int) mouseY)) {
            firstRow = Math.max(0, Math.min(firstRow - (int) Math.signum(scrollY),
                    maxFirstRow(filteredEntries().size())));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollY);
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
                   @Nullable Component tooltip, OnPress onPress) {
            super(x, y, SIDE_BUTTON_WIDTH, SIDE_BUTTON_HEIGHT, text, onPress, DEFAULT_NARRATION);
            this.icon = icon;
            if (tooltip != null) {
                setTooltip(Tooltip.create(tooltip));
            }
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

    private class CraftClearButton extends Button {
        CraftClearButton(int x, int y) {
            super(x, y, CRAFT_CLEAR_BUTTON_SIZE, CRAFT_CLEAR_BUTTON_SIZE, Component.literal("×"),
                    button -> IdeaStorageNetwork.sendToServer(new MessageIdeaStorageClearCrafting()),
                    DEFAULT_NARRATION);
            setTooltip(Tooltip.create(Component.translatable("gui.psitweaks.idea_storage.craft_clear")));
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            int x = getX();
            int y = getY();
            guiGraphics.fill(x, y, x + this.width, y + this.height, COLOR_BACKGROUND);
            guiGraphics.fill(x, y, x + this.width, y + 1, COLOR_FRAME_OUTER);
            guiGraphics.fill(x, y + this.height - 1, x + this.width, y + this.height, COLOR_FRAME_OUTER);
            guiGraphics.fill(x, y, x + 1, y + this.height, COLOR_FRAME_OUTER);
            guiGraphics.fill(x + this.width - 1, y, x + this.width, y + this.height, COLOR_FRAME_OUTER);
            guiGraphics.drawCenteredString(IdeaStorageScreen.this.font, getMessage(),
                    x + this.width / 2, y + 2, COLOR_TEXT);
        }
    }

    private record EntryCell(IdeaStorageDisplayEntry entry, int x, int y) {
    }

    public record StorageItemReference(ItemStack stack, Rect2i area) {
    }
}
