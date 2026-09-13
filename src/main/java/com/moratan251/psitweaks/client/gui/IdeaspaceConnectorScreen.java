package com.moratan251.psitweaks.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.moratan251.psitweaks.common.menu.IdeaspaceConnectorMenu;
import com.moratan251.psitweaks.common.network.MessageConnectorAction;
import com.moratan251.psitweaks.common.network.MessageConnectorTemplate;
import com.moratan251.psitweaks.common.network.MessageConnectorExportSettings;
import com.moratan251.psitweaks.common.storage.connector.ConnectorExportSettings;
import com.moratan251.psitweaks.common.storage.connector.ConnectorResource;
import com.moratan251.psitweaks.common.storage.connector.ConnectorSideMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class IdeaspaceConnectorScreen extends AbstractContainerScreen<IdeaspaceConnectorMenu> {
    private static final ResourceLocation SLOT_SPRITE = ResourceLocation.fromNamespaceAndPath("minecraft", "container/slot");
    private static final int GRID_X = 7, SELECTED_Y = 40, CELL = 18;
    private static final int NEIGHBOR_X = 37, FACE_ROW_Y = 44, FACE_ROW_HEIGHT = 21;
    private static final int ENERGY_X = 150, ENERGY_Y = 74;
    private final IdeaStorageDisplayEntry energyIcon = IdeaStorageDisplayEntry.energy(0);
    private final Button[] modeButtons = new Button[6], autoButtons = new Button[6];
    private final Button[] slotSettingsButtons = new Button[9];
    private final ItemStack[] neighborIcons = new ItemStack[6];
    private final Component[] neighborNames = new Component[6];
    private int neighborRefreshTicks;
    private Button settingsButton, backButton, helpButton, useCommonButton;
    private Button exportButton, applyExportButton;
    private final EditBox[] exportAmounts = new EditBox[ConnectorExportSettings.TYPES];
    private final EditBox[] exportIntervals = new EditBox[ConnectorExportSettings.TYPES];
    private boolean exportPage, exportDraftDirty, loadingExportValues;
    private long exportValuesRevision = -1;
    private boolean settingsOpen;
    private int settingsSlot = -1;
    private boolean ghostClick;
    private boolean draggingEnergy;
    private boolean discardEnergyRelease;
    private CompoundTag lastState;
    private long templateRevision = -1;
    private List<IdeaStorageDisplayEntry> selected = List.of();

    public IdeaspaceConnectorScreen(IdeaspaceConnectorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 204;
        inventoryLabelX = IdeaspaceConnectorMenu.INVENTORY_X;
        inventoryLabelY = 94;
    }

    private static Component text(String suffix, Object... args) {
        return Component.translatable("gui.psitweaks.connector." + suffix, args);
    }

    @Override protected void init() {
        exportValuesRevision = -1;
        String[] draftAmounts = new String[ConnectorExportSettings.TYPES], draftIntervals = new String[ConnectorExportSettings.TYPES];
        boolean restoreDraft = exportPage && exportDraftDirty;
        if (restoreDraft) for (int type = 0; type < ConnectorExportSettings.TYPES; type++) {
            draftAmounts[type] = exportAmounts[type].getValue();
            draftIntervals[type] = exportIntervals[type].getValue();
        }
        super.init();
        minecraft.player.stopUsingItem();
        for (Direction side : Direction.values()) {
            int i = side.ordinal();
            int y = topPos + FACE_ROW_Y + FACE_ROW_HEIGHT * i;
            modeButtons[i] = addRenderableWidget(Button.builder(Component.empty(),
                    button -> send(settingsSlot < 0 ? IdeaspaceConnectorMenu.SIDE : IdeaspaceConnectorMenu.SLOT_SIDE,
                            Math.max(0, settingsSlot), i)).bounds(leftPos + 59, y, 49, 20).build());
            autoButtons[i] = addRenderableWidget(Button.builder(Component.empty(),
                    button -> send(settingsSlot < 0 ? IdeaspaceConnectorMenu.AUTO : IdeaspaceConnectorMenu.SLOT_AUTO,
                            Math.max(0, settingsSlot), i)).bounds(leftPos + 112, y, 56, 20).build());
        }
        for (int slot = 0; slot < 9; slot++) {
            final int index = slot;
            slotSettingsButtons[slot] = addRenderableWidget(Button.builder(Component.literal(Integer.toString(slot + 1)),
                    button -> openSettings(index)).bounds(leftPos + GRID_X + slot * CELL + 1, topPos + 59, 16, 12)
                    .tooltip(Tooltip.create(text("slot_settings", slot + 1))).build());
        }
        settingsButton = addRenderableWidget(Button.builder(text("settings"), button -> openSettings(-1))
                .bounds(leftPos + 8, topPos + 74, 124, 18).build());
        useCommonButton = addRenderableWidget(Button.builder(Component.empty(),
                button -> {
                    exportDraftDirty = false;
                    exportValuesRevision = -1;
                    send(IdeaspaceConnectorMenu.SLOT_COMMON, settingsSlot, slotUsesCommon() ? 1 : 0);
                })
                .bounds(leftPos + 8, topPos + 24, 160, 16).tooltip(Tooltip.create(text("inherit_hint"))).build());
        backButton = addRenderableWidget(Button.builder(text("back"), button -> {
                    if (exportPage) setExportPage(false); else setSettingsOpen(false);
                }).bounds(leftPos + 112, topPos + 180, 56, 18).build());
        exportButton = addRenderableWidget(Button.builder(text("export_settings"), button -> setExportPage(true))
                .bounds(leftPos + 8, topPos + 180, 100, 18).build());
        applyExportButton = addRenderableWidget(Button.builder(Component.translatable("gui.psitweaks.apply"), button -> applyExportSettings())
                .bounds(leftPos + 8, topPos + 180, 100, 18).build());
        for (int type = 0; type < ConnectorExportSettings.TYPES; type++) {
            int y = topPos + 60 + type * 27;
            exportAmounts[type] = addExportField(leftPos + 50, y, 70, text("export_amount"));
            exportAmounts[type].setTooltip(Tooltip.create(text("export_amount_hint", ConnectorExportSettings.maximumAmount(type))));
            exportIntervals[type] = addExportField(leftPos + 124, y, 44, text("export_interval"));
            exportIntervals[type].setTooltip(Tooltip.create(text("export_interval_hint", ConnectorExportSettings.MAX_INTERVAL)));
        }
        helpButton = addRenderableWidget(Button.builder(Component.literal("?"), button -> {})
                .bounds(leftPos + 154, topPos + 6, 14, 14).tooltip(Tooltip.create(text("help"))).build());
        setSettingsOpen(settingsOpen);
        if (restoreDraft) for (int type = 0; type < ConnectorExportSettings.TYPES; type++) {
            exportAmounts[type].setValue(draftAmounts[type]);
            exportIntervals[type].setValue(draftIntervals[type]);
        }
        lastState = null;
        updateState();
    }

    private void openSettings(int slot) {
        settingsSlot = slot;
        exportPage = false;
        setSettingsOpen(true);
    }

    private EditBox addExportField(int x, int y, int width, Component label) {
        var field = new EditBox(font, x, y, width, 18, label);
        field.setMaxLength(10);
        field.setFilter(value -> value.isEmpty() || value.matches("[0-9]+"));
        field.setResponder(value -> {
            if (!loadingExportValues) exportDraftDirty = true;
            updateExportApplyButton();
        });
        return addRenderableWidget(field);
    }

    private void setExportPage(boolean open) {
        exportPage = open;
        exportDraftDirty = false;
        exportValuesRevision = -1;
        setSettingsOpen(true);
    }

    private void setSettingsOpen(boolean open) {
        cancelEnergyDrag();
        if (!open) exportPage = false;
        settingsOpen = open;
        menu.setInventoryVisible(!open);
        settingsButton.visible = helpButton.visible = !open;
        backButton.visible = open;
        exportButton.visible = open && !exportPage;
        applyExportButton.visible = open && exportPage;
        for (int type = 0; type < ConnectorExportSettings.TYPES; type++)
            exportAmounts[type].visible = exportIntervals[type].visible = open && exportPage;
        useCommonButton.visible = open && settingsSlot >= 0;
        for (Button button : slotSettingsButtons) button.visible = !open;
        for (int i = 0; i < 6; i++) modeButtons[i].visible = autoButtons[i].visible = open && !exportPage;
        if (open && !exportPage) updateNeighbors();
        setFocused(null);
        lastState = null;
        updateState();
    }

    /** JEI only sees the nine filter targets on the main page. */
    public List<Rect2i> publishedSlotAreas() {
        if (settingsOpen) return List.of();
        List<Rect2i> areas = new ArrayList<>();
        for (int i = 0; i < 9; i++) areas.add(new Rect2i(leftPos + GRID_X + i * CELL, topPos + SELECTED_Y, CELL, CELL));
        return areas;
    }

    public void acceptGhostResource(int slot, ConnectorResource resource) {
        if (settingsOpen || slot < 0 || slot >= 9 || resource.kind() == ConnectorResource.Kind.EMPTY) return;
        CompoundTag template = resource.save(minecraft.player.registryAccess());
        if (template.sizeInBytes() > MessageConnectorTemplate.MAX_TEMPLATE_SIZE) {
            minecraft.player.displayClientMessage(text("template_too_large"), true);
            return;
        }
        PacketDistributor.sendToServer(new MessageConnectorTemplate(menu.containerId, menu.session(), slot, template));
    }

    private void send(int action, int slot, int argument) {
        PacketDistributor.sendToServer(new MessageConnectorAction(menu.containerId, menu.session(), menu.revision(), action, slot, argument));
    }

    @Override protected void containerTick() {
        super.containerTick();
        updateState();
        if (settingsOpen && !exportPage && ++neighborRefreshTicks >= 10) updateNeighbors();
    }

    private void updateNeighbors() {
        neighborRefreshTicks = 0;
        for (Direction direction : Direction.values()) {
            int i = direction.ordinal();
            neighborIcons[i] = ItemStack.EMPTY;
            neighborNames[i] = null;
            BlockPos pos = menu.blockPos().relative(direction);
            if (minecraft.level == null || !minecraft.level.hasChunkAt(pos)) continue;
            var state = minecraft.level.getBlockState(pos);
            if (state.isAir()) continue;
            // Ask the block for its pick-block representation so modded variants and parts
            // display correctly. This only obtains an icon; it never changes inventory.
            Vec3 hitPos = Vec3.atCenterOf(pos).relative(direction.getOpposite(), 0.5);
            var hit = new BlockHitResult(hitPos, direction.getOpposite(), pos, false);
            ItemStack icon = state.getCloneItemStack(hit, minecraft.level, pos, minecraft.player);
            neighborIcons[i] = icon.copyWithCount(1);
            neighborNames[i] = icon.isEmpty() ? state.getBlock().getName() : icon.getHoverName();
        }
    }

    private void updateState() {
        CompoundTag state = menu.clientState();
        if (lastState == state) return;
        lastState = state;
        boolean reuseTemplates = templateRevision == state.getLong("Templates");
        selected = readEntries(state, "Selected", 9, selected, reuseTemplates);
        templateRevision = state.getLong("Templates");
        boolean inherited = slotUsesCommon();
        useCommonButton.setMessage(text(inherited ? "use_common" : "use_individual"));
        int[] sides = state.getIntArray(settingsSlot < 0 || inherited ? "Sides" : "SlotSides");
        int offset = settingsSlot < 0 || inherited ? 0 : settingsSlot * 6;
        int[] slotAutomatic = state.getIntArray("SlotAutomatic");
        int automaticMask = settingsSlot < 0 || inherited ? state.getInt("Automatic")
                : settingsSlot < slotAutomatic.length ? slotAutomatic[settingsSlot] : 0;
        for (int i = 0; i < 6; i++) {
            ConnectorSideMode mode = ConnectorSideMode.byId(offset + i < sides.length ? sides[offset + i] : 0);
            modeButtons[i].setMessage(text("mode." + mode.name().toLowerCase(Locale.ROOT)));
            modeButtons[i].active = settingsSlot < 0 || !inherited;
            boolean automatic = (automaticMask & (1 << i)) != 0;
            autoButtons[i].setMessage(text(automatic ? "auto_on" : "auto_off"));
            autoButtons[i].active = modeButtons[i].active && mode.output;
        }
        if (exportPage) {
            boolean editable = settingsSlot < 0 || !inherited;
            loadingExportValues = true;
            try {
                int[] amounts = state.getIntArray(inherited ? "ExportAmounts" : "SlotExportAmounts");
                int[] intervals = state.getIntArray(inherited ? "ExportIntervals" : "SlotExportIntervals");
                for (int type = 0; type < ConnectorExportSettings.TYPES; type++) {
                    exportAmounts[type].setEditable(editable);
                    exportIntervals[type].setEditable(editable);
                    if (!exportDraftDirty && exportValuesRevision != menu.revision()) {
                        int index = inherited ? type : settingsSlot * ConnectorExportSettings.TYPES + type;
                        var value = ConnectorExportSettings.read(type, amounts, intervals, index);
                        exportAmounts[type].setValue(Integer.toString(value.amount()));
                        exportIntervals[type].setValue(Integer.toString(value.interval()));
                    }
                }
                exportValuesRevision = menu.revision();
            } finally { loadingExportValues = false; }
            updateExportApplyButton();
        }
    }

    private List<ConnectorExportSettings> readExportFields() {
        List<ConnectorExportSettings> values = new ArrayList<>();
        try {
            for (int type = 0; type < ConnectorExportSettings.TYPES; type++) {
                if (exportAmounts[type] == null || exportIntervals[type] == null) return List.of();
                var value = new ConnectorExportSettings(Integer.parseInt(exportAmounts[type].getValue()),
                        Integer.parseInt(exportIntervals[type].getValue()));
                if (!value.valid(type)) return List.of();
                values.add(value);
            }
        } catch (NumberFormatException ignored) { return List.of(); }
        return values;
    }

    private void updateExportApplyButton() {
        if (applyExportButton != null) applyExportButton.active = exportPage
                && (settingsSlot < 0 || !slotUsesCommon()) && readExportFields().size() == ConnectorExportSettings.TYPES;
    }

    private void applyExportSettings() {
        var settings = readExportFields();
        if (!applyExportButton.active || settings.size() != ConnectorExportSettings.TYPES) return;
        PacketDistributor.sendToServer(new MessageConnectorExportSettings(menu.containerId, menu.session(), menu.revision(), settingsSlot, settings));
        exportDraftDirty = false;
        exportValuesRevision = -1;
    }

    private boolean slotUsesCommon() {
        return settingsSlot < 0 || (menu.clientState().getInt("SlotOverrides") & (1 << settingsSlot)) == 0;
    }

    private List<IdeaStorageDisplayEntry> readEntries(CompoundTag state, String key, int maximum,
                                                   List<IdeaStorageDisplayEntry> previous, boolean reuseTemplates) {
        List<IdeaStorageDisplayEntry> entries = new ArrayList<>();
        var list = state.getList(key, Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(maximum, list.size()); i++) {
            CompoundTag tag = list.getCompound(i);
            long amount = tag.getLong("Amount");
            if (reuseTemplates && list.size() == previous.size()) {
                IdeaStorageDisplayEntry entry = previous.get(i);
                entries.add(entry == null ? null : new IdeaStorageDisplayEntry(entry.kind(), entry.itemTemplate(),
                        entry.fluidTemplate(), entry.chemicalId(), amount));
                continue;
            }
            ConnectorResource resource = ConnectorResource.load(tag, minecraft.player.registryAccess());
            entries.add(switch (resource.kind()) {
                case EMPTY -> null;
                case ITEM -> new IdeaStorageDisplayEntry(IdeaStorageDisplayEntry.Kind.ITEM,
                        resource.item().template(), FluidStack.EMPTY, null, amount);
                case FLUID -> new IdeaStorageDisplayEntry(IdeaStorageDisplayEntry.Kind.FLUID,
                        ItemStack.EMPTY, resource.fluid().template(), null, amount);
                case CHEMICAL -> new IdeaStorageDisplayEntry(IdeaStorageDisplayEntry.Kind.CHEMICAL,
                        ItemStack.EMPTY, FluidStack.EMPTY, resource.chemical(), amount);
                case ENERGY -> IdeaStorageDisplayEntry.energy(amount);
            });
        }
        return entries;
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF252B34);
        graphics.fill(leftPos + 2, topPos + 2, leftPos + imageWidth - 2, topPos + imageHeight - 2, 0xFFCDD2DA);
        if (settingsOpen) {
            if (settingsSlot >= 0 && settingsSlot < selected.size() && selected.get(settingsSlot) != null)
                IdeaStorageScreen.renderEntry(graphics, selected.get(settingsSlot), leftPos + 151, topPos + 7);
            if (exportPage) return;
            for (Direction direction : Direction.values()) {
                int i = direction.ordinal();
                int x = leftPos + NEIGHBOR_X, y = topPos + FACE_ROW_Y + 1 + FACE_ROW_HEIGHT * i;
                drawSlotBackground(graphics, x, y);
                ItemStack icon = neighborIcons[i];
                if (icon != null && !icon.isEmpty()) graphics.renderItem(icon, x + 1, y + 1);
                else if (neighborNames[i] != null) graphics.drawCenteredString(font, "?", x + 9, y + 5, 0xFFFFFFFF);
            }
        } else {
            for (int i = 0; i < 9; i++) drawCell(graphics, selected, i, SELECTED_Y);
            drawSlotBackground(graphics, leftPos + ENERGY_X, topPos + ENERGY_Y);
            IdeaStorageScreen.renderEntry(graphics, energyIcon, leftPos + ENERGY_X + 1, topPos + ENERGY_Y + 1);
            for (var slot : menu.slots) {
                int x = leftPos + slot.x - 1, y = topPos + slot.y - 1;
                drawSlotBackground(graphics, x, y);
            }
        }
    }

    private void drawSlotBackground(GuiGraphics graphics, int x, int y) {
        graphics.blitSprite(SLOT_SPRITE, x, y, CELL, CELL);
    }

    private void drawCell(GuiGraphics graphics, List<IdeaStorageDisplayEntry> entries, int index, int startY) {
        int x = leftPos + GRID_X + index % 9 * CELL;
        int y = topPos + startY + index / 9 * CELL;
        drawSlotBackground(graphics, x, y);
        IdeaStorageDisplayEntry entry = index < entries.size() ? entries.get(index) : null;
        if (entry == null) return;
        IdeaStorageScreen.renderEntry(graphics, entry, x + 1, y + 1);
        String count = IdeaStorageAmountFormatter.formatGrid(entry.amount(), entry.displayAmountScale());
        float scale = Math.min(0.65F, 16F / Math.max(1, font.width(count)));
        graphics.pose().pushPose();
        graphics.pose().translate(x + 17, y + 17, 400);
        graphics.pose().scale(scale, scale, 1);
        graphics.drawString(font, count, -font.width(count), -font.lineHeight, 0xFFFFFFFF, true);
        graphics.pose().popPose();
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        Component heading = settingsOpen ? settingsSlot < 0 ? text(exportPage ? "export_common_title" : "settings")
                : text(exportPage ? "export_slot_title" : "slot_settings", settingsSlot + 1) : title;
        graphics.drawString(font, heading, 8, 8, 0xFF202A35, false);
        if (exportPage) {
            graphics.drawString(font, text("export_amount"), 50, 47, 0xFF202A35, false);
            Component intervalLabel = text("export_interval");
            graphics.drawString(font, intervalLabel, 168 - font.width(intervalLabel), 47, 0xFF202A35, false);
            for (int type = 0; type < ConnectorExportSettings.TYPES; type++) {
                Component label = text("export_type." + type);
                // Wrap the unit below the name so labels stay clear of the amount field.
                int lines = font.split(label, 38).size();
                int y = 60 + type * 27 + Math.max(0, (18 - lines * font.lineHeight) / 2);
                graphics.drawWordWrap(font, label, 8, y, 38, 0xFF202A35);
            }
            graphics.drawString(font, text("export_tick_hint"), 8, 166, 0xFF202A35, false);
            return;
        }
        if (settingsOpen) {
            if (settingsSlot < 0) graphics.drawString(font, text("faces"), 8, 26, 0xFF202A35, false);
            for (Direction side : Direction.values()) graphics.drawString(font, text("face." + side.getName()),
                    8, FACE_ROW_Y + 6 + side.ordinal() * FACE_ROW_HEIGHT, 0xFF202A35, false);
        } else {
            graphics.drawString(font, text("published"), 8, 26, 0xFF202A35, false);
            graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFF202A35, false);
        }
        if (menu.clientState().getBoolean("LoadFailed"))
            graphics.drawWordWrap(font, text("load_failed"), 8, settingsOpen ? 170 : 186, 160, 0xFFAA2222);
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        updateState();
        super.render(graphics, mouseX, mouseY, partialTick);
        if (settingsOpen) {
            if (settingsSlot >= 0 && settingsSlot < selected.size() && selected.get(settingsSlot) != null
                    && new Rect2i(leftPos + 151, topPos + 7, 16, 16).contains(mouseX, mouseY))
                graphics.renderTooltip(font, selected.get(settingsSlot).displayName(), mouseX, mouseY);
            if (exportPage) return;
            for (Direction side : Direction.values()) {
                int i = side.ordinal();
                var area = new Rect2i(leftPos + NEIGHBOR_X, topPos + FACE_ROW_Y + 1 + FACE_ROW_HEIGHT * i, CELL, CELL);
                if (neighborNames[i] != null && area.contains(mouseX, mouseY)) {
                    graphics.renderTooltip(font, neighborNames[i], mouseX, mouseY);
                    break;
                }
            }
            return;
        }
        if (draggingEnergy) {
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 500);
            IdeaStorageScreen.renderEntry(graphics, energyIcon, mouseX - 8, mouseY - 8);
            graphics.pose().popPose();
            return;
        }
        if (energyIconAt(mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, List.of(energyIcon.displayName(), text("energy_hint")), mouseX, mouseY);
            return;
        }
        renderTooltip(graphics, mouseX, mouseY);
        int index = cellAt(mouseX, mouseY, SELECTED_Y, 1);
        List<IdeaStorageDisplayEntry> entries = selected;
        if (index < 0 || index >= entries.size()) return;
        IdeaStorageDisplayEntry entry = entries.get(index);
        if (entry == null) return;
        String unit = switch (entry.kind()) { case ITEM -> ""; case ENERGY -> " FE"; default -> " mB"; };
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(entry.displayName());
        tooltip.add(text("amount", String.format(Locale.ROOT, "%,d", entry.amount()) + unit));
        tooltip.add(text("clear_hint"));
        graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
    }

    private int cellAt(double x, double y, int startY, int rows) {
        int dx = (int) Math.floor(x - leftPos - GRID_X), dy = (int) Math.floor(y - topPos - startY);
        return dx >= 0 && dx < 9 * CELL && dy >= 0 && dy < rows * CELL ? dx / CELL + dy / CELL * 9 : -1;
    }

    private boolean energyIconAt(double x, double y) {
        return !settingsOpen && x >= leftPos + ENERGY_X && x < leftPos + ENERGY_X + CELL
                && y >= topPos + ENERGY_Y && y < topPos + ENERGY_Y + CELL;
    }

    private void cancelEnergyDrag() {
        if (draggingEnergy) {
            draggingEnergy = false;
            // Swallow the original left-button release even if Escape or a resize cancelled the drag.
            discardEnergyRelease = true;
        }
    }

    @Override public boolean mouseClicked(double x, double y, int button) {
        if (draggingEnergy || discardEnergyRelease) return true;
        if (energyIconAt(x, y) && (button == 0 || button == 1)) {
            if (button == 0 && menu.getCarried().isEmpty()) {
                draggingEnergy = true;
                ghostClick = false;
                setFocused(null);
            } else {
                ghostClick = true;
            }
            return true;
        }
        int index = settingsOpen ? -1 : cellAt(x, y, SELECTED_Y, 1);
        if (index >= 0 && (button == 0 || button == 1)) {
            ghostClick = true;
            if (button == 1 && menu.getCarried().isEmpty()) send(IdeaspaceConnectorMenu.CLEAR, index, 0);
            else {
                if (!menu.getCarried().isEmpty())
                    send(IdeaspaceConnectorMenu.ASSIGN, index, button == 1 || hasShiftDown() ? 1 : 0);
            }
            return true;
        }
        return super.mouseClicked(x, y, button);
    }

    @Override public boolean mouseDragged(double x, double y, int button, double dragX, double dragY) {
        if (draggingEnergy || discardEnergyRelease || ghostClick) return true;
        return super.mouseDragged(x, y, button, dragX, dragY);
    }

    @Override public boolean mouseReleased(double x, double y, int button) {
        if (discardEnergyRelease) {
            if (button == 0) discardEnergyRelease = false;
            return true;
        }
        if (draggingEnergy) {
            if (button == 0) {
                draggingEnergy = false;
                int slot = settingsOpen ? -1 : cellAt(x, y, SELECTED_Y, 1);
                if (slot >= 0 && menu.getCarried().isEmpty()) send(IdeaspaceConnectorMenu.ENERGY, slot, 0);
            }
            return true;
        }
        if (ghostClick && (button == 0 || button == 1)) {
            ghostClick = false;
            return true;
        }
        return super.mouseReleased(x, y, button);
    }

    @Override public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (draggingEnergy || discardEnergyRelease) {
            if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) cancelEnergyDrag();
            else if (minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(key, scanCode))) onClose();
            // Inventory shortcuts must not act on real items underneath the FE preview.
            return true;
        }
        if (exportPage) {
            if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) { setExportPage(false); return true; }
            if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER || key == org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER) {
                applyExportSettings();
                return true;
            }
            if (getFocused() instanceof EditBox field && key != org.lwjgl.glfw.GLFW.GLFW_KEY_TAB) {
                field.keyPressed(key, scanCode, modifiers);
                return true; // Do not turn number/drop/inventory keys into container shortcuts while typing.
            }
        }
        if (settingsOpen && key == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
            setSettingsOpen(false);
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override public void removed() {
        draggingEnergy = false;
        discardEnergyRelease = false;
        ghostClick = false;
        super.removed();
    }
}
