package com.moratan251.psitweaks.client.gui.machine;

import com.moratan251.psitweaks.common.handler.NetworkHandler;
import com.moratan251.psitweaks.common.items.curios.ItemAutoCasterCustomTick;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class GuiAutoCasterCustomTick extends Screen {
    private static final int BOX_WIDTH = 160;
    private static final int BOX_HEIGHT = 20;

    private final ItemStack stack;
    private final InteractionHand hand;

    private EditBox intervalBox;
    private Button applyButton;
    private int left;
    private int top;

    public GuiAutoCasterCustomTick(ItemStack stack, InteractionHand hand) {
        super(Component.translatable("screen.psitweaks.auto_caster_custom_tick"));
        this.stack = stack;
        this.hand = hand;
    }

    @Override
    protected void init() {
        super.init();
        this.left = this.width / 2 - 80;
        this.top = this.height / 2 - 34;

        this.intervalBox = new EditBox(this.font, this.left, this.top, BOX_WIDTH, BOX_HEIGHT,
                Component.translatable("screen.psitweaks.auto_caster_custom_tick.interval"));
        this.intervalBox.setFilter(text -> text.isEmpty() || text.matches("\\d+"));
        this.intervalBox.setMaxLength(6);
        this.intervalBox.setValue(String.valueOf(ItemAutoCasterCustomTick.getCastInterval(this.stack)));
        this.intervalBox.setResponder(text -> updateApplyButtonState());
        this.addRenderableWidget(this.intervalBox);

        this.applyButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.psitweaks.apply"), button -> applyValue())
                .bounds(this.left, this.top + 28, 78, 20)
                .build());

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose())
                .bounds(this.left + 82, this.top + 28, 78, 20)
                .build());

        this.setInitialFocus(this.intervalBox);
        this.updateApplyButtonState();
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.top - 24, 0xFFFFFF);
        graphics.drawString(this.font,
                Component.translatable("screen.psitweaks.auto_caster_custom_tick.interval"),
                this.left,
                this.top - 12,
                0xA0A0A0,
                false);
        graphics.drawString(this.font,
                Component.translatable("screen.psitweaks.auto_caster_custom_tick.range",
                        ItemAutoCasterCustomTick.MIN_CAST_INTERVAL,
                        ItemAutoCasterCustomTick.MAX_CAST_INTERVAL),
                this.left,
                this.top + 56,
                0xA0A0A0,
                false);

        if (!this.applyButton.active && !this.intervalBox.getValue().isBlank()) {
            graphics.drawString(this.font,
                    Component.translatable("screen.psitweaks.auto_caster_custom_tick.invalid",
                            ItemAutoCasterCustomTick.MIN_CAST_INTERVAL,
                            ItemAutoCasterCustomTick.MAX_CAST_INTERVAL),
                    this.left,
                    this.top + 68,
                    0xFF5555,
                    false);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            if (this.applyButton.active) {
                applyValue();
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        if (this.intervalBox != null) {
            this.intervalBox.tick();
        }
    }

    private void updateApplyButtonState() {
        this.applyButton.active = parseValue() != null;
    }

    private Integer parseValue() {
        if (this.intervalBox == null) {
            return null;
        }

        String value = this.intervalBox.getValue().trim();
        if (value.isEmpty()) {
            return null;
        }

        try {
            int parsed = Integer.parseInt(value);
            if (parsed < ItemAutoCasterCustomTick.MIN_CAST_INTERVAL || parsed > ItemAutoCasterCustomTick.MAX_CAST_INTERVAL) {
                return null;
            }
            return parsed;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private void applyValue() {
        Integer value = parseValue();
        if (value == null) {
            return;
        }

        ItemAutoCasterCustomTick.setCastInterval(this.stack, value);
        NetworkHandler.CHANNEL.sendToServer(new MessageAutoCasterCustomTickSync(this.hand, value));
        this.onClose();
    }
}
