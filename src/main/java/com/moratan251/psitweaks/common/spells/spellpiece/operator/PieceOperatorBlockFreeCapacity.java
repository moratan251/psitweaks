package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.common.compat.BlockResourceMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import vazkii.psi.api.spell.Spell;

/**
 * How much more of one resource a block accepts, from simulated insertions: items (count, slot by slot), fluids and
 * chemicals (mB, one request) by exact ID without extra data components; FE is the empty part of the buffer. An
 * unknown ID reads as 0.
 */
public class PieceOperatorBlockFreeCapacity extends PieceOperatorBlockResourceBase {
    public PieceOperatorBlockFreeCapacity(Spell spell) {
        super(spell);
    }

    @Override
    protected boolean resourceOptional() {
        return false;
    }

    /**
     * Slots are simulated one at a time, so handlers whose slots share a pool may report more than really fits. A known
     * pool is asked through its last slot only, which on the Ideaspace Connector takes any item its input allows.
     */
    @Override
    protected long readItems(IItemHandler handler, @Nullable String resource, boolean pooled) {
        ResourceLocation id = resourceId(resource);
        Item item = id == null ? Items.AIR : BuiltInRegistries.ITEM.getOptional(id).orElse(Items.AIR);
        if (item == Items.AIR) {
            return 0;
        }
        long total = 0;
        for (int slot = pooled ? Math.max(0, handler.getSlots() - 1) : 0; slot < handler.getSlots(); slot++) {
            int limit = handler.getSlotLimit(slot);
            if (limit <= 0) {
                continue;
            }
            ItemStack rest = handler.insertItem(slot, new ItemStack(item, limit), true);
            total = saturatedAdd(total, Math.max(0, limit - rest.getCount()));
        }
        return total;
    }

    @Override
    protected long readFluids(IFluidHandler handler, @Nullable String resource) {
        ResourceLocation id = resourceId(resource);
        Fluid fluid = id == null ? Fluids.EMPTY : BuiltInRegistries.FLUID.getOptional(id).orElse(Fluids.EMPTY);
        if (fluid == Fluids.EMPTY) {
            return 0;
        }
        return Math.max(0, handler.fill(new FluidStack(fluid, Integer.MAX_VALUE), IFluidHandler.FluidAction.SIMULATE));
    }

    @Override
    protected long readChemicals(ServerLevel level, BlockPos pos, @Nullable Direction side, @Nullable String resource,
                                 boolean pooled) {
        ResourceLocation id = resourceId(resource);
        return id == null ? 0 : BlockResourceMekanism.freeCapacity(level, pos, side, id, pooled);
    }

    @Override
    protected long readEnergy(IEnergyStorage storage) {
        return storage.canReceive() ? Math.max(0, (long) storage.getMaxEnergyStored() - storage.getEnergyStored()) : 0;
    }

    @Nullable
    private static ResourceLocation resourceId(@Nullable String resource) {
        return resource == null || resource.isEmpty() ? null : ResourceLocation.tryParse(resource);
    }
}
