package com.moratan251.psitweaks.common.items;

import java.util.ArrayList;
import java.util.List;
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
import net.neoforged.neoforge.items.ComponentItemHandler;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.cad.ISocketable;
import vazkii.psi.api.spell.ISpellAcceptor;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.common.core.handler.PlayerDataHandler;
import vazkii.psi.common.item.ItemCAD;
import vazkii.psi.common.item.base.ModDataComponents;

public class ItemInlineCasterBase extends Item {
    private final int slotCount;

    public ItemInlineCasterBase(Properties properties, int slotCount) {
        super(properties.stacksTo(1).rarity(Rarity.RARE));
        this.slotCount = slotCount;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack casterStack = player.getItemInHand(hand);
        if (!ISocketable.isSocketable(casterStack)) {
            return new InteractionResultHolder<>(InteractionResult.PASS, casterStack);
        }

        ItemStack bulletStack = ISocketable.socketable(casterStack).getSelectedBullet();
        if (!ISpellAcceptor.hasSpell(bulletStack)) {
            return new InteractionResultHolder<>(InteractionResult.PASS, casterStack);
        }

        PlayerDataHandler.PlayerData data = PlayerDataHandler.get(player);
        ItemStack cadStack = PsiAPI.getPlayerCAD(player);
        if (cadStack.isEmpty()) {
            return new InteractionResultHolder<>(InteractionResult.PASS, casterStack);
        }

        boolean casted = ItemCAD.cast(level, player, data, bulletStack, cadStack, 40, 25, 0.5F, context -> {
            context.tool = casterStack;
            context.castFrom = hand;
        }).isPresent();
        return new InteractionResultHolder<>(casted ? InteractionResult.SUCCESS : InteractionResult.PASS, casterStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Component selectedBulletName = ISocketable.getSocketedItemName(stack, "psimisc.none");
        tooltip.add(Component.translatable("psimisc.spell_selected", selectedBulletName));
        tooltip.add(Component.translatable("tooltip.psitweaks.spell_magazine.bullets", countBullets(stack), slotCount));
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

    static ComponentItemHandler createBulletHandler(ItemStack stack, int slotCount) {
        return new ComponentItemHandler(stack, ModDataComponents.BULLETS.get(), slotCount) {
            @Override
            public boolean isItemValid(int slot, ItemStack bullet) {
                return slot >= 0 && slot < slotCount && (bullet.isEmpty() || ISpellAcceptor.isContainer(bullet));
            }
        };
    }

    public static class InlineCasterSocketable implements ISocketable, ISpellAcceptor {
        private final ItemStack stack;
        private final int slotCount;
        private final ComponentItemHandler bulletHandler;

        public InlineCasterSocketable(ItemStack stack, int slotCount) {
            this.stack = stack;
            this.slotCount = slotCount;
            this.bulletHandler = createBulletHandler(stack, slotCount);
        }

        @Override
        public boolean isSocketSlotAvailable(int slot) {
            return slot >= 0 && slot < slotCount;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack bullet) {
            return isSocketSlotAvailable(slot) && ISpellAcceptor.isContainer(bullet);
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
            return stack.getOrDefault(ModDataComponents.SELECTED_SLOT, 0);
        }

        @Override
        public void setSelectedSlot(int slot) {
            if (slot >= 0 && slot <= slotCount) {
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
        public boolean canLoopcast() {
            return true;
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
