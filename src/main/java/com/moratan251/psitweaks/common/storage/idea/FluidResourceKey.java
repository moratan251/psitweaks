package com.moratan251.psitweaks.common.storage.idea;

import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import java.util.Objects;
import net.minecraft.nbt.Tag;
import net.minecraftforge.fluids.FluidStack;

/** Fluid ID + NBT一式からなる不変の資源キー。量は1 mBへ正規化する。 */
public final class FluidResourceKey {
    private final FluidStack template;

    private FluidResourceKey(FluidStack template) {
        this.template = template;
    }

    public static Optional<FluidResourceKey> of(FluidStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new FluidResourceKey(new FluidStack(stack, 1)));
    }

    public static Optional<FluidResourceKey> parse(Tag tag) {
        return tag instanceof CompoundTag compound ? FluidResourceKey.of(FluidStack.loadFluidStackFromNBT(compound)) : Optional.empty();
    }

    public Tag save() {
        return template.writeToNBT(new CompoundTag());
    }

    public FluidStack template() {
        return template.copy();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FluidResourceKey other
                && this.template.isFluidEqual(other.template);
    }

    @Override
    public int hashCode() {
        return Objects.hash(template.getFluid(), template.getTag());
    }

    @Override
    public String toString() {
        return template.getFluid().toString();
    }
}
