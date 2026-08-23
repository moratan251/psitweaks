package com.moratan251.psitweaks.common.menu;

import com.moratan251.psitweaks.common.network.MessageIdeaStorageExtract;
import com.moratan251.psitweaks.common.network.MessageIdeaStorageSync;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageService;
import com.moratan251.psitweaks.common.storage.idea.ItemResourceKey;
import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * イデアストレージ閲覧 GUI のコンテナ。
 * ストレージエントリは仮想(実スロットを持たず描画+payload)で、実スロットは
 * プレイヤーインベントリ(0..35)・クラフトマトリクス(36..44)・クラフト結果(45)。
 * 表示行数に応じてプレイヤーインベントリの座標を動的に計算する。
 */
public class IdeaStorageMenu extends AbstractContainerMenu {
    public static final int GRID_COLUMNS = 9;
    public static final int GRID_X = 8;
    public static final int GRID_Y = 34;
    public static final int SCROLLBAR_X = 174;
    public static final int SCROLLBAR_WIDTH = 10;
    public static final int PLAYER_INVENTORY_X = 17;
    public static final int SCREEN_WIDTH = 194;

    /** クラフトウィンドウ(画面左側のポップアップ)のレイアウト。負の x はメイン画面の左外。右端はサイドボタン(-20)より左に収める。 */
    public static final int CRAFT_PANEL_X = -140;
    public static final int CRAFT_PANEL_Y = 18;
    public static final int CRAFT_PANEL_WIDTH = 116;
    public static final int CRAFT_PANEL_HEIGHT = 74;
    public static final int CRAFT_MATRIX_X = -132;
    public static final int CRAFT_MATRIX_Y = 24;
    public static final int CRAFT_RESULT_X = -52;
    public static final int CRAFT_RESULT_Y = 42;

    public static final int CRAFT_MATRIX_START = 36;
    public static final int CRAFT_RESULT_SLOT = 45;

    /** 結果スロット shift クリック時の連続クラフト上限(無限ループ防止)。 */
    private static final int MAX_SHIFT_CRAFT_ITERATIONS = 64;

    private final UUID ownerUuid;
    private final Player player;
    @Nullable
    private final PlayerIdeaStorage storage;
    private final int gridRows;
    private final TransientCraftingContainer craftMatrix = new TransientCraftingContainer(this, 3, 3);
    private final ResultContainer craftResult = new ResultContainer();
    private boolean craftOpen;
    private long lastSyncedVersion = -1L;

    public static IdeaStorageMenu fromNetwork(int windowId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        return new IdeaStorageMenu(windowId, playerInventory, buf.readUUID(), buf.readVarInt());
    }

    public IdeaStorageMenu(int windowId, Inventory playerInventory, UUID ownerUuid, int gridRows) {
        super(ModMenuTypes.IDEA_STORAGE.get(), windowId);
        this.ownerUuid = ownerUuid;
        this.player = playerInventory.player;
        this.gridRows = Math.max(PlayerIdeaStorage.GRID_ROWS_MIN, Math.min(PlayerIdeaStorage.GRID_ROWS_MAX, gridRows));
        if (player instanceof ServerPlayer serverPlayer) {
            this.storage = IdeaStorageService.get(serverPlayer.server, ownerUuid);
        } else {
            this.storage = null;
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, PLAYER_INVENTORY_X + col * 18, playerInventoryY() + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, PLAYER_INVENTORY_X + col * 18, hotbarY()));
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                this.addSlot(new CraftSlot(craftMatrix, col + row * 3, CRAFT_MATRIX_X + col * 18, CRAFT_MATRIX_Y + row * 18));
            }
        }
        this.addSlot(new CraftResultSlot(CRAFT_RESULT_X, CRAFT_RESULT_Y));
    }

    public UUID ownerUuid() {
        return ownerUuid;
    }

    public int gridRows() {
        return gridRows;
    }

    public int gridBottom() {
        return GRID_Y + gridRows * 18;
    }

    public int inventoryLabelY() {
        return gridBottom() + 4;
    }

    public int playerInventoryY() {
        return gridBottom() + 14;
    }

    public int hotbarY() {
        return playerInventoryY() + 58;
    }

    public int screenHeight() {
        return hotbarY() + 18 + 8;
    }

    public boolean isCraftOpen() {
        return craftOpen;
    }

    /** 開閉状態はクライアント(楽観)とサーバー(payload)の双方で同じ値に更新する。 */
    public void setCraftOpen(boolean open) {
        this.craftOpen = open;
    }

    public boolean isCraftSlot(Slot slot) {
        return slot.container == craftMatrix || slot.container == craftResult;
    }

    @Override
    public boolean stillValid(Player player) {
        // 仮想ストレージのため距離判定なし
        return true;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        // ウィンドウ非表示時はクラフトスロットへの操作を拒否(クライアント・サーバー共通判定)
        if (!craftOpen && slotId >= CRAFT_MATRIX_START && slotId <= CRAFT_RESULT_SLOT) {
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public boolean canDragTo(Slot slot) {
        return (!isCraftSlot(slot) || craftOpen) && super.canDragTo(slot);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        Slot slot = index >= 0 && index < this.slots.size() ? this.slots.get(index) : null;
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        if (index >= CRAFT_MATRIX_START && index < CRAFT_RESULT_SLOT) {
            // クラフトマトリクス → プレイヤーインベントリ
            ItemStack stack = slot.getItem();
            if (!this.moveItemStackTo(stack, 0, CRAFT_MATRIX_START, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            return ItemStack.EMPTY;
        }
        if (index == CRAFT_RESULT_SLOT) {
            quickMoveCraftResult(player, slot);
            return ItemStack.EMPTY;
        }
        // プレイヤーインベントリ → ストレージ格納
        if (!(player instanceof ServerPlayer) || storage == null || storage.isLoadFailed()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        long moved = storage.insert(stack, stack.getCount());
        if (moved <= 0) {
            return ItemStack.EMPTY;
        }
        stack.shrink((int) moved);
        slot.setChanged();
        return ItemStack.EMPTY;
    }

    /** 結果スロットの shift クリック: インベントリに入るまで繰り返しクラフトする(ループ上限あり)。 */
    private void quickMoveCraftResult(Player player, Slot resultSlot) {
        ItemStack template = resultSlot.getItem().copy();
        int guard = 0;
        while (resultSlot.hasItem() && guard++ < MAX_SHIFT_CRAFT_ITERATIONS) {
            ItemStack crafted = resultSlot.remove(resultSlot.getItem().getCount());
            if (crafted.isEmpty()) {
                break;
            }
            resultSlot.onTake(player, crafted);
            if (!this.moveItemStackTo(crafted, 0, CRAFT_MATRIX_START, false)) {
                // インベントリに入りきらない分はドロップ(ロスト防止)
                player.drop(crafted, false);
                break;
            }
            refreshCraftingResult();
            if (!ItemStack.isSameItemSameComponents(resultSlot.getItem(), template)) {
                break;
            }
        }
    }

    /** マトリクス変更時に結果スロットを再計算する(クライアント・サーバー双方で実行)。 */
    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (container == craftMatrix) {
            refreshCraftingResult();
        }
    }

    private void refreshCraftingResult() {
        Level level = player.level();
        CraftingInput input = craftMatrix.asCraftInput();
        Optional<RecipeHolder<CraftingRecipe>> recipeOptional = input.isEmpty()
                ? Optional.empty()
                : level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
        if (recipeOptional.isPresent()) {
            craftResult.setItem(0, recipeOptional.get().value().assemble(input, level.registryAccess()));
            craftResult.setRecipeUsed(recipeOptional.get());
        } else {
            craftResult.setItem(0, ItemStack.EMPTY);
            craftResult.setRecipeUsed(null);
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (player instanceof ServerPlayer serverPlayer) {
            returnCraftMatrixContents(serverPlayer);
        }
    }

    /** Menu 終了時: マトリクス残りをストレージ → インベントリ → ドロップの順で回収する。 */
    private void returnCraftMatrixContents(ServerPlayer serverPlayer) {
        for (int i = 0; i < craftMatrix.getContainerSize(); i++) {
            ItemStack stack = craftMatrix.removeItemNoUpdate(i);
            if (stack.isEmpty()) {
                continue;
            }
            long leftover = stack.getCount();
            if (storage != null && !storage.isLoadFailed()) {
                leftover -= storage.insert(stack, leftover);
            }
            if (leftover > 0) {
                leftover = addToPlayerInventory(serverPlayer, stack, leftover, stack.getMaxStackSize());
            }
            if (leftover > 0) {
                serverPlayer.drop(stack.copyWithCount((int) leftover), false);
            }
        }
        craftResult.removeItemNoUpdate(0);
    }

    /** 行数変更: 範囲検証して保存し、Menu を閉じてから新しい行数で開き直す(QIO の recreateViewer と同じ発想)。 */
    public void handleResize(ServerPlayer serverPlayer, int rows) {
        int clamped = Math.max(PlayerIdeaStorage.GRID_ROWS_MIN, Math.min(PlayerIdeaStorage.GRID_ROWS_MAX, rows));
        if (storage != null) {
            storage.setGridRows(clamped);
        }
        serverPlayer.closeContainer();
        serverPlayer.openMenu(new Provider(ownerUuid, clamped), buf -> {
            buf.writeUUID(ownerUuid);
            buf.writeVarInt(clamped);
        });
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (player instanceof ServerPlayer serverPlayer
                && storage != null
                && storage.getVersion() != lastSyncedVersion) {
            lastSyncedVersion = storage.getVersion();
            PacketDistributor.sendToPlayer(serverPlayer, createSyncMessage());
        }
    }

    private MessageIdeaStorageSync createSyncMessage() {
        List<MessageIdeaStorageSync.Entry> entries = new ArrayList<>();
        if (storage != null) {
            for (var entry : storage.itemEntries()) {
                entries.add(new MessageIdeaStorageSync.Entry(entry.getKey().template(), entry.getValue()));
            }
        }
        return new MessageIdeaStorageSync(entries, storage != null && storage.isLoadFailed());
    }

    /** グリッドクリックによる払出。クライアント payload を信用せず、サーバー側のストレージで検証する。 */
    public void handleExtract(ServerPlayer player, ItemStack template, int mode) {
        if (storage == null || storage.isLoadFailed() || template.isEmpty()) {
            return;
        }
        Optional<ItemResourceKey> keyOptional = ItemResourceKey.of(template);
        if (keyOptional.isEmpty()) {
            return;
        }
        ItemResourceKey key = keyOptional.get();
        int maxStack = key.getMaxStackSize();
        int halfStack = (maxStack + 1) / 2;
        long want = switch (mode) {
            case MessageIdeaStorageExtract.MODE_CURSOR_STACK,
                 MessageIdeaStorageExtract.MODE_INVENTORY_STACK -> maxStack;
            case MessageIdeaStorageExtract.MODE_CURSOR_HALF_STACK,
                 MessageIdeaStorageExtract.MODE_INVENTORY_HALF_STACK -> halfStack;
            default -> 0L;
        };
        if (want <= 0) {
            return;
        }
        boolean toCursor = mode == MessageIdeaStorageExtract.MODE_CURSOR_STACK
                || mode == MessageIdeaStorageExtract.MODE_CURSOR_HALF_STACK;
        if (toCursor && !this.getCarried().isEmpty()) {
            return;
        }
        long extracted = storage.extract(key, want);
        if (extracted <= 0) {
            return;
        }
        if (toCursor) {
            this.setCarried(template.copyWithCount((int) extracted));
            return;
        }
        long leftover = addToPlayerInventory(player, template, extracted, maxStack);
        if (leftover > 0) {
            // インベントリに入りきらなかった分はストレージへ戻す(ロスト防止)
            storage.insert(template, leftover);
        }
    }

    private static long addToPlayerInventory(Player player, ItemStack template, long amount, int maxStack) {
        // PIT-029: Inventory#add はクリエイティブで残数を消去して成功を返すため、
        // IItemHandler 化してシミュレートで受理量を確定してから本挿入する。
        PlayerMainInvWrapper wrapper = new PlayerMainInvWrapper(player.getInventory());
        long remaining = amount;
        while (remaining > 0) {
            int chunk = (int) Math.min(remaining, maxStack);
            ItemStack simulated = ItemHandlerHelper.insertItem(wrapper, template.copyWithCount(chunk), true);
            int accepted = chunk - simulated.getCount();
            if (accepted <= 0) {
                break;
            }
            ItemHandlerHelper.insertItem(wrapper, template.copyWithCount(accepted), false);
            remaining -= accepted;
        }
        return remaining;
    }

    /** カーソル保持アイテムの格納。サーバー側の getCarried() を正本として検証する。 */
    public void handleDeposit(ServerPlayer player, ItemStack template) {
        if (storage == null || storage.isLoadFailed()) {
            return;
        }
        ItemStack carried = this.getCarried();
        if (carried.isEmpty() || !ItemStack.isSameItemSameComponents(carried, template)) {
            return;
        }
        long moved = storage.insert(carried, carried.getCount());
        if (moved <= 0) {
            return;
        }
        carried.shrink((int) moved);
        this.setCarried(carried.isEmpty() ? ItemStack.EMPTY : carried);
    }

    /** クラフトマトリクスのスロット。閉じている間は非アクティブ(ホバー/ドラッグ不可)にする。 */
    private class CraftSlot extends Slot {
        CraftSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean isActive() {
            return craftOpen;
        }
    }

    /** クラフト結果スロット。onTake で材料を各スロット1個ずつ消費し、クラフト残り(バケツ等)を処理する。 */
    private class CraftResultSlot extends Slot {
        CraftResultSlot(int x, int y) {
            super(craftResult, 0, x, y);
        }

        @Override
        public boolean isActive() {
            return craftOpen;
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return false;
        }

        @Override
        public void onTake(@NotNull Player player, @NotNull ItemStack stack) {
            super.onTake(player, stack);
            Level level = player.level();
            CraftingInput input = craftMatrix.asCraftInput();
            // レシピが一致しない場合はクラフト残りを配らない(材料の複製・消失を防止)
            Optional<RecipeHolder<CraftingRecipe>> recipeOptional =
                    level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
            NonNullList<ItemStack> remaining = recipeOptional
                    .map(holder -> holder.value().getRemainingItems(input))
                    .orElseGet(() -> NonNullList.withSize(input.size(), ItemStack.EMPTY));
            for (int i = 0; i < remaining.size(); i++) {
                ItemStack slotStack = craftMatrix.getItem(i);
                ItemStack remainder = remaining.get(i);
                if (!slotStack.isEmpty()) {
                    craftMatrix.removeItem(i, 1);
                    slotStack = craftMatrix.getItem(i);
                }
                if (remainder.isEmpty()) {
                    continue;
                }
                if (slotStack.isEmpty()) {
                    craftMatrix.setItem(i, remainder);
                } else if (ItemStack.isSameItemSameComponents(slotStack, remainder)) {
                    remainder.grow(slotStack.getCount());
                    craftMatrix.setItem(i, remainder);
                } else if (!player.getInventory().add(remainder)) {
                    player.drop(remainder, false);
                }
            }
        }
    }

    public record Provider(UUID owner, int rows) implements MenuProvider {
        @Override
        public @NotNull Component getDisplayName() {
            return Component.translatable("container.psitweaks.idea_storage");
        }

        @Override
        public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
            return new IdeaStorageMenu(windowId, playerInventory, owner, rows);
        }
    }
}
