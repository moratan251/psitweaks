package com.moratan251.psitweaks.client.gui;

import com.moratan251.psitweaks.common.menu.IdeaspaceConnectorMenu;
import com.moratan251.psitweaks.common.network.MessageConnectorAction;
import com.moratan251.psitweaks.common.network.MessageConnectorTemplate;
import com.moratan251.psitweaks.common.storage.connector.ConnectorResource;
import com.moratan251.psitweaks.common.storage.connector.ConnectorSideMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class IdeaspaceConnectorScreen extends AbstractContainerScreen<IdeaspaceConnectorMenu> {
    private static final int GRID_X = 7, SELECTED_Y = 40, CELL = 18;
    private static final int NEIGHBOR_X = 37, FACE_ROW_Y = 28, FACE_ROW_HEIGHT = 24;
    private final Button[] modeButtons = new Button[6], autoButtons = new Button[6];
    private final ItemStack[] neighborIcons = new ItemStack[6];
    private final Component[] neighborNames = new Component[6];
    private int neighborRefreshTicks;
    private Button settingsButton, backButton, energyButton, helpButton;
    private boolean settingsOpen;
    private boolean ghostClick;
    private int selectedSlot;
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
        super.init();
        minecraft.player.stopUsingItem();
        for (Direction side : Direction.values()) {
            int i = side.ordinal();
            int y = topPos + FACE_ROW_Y + FACE_ROW_HEIGHT * i;
            modeButtons[i] = addRenderableWidget(Button.builder(Component.empty(),
                    button -> send(IdeaspaceConnectorMenu.SIDE, 0, i)).bounds(leftPos + 59, y, 49, 20).build());
            autoButtons[i] = addRenderableWidget(Button.builder(Component.empty(),
                    button -> send(IdeaspaceConnectorMenu.AUTO, 0, i)).bounds(leftPos + 112, y, 56, 20).build());
        }
        settingsButton = addRenderableWidget(Button.builder(text("settings"), button -> setSettingsOpen(true))
                .bounds(leftPos + 8, topPos + 66, 124, 18).build());
        backButton = addRenderableWidget(Button.builder(text("back"), button -> setSettingsOpen(false))
                .bounds(leftPos + 8, topPos + 180, 160, 18).build());
        energyButton = addRenderableWidget(Button.builder(Component.literal("FE"), button -> send(IdeaspaceConnectorMenu.ENERGY, selectedSlot, 0))
                .bounds(leftPos + 138, topPos + 66, 30, 18).tooltip(Tooltip.create(text("energy_hint"))).build());
        helpButton = addRenderableWidget(Button.builder(Component.literal("?"), button -> {})
                .bounds(leftPos + 154, topPos + 6, 14, 14).tooltip(Tooltip.create(text("help"))).build());
        setSettingsOpen(settingsOpen);
        lastState = null;
        updateState();
    }

    private void setSettingsOpen(boolean open) {
        settingsOpen = open;
        menu.setInventoryVisible(!open);
        settingsButton.visible = energyButton.visible = helpButton.visible = !open;
        backButton.visible = open;
        for (int i = 0; i < 6; i++) modeButtons[i].visible = autoButtons[i].visible = open;
        if (open) updateNeighbors();
        setFocused(null);
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
        selectedSlot = slot;
        PacketDistributor.sendToServer(new MessageConnectorTemplate(menu.containerId, menu.session(), slot, template));
    }

    private void send(int action, int slot, int argument) {
        PacketDistributor.sendToServer(new MessageConnectorAction(menu.containerId, menu.session(), menu.revision(), action, slot, argument));
    }

    @Override protected void containerTick() {
        super.containerTick();
        updateState();
        if (settingsOpen && ++neighborRefreshTicks >= 10) updateNeighbors();
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
        int[] sides = state.getIntArray("Sides");
        for (int i = 0; i < 6; i++) {
            ConnectorSideMode mode = ConnectorSideMode.byId(i < sides.length ? sides[i] : 0);
            modeButtons[i].setMessage(text("mode." + mode.name().toLowerCase(Locale.ROOT)));
            boolean automatic = (state.getInt("Automatic") & (1 << i)) != 0;
            autoButtons[i].setMessage(text(automatic ? "auto_on" : "auto_off"));
            autoButtons[i].active = mode.output;
        }
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
            for (Direction direction : Direction.values()) {
                int i = direction.ordinal();
                int x = leftPos + NEIGHBOR_X, y = topPos + FACE_ROW_Y + 1 + FACE_ROW_HEIGHT * i;
                graphics.fill(x, y, x + CELL, y + CELL, 0xFF59616D);
                graphics.fill(x + 1, y + 1, x + CELL - 1, y + CELL - 1, 0xFF919AA9);
                ItemStack icon = neighborIcons[i];
                if (icon != null && !icon.isEmpty()) graphics.renderItem(icon, x + 1, y + 1);
                else if (neighborNames[i] != null) graphics.drawCenteredString(font, "?", x + 9, y + 5, 0xFFFFFFFF);
            }
        } else {
            for (int i = 0; i < 9; i++) drawCell(graphics, selected, i, SELECTED_Y, i == selectedSlot);
            for (var slot : menu.slots) {
                int x = leftPos + slot.x - 1, y = topPos + slot.y - 1;
                graphics.fill(x, y, x + CELL, y + CELL, 0xFF59616D);
                graphics.fill(x + 1, y + 1, x + CELL - 1, y + CELL - 1, 0xFF919AA9);
            }
        }
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
        graphics.drawString(font, settingsOpen ? text("faces") : title, 8, 8, 0xFF202A35, false);
        if (settingsOpen) {
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

    @Override public boolean mouseClicked(double x, double y, int button) {
        int index = settingsOpen ? -1 : cellAt(x, y, SELECTED_Y, 1);
        if (index >= 0 && (button == 0 || button == 1)) {
            ghostClick = true;
            if (button == 1 && menu.getCarried().isEmpty()) send(IdeaspaceConnectorMenu.CLEAR, index, 0);
            else {
                selectedSlot = index;
                if (!menu.getCarried().isEmpty())
                    send(IdeaspaceConnectorMenu.ASSIGN, index, button == 1 || hasShiftDown() ? 1 : 0);
            }
            return true;
        }
        return super.mouseClicked(x, y, button);
    }

    @Override public boolean mouseReleased(double x, double y, int button) {
        if (ghostClick && (button == 0 || button == 1)) {
            ghostClick = false;
            return true;
        }
        return super.mouseReleased(x, y, button);
    }

    @Override public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (settingsOpen && key == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
            setSettingsOpen(false);
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }
}
