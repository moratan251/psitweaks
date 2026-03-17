package com.moratan251.psitweaks.common.items;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.cad.ISocketable;
import vazkii.psi.api.spell.ISpellAcceptor;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.common.core.handler.PsiSoundHandler;
import vazkii.psi.common.core.handler.PlayerDataHandler;

import java.util.List;

public class ItemSpellMagazine extends Item {
    public static final int SLOT_COUNT = 12;

    private static final String TAG_SELECTED_SLOT = "selectedSlot";
    private static final String TAG_BULLET_PREFIX = "bullet";

    public ItemSpellMagazine(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack magazineStack = player.getItemInHand(hand);
        ItemStack cadStack = PlayerDataHandler.get(player).getCAD();
        if (cadStack.isEmpty() || !ISocketable.isSocketable(cadStack)) {
            return InteractionResultHolder.pass(magazineStack);
        }

        if (!level.isClientSide) {
            if (swapBullets(magazineStack, cadStack)) {
                player.getInventory().setChanged();
                level.playSound(null, player.getX(), player.getY(), player.getZ(), PsiSoundHandler.bulletCreate, SoundSource.PLAYERS, 0.5F, 1.0F);
            }
        }

        return InteractionResultHolder.sidedSuccess(magazineStack, level.isClientSide);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        Component selectedBulletName = ISocketable.getSocketedItemName(stack, "psimisc.none");
        tooltip.add(Component.translatable("psimisc.spell_selected", selectedBulletName));
        tooltip.add(Component.translatable("tooltip.psitweaks.spell_magazine.bullets", countBullets(stack), SLOT_COUNT));
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new SpellMagazineSocketable(stack);
    }

    private static boolean swapBullets(ItemStack magazineStack, ItemStack cadStack) {
        ISocketable magazine = ISocketable.socketable(magazineStack);
        ISocketable cad = ISocketable.socketable(cadStack);
        boolean changed = false;

        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            if (!cad.isSocketSlotAvailable(slot)) {
                continue;
            }

            ItemStack magazineBullet = magazine.getBulletInSocket(slot).copy();
            ItemStack cadBullet = cad.getBulletInSocket(slot).copy();
            if (ItemStack.matches(magazineBullet, cadBullet)) {
                continue;
            }

            cad.setBulletInSocket(slot, magazineBullet);
            magazine.setBulletInSocket(slot, cadBullet);
            changed = true;
        }

        return changed;
    }

    private static int countBullets(ItemStack stack) {
        if (!ISocketable.isSocketable(stack)) {
            return 0;
        }

        ISocketable socketable = ISocketable.socketable(stack);
        int count = 0;
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            if (!socketable.getBulletInSocket(slot).isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private static String getBulletTag(int slot) {
        return TAG_BULLET_PREFIX + slot;
    }

    public static class SpellMagazineSocketable implements ICapabilityProvider, ISocketable, ISpellAcceptor {
        private final ItemStack stack;
        private final LazyOptional<SpellMagazineSocketable> holder = LazyOptional.of(() -> this);

        public SpellMagazineSocketable(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable net.minecraft.core.Direction side) {
            if (cap == PsiAPI.SOCKETABLE_CAPABILITY || cap == PsiAPI.SPELL_ACCEPTOR_CAPABILITY) {
                return holder.cast();
            }
            return LazyOptional.empty();
        }

        @Override
        public boolean isSocketSlotAvailable(int slot) {
            return slot >= 0 && slot < SLOT_COUNT;
        }

        @Override
        public @NotNull ItemStack getBulletInSocket(int slot) {
            if (!isSocketSlotAvailable(slot)) {
                return ItemStack.EMPTY;
            }

            CompoundTag bulletData = stack.getOrCreateTag().getCompound(getBulletTag(slot));
            return bulletData.isEmpty() ? ItemStack.EMPTY : ItemStack.of(bulletData);
        }

        @Override
        public void setBulletInSocket(int slot, @NotNull ItemStack bullet) {
            if (!isSocketSlotAvailable(slot)) {
                return;
            }

            CompoundTag bulletData = new CompoundTag();
            if (!bullet.isEmpty()) {
                bullet.save(bulletData);
            }

            stack.getOrCreateTag().put(getBulletTag(slot), bulletData);
        }

        @Override
        public int getSelectedSlot() {
            int selectedSlot = stack.getOrCreateTag().getInt(TAG_SELECTED_SLOT);
            return Math.max(0, Math.min(selectedSlot, SLOT_COUNT - 1));
        }

        @Override
        public void setSelectedSlot(int slot) {
            if (isSocketSlotAvailable(slot)) {
                stack.getOrCreateTag().putInt(TAG_SELECTED_SLOT, slot);
            }
        }

        @Override
        public void setSpell(Player player, Spell spell) {
            int slot = getSelectedSlot();
            ItemStack bullet = getBulletInSocket(slot);
            if (bullet.isEmpty() || !ISpellAcceptor.isAcceptor(bullet)) {
                return;
            }

            ISpellAcceptor.acceptor(bullet).setSpell(player, spell);
            setBulletInSocket(slot, bullet);
        }

        @Override
        public boolean castableFromSocket() {
            return false;
        }

        @Override
        public Spell getSpell() {
            ItemStack bullet = getSelectedBullet();
            return ISpellAcceptor.isAcceptor(bullet) ? ISpellAcceptor.acceptor(bullet).getSpell() : null;
        }

        @Override
        public boolean containsSpell() {
            ItemStack bullet = getSelectedBullet();
            return ISpellAcceptor.isAcceptor(bullet) && ISpellAcceptor.acceptor(bullet).containsSpell();
        }

        @Override
        public java.util.ArrayList<Entity> castSpell(vazkii.psi.api.spell.SpellContext context) {
            ItemStack bullet = getSelectedBullet();
            return ISpellAcceptor.isAcceptor(bullet) ? ISpellAcceptor.acceptor(bullet).castSpell(context) : new java.util.ArrayList<>();
        }

        @Override
        public boolean loopcastSpell(vazkii.psi.api.spell.SpellContext context) {
            ItemStack bullet = getSelectedBullet();
            return ISpellAcceptor.isAcceptor(bullet) && ISpellAcceptor.acceptor(bullet).loopcastSpell(context);
        }

        @Override
        public double getCostModifier() {
            ItemStack bullet = getSelectedBullet();
            return ISpellAcceptor.isAcceptor(bullet) ? ISpellAcceptor.acceptor(bullet).getCostModifier() : 1.0;
        }

        @Override
        public boolean requiresSneakForSpellSet() {
            ItemStack bullet = getSelectedBullet();
            return !bullet.isEmpty() && ISpellAcceptor.isAcceptor(bullet) && ISpellAcceptor.acceptor(bullet).requiresSneakForSpellSet();
        }
    }
}
