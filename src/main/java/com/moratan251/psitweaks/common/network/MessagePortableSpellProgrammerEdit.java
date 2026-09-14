package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.menu.PortableSpellProgrammerMenu;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import vazkii.psi.api.spell.Spell;

public record MessagePortableSpellProgrammerEdit(int containerId, UUID session, Spell spell)
        implements CustomPacketPayload {
    public static final Type<MessagePortableSpellProgrammerEdit> TYPE =
            new Type<>(Psitweaks.location("portable_spell_programmer_edit"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessagePortableSpellProgrammerEdit> STREAM_CODEC =
            CustomPacketPayload.codec(MessagePortableSpellProgrammerEdit::write, MessagePortableSpellProgrammerEdit::read);

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        buf.writeUUID(session);
        Spell.STREAM_CODEC.encode(buf, spell);
    }

    private static MessagePortableSpellProgrammerEdit read(RegistryFriendlyByteBuf buf) {
        return new MessagePortableSpellProgrammerEdit(buf.readVarInt(), buf.readUUID(), Spell.STREAM_CODEC.decode(buf));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(MessagePortableSpellProgrammerEdit message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof PortableSpellProgrammerMenu menu) {
                menu.handleEdit(context.player(), message);
            }
        });
    }
}
