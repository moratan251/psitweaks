package com.moratan251.psitweaks.client.gui.machine;

import com.moratan251.psitweaks.common.menu.PortableSpellProgrammerMenu;
import com.moratan251.psitweaks.common.network.MessagePortableSpellProgrammerEdit;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import com.moratan251.psitweaks.common.network.IdeaStorageNetwork;
import vazkii.psi.client.gui.GuiProgrammer;

/** Reuses Psi's editor and its QoL extensions without creating a block in the world. */
public final class GuiPortableSpellProgrammer extends GuiProgrammer implements MenuAccess<PortableSpellProgrammerMenu> {
    private final PortableSpellProgrammerMenu menu;
    private final Component title;
    private boolean initialApplied;

    public GuiPortableSpellProgrammer(PortableSpellProgrammerMenu menu, Inventory inventory, Component title) {
        super(null, menu.initialSpell());
        this.menu = menu;
        this.title = title;
    }

    @Override
    public PortableSpellProgrammerMenu getMenu() { return menu; }

    @Override
    public Component getTitle() { return title; }

    @Override
    public void onSpellChanged(boolean nameOnly) {
        super.onSpellChanged(nameOnly);
        if (initialApplied && getMinecraft().player != null && getMinecraft().player.containerMenu == menu) {
            IdeaStorageNetwork.sendToServer(new MessagePortableSpellProgrammerEdit(menu.containerId, menu.session(), spell.copy()));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!initialApplied && menu.initialReceived()) {
            spell = menu.initialSpell();
            if (spellNameField != null) spellNameField.setValue(spell.name);
            super.onSpellChanged(false);
            undoSteps.clear(); redoSteps.clear();
            initialApplied = true;
        }
        var player = getMinecraft().player;
        if (player == null || player.containerMenu != menu || !menu.stillValid(player)) {
            onClose();
        }
    }

    @Override public boolean mouseClicked(double x, double y, int button) {
        return !initialApplied || super.mouseClicked(x, y, button);
    }
    @Override public boolean charTyped(char c, int modifiers) { return !initialApplied || super.charTyped(c, modifiers); }
    @Override public boolean keyPressed(int key, int scan, int modifiers) {
        return !initialApplied && key != 256 || super.keyPressed(key, scan, modifiers);
    }
    @Override
    public void onClose() {
        var player = getMinecraft().player;
        if (player != null && player.containerMenu == menu) {
            player.closeContainer();
        } else {
            super.onClose();
        }
    }
}
