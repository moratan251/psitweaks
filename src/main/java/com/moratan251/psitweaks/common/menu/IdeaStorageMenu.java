package com.moratan251.psitweaks.common.menu;

import com.moratan251.psitweaks.common.network.MessageIdeaStorageExtract;
import com.moratan251.psitweaks.common.network.MessageIdeaStorageFillCrafting;
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
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
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

    private final UUID ownerUuid;
    private final Player player;
    @Nullable
    private final PlayerIdeaStorage storage;
    private final int gridRows;
    private final TransientCraftingContainer craftMatrix = new TransientCraftingContainer(this, 3, 3);
    private final ResultContainer craftResult = new ResultContainer();
    private boolean craftOpen;
    private long lastSyncedVersion = -1L;
    private List<MessageIdeaStorageSync.Entry> clientStorageEntries = List.of();

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

    /** JEI/EMIの表示上の材料可否判定にだけ使うクライアントsnapshot。数量の正本にはしない。 */
    public void applyClientStorageEntries(List<MessageIdeaStorageSync.Entry> entries) {
        this.clientStorageEntries = List.copyOf(entries);
    }

    public List<MessageIdeaStorageSync.Entry> clientStorageEntries() {
        return clientStorageEntries;
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
            return quickMoveCraftResult(player, slot);
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

    /** 結果スロットのshiftクリック1回分。連続実行はAbstractContainerMenuの標準QUICK_MOVEループが行う。 */
    private ItemStack quickMoveCraftResult(Player player, Slot resultSlot) {
        ItemStack result = resultSlot.getItem();
        ItemStack original = result.copy();
        PlayerMainInvWrapper inventory = new PlayerMainInvWrapper(player.getInventory());
        if (!ItemHandlerHelper.insertItem(inventory, original, true).isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (!this.moveItemStackTo(result, 0, CRAFT_MATRIX_START, true)) {
            return ItemStack.EMPTY;
        }
        resultSlot.onQuickCraft(result, original);
        if (result.isEmpty()) {
            resultSlot.setByPlayer(ItemStack.EMPTY);
        } else {
            resultSlot.setChanged();
        }
        if (result.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        resultSlot.onTake(player, result);
        return original;
    }

    /** マトリクス変更時の結果はサーバーだけで確定し、結果スロットを明示同期する。 */
    @Override
    public void slotsChanged(Container container) {
        if (container == craftMatrix) {
            refreshCraftingResult();
        } else {
            super.slotsChanged(container);
        }
    }

    private void refreshCraftingResult() {
        Level level = player.level();
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        CraftingInput input = craftMatrix.asCraftInput();
        Optional<RecipeHolder<CraftingRecipe>> recipeOptional = input.isEmpty()
                ? Optional.empty()
                : level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
        ItemStack result = ItemStack.EMPTY;
        if (recipeOptional.isPresent()) {
            RecipeHolder<CraftingRecipe> recipe = recipeOptional.get();
            if (craftResult.setRecipeUsed(level, serverPlayer, recipe)) {
                ItemStack assembled = recipe.value().assemble(input, level.registryAccess());
                if (assembled.isItemEnabled(level.enabledFeatures())) {
                    result = assembled;
                }
            }
        }
        craftResult.setItem(0, result);
        setRemoteSlot(CRAFT_RESULT_SLOT, result);
        serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(
                containerId, incrementStateId(), CRAFT_RESULT_SLOT, result));
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

    /**
     * JEI/EMIの材料配置要求。グリッドは成功確定まで変更せず、ストレージとインベントリの
     * 一時変更は途中失敗時にすべて逆操作して戻す。
     */
    public boolean handleFillCrafting(ServerPlayer serverPlayer, List<ItemStack> requestedTemplates) {
        if (storage == null || storage.isLoadFailed() || !craftOpen
                || requestedTemplates.size() != MessageIdeaStorageFillCrafting.CRAFT_SLOT_COUNT) {
            return false;
        }

        List<ItemStack> desired = new ArrayList<>(MessageIdeaStorageFillCrafting.CRAFT_SLOT_COUNT);
        List<ItemStack> original = new ArrayList<>(MessageIdeaStorageFillCrafting.CRAFT_SLOT_COUNT);
        for (int i = 0; i < MessageIdeaStorageFillCrafting.CRAFT_SLOT_COUNT; i++) {
            ItemStack requested = requestedTemplates.get(i);
            desired.add(requested.isEmpty() ? ItemStack.EMPTY : requested.copyWithCount(1));
            original.add(craftMatrix.getItem(i).copy());
        }

        List<StorageOperation> storageOperations = new ArrayList<>();
        List<InventoryWithdrawal> inventoryWithdrawals = new ArrayList<>();
        ItemStack[] finalStacks = new ItemStack[MessageIdeaStorageFillCrafting.CRAFT_SLOT_COUNT];
        List<ItemStack> remainingOriginal = new ArrayList<>(MessageIdeaStorageFillCrafting.CRAFT_SLOT_COUNT);

        // 既に目的材料があるマスはそのスタックを維持し、それ以外はグリッド内の材料poolにする。
        for (int i = 0; i < original.size(); i++) {
            ItemStack current = original.get(i);
            ItemStack target = desired.get(i);
            if (!current.isEmpty() && !target.isEmpty()
                    && ItemStack.isSameItemSameComponents(current, target)) {
                finalStacks[i] = current.copy();
                remainingOriginal.add(ItemStack.EMPTY);
                continue;
            }
            remainingOriginal.add(current.copy());
        }

        // 別マスにある材料と、維持したスタックの余剰を先に予約する。
        for (int i = 0; i < desired.size(); i++) {
            ItemStack target = desired.get(i);
            if (target.isEmpty()) {
                finalStacks[i] = ItemStack.EMPTY;
                continue;
            }
            if (finalStacks[i] != null) {
                continue;
            }
            ItemStack reserved = withdrawOneFromFinalStacks(finalStacks, target);
            if (reserved.isEmpty()) {
                reserved = withdrawOneFromStacks(remainingOriginal, target);
            }
            if (!reserved.isEmpty()) {
                finalStacks[i] = reserved;
            }
        }

        // 予約されなかった元グリッド材料だけを一時的にストレージへ戻す。
        for (ItemStack current : remainingOriginal) {
            if (current.isEmpty()) {
                continue;
            }
            long inserted = storage.insert(current, current.getCount());
            if (inserted > 0) {
                storageOperations.add(new StorageOperation(current.copyWithCount(1), inserted));
            }
            if (inserted != current.getCount()) {
                rollbackCraftingTransfer(serverPlayer, storageOperations, inventoryWithdrawals);
                return false;
            }
        }

        // グリッド内だけで足りないスロットは、ストレージ優先・プレイヤーインベントリ補完で予約する。
        for (int i = 0; i < desired.size(); i++) {
            ItemStack target = desired.get(i);
            if (target.isEmpty()) {
                continue;
            }
            if (finalStacks[i] != null) {
                continue;
            }
            Optional<ItemResourceKey> key = ItemResourceKey.of(target);
            long extracted = key.map(value -> storage.extract(value, 1L)).orElse(0L);
            if (extracted == 1L) {
                storageOperations.add(new StorageOperation(target.copyWithCount(1), -1L));
                finalStacks[i] = target.copyWithCount(1);
                continue;
            }
            int inventorySlot = withdrawOneFromInventory(serverPlayer.getInventory(), target);
            if (inventorySlot >= 0) {
                inventoryWithdrawals.add(new InventoryWithdrawal(inventorySlot, target.copyWithCount(1)));
                finalStacks[i] = target.copyWithCount(1);
                continue;
            }
            rollbackCraftingTransfer(serverPlayer, storageOperations, inventoryWithdrawals);
            return false;
        }

        for (int i = 0; i < finalStacks.length; i++) {
            craftMatrix.setItem(i, finalStacks[i] == null ? ItemStack.EMPTY : finalStacks[i]);
        }
        refreshCraftingResult();
        return true;
    }

    private static ItemStack withdrawOneFromFinalStacks(ItemStack[] stacks, ItemStack template) {
        for (ItemStack stack : stacks) {
            if (stack != null && stack.getCount() > 1
                    && ItemStack.isSameItemSameComponents(stack, template)) {
                stack.shrink(1);
                return template.copyWithCount(1);
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack withdrawOneFromStacks(List<ItemStack> stacks, ItemStack template) {
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            if (stack.isEmpty() || !ItemStack.isSameItemSameComponents(stack, template)) {
                continue;
            }
            stack.shrink(1);
            if (stack.isEmpty()) {
                stacks.set(i, ItemStack.EMPTY);
            }
            return template.copyWithCount(1);
        }
        return ItemStack.EMPTY;
    }

    /** グリッド全量をストレージへ返す。全量が入らない場合は挿入分をrollbackして何も変更しない。 */
    public boolean handleClearCrafting() {
        if (storage == null || storage.isLoadFailed() || !craftOpen) {
            return false;
        }
        List<StorageOperation> operations = new ArrayList<>();
        for (int i = 0; i < craftMatrix.getContainerSize(); i++) {
            ItemStack stack = craftMatrix.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            long inserted = storage.insert(stack, stack.getCount());
            if (inserted > 0) {
                operations.add(new StorageOperation(stack.copyWithCount(1), inserted));
            }
            if (inserted != stack.getCount()) {
                rollbackStorageOperations(operations);
                return false;
            }
        }
        craftMatrix.clearContent();
        craftResult.setItem(0, ItemStack.EMPTY);
        return true;
    }

    private static int withdrawOneFromInventory(Inventory inventory, ItemStack template) {
        for (int slot = 0; slot < CRAFT_MATRIX_START; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty() || !ItemStack.isSameItemSameComponents(stack, template)) {
                continue;
            }
            stack.shrink(1);
            if (stack.isEmpty()) {
                inventory.setItem(slot, ItemStack.EMPTY);
            }
            return slot;
        }
        return -1;
    }

    private void rollbackCraftingTransfer(ServerPlayer serverPlayer, List<StorageOperation> storageOperations,
                                          List<InventoryWithdrawal> inventoryWithdrawals) {
        for (int i = inventoryWithdrawals.size() - 1; i >= 0; i--) {
            InventoryWithdrawal withdrawal = inventoryWithdrawals.get(i);
            ItemStack current = serverPlayer.getInventory().getItem(withdrawal.slot());
            if (current.isEmpty()) {
                serverPlayer.getInventory().setItem(withdrawal.slot(), withdrawal.template().copyWithCount(1));
            } else {
                current.grow(1);
            }
        }
        rollbackStorageOperations(storageOperations);
    }

    private void rollbackStorageOperations(List<StorageOperation> operations) {
        for (int i = operations.size() - 1; i >= 0; i--) {
            StorageOperation operation = operations.get(i);
            if (operation.delta() > 0) {
                ItemResourceKey.of(operation.template())
                        .ifPresent(key -> storage.extract(key, operation.delta()));
            } else if (operation.delta() < 0) {
                storage.insert(operation.template(), -operation.delta());
            }
        }
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

    /** 消費前と同じ材料をストレージから1個補充する。クラフト残りがあるスロットは置換しない。 */
    private void refillCraftMatrixFromStorage(List<ItemStack> consumedTemplates, NonNullList<ItemStack> remainders) {
        if (storage == null || storage.isLoadFailed()) {
            return;
        }
        for (int i = 0; i < consumedTemplates.size(); i++) {
            ItemStack template = consumedTemplates.get(i);
            if (template.isEmpty() || !remainders.get(i).isEmpty()) {
                continue;
            }
            ItemStack current = craftMatrix.getItem(i);
            if (!current.isEmpty()
                    && (!ItemStack.isSameItemSameComponents(current, template)
                    || current.getCount() >= current.getMaxStackSize())) {
                continue;
            }
            Optional<ItemResourceKey> key = ItemResourceKey.of(template);
            if (key.isEmpty() || storage.extract(key.get(), 1L) != 1L) {
                continue;
            }
            if (current.isEmpty()) {
                craftMatrix.setItem(i, template.copyWithCount(1));
            } else {
                current.grow(1);
                craftMatrix.setChanged();
            }
        }
    }

    /** バケツ等のクラフト残りを全量格納できる時だけストレージへ移し、連続補充を可能にする。 */
    private boolean storeCraftingRemainder(ItemStack remainder) {
        if (storage == null || storage.isLoadFailed() || remainder.isEmpty()
                || storage.simulateInsert(remainder, remainder.getCount()) != remainder.getCount()) {
            return false;
        }
        return storage.insert(remainder, remainder.getCount()) == remainder.getCount();
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

    /** バニラResultSlot契約を維持しつつ、クラフト残りの格納とストレージ補充を追加する。 */
    private class CraftResultSlot extends ResultSlot {
        CraftResultSlot(int x, int y) {
            super(IdeaStorageMenu.this.player, craftMatrix, craftResult, 0, x, y);
        }

        @Override
        public boolean isActive() {
            return craftOpen;
        }

        @Override
        public void onTake(@NotNull Player player, @NotNull ItemStack stack) {
            checkTakeAchievements(stack);
            CraftingInput.Positioned positioned = craftMatrix.asPositionedCraftInput();
            CraftingInput input = positioned.input();
            int left = positioned.left();
            int top = positioned.top();
            net.neoforged.neoforge.common.CommonHooks.setCraftingPlayer(player);
            NonNullList<ItemStack> remaining;
            try {
                remaining = player.level().getRecipeManager()
                        .getRemainingItemsFor(RecipeType.CRAFTING, input, player.level());
            } finally {
                net.neoforged.neoforge.common.CommonHooks.setCraftingPlayer(null);
            }

            List<ItemStack> consumedTemplates = new ArrayList<>(craftMatrix.getContainerSize());
            NonNullList<ItemStack> unhandledRemainders =
                    NonNullList.withSize(craftMatrix.getContainerSize(), ItemStack.EMPTY);
            for (int i = 0; i < craftMatrix.getContainerSize(); i++) {
                consumedTemplates.add(ItemStack.EMPTY);
            }

            for (int row = 0; row < input.height(); row++) {
                for (int column = 0; column < input.width(); column++) {
                    int craftSlot = column + left + (row + top) * craftMatrix.getWidth();
                    ItemStack slotStack = craftMatrix.getItem(craftSlot);
                    if (!slotStack.isEmpty()) {
                        consumedTemplates.set(craftSlot, slotStack.copyWithCount(1));
                        craftMatrix.removeItem(craftSlot, 1);
                        slotStack = craftMatrix.getItem(craftSlot);
                    }

                    ItemStack remainder = remaining.get(column + row * input.width());
                    if (remainder.isEmpty() || storeCraftingRemainder(remainder)) {
                        continue;
                    }
                    unhandledRemainders.set(craftSlot, remainder.copy());
                    if (slotStack.isEmpty()) {
                        craftMatrix.setItem(craftSlot, remainder);
                    } else if (ItemStack.isSameItemSameComponents(slotStack, remainder)) {
                        remainder.grow(slotStack.getCount());
                        craftMatrix.setItem(craftSlot, remainder);
                    } else if (!player.getInventory().add(remainder)) {
                        player.drop(remainder, false);
                    }
                }
            }
            refillCraftMatrixFromStorage(consumedTemplates, unhandledRemainders);
        }
    }

    private record StorageOperation(ItemStack template, long delta) {
    }

    private record InventoryWithdrawal(int slot, ItemStack template) {
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
