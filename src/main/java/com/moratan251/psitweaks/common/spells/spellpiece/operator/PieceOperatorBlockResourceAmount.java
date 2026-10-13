package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.common.compat.BlockResourceMekanism;
import com.moratan251.psitweaks.common.spells.util.WildcardStringMatcher;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import vazkii.psi.api.spell.Spell;

/**
 * Total items (count), fluids or chemicals (mB) or FE in a block. The String filter works like the one of the
 * ideaspace logistics tricks: a wildcard ID, matched against the path unless it contains ':'; unconnected or empty
 * counts everything.
 */
public class PieceOperatorBlockResourceAmount extends PieceOperatorBlockResourceBase {
    public PieceOperatorBlockResourceAmount(Spell spell) {
        super(spell);
    }

    @Override
    protected boolean resourceOptional() {
        return true;
    }

    @Override
    protected long readItems(IItemHandler handler, @Nullable String resource, boolean pooled) {
        Predicate<ResourceLocation> filter = idFilter(resource);
        long total = 0;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (!stack.isEmpty() && filter.test(BuiltInRegistries.ITEM.getKey(stack.getItem()))) {
                total = saturatedAdd(total, stack.getCount());
            }
        }
        return total;
    }

    @Override
    protected long readFluids(IFluidHandler handler, @Nullable String resource) {
        Predicate<ResourceLocation> filter = idFilter(resource);
        long total = 0;
        for (int tank = 0; tank < handler.getTanks(); tank++) {
            FluidStack stack = handler.getFluidInTank(tank);
            if (!stack.isEmpty() && filter.test(BuiltInRegistries.FLUID.getKey(stack.getFluid()))) {
                total = saturatedAdd(total, stack.getAmount());
            }
        }
        return total;
    }

    @Override
    protected long readChemicals(ServerLevel level, BlockPos pos, @Nullable Direction side, @Nullable String resource,
                                 boolean pooled) {
        return BlockResourceMekanism.amount(level, pos, side, idFilter(resource));
    }

    @Override
    protected long readEnergy(IEnergyStorage storage) {
        return Math.max(0, storage.getEnergyStored());
    }

    private static Predicate<ResourceLocation> idFilter(@Nullable String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return id -> true;
        }
        WildcardStringMatcher matcher = WildcardStringMatcher.compile(pattern);
        boolean fullId = pattern.indexOf(':') >= 0;
        return id -> matcher.matches(fullId ? id.toString() : id.getPath());
    }
}
