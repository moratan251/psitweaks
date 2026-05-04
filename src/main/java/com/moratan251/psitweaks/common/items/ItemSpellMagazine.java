package com.moratan251.psitweaks.common.items;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ComponentItemHandler;
import vazkii.psi.api.cad.ISocketable;
import vazkii.psi.api.spell.ISpellAcceptor;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.common.core.handler.PsiSoundHandler;
import vazkii.psi.common.core.handler.PlayerDataHandler;
import vazkii.psi.common.item.base.ModDataComponents;

public class ItemSpellMagazine extends Item {
    public static final int SLOT_COUNT = 12;

    public ItemSpellMagazine(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack magazineStack = player.getItemInHand(hand);
        ItemStack cadStack = PlayerDataHandler.get(player).getCAD();
        if (cadStack.isEmpty() || !ISocketable.isSocketable(cadStack)) {
            return new InteractionResultHolder<>(InteractionResult.PASS, magazineStack);
        }

        if (!level.isClientSide && swapBullets(magazineStack, cadStack)) {
            player.getInventory().setChanged();
            level.playSound(null, player.getX(), player.getY(), player.getZ(), PsiSoundHandler.bulletCreate,
                    SoundSource.PLAYERS, 0.5F, 1.0F);
        }

        return InteractionResultHolder.sidedSuccess(magazineStack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Component selectedBulletName = ISocketable.getSocketedItemName(stack, "psimisc.none");
        tooltip.add(Component.translatable("psimisc.spell_selected", selectedBulletName));
        tooltip.add(Component.translatable("tooltip.psitweaks.spell_magazine.bullets", countBullets(stack), SLOT_COUNT));
    }

    public static ComponentItemHandler createBulletHandler(ItemStack stack) {
        return new ComponentItemHandler(stack, ModDataComponents.BULLETS.get(), SLOT_COUNT) {
            @Override
            public boolean isItemValid(int slot, ItemStack bullet) {
                return slot >= 0 && slot < SLOT_COUNT && ISpellAcceptor.isContainer(bullet);
            }
        };
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

    public static class SpellMagazineSocketable implements ISocketable, ISpellAcceptor {
        private final ItemStack stack;
        private final ComponentItemHandler bulletHandler;

        public SpellMagazineSocketable(ItemStack stack) {
            this.stack = stack;
            this.bulletHandler = createBulletHandler(stack);
        }

        @Override
        public boolean isSocketSlotAvailable(int slot) {
            return slot >= 0 && slot < SLOT_COUNT;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack bullet) {
            return isSocketSlotAvailable(slot) && ISpellAcceptor.isContainer(bullet);
        }

        @Override
        public List<Integer> getRadialMenuSlots() {
            List<Integer> slots = new ArrayList<>();
            for (int slot = 0; slot < SLOT_COUNT; slot++) {
                slots.add(slot);
            }
            return slots;
        }

        @Override
        public ItemStack getBulletInSocket(int slot) {
            return isSocketSlotAvailable(slot) ? bulletHandler.getStackInSlot(slot) : ItemStack.EMPTY;
        }

        @Override
        public void setBulletInSocket(int slot, ItemStack bullet) {
            if (isSocketSlotAvailable(slot)) {
                bulletHandler.setStackInSlot(slot, bullet);
            }
        }

        @Override
        public int getSelectedSlot() {
            return Mth.clamp(stack.getOrDefault(ModDataComponents.SELECTED_SLOT, 0), 0, SLOT_COUNT - 1);
        }

        @Override
        public void setSelectedSlot(int slot) {
            if (isSocketSlotAvailable(slot)) {
                stack.set(ModDataComponents.SELECTED_SLOT, slot);
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
            return !bullet.isEmpty() && ISpellAcceptor.isAcceptor(bullet)
                    && ISpellAcceptor.acceptor(bullet).requiresSneakForSpellSet();
        }
    }
}
