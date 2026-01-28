package com.moratan251.psitweaks.common;

import com.moratan251.psitweaks.client.guis.ModMenuTypes;
import com.moratan251.psitweaks.common.items.data.PortableCADAssemblerData;
import com.moratan251.psitweaks.common.menu.slot.SlotBulletSocket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import vazkii.psi.api.cad.EnumCADComponent;
import vazkii.psi.api.cad.ISocketable;

public class PortableCADAssemblerMenu extends AbstractContainerMenu {

    // 本家の画面に寄せるなら、座標は後で調整
    private static final int SLOT_CAD_X = 8;
    private static final int SLOT_CAD_Y = 18;

    private static final int COMPONENT_ROW_Y = 54;
    private static final int COMPONENT_START_X = 8;

    // バレットスロット表示数（最大表示枠）
    // 有効/無効は isBulletSlotEnabled で切替（本家準拠）
    private static final int BULLET_COUNT = 12;
    private static final int BULLET_START_X = 8;
    private static final int BULLET_START_Y = 90;

    private final InteractionHand hand;
    private final Player player;
    private final ItemStack portableStack;

    private final PortableCADAssemblerData data = new PortableCADAssemblerData();

    private final SimpleContainer dummy = new SimpleContainer(1);

    public PortableCADAssemblerMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, buf.readEnum(InteractionHand.class));
    }

    public PortableCADAssemblerMenu(int id, Inventory inv, InteractionHand hand) {
        super(ModMenuTypes.PORTABLE_CAD_ASSEMBLER.get(), id);

        this.hand = hand;
        this.player = inv.player;
        this.portableStack = inv.player.getItemInHand(hand);

        data.readFromItem(portableStack);

        // 0: socketable(CAD)
        addSlot(new SlotItemHandler(data.getInventory(), 0, SLOT_CAD_X, SLOT_CAD_Y) {
            @Override
            public void setChanged() {
                super.setChanged();
                save();
            }
        });

        // 1-5: components
        for (EnumCADComponent c : EnumCADComponent.values()) {
            int idx = c.ordinal(); // 0..?
            int slot = 1 + idx;
            if (slot >= PortableCADAssemblerData.SIZE) break;

            addSlot(new SlotItemHandler(data.getInventory(), slot,
                    COMPONENT_START_X + idx * 18, COMPONENT_ROW_Y) {
                @Override
                public void setChanged() {
                    super.setChanged();
                    save();
                }
            });
        }

        // バレット：CADのsocketへ直結
        for (int i = 0; i < BULLET_COUNT; i++) {
            int x = BULLET_START_X + (i % 6) * 18;
            int y = BULLET_START_Y + (i / 6) * 18;

            int socketIndex = i;

            addSlot(new SlotBulletSocket(
                    dummy, 0, x, y,
                    this::getSocketable,
                    socketIndex,
                    () -> data.isBulletSlotEnabled(socketIndex)
            ) {
                @Override
                public void setChanged() {
                    super.setChanged();
                    // バレットはCAD側NBTが変わるだけだが、
                    // CADスタック自体は portable の0番に入っているので、念のため保存
                    save();
                }
            });
        }

        // プレイヤーインベントリ
        addPlayerInventory(inv, 8, 140);
    }

    private void addPlayerInventory(Inventory inv, int leftCol, int topRow) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, leftCol + col * 18, topRow + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inv, col, leftCol + col * 18, topRow + 58));
        }
    }

    private ISocketable getSocketable() {
        return data.getSocketable();
    }

    private void save() {
        if (!player.level().isClientSide) {
            data.writeToItem(portableStack);
        }
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        save();
    }

    @Override
    public boolean stillValid(@NotNull Player p) {
        // 開いている携帯アセンブラが失われたら閉じる
        ItemStack current = p.getItemInHand(hand);
        return !current.isEmpty() && current == portableStack;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player pPlayer, int pIndex) {
        // 後で実装（Shiftクリック）
        return ItemStack.EMPTY;
    }
}