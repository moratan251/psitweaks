package com.moratan251.psitweaks.client.guis;

import com.moratan251.psitweaks.client.guis.ModMenuTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import vazkii.psi.api.cad.EnumCADComponent;
import vazkii.psi.api.cad.ISocketable;
import vazkii.psi.common.item.ItemCAD;
import vazkii.psi.common.item.ItemSpellBullet;
import vazkii.psi.api.cad.ICADComponent;

public class PortableCADAssemblerMenu extends AbstractContainerMenu {

    private static final String NBT_INV_KEY = "PortableCadAssemblerInv";

    // スロット構成案:
    // 0: CAD本体
    // 1-5: コンポーネント (EnumCADComponent ordinal + 1)
    // 6.. : バレット（最大 12 など。Psi本家の最大に合わせたいならそこを参照）
    private static final int SLOT_CAD = 0;
    private static final int SLOT_COMPONENT_START = 1; // + EnumCADComponent.ordinal()
    private static final int SLOT_COMPONENT_COUNT = 5;

    private static final int BULLET_START = 6;
    private static final int BULLET_COUNT = 12; // ここは後で本家に合わせて調整可

    private final InteractionHand hand;
    private final Player player;
    private final ItemStack containerStack;

    private final ItemStackHandler handler;

    public PortableCADAssemblerMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, buf.readEnum(InteractionHand.class));
    }

    public PortableCADAssemblerMenu(int id, Inventory inv, InteractionHand hand) {
        super(ModMenuTypes.PORTABLE_CAD_ASSEMBLER.get(), id);
        this.hand = hand;
        this.player = inv.player;
        this.containerStack = inv.player.getItemInHand(hand);

        this.handler = new ItemStackHandler(BULLET_START + BULLET_COUNT);
        loadFromItem();

        // --- ここで slots を追加 ---
        // CADスロット
        addSlot(new SlotItemHandlerLike(handler, SLOT_CAD, 8, 18) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.getItem() instanceof ItemCAD;
            }

            @Override
            public void setChanged() {
                super.setChanged();
                saveToItem();
            }
        });

        // コンポーネント 5種（Assembly/Core/Socket/Battery/Colorizer）
        // EnumCADComponent の順序はPsi本家に従う必要あり（ordinalを使うため）
        for (EnumCADComponent c : EnumCADComponent.values()) {
            int slot = SLOT_COMPONENT_START + c.ordinal();
            // 念のため 5種のみの想定
            if (c.ordinal() >= SLOT_COMPONENT_COUNT) break;

            int x = 8 + c.ordinal() * 18;
            int y = 54;

            addSlot(new SlotItemHandlerLike(handler, slot, x, y) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    if (stack.isEmpty()) return true;
                    if (!(stack.getItem() instanceof ICADComponent comp)) return false;
                    return comp.getComponentType(stack) == c;
                }

                @Override
                public void setChanged() {
                    super.setChanged();
                    saveToItem();
                }
            });
        }

        // バレットスロット
        for (int i = 0; i < BULLET_COUNT; i++) {
            int slot = BULLET_START + i;
            int x = 8 + (i % 6) * 18;
            int y = 90 + (i / 6) * 18;

            addSlot(new SlotItemHandlerLike(handler, slot, x, y) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return stack.getItem() instanceof ItemSpellBullet;
                }

                @Override
                public boolean isActive() {
                    return isBulletSlotEnabled(i);
                }

                @Override
                public void setChanged() {
                    super.setChanged();
                    saveToItem();
                }
            });
        }

        // プレイヤーインベントリ（省略せず付ける）
        addPlayerInventory(inv, 8, 140);

    }

    private void addPlayerInventory(Inventory inv, int leftCol, int topRow) {
        // main
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, leftCol + col * 18, topRow + row * 18));
            }
        }
        // hotbar
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inv, col, leftCol + col * 18, topRow + 58));
        }
    }

    private void loadFromItem() {
        CompoundTag tag = containerStack.getTag();
        if (tag != null && tag.contains(NBT_INV_KEY)) {
            handler.deserializeNBT(tag.getCompound(NBT_INV_KEY));
        }
    }

    private void saveToItem() {
        if (player.level().isClientSide) return; // NBT書込はサーバーで
        CompoundTag tag = containerStack.getOrCreateTag();
        tag.put(NBT_INV_KEY, handler.serializeNBT());
    }

    private ItemStack getCadStack() {
        return handler.getStackInSlot(SLOT_CAD);
    }

    private ISocketable getSocketable() {
        ItemStack cad = getCadStack();
        return ISocketable.socketable(cad);
    }

    private boolean isBulletSlotEnabled(int bulletSlot) {
        ItemStack cad = getCadStack();
        if (cad.isEmpty()) return false;
        ISocketable socketable = getSocketable();
        return socketable != null && socketable.isSocketSlotAvailable(bulletSlot);
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        saveToItem();
    }

    @Override
    public boolean stillValid(@NotNull Player p) {
        // 開いてる最中にアイテムが無くなったら閉じさせる（事故防止）
        ItemStack current = p.getItemInHand(hand);
        return !current.isEmpty() && current == containerStack;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player pPlayer, int pIndex) {
        // 後で実装（Shiftクリック移動）
        return ItemStack.EMPTY;
    }
}