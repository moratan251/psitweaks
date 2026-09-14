package com.moratan251.psitweaks.client.gui.machine;

import com.moratan251.psitweaks.common.menu.PortableSpellProgrammerMenu;
import com.moratan251.psitweaks.common.network.MessagePortableSpellProgrammerEdit;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import vazkii.psi.client.gui.GuiProgrammer;

/** Reuses Psi's editor and its QoL extensions without creating a block in the world. */
public final class GuiPortableSpellProgrammer extends GuiProgrammer implements MenuAccess<PortableSpellProgrammerMenu> {
    private final PortableSpellProgrammerMenu menu;
    private final Component title;

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
        if (getMinecraft().player != null && getMinecraft().player.containerMenu == menu) {
            PacketDistributor.sendToServer(new MessagePortableSpellProgrammerEdit(menu.containerId, menu.session(), spell.copy()));
        }
    }

    @Override
    public void tick() {
        super.tick();
        var player = getMinecraft().player;
        if (player == null || player.containerMenu != menu || !menu.stillValid(player)) {
            onClose();
        }
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
