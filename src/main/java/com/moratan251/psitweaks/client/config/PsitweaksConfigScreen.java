package com.moratan251.psitweaks.client.config;

import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class PsitweaksConfigScreen extends Screen {
    private final Screen parent;
    private boolean disableDamagePsiDeduction;
    private boolean disableRegenCooldown;

    public PsitweaksConfigScreen(Screen parent) {
        super(Component.translatable("screen.psitweaks.configuration.title"));
        this.parent = parent;
        disableDamagePsiDeduction = PsitweaksConfig.COMMON.disableDamagePsiDeduction.get();
        disableRegenCooldown = PsitweaksConfig.COMMON.disableRegenCooldown.get();
    }

    @Override
    protected void init() {
        int left = width / 2 - 155;
        int top = height / 2 - 45;

        addRenderableWidget(CycleButton.onOffBuilder(disableDamagePsiDeduction)
                .create(left, top, 310, 20,
                        Component.translatable("screen.psitweaks.configuration.disable_damage_psi_deduction"),
                        (button, value) -> disableDamagePsiDeduction = value));

        addRenderableWidget(CycleButton.onOffBuilder(disableRegenCooldown)
                .create(left, top + 26, 310, 20,
                        Component.translatable("screen.psitweaks.configuration.disable_regen_cooldown"),
                        (button, value) -> disableRegenCooldown = value));

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> saveAndClose())
                .bounds(width / 2 - 100, top + 64, 200, 20)
                .build());
    }

    private void saveAndClose() {
        PsitweaksConfig.COMMON.disableDamagePsiDeduction.set(disableDamagePsiDeduction);
        PsitweaksConfig.COMMON.disableRegenCooldown.set(disableRegenCooldown);
        PsitweaksConfig.COMMON_SPEC.save();
        onClose();
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(font, title, width / 2, height / 2 - 76, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}
