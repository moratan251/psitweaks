package com.moratan251.psitweaks.common.menu;

import com.moratan251.psitweaks.common.items.ItemPortableCADAssembler;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import vazkii.psi.api.cad.EnumCADComponent;
import vazkii.psi.api.cad.ICADComponent;
import vazkii.psi.api.cad.ISocketable;
import vazkii.psi.api.inventory.InventorySocketable;
import vazkii.psi.api.spell.ISpellAcceptor;
import vazkii.psi.common.block.base.ModBlocks;
import vazkii.psi.common.block.tile.TileCADAssembler;
import vazkii.psi.common.block.tile.container.slot.InventoryAssemblerOutput;
import vazkii.psi.common.block.tile.container.slot.SlotCADOutput;
import vazkii.psi.common.block.tile.container.slot.SlotSocketable;
import vazkii.psi.common.block.tile.container.slot.ValidatorSlot;

public class PortableCADAssemblerMenu extends AbstractContainerMenu {
    private static final EquipmentSlot[] EQUIPMENT_SLOTS =
            new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    public final TileCADAssembler assembler;
    private final UUID portableId;
    private final int cadComponentStart;
    private final int socketableStart;
    private final int socketableEnd;
    private final int bulletStart;
    private final int bulletEnd;
    private final int playerStart;
    private final int playerEnd;
    private final int hotbarStart;
    private final int hotbarEnd;
    private final int armorStart;

    public static PortableCADAssemblerMenu fromNetwork(int windowId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        InteractionHand hand = buf.readEnum(InteractionHand.class);
        UUID portableId = buf.readUUID();
        return new PortableCADAssemblerMenu(windowId, playerInventory, hand, portableId);
    }

    public PortableCADAssemblerMenu(int windowId, Inventory playerInventory, InteractionHand hand, UUID portableId) {
        super(ModMenuTypes.PORTABLE_CAD_ASSEMBLER.get(), windowId);
        this.portableId = portableId;
        Player player = playerInventory.player;
        int playerSize = playerInventory.getContainerSize();

        this.assembler = createVirtualAssembler(player, hand, portableId);
        IItemHandlerModifiable assemblerInv = assembler.getInventory();
        assembler.clearCachedCAD();

        InventoryAssemblerOutput output = new InventoryAssemblerOutput(player, assembler);
        InventorySocketable bullets = new InventorySocketable(assembler, assembler.getSocketableStack());

        this.addSlot(new SlotCADOutput(output, assembler, 120, 35));

        this.cadComponentStart = this.slots.size();
        this.addSlot(new SlotItemHandler(assemblerInv, componentSlot(EnumCADComponent.ASSEMBLY), 120, 91));
        this.addSlot(new SlotItemHandler(assemblerInv, componentSlot(EnumCADComponent.CORE), 100, 91));
        this.addSlot(new SlotItemHandler(assemblerInv, componentSlot(EnumCADComponent.SOCKET), 140, 91));
        this.addSlot(new SlotItemHandler(assemblerInv, componentSlot(EnumCADComponent.BATTERY), 110, 111));
        this.addSlot(new SlotItemHandler(assemblerInv, componentSlot(EnumCADComponent.DYE), 130, 111));

        this.socketableStart = this.slots.size();
        this.addSlot(new SlotSocketable(assemblerInv, bullets, 0, 35, 21));
        this.socketableEnd = this.slots.size();

        this.bulletStart = this.slots.size();
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 3; col++) {
                this.addSlot(new ValidatorSlot(bullets, col + row * 3, 17 + col * 18, 57 + row * 18));
            }
        }
        this.bulletEnd = this.slots.size();

        int xs = 48;
        int ys = 143;
        this.playerStart = this.slots.size();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, xs + col * 18, ys + row * 18));
            }
        }
        this.playerEnd = this.slots.size();

        this.hotbarStart = this.slots.size();
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, xs + col * 18, ys + 58));
        }
        this.hotbarEnd = this.slots.size();

        this.armorStart = this.slots.size();
        for (int armorSlot = 0; armorSlot < 4; armorSlot++) {
            EquipmentSlot equipmentSlot = EQUIPMENT_SLOTS[armorSlot];
            this.addSlot(new Slot(playerInventory, playerSize - 2 - armorSlot, xs - 27, ys + 18 * armorSlot) {
                @Override
                public int getMaxStackSize() {
                    return 1;
                }

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return !stack.isEmpty() && stack.getItem().canEquip(stack, equipmentSlot, player);
                }
            });
        }

        this.addSlot(new Slot(playerInventory, playerSize - 1, 219, 143));
    }

    public TileCADAssembler getAssembler() {
        return assembler;
    }

    @Override
    public boolean stillValid(Player player) {
        return !player.isRemoved() && findPortableStack(player, portableId).isPresent();
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int from) {
        ItemStack mergeStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(from);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            if (isMatchingPortable(stackInSlot, portableId)) {
                return ItemStack.EMPTY;
            }

            mergeStack = stackInSlot.copy();
            if (from >= this.playerStart) {
                if (stackInSlot.getItem() instanceof ICADComponent cadComponent) {
                    EnumCADComponent componentType = cadComponent.getComponentType(stackInSlot);
                    int componentSlot = this.cadComponentStart + componentType.ordinal();
                    if (!this.moveItemStackTo(stackInSlot, componentSlot, componentSlot + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (ISocketable.isSocketable(stackInSlot)) {
                    if (!this.moveItemStackTo(stackInSlot, this.socketableStart, this.socketableEnd, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (ISpellAcceptor.isContainer(stackInSlot)) {
                    if (!this.moveItemStackTo(stackInSlot, this.bulletStart, this.bulletEnd, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (from < this.hotbarStart) {
                    if (!this.moveItemStackTo(stackInSlot, this.hotbarStart, this.hotbarEnd, true)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(stackInSlot, this.playerStart, this.playerEnd, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (stackInSlot.getItem() instanceof ArmorItem armor) {
                int armorSlot = this.armorStart + armor.getType().ordinal() - 1;
                if (armorSlot < this.armorStart || armorSlot >= this.armorStart + 4
                        || !this.moveItemStackTo(stackInSlot, armorSlot, armorSlot + 1, true)) {
                    if (!this.moveItemStackTo(stackInSlot, this.playerStart, this.hotbarEnd, true)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!this.moveItemStackTo(stackInSlot, this.playerStart, this.hotbarEnd, true)) {
                return ItemStack.EMPTY;
            }

            slot.setChanged();
            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else if (stackInSlot.getCount() == mergeStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stackInSlot);
        }

        return mergeStack;
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickType, Player player) {
        if (isMatchingPortable(this.getCarried(), portableId)) {
            return;
        }

        if (slotId >= 0 && slotId < this.slots.size()) {
            Slot slot = this.slots.get(slotId);
            if (isMatchingPortable(slot.getItem(), portableId)) {
                return;
            }
        }

        if (clickType == ClickType.SWAP && dragType >= 0 && dragType < 9) {
            ItemStack hotbarStack = player.getInventory().getItem(dragType);
            if (isMatchingPortable(hotbarStack, portableId)) {
                return;
            }
        }

        super.clicked(slotId, dragType, clickType, player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide) {
            findPortableStack(player, portableId).ifPresent(stack -> saveAssemblerData(player, stack));
        }
        this.assembler.clearCachedCAD();
    }

    private static TileCADAssembler createVirtualAssembler(Player player, InteractionHand hand, UUID portableId) {
        BlockPos pos = player.blockPosition();
        BlockState state = ModBlocks.cadAssembler.get().defaultBlockState();
        TileCADAssembler virtualAssembler = new TileCADAssembler(pos, state);
        virtualAssembler.setLevel(player.level());

        ItemStack handheld = player.getItemInHand(hand);
        if (isMatchingPortable(handheld, portableId)) {
            loadAssemblerData(player, virtualAssembler, handheld);
            return virtualAssembler;
        }

        findPortableStack(player, portableId).ifPresent(stack -> loadAssemblerData(player, virtualAssembler, stack));
        return virtualAssembler;
    }

    private static int componentSlot(EnumCADComponent component) {
        return component.ordinal() + 1;
    }

    private static void loadAssemblerData(Player player, TileCADAssembler assembler, ItemStack stack) {
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (root.contains(ItemPortableCADAssembler.TAG_ASSEMBLER_DATA, Tag.TAG_COMPOUND)) {
            assembler.readPacketNBT(root.getCompound(ItemPortableCADAssembler.TAG_ASSEMBLER_DATA).copy(), player.registryAccess());
        }
    }

    private void saveAssemblerData(Player player, ItemStack stack) {
        CompoundTag data = this.assembler.getUpdateTag(player.registryAccess());
        CustomData.update(DataComponents.CUSTOM_DATA, stack,
                root -> root.put(ItemPortableCADAssembler.TAG_ASSEMBLER_DATA, data));
    }

    private static Optional<ItemStack> findPortableStack(Player player, UUID portableId) {
        Inventory inv = player.getInventory();
        for (int index = 0; index < inv.getContainerSize(); index++) {
            ItemStack stack = inv.getItem(index);
            if (isMatchingPortable(stack, portableId)) {
                return Optional.of(stack);
            }
        }
        return Optional.empty();
    }

    private static boolean isMatchingPortable(ItemStack stack, UUID portableId) {
        if (!(stack.getItem() instanceof ItemPortableCADAssembler)) {
            return false;
        }
        UUID stackId = ItemPortableCADAssembler.getInstanceId(stack);
        return portableId.equals(stackId);
    }

    public record Provider(InteractionHand hand, UUID portableId) implements MenuProvider {
        @Override
        public @NotNull Component getDisplayName() {
            return Component.translatable("item.psitweaks.portable_cad_assembler");
        }

        @Override
        public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
            return new PortableCADAssemblerMenu(windowId, playerInventory, hand, portableId);
        }
    }
}
