package com.moratan251.psitweaks.common.items;

import com.moratan251.psitweaks.common.menu.PortableSpellProgrammerMenu;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import vazkii.psi.api.capability.PsiCapabilities;
import vazkii.psi.api.spell.ISpellAcceptor;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.common.core.handler.PsiSoundHandler;
import vazkii.psi.common.item.base.ModDataComponents;
import vazkii.psi.common.spell.SpellCompiler;

public final class ItemPortableSpellProgrammer extends Item {
    public ItemPortableSpellProgrammer(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.stopUsingItem();
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer && !player.isSpectator()) {
            if (player.isShiftKeyDown()) {
                if (hand == InteractionHand.MAIN_HAND) {
                    registerOffhandSpell(player, stack);
                }
            } else {
                int slot = hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : 40;
                var provider = new PortableSpellProgrammerMenu.Provider(slot, UUID.randomUUID());
                serverPlayer.openMenu(provider, buf -> {
                    buf.writeVarInt(slot);
                    buf.writeUUID(provider.session());
                    Spell.STREAM_CODEC.encode(buf, getSpellCopy(stack));
                });
            }
        }
        // Consume even an unsuccessful registration so the offhand CAD, bow or food is not used.
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public static Spell getSpellCopy(ItemStack stack) {
        Spell saved = stack.get(ModDataComponents.SPELL.get());
        return saved == null ? new Spell() : saved.copy();
    }

    public static void saveSpell(ItemStack stack, Spell spell) {
        stack.set(ModDataComponents.SPELL.get(), spell.copy());
    }

    /** Uses the same acceptor as Psi's block, including selected sockets and stacked bullets. */
    public static boolean registerOffhandSpell(Player player, ItemStack programmer) {
        if (player.level().isClientSide || player.isSpectator()
                || !(programmer.getItem() instanceof ItemPortableSpellProgrammer)
                || player.getMainHandItem() != programmer || !player.isShiftKeyDown()) {
            return false;
        }
        ItemStack target = player.getOffhandItem();
        var acceptor = PsiCapabilities.spellAcceptor(target);
        if (acceptor == null) {
            player.displayClientMessage(Component.translatable("message.psitweaks.portable_spell_programmer.no_target"), true);
            return false;
        }
        // CADs and socketable equipment expose an acceptor even when their selected socket is empty.
        // Check the actual destination before renewing the spell UUID or reporting success.
        var socketable = PsiCapabilities.socketable(target);
        if (socketable != null && (!socketable.isSocketSlotAvailable(socketable.getSelectedSlot())
                || !ISpellAcceptor.isAcceptor(socketable.getSelectedBullet()))) {
            player.displayClientMessage(Component.translatable("message.psitweaks.portable_spell_programmer.no_selected_bullet"), true);
            return false;
        }
        Spell spell = getSpellCopy(programmer);
        if (spell.grid.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.psitweaks.portable_spell_programmer.empty_spell"), true);
            return false;
        }
        if (new SpellCompiler().compile(spell, player.registryAccess(), player).right().isPresent()) {
            player.displayClientMessage(Component.translatable("message.psitweaks.portable_spell_programmer.compile_failed"), true);
            player.level().playSound(null, player.blockPosition(), PsiSoundHandler.compileError.get(),
                    SoundSource.PLAYERS, 0.5F, 1F);
            return false;
        }
        spell.uuid = UUID.randomUUID();
        acceptor.setSpell(player, spell);
        saveSpell(programmer, spell);
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        player.level().playSound(null, player.blockPosition(), PsiSoundHandler.bulletCreate.get(),
                SoundSource.PLAYERS, 0.5F, 1F);
        return true;
    }
}
