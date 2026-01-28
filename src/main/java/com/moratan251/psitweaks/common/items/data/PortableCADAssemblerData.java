package com.moratan251.psitweaks.common.items.data;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import vazkii.psi.api.cad.AssembleCADEvent;
import vazkii.psi.api.cad.EnumCADComponent;
import vazkii.psi.api.cad.ICADComponent;
import vazkii.psi.api.cad.ISocketable;
import vazkii.psi.api.cad.ITileCADAssembler;
import vazkii.psi.api.cad.PostCADCraftEvent;
import vazkii.psi.common.item.ItemCAD;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * TileCADAssembler (Psi本体) をベースにした、アイテム内蔵版CAD Assemblerのデータ。
 * - 保持するのは 6スロット（0=socketable(CAD), 1-5=components）
 * - バレットは CAD(ItemCAD) 側の ISocketable に格納される（本家準拠）
 */
public class PortableCADAssemblerData implements ITileCADAssembler {

    public static final String NBT_KEY_ITEMS = "Items"; // ContainerHelper形式に寄せる
    public static final int SIZE = 6;

    private final CADStackHandler inventory = new CADStackHandler(SIZE);
    private ItemStack cachedCAD;

    public IItemHandlerModifiable getInventory() {
        return inventory;
    }

    public void readFromItem(@NotNull ItemStack portableAssemblerStack) {
        CompoundTag tag = portableAssemblerStack.getTag();
        if (tag == null) {
            // 空に初期化
            for (int i = 0; i < inventory.getSlots(); i++) inventory.setStackInSlot(i, ItemStack.EMPTY);
            cachedCAD = null;
            return;
        }
        readFromNBT(tag);
    }

    public void writeToItem(@NotNull ItemStack portableAssemblerStack) {
        CompoundTag tag = portableAssemblerStack.getOrCreateTag();
        writeToNBT(tag);
    }

    public void readFromNBT(@NotNull CompoundTag tag) {
        // Tile版の saveAdditional/load と同等（Items配列）
        NonNullList<ItemStack> items = inventory.getItems();
        // いったん初期化してから load
        for (int i = 0; i < items.size(); i++) items.set(i, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items);
        inventory.setItems(items);
        cachedCAD = null;
    }

    public void writeToNBT(@NotNull CompoundTag tag) {
        ContainerHelper.saveAllItems(tag, inventory.getItems());
    }

    @Override
    public void clearCachedCAD() {
        cachedCAD = null;
    }

    @Override
    public ItemStack getCachedCAD(Player player) {
        ItemStack cad = cachedCAD;
        if (cad == null) {
            ItemStack assembly = getStackForComponent(EnumCADComponent.ASSEMBLY);
            if (!assembly.isEmpty()) {
                List<ItemStack> components = IntStream.range(1, 6)
                        .mapToObj(inventory::getStackInSlot)
                        .collect(Collectors.toList());
                cad = ItemCAD.makeCADWithAssembly(assembly, components);
            } else {
                cad = ItemStack.EMPTY;
            }

            AssembleCADEvent assembling = new AssembleCADEvent(cad, this, player);
            MinecraftForge.EVENT_BUS.post(assembling);

            if (assembling.isCanceled()) {
                cad = ItemStack.EMPTY;
            } else {
                cad = assembling.getCad();
            }

            cachedCAD = cad;
        }
        return cad;
    }

    public int getComponentSlot(EnumCADComponent componentType) {
        return componentType.ordinal() + 1;
    }

    @Override
    public ItemStack getStackForComponent(EnumCADComponent componentType) {
        return inventory.getStackInSlot(getComponentSlot(componentType));
    }

    @Override
    public boolean setStackForComponent(EnumCADComponent componentType, ItemStack component) {
        int slot = getComponentSlot(componentType);
        if (component.isEmpty()) {
            inventory.setStackInSlot(slot, component);
            return true;
        } else if (component.getItem() instanceof ICADComponent componentItem) {
            if (componentItem.getComponentType(component) == componentType) {
                inventory.setStackInSlot(slot, component);
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack getSocketableStack() {
        return inventory.getStackInSlot(0);
    }

    @Override
    public ISocketable getSocketable() {
        return ISocketable.socketable(getSocketableStack());
    }

    @Override
    public boolean setSocketableStack(ItemStack stack) {
        if (stack.isEmpty() || ISocketable.isSocketable(stack)) {
            inventory.setStackInSlot(0, stack);
            return true;
        }
        return false;
    }

    @Override
    public void onCraftCAD(ItemStack cad) {
        // 本家と同じイベントは投げる（他アドオン連携用）
        MinecraftForge.EVENT_BUS.post(new PostCADCraftEvent(cad, this));
        for (int i = 1; i < 6; i++) {
            inventory.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    @Override
    public boolean isBulletSlotEnabled(int slot) {
        if (getSocketableStack().isEmpty()) return false;
        ISocketable socketable = getSocketable();
        return socketable != null && socketable.isSocketSlotAvailable(slot);
    }

    /**
     * Tile版の inner class をほぼそのまま移植
     */
    private class CADStackHandler extends ItemStackHandler {

        private CADStackHandler(int size) {
            super(size);
        }

        private NonNullList<ItemStack> getItems() {
            return this.stacks;
        }

        private void setItems(NonNullList<ItemStack> items) {
            this.stacks = items;
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            if (0 < slot && slot < 6) {
                clearCachedCAD();
            }
            // BlockEntity#setChanged() は無いので、保存は Menu 側が行う想定
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (stack.isEmpty()) return true;

            if (slot == 0) {
                return ISocketable.isSocketable(stack);
            } else if (slot < 6) {
                return stack.getItem() instanceof ICADComponent
                        && ((ICADComponent) stack.getItem()).getComponentType(stack).ordinal() == slot - 1;
            }

            return false;
        }
    }
}