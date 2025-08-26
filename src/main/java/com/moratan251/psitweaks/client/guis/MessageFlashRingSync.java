package com.moratan251.psitweaks.client.guis;

import com.moratan251.psitweaks.common.items.ItemFlashRing;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.spell.ISpellAcceptor;
import vazkii.psi.api.spell.Spell;


import java.util.function.Supplier;

public class MessageFlashRingSync {

    private final Spell spell;

    public MessageFlashRingSync(Spell spell) {
        this.spell = spell;
    }

    // 送信側
    public static void encode(MessageFlashRingSync msg, FriendlyByteBuf buf) {
        CompoundTag tag = new CompoundTag();
        msg.spell.writeToNBT(tag);
        buf.writeNbt(tag);
    }

    // 受信側
    public static MessageFlashRingSync decode(FriendlyByteBuf buf) {
        CompoundTag tag = buf.readNbt();
        Spell spell = Spell.createFromNBT(tag);
        return new MessageFlashRingSync(spell);
    }

    // サーバー側ハンドラー
    public static void handle(MessageFlashRingSync msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            Player player = context.getSender(); // サーバー側
            if (player == null) return;

            ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemFlashRing) {
                stack.getCapability(PsiAPI.SPELL_ACCEPTOR_CAPABILITY)
                        .ifPresent(cap -> cap.setSpell(player, msg.spell));
                ISpellAcceptor.acceptor(stack).setSpell(player, msg.spell);
            } else {
                stack = player.getItemInHand(InteractionHand.OFF_HAND);
                if (!stack.isEmpty() && stack.getItem() instanceof ItemFlashRing) {
                    stack.getCapability(PsiAPI.SPELL_ACCEPTOR_CAPABILITY)
                            .ifPresent(cap -> cap.setSpell(player, msg.spell));
                    ISpellAcceptor.acceptor(stack).setSpell(player, msg.spell);
                }
            }
        });
        context.setPacketHandled(true);
    }

    public Spell getSpell() {
        return spell;
    }
}