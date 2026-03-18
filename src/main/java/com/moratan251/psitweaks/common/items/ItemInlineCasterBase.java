package com.moratan251.psitweaks.common.items;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
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
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.common.core.handler.PlayerDataHandler;
import vazkii.psi.common.item.ItemCAD;

import java.util.ArrayList;
import java.util.List;

public class ItemInlineCasterBase extends Item {
    private static final String TAG_SELECTED_SLOT = "selectedSlot";
    private static final String TAG_BULLET_PREFIX = "bullet";

    private final int slotCount;

    public ItemInlineCasterBase(Properties properties, int slotCount) {
        super(properties.stacksTo(1).rarity(Rarity.RARE));
        this.slotCount = slotCount;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack casterStack = player.getItemInHand(hand);
        if (!ISpellAcceptor.hasSpell(casterStack)) {
            return new InteractionResultHolder<>(InteractionResult.PASS, casterStack);
        }

        PlayerDataHandler.PlayerData data = PlayerDataHandler.get(player);
        ItemStack cadStack = PsiAPI.getPlayerCAD(player);
        if (cadStack.isEmpty()) {
            return new InteractionResultHolder<>(InteractionResult.PASS, casterStack);
        }

        boolean casted = ItemCAD.cast(level, player, data, casterStack, cadStack, 5, 10, 0.05F,
                context -> context.tool = casterStack).isPresent();
        return new InteractionResultHolder<>(casted ? InteractionResult.SUCCESS : InteractionResult.PASS, casterStack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        Component selectedBulletName = ISocketable.getSocketedItemName(stack, "psimisc.none");
        tooltip.add(Component.translatable("psimisc.spell_selected", selectedBulletName));
        tooltip.add(Component.translatable("tooltip.psitweaks.spell_magazine.bullets", countBullets(stack), slotCount));
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new InlineCasterSocketable(stack, slotCount);
    }

    private int countBullets(ItemStack stack) {
        if (!ISocketable.isSocketable(stack)) {
            return 0;
        }

        ISocketable socketable = ISocketable.socketable(stack);
        int count = 0;
        for (int slot = 0; slot < slotCount; slot++) {
            if (!socketable.getBulletInSocket(slot).isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private static String getBulletTag(int slot) {
        return TAG_BULLET_PREFIX + slot;
    }

    public static class InlineCasterSocketable implements ICapabilityProvider, ISocketable, ISpellAcceptor {
        private final ItemStack stack;
        private final int slotCount;
        private final LazyOptional<InlineCasterSocketable> holder = LazyOptional.of(() -> this);

        public InlineCasterSocketable(ItemStack stack, int slotCount) {
            this.stack = stack;
            this.slotCount = slotCount;
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
            return slot >= 0 && slot < slotCount;
        }

        @Override
        public List<Integer> getRadialMenuSlots() {
            List<Integer> slots = new ArrayList<>();
            for (int slot = 0; slot <= slotCount; slot++) {
                slots.add(slot);
            }
            return slots;
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
            return stack.getOrCreateTag().getInt(TAG_SELECTED_SLOT);
        }

        @Override
        public void setSelectedSlot(int slot) {
            if (slot >= 0 && slot <= slotCount) {
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
        public ArrayList<Entity> castSpell(SpellContext context) {
            ItemStack bullet = getSelectedBullet();
            return ISpellAcceptor.isAcceptor(bullet) ? ISpellAcceptor.acceptor(bullet).castSpell(context) : new ArrayList<>();
        }

        @Override
        public boolean loopcastSpell(SpellContext context) {
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
