package com.moratan251.psitweaks.common.storage.idea;

import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.fluids.FluidStack;

/** Fluid ID + Data Component一式からなる不変の資源キー。量は1 mBへ正規化する。 */
public final class FluidResourceKey {
    private final FluidStack template;

    private FluidResourceKey(FluidStack template) {
        this.template = template;
    }

    public static Optional<FluidResourceKey> of(FluidStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new FluidResourceKey(stack.copyWithAmount(1)));
    }

    public static Optional<FluidResourceKey> parse(HolderLookup.Provider registries, Tag tag) {
        return FluidStack.parse(registries, tag).flatMap(FluidResourceKey::of);
    }

    public Tag save(HolderLookup.Provider registries) {
        return template.save(registries);
    }

    public FluidStack template() {
        return template.copy();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FluidResourceKey other
                && FluidStack.isSameFluidSameComponents(this.template, other.template);
    }

    @Override
    public int hashCode() {
        return FluidStack.hashFluidAndComponents(template);
    }

    @Override
    public String toString() {
        return template.getFluid().toString();
    }
}
