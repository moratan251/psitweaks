package com.moratan251.psitweaks.common.items;

/*
 * This class is based on code from Psi (https://github.com/VazkiiMods/Psi)
 * Licensed under the MIT License.
 *
 * Original author: Vazkii
 */

import com.moratan251.psitweaks.Psitweaks;
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
import vazkii.psi.api.internal.TooltipHelper;
import vazkii.psi.api.spell.ISpellAcceptor;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.common.core.handler.PlayerDataHandler;
import vazkii.psi.common.item.ItemCAD;
import vazkii.psi.common.item.ItemSpellDrive;

import java.util.ArrayList;
import java.util.List;

public class ItemFlashRing extends Item {

    public ItemFlashRing(Item.Properties properties) {
        super(properties.stacksTo(1));
    }

    // 1.20.1ではRarityをNBTで管理
    @Override
    public @NotNull Rarity getRarity(@NotNull ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("spell")) {
            return Rarity.RARE;
        }
        return Rarity.COMMON;
    }

    @NotNull
    @Override
    public Component getName(@NotNull ItemStack stack) {
        if (!ISpellAcceptor.hasSpell(stack)) {
            return super.getName(stack);
        }
        Spell spell = ItemSpellDrive.getSpell(stack);
        if (spell != null && !spell.name.isEmpty()) {
            return Component.literal(spell.name);
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag advanced) {
        TooltipHelper.tooltipIfShift(tooltip,
                () -> tooltip.add(Component.translatable("psimisc.bullet_cost",
                        (int) (ISpellAcceptor.acceptor(stack).getCostModifier() * 100))));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand usedHand) {
        ItemStack held = player.getItemInHand(usedHand);
        boolean isSneaking = player.isShiftKeyDown();
        if (isSneaking && level.isClientSide) {
            Psitweaks.proxyPsitweaks.openFlashRingGUI(held);
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, held);
        }

        if (!isSneaking && ISpellAcceptor.hasSpell(held)) {
            PlayerDataHandler.PlayerData data = PlayerDataHandler.get(player);
            ItemStack cad = data.getCAD();

            if (cad.isEmpty()) {
                return new InteractionResultHolder<>(InteractionResult.PASS, held);
            }

            boolean casted = ItemCAD.cast(level, player, data, held, cad,
                    100, 25, 0.5f, ctx -> ctx.tool = held).isPresent();

            return new InteractionResultHolder<>(casted ? InteractionResult.SUCCESS : InteractionResult.PASS, held);
        }

        return new InteractionResultHolder<>(InteractionResult.PASS, held);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new SpellAcceptor(stack);
    }

    public static class SpellAcceptor implements ICapabilityProvider, ISpellAcceptor {
        protected final ItemStack stack;
        private final LazyOptional<ISpellAcceptor> holder = LazyOptional.of(() -> this);

        public SpellAcceptor(ItemStack stack) {
            this.stack = stack;
        }

        @NotNull
        @Override
        public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable net.minecraft.core.Direction side) {
            if (cap == PsiAPI.SPELL_ACCEPTOR_CAPABILITY) {
                return holder.cast();
            }
            return LazyOptional.empty();
        }

        @Override
        public void setSpell(Player player, Spell spell) {
            ItemSpellDrive.setSpell(stack, spell);
        }

        @Override
        public Spell getSpell() {
            return ItemSpellDrive.getSpell(stack);
        }

        @Override
        public boolean containsSpell() {
            CompoundTag tag = stack.getTag();
            return tag != null && tag.contains("spell");
        }

        @Override
        public ArrayList<Entity> castSpell(SpellContext context) {
            context.cspell.safeExecute(context);
            return new ArrayList<>();
        }

        @Override
        public boolean loopcastSpell(SpellContext context) {
            return false;
        }

        @Override
        public double getCostModifier() {
            return 1.0;
        }

        @Override
        public boolean requiresSneakForSpellSet() {
            return true;
        }
    }
}
