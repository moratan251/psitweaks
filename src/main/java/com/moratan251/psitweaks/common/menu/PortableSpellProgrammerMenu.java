package com.moratan251.psitweaks.common.menu;

import com.moratan251.psitweaks.common.items.ItemPortableSpellProgrammer;
import com.moratan251.psitweaks.common.network.MessagePortableSpellProgrammerEdit;
import java.util.UUID;
import com.moratan251.psitweaks.client.gui.machine.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import vazkii.psi.api.spell.Spell;

/** A slotless editing session; the original held stack remains the source of truth. */
public final class PortableSpellProgrammerMenu extends AbstractContainerMenu {
    private final com.moratan251.psitweaks.common.network.MenuPayloadBuffer payloadBuffer = new com.moratan251.psitweaks.common.network.MenuPayloadBuffer();
    public com.moratan251.psitweaks.common.network.MenuPayloadBuffer payloadBuffer() { return payloadBuffer; }
    private final Player owner;
    private final int inventorySlot;
    private final ItemStack boundStack;
    private final UUID session;
    private Spell initialSpell;

    public static PortableSpellProgrammerMenu fromNetwork(int id, Inventory inventory, FriendlyByteBuf buf) {
        return new PortableSpellProgrammerMenu(id, inventory, buf.readVarInt(), buf.readUUID(),
                new Spell());
    }

    public PortableSpellProgrammerMenu(int id, Inventory inventory, int slot, UUID session, Spell initialSpell) {
        super(ModMenuTypes.PORTABLE_SPELL_PROGRAMMER.get(), id);
        this.owner = inventory.player;
        this.inventorySlot = slot;
        this.boundStack = slot >= 0 && slot < inventory.getContainerSize() ? inventory.getItem(slot) : ItemStack.EMPTY;
        this.session = session;
        this.initialSpell = initialSpell.copy();
    }

    private boolean initialReceived;
    public boolean initialReceived() { return initialReceived; }
    public void receiveInitial(MessagePortableSpellProgrammerEdit message) {
        if (owner.level().isClientSide && !initialReceived && message.containerId() == containerId && session.equals(message.session())) {
            initialSpell = message.spell().copy(); initialReceived = true;
        }
    }
    public UUID session() { return session; }

    public Spell initialSpell() { return initialSpell.copy(); }

    @Override
    public boolean stillValid(Player player) {
        if (player != owner || !player.isAlive() || player.isSpectator()
                || !(inventorySlot == 40 || inventorySlot >= 0 && inventorySlot < 9)
                || inventorySlot != 40 && player.getInventory().selected != inventorySlot) {
            return false;
        }
        ItemStack current = player.getInventory().getItem(inventorySlot);
        return current.getItem() instanceof ItemPortableSpellProgrammer
                && (player.level().isClientSide || current == boundStack);
    }

    public boolean handleEdit(Player player, MessagePortableSpellProgrammerEdit message) {
        if (player.level().isClientSide || player.containerMenu != this || !stillValid(player)
                || message.containerId() != containerId || !session.equals(message.session())) {
            return false;
        }
        // Incomplete spells must remain editable. Psi validates compilation when registering a bullet.
        ItemPortableSpellProgrammer.saveSpell(boundStack, message.spell());
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        // This menu has no inventory slots or carried items to manipulate.
    }

    public record Provider(int slot, UUID session) implements MenuProvider {
        @Override
        public Component getDisplayName() {
            return Component.translatable("item.psitweaks.portable_spell_programmer");
        }

        @Override
        public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
            return new PortableSpellProgrammerMenu(id, inventory, slot, session,
                    ItemPortableSpellProgrammer.getSpellCopy(inventory.getItem(slot)));
        }
    }
}
