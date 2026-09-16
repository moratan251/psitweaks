package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.menu.PortableSpellProgrammerMenu;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import vazkii.psi.api.spell.Spell;

public record MessagePortableSpellProgrammerEdit(int containerId, UUID session, Spell spell)
        {

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        buf.writeUUID(session);
        buf.writeNbt(com.moratan251.psitweaks.common.items.ItemPortableSpellProgrammer.spellTag(spell));
    }

    public static MessagePortableSpellProgrammerEdit read(FriendlyByteBuf buf) {
        return new MessagePortableSpellProgrammerEdit(buf.readVarInt(), buf.readUUID(), Spell.createFromNBT(buf.readNbt(new net.minecraft.nbt.NbtAccounter(32L * 1024 * 1024))));
    }


    public static void handle(MessagePortableSpellProgrammerEdit message, java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> supplied) {
        var context = supplied.get();
        context.setPacketHandled(true);
        context.enqueueWork(() -> {
            if (context.getSender() != null && context.getSender().containerMenu instanceof PortableSpellProgrammerMenu menu) {
                menu.handleEdit(context.getSender(), message);
            }
        });
    }
}
