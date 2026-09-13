package com.moratan251.psitweaks.client.gui;

import com.moratan251.psitweaks.common.menu.IdeaspaceConnectorMenu;
import com.moratan251.psitweaks.common.network.MessageConnectorAction;
import com.moratan251.psitweaks.common.storage.connector.ConnectorResource;
import com.moratan251.psitweaks.common.storage.connector.ConnectorSideMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class IdeaspaceConnectorScreen extends AbstractContainerScreen<IdeaspaceConnectorMenu> {
    private static final int GRID_X = 10, SELECTED_Y = 38, AVAILABLE_Y = 88, CELL = 18;
    private final Button[] modeButtons = new Button[6], autoButtons = new Button[6];
    private Button previous, next;
    private int selectedSlot;
    private CompoundTag lastState;
    private long templateRevision = -1;
    private List<IdeaStorageDisplayEntry> selected = List.of(), available = List.of();

    public IdeaspaceConnectorScreen(IdeaspaceConnectorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 382;
        imageHeight = 224;
    }

    private static Component text(String suffix, Object... args) {
        return Component.translatable("gui.psitweaks.connector." + suffix, args);
    }

    @Override protected void init() {
        super.init();
        for (Direction side : Direction.values()) {
            int i = side.ordinal();
            int y = topPos + 38 + 25 * i;
            modeButtons[i] = addRenderableWidget(Button.builder(Component.empty(),
                    button -> send(IdeaspaceConnectorMenu.SIDE, 0, i)).bounds(leftPos + 222, y, 82, 20).build());
            autoButtons[i] = addRenderableWidget(Button.builder(Component.empty(),
                    button -> send(IdeaspaceConnectorMenu.AUTO, 0, i)).bounds(leftPos + 308, y, 64, 20).build());
        }
        previous = addRenderableWidget(Button.builder(Component.literal("<"), button -> send(IdeaspaceConnectorMenu.PAGE, 0, -1))
                .bounds(leftPos + 10, topPos + 147, 22, 18).build());
        next = addRenderableWidget(Button.builder(Component.literal(">"), button -> send(IdeaspaceConnectorMenu.PAGE, 0, 1))
                .bounds(leftPos + 150, topPos + 147, 22, 18).build());
        lastState = null;
        updateState();
    }

    private void send(int action, int slot, int argument) {
        PacketDistributor.sendToServer(new MessageConnectorAction(menu.containerId, menu.session(), menu.revision(), action, slot, argument));
    }

    @Override protected void containerTick() {
        super.containerTick();
        updateState();
    }

    private void updateState() {
        CompoundTag state = menu.clientState();
        if (lastState == state) return;
        lastState = state;
        boolean reuseTemplates = templateRevision == state.getLong("Templates");
        selected = readEntries(state, "Selected", 9, selected, reuseTemplates);
        available = readEntries(state, "Available", IdeaspaceConnectorMenu.PAGE_SIZE, available, reuseTemplates);
        templateRevision = state.getLong("Templates");
        int[] sides = state.getIntArray("Sides");
        for (int i = 0; i < 6; i++) {
            ConnectorSideMode mode = ConnectorSideMode.byId(i < sides.length ? sides[i] : 0);
            modeButtons[i].setMessage(text("mode." + mode.name().toLowerCase(Locale.ROOT)));
            boolean automatic = (state.getInt("Automatic") & (1 << i)) != 0;
            autoButtons[i].setMessage(text(automatic ? "auto_on" : "auto_off"));
            autoButtons[i].active = mode.output;
        }
        previous.active = state.getInt("Page") > 0;
        next.active = state.getInt("Page") + 1 < state.getInt("Pages");
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
        for (int i = 0; i < 9; i++) drawCell(graphics, selected, i, SELECTED_Y, i == selectedSlot);
        for (int i = 0; i < IdeaspaceConnectorMenu.PAGE_SIZE; i++) drawCell(graphics, available, i, AVAILABLE_Y, false);
    }

    private void drawCell(GuiGraphics graphics, List<IdeaStorageDisplayEntry> entries, int index, int startY, boolean active) {
        int x = leftPos + GRID_X + index % 9 * CELL;
        int y = topPos + startY + index / 9 * CELL;
        graphics.fill(x, y, x + CELL, y + CELL, active ? 0xFF61D9C3 : 0xFF59616D);
        graphics.fill(x + 1, y + 1, x + CELL - 1, y + CELL - 1, 0xFF919AA9);
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
        graphics.drawString(font, title, 10, 8, 0xFF202A35, false);
        graphics.drawString(font, text("published"), 10, 24, 0xFF202A35, false);
        graphics.drawString(font, text("available"), 10, 73, 0xFF202A35, false);
        graphics.drawString(font, text("faces"), 190, 24, 0xFF202A35, false);
        for (Direction side : Direction.values()) graphics.drawString(font, text("face." + side.getName()),
                184, 44 + side.ordinal() * 25, 0xFF202A35, false);
        graphics.drawCenteredString(font, text("page", menu.clientState().getInt("Page") + 1,
                Math.max(1, menu.clientState().getInt("Pages"))), 91, 152, 0xFFFFFFFF);
        graphics.drawWordWrap(font, text("help"), 10, 191, imageWidth - 20, 0xFF202A35);
        if (menu.clientState().getBoolean("LoadFailed"))
            graphics.drawString(font, text("load_failed"), 10, 173, 0xFFAA2222, false);
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        updateState();
        super.render(graphics, mouseX, mouseY, partialTick);
        int index = cellAt(mouseX, mouseY, SELECTED_Y, 1);
        List<IdeaStorageDisplayEntry> entries = selected;
        boolean configuration = index >= 0;
        if (!configuration) { index = cellAt(mouseX, mouseY, AVAILABLE_Y, 3); entries = available; }
        if (index < 0 || index >= entries.size()) return;
        IdeaStorageDisplayEntry entry = entries.get(index);
        if (entry == null) return;
        String unit = switch (entry.kind()) { case ITEM -> ""; case ENERGY -> " FE"; default -> " mB"; };
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(entry.displayName());
        tooltip.add(text("amount", String.format(Locale.ROOT, "%,d", entry.amount()) + unit));
        tooltip.add(text(configuration ? "clear_hint" : "select_hint"));
        graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
    }

    private int cellAt(double x, double y, int startY, int rows) {
        int dx = (int) Math.floor(x - leftPos - GRID_X), dy = (int) Math.floor(y - topPos - startY);
        return dx >= 0 && dx < 9 * CELL && dy >= 0 && dy < rows * CELL ? dx / CELL + dy / CELL * 9 : -1;
    }

    @Override public boolean mouseClicked(double x, double y, int button) {
        int index = cellAt(x, y, SELECTED_Y, 1);
        if (index >= 0 && (button == 0 || button == 1)) {
            if (button == 1) send(IdeaspaceConnectorMenu.CLEAR, index, 0);
            else selectedSlot = index;
            return true;
        }
        index = cellAt(x, y, AVAILABLE_Y, 3);
        if (index >= 0 && index < available.size() && button == 0) {
            send(IdeaspaceConnectorMenu.ASSIGN, selectedSlot, index);
            return true;
        }
        return super.mouseClicked(x, y, button);
    }
}
