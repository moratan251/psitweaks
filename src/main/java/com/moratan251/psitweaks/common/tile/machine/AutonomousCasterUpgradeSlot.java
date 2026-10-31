package com.moratan251.psitweaks.common.tile.machine;

import com.moratan251.psitweaks.common.caster.AutonomousCasterUpgrade;
import com.moratan251.psitweaks.common.items.ItemAutonomousCasterUpgrade;
import java.util.function.BiPredicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.container.SelectedWindowData.WindowType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.slot.VirtualInventoryContainerSlot;
import mekanism.common.inventory.slot.BasicInventorySlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 自律詠唱機のアップグレードスロット。アップグレード窓の中にだけあり、手でだけ出し入れできる。
 * 1スロットに1個で、同じ種類は機械に1つまで。
 * <p>
 * 自律詠唱機は Mekanism のアップグレードを載せないので、窓の種類は Mekanism のアップグレード窓のものを借りる
 * （Mekanism の WindowType は列挙で足せない）。窓を開いている間だけ、スロットがあることになる。
 */
public class AutonomousCasterUpgradeSlot extends BasicInventorySlot {
    /** (このスロット, 種類) → ほかのスロットに同じ種類が入っているか。 */
    private final BiPredicate<AutonomousCasterUpgradeSlot, AutonomousCasterUpgrade> installedElsewhere;

    AutonomousCasterUpgradeSlot(@Nullable IContentsListener listener,
                                BiPredicate<AutonomousCasterUpgradeSlot, AutonomousCasterUpgrade> installedElsewhere) {
        super(1, ConstantPredicates.manualOnly(), ConstantPredicates.manualOnly(),
                stack -> ItemAutonomousCasterUpgrade.typeOf(stack) != null, listener, 0, 0);
        this.installedElsewhere = installedElsewhere;
        setSlotOverlay(SlotOverlay.UPGRADE);
    }

    /** 入っているアップグレードの種類。空なら null。 */
    @Nullable
    public AutonomousCasterUpgrade upgrade() {
        return ItemAutonomousCasterUpgrade.typeOf(getStack());
    }

    @Override
    public boolean isItemValidForInsertion(@NotNull ItemStack stack, @NotNull AutomationType automationType) {
        if (!super.isItemValidForInsertion(stack, automationType)) {
            return false;
        }
        AutonomousCasterUpgrade type = ItemAutonomousCasterUpgrade.typeOf(stack);
        return type != null && !installedElsewhere.test(this, type);
    }

    @NotNull
    @Override
    public VirtualInventoryContainerSlot createContainerSlot() {
        return new VirtualInventoryContainerSlot(this, new SelectedWindowData(WindowType.UPGRADE), getSlotOverlay(), this::setStackUnchecked);
    }
}
