package com.moratan251.psitweaks.common.menu;

import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.network.MessageIdeaStorageExtract;
import com.moratan251.psitweaks.common.network.MessageIdeaStorageFillCrafting;
import com.moratan251.psitweaks.common.network.MessageIdeaStorageSync;
import com.moratan251.psitweaks.common.network.MessageIdeaStorageTransferContents;
import com.moratan251.psitweaks.common.storage.idea.FluidResourceKey;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageChemicalTransfer;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageContainerTransferBatch;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageDefaults;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageService;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageTransferDirection;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageWithdrawal;
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
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
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
    private long lastSyncedMaxEnergy = -1L;
    private int lastSyncedMaxItemTypes = -1;
    private int lastSyncedMaxFluidTypes = -1;
    private int lastSyncedMaxChemicalTypes = -1;
    private int craftingMutationDepth;
    private boolean craftingResultDirty;
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
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != craftResult && (!isCraftSlot(slot) || craftOpen)
                && super.canTakeItemForPickAll(stack, slot);
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

    /** 結果スロットのshiftクリックをサーバー内で完成品約1スタック分まとめて処理する。 */
    private ItemStack quickMoveCraftResult(Player player, Slot resultSlot) {
        if (!(player instanceof ServerPlayer)) {
            return ItemStack.EMPTY;
        }
        ItemStack expectedResult = resultSlot.getItem().copy();
        if (expectedResult.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int maxCrafts = IdeaStorageCraftingLimits.calculateShiftCraftLimit(
                expectedResult.getCount(), expectedResult.getMaxStackSize());
        PlayerMainInvWrapper inventory = new PlayerMainInvWrapper(player.getInventory());

        beginCraftingMutation();
        try {
            for (int crafted = 0; crafted < maxCrafts; crafted++) {
                ItemStack result = resultSlot.getItem();
                if (!ItemStack.matches(expectedResult, result)) {
                    break;
                }
                ItemStack original = result.copy();
                if (!ItemHandlerHelper.insertItem(inventory, original, true).isEmpty()
                        || !this.moveItemStackTo(result, 0, CRAFT_MATRIX_START, true)) {
                    break;
                }
                resultSlot.onQuickCraft(result, original);
                if (result.isEmpty()) {
                    resultSlot.setByPlayer(ItemStack.EMPTY);
                } else {
                    resultSlot.setChanged();
                }
                if (result.getCount() == original.getCount()) {
                    break;
                }
                resultSlot.onTake(player, result);
                if (craftingResultDirty) {
                    refreshCraftingResult(false);
                }
            }
        } finally {
            endCraftingMutation();
        }
        // バニラのQUICK_MOVEループは使わず、この1回の呼び出しだけで完結させる。
        return ItemStack.EMPTY;
    }

    /** マトリクス変更時の結果はサーバーだけで確定し、クラフト中の途中状態は同期しない。 */
    @Override
    public void slotsChanged(Container container) {
        if (container == craftMatrix) {
            if (craftingMutationDepth > 0) {
                craftingResultDirty = true;
            } else {
                refreshCraftingResult(true);
            }
        } else {
            super.slotsChanged(container);
        }
    }

    private void refreshCraftingResult() {
        refreshCraftingResult(true);
    }

    private void refreshCraftingResult(boolean synchronizeResultSlot) {
        Level level = player.level();
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            craftingResultDirty = false;
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
        craftingResultDirty = false;
        if (synchronizeResultSlot) {
            setRemoteSlot(CRAFT_RESULT_SLOT, result);
            serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(
                    containerId, incrementStateId(), CRAFT_RESULT_SLOT, result));
        }
    }

    private void beginCraftingMutation() {
        craftingMutationDepth++;
    }

    private void endCraftingMutation() {
        if (craftingMutationDepth <= 0) {
            throw new IllegalStateException("Crafting mutation depth underflow");
        }
        craftingMutationDepth--;
        if (craftingMutationDepth == 0 && craftingResultDirty) {
            // ServerboundContainerClick処理後の標準broadcastChangesに最終差分だけ同期させる。
            refreshCraftingResult(false);
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
        if (player instanceof ServerPlayer serverPlayer && storage != null
                && (storage.getVersion() != lastSyncedVersion
                || storage.maxItemTypes() != lastSyncedMaxItemTypes
                || storage.maxEnergy() != lastSyncedMaxEnergy
                || storage.maxFluidTypes() != lastSyncedMaxFluidTypes
                || storage.maxChemicalTypes() != lastSyncedMaxChemicalTypes)) {
            lastSyncedVersion = storage.getVersion();
            lastSyncedMaxItemTypes = storage.maxItemTypes();
            lastSyncedMaxEnergy = storage.maxEnergy();
            lastSyncedMaxFluidTypes = storage.maxFluidTypes();
            lastSyncedMaxChemicalTypes = storage.maxChemicalTypes();
            PacketDistributor.sendToPlayer(serverPlayer, createSyncMessage());
        }
    }

    private MessageIdeaStorageSync createSyncMessage() {
        List<MessageIdeaStorageSync.Entry> entries = new ArrayList<>();
        List<MessageIdeaStorageSync.FluidEntry> fluidEntries = new ArrayList<>();
        List<MessageIdeaStorageSync.ChemicalEntry> chemicalEntries = new ArrayList<>();
        if (storage != null) {
            for (var entry : storage.itemEntries()) {
                entries.add(new MessageIdeaStorageSync.Entry(entry.getKey().template(), entry.getValue()));
            }
            for (var entry : storage.fluidEntries()) {
                fluidEntries.add(new MessageIdeaStorageSync.FluidEntry(entry.getKey().template(), entry.getValue()));
            }
            for (var entry : storage.chemicalEntries()) {
                chemicalEntries.add(new MessageIdeaStorageSync.ChemicalEntry(entry.getKey(), entry.getValue()));
            }
        }
        return new MessageIdeaStorageSync(entries, fluidEntries, chemicalEntries,
                storage == null ? 0 : storage.maxItemTypes(),
                storage == null ? 0 : storage.maxFluidTypes(),
                storage == null ? 0 : storage.maxChemicalTypes(),
                storage == null ? 0 : storage.energy(),
                storage == null ? 0 : storage.maxEnergy(),
                storage != null && storage.isLoadFailed());
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
            IdeaStorageWithdrawal<ItemResourceKey> withdrawal = key
                    .map(value -> storage.withdrawItem(value, 1L)).orElse(null);
            if (withdrawal != null && withdrawal.amount() == 1L) {
                storageOperations.add(new StorageOperation(target.copyWithCount(1), -1L, withdrawal));
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
                operation.withdrawal().restore(-operation.delta());
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
        IdeaStorageWithdrawal<ItemResourceKey> withdrawal = storage.withdrawItem(key, want);
        long extracted = withdrawal.amount();
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
            withdrawal.restore(leftover);
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
        depositCarried(false);
    }

    public void depositCarried(boolean single) {
        if (storage == null || storage.isLoadFailed()) return;
        ItemStack carried = getCarried();
        if (carried.isEmpty()) return;
        long moved = storage.insert(carried, single ? 1 : carried.getCount());
        if (moved <= 0) {
            return;
        }
        carried.shrink((int) moved);
        this.setCarried(carried.isEmpty() ? ItemStack.EMPTY : carried);
    }

    /** 保存済み空バケツ1個と対象Fluid 1 Bを原子的に消費し、完成バケツをカーソルへ置く。 */
    public void handleFillStoredBucket(FluidStack targetTemplate) {
        if (storage == null || storage.isLoadFailed() || !this.getCarried().isEmpty()
                || targetTemplate == null || targetTemplate.isEmpty()) {
            return;
        }

        ItemStack emptyBucket = new ItemStack(Items.BUCKET);
        Optional<ItemResourceKey> bucketKey = ItemResourceKey.of(emptyBucket);
        Optional<FluidResourceKey> targetKey = FluidResourceKey.of(targetTemplate);
        if (bucketKey.isEmpty() || targetKey.isEmpty()
                || storage.simulateExtract(bucketKey.get(), 1L) != 1L
                || storage.simulateExtractFluid(targetKey.get(), IdeaStorageDefaults.RAW_UNITS_PER_BUCKET)
                != IdeaStorageDefaults.RAW_UNITS_PER_BUCKET) {
            return;
        }

        FluidContainerTransfer transfer = planFluidContainerTransfer(
                emptyBucket, targetTemplate, IdeaStorageTransferDirection.INTO_CONTAINER);
        if (transfer == null || transfer.intoStorage()
                || transfer.amount() != IdeaStorageDefaults.RAW_UNITS_PER_BUCKET
                || transfer.resultContainer().isEmpty()
                || transfer.resultContainer().is(Items.BUCKET)) {
            return;
        }

        if (storage.extractItemAndFluid(bucketKey.get(), 1L, transfer.key(), transfer.amount())) {
            this.setCarried(transfer.resultContainer().copyWithCount(1));
        }
    }

    /** 右クリックされた容器とFluid/Chemicalカテゴリ間をsimulation先行で転送する。 */
    public void handleTransferContents(ServerPlayer serverPlayer, int targetKind, FluidStack fluidTemplate,
                                       @Nullable ResourceLocation chemicalId, boolean bulk) {
        if (storage == null || storage.isLoadFailed()) {
            return;
        }
        ItemStack carried = this.getCarried();
        if (carried.isEmpty()) {
            return;
        }
        // Empty buckets are ordinary items unless a fluid entry is explicitly targeted for filling.
        if (carried.is(Items.BUCKET) && targetKind != MessageIdeaStorageTransferContents.TARGET_FLUID) {
            depositCarried(true);
            return;
        }
        if (Capabilities.FluidHandler.ITEM.getCapability(carried, null) == null
                && !MekanismCompat.isIdeaStorageChemicalContainer(carried)) {
            depositCarried(true);
            return;
        }
        if (targetKind == MessageIdeaStorageTransferContents.TARGET_FLUID) {
            FluidContainerTransfer transfer = planFluidContainerTransfer(
                    carried, fluidTemplate, IdeaStorageTransferDirection.EITHER);
            if (transfer != null && commitFluidContainerTransfer(serverPlayer, carried, transfer)) {
                continueFluidContainerTransfer(serverPlayer, fluidTemplate, transfer, bulk);
                return;
            }
            IdeaStorageChemicalTransfer chemicalTransfer = MekanismCompat.planIdeaStorageChemicalTransfer(
                    storage, carried, null, IdeaStorageTransferDirection.EITHER);
            if (chemicalTransfer != null
                    && commitChemicalContainerTransfer(serverPlayer, carried, chemicalTransfer)) {
                continueChemicalContainerTransfer(serverPlayer, null, chemicalTransfer, bulk);
            }
            return;
        }
        if (targetKind == MessageIdeaStorageTransferContents.TARGET_CHEMICAL && chemicalId != null) {
            IdeaStorageChemicalTransfer transfer =
                    MekanismCompat.planIdeaStorageChemicalTransfer(
                            storage, carried, chemicalId, IdeaStorageTransferDirection.EITHER);
            if (transfer != null && commitChemicalContainerTransfer(serverPlayer, carried, transfer)) {
                continueChemicalContainerTransfer(serverPlayer, chemicalId, transfer, bulk);
                return;
            }
            FluidContainerTransfer fluidTransfer =
                    planFluidContainerTransfer(carried, FluidStack.EMPTY, IdeaStorageTransferDirection.EITHER);
            if (fluidTransfer != null && commitFluidContainerTransfer(serverPlayer, carried, fluidTransfer)) {
                continueFluidContainerTransfer(serverPlayer, FluidStack.EMPTY, fluidTransfer, bulk);
            }
            return;
        }

        FluidContainerTransfer fluidTransfer = planFluidContainerTransfer(
                carried, FluidStack.EMPTY, IdeaStorageTransferDirection.EITHER);
        if (fluidTransfer != null && commitFluidContainerTransfer(serverPlayer, carried, fluidTransfer)) {
            continueFluidContainerTransfer(serverPlayer, FluidStack.EMPTY, fluidTransfer, bulk);
            return;
        }
        IdeaStorageChemicalTransfer chemicalTransfer =
                MekanismCompat.planIdeaStorageChemicalTransfer(
                        storage, carried, null, IdeaStorageTransferDirection.EITHER);
        if (chemicalTransfer != null
                && commitChemicalContainerTransfer(serverPlayer, carried, chemicalTransfer)) {
            continueChemicalContainerTransfer(serverPlayer, null, chemicalTransfer, bulk);
        }
    }

    private void continueFluidContainerTransfer(ServerPlayer player, FluidStack targetTemplate,
                                                FluidContainerTransfer firstTransfer, boolean bulk) {
        IdeaStorageContainerTransferBatch.continueAfterFirst(bulk, () -> {
            ItemStack carried = this.getCarried();
            if (carried.isEmpty()) {
                return false;
            }
            FluidContainerTransfer next = planFluidContainerTransfer(
                    carried, targetTemplate, IdeaStorageTransferDirection.fixed(firstTransfer.intoStorage()));
            return commitFluidContainerTransfer(player, carried, next);
        });
    }

    private void continueChemicalContainerTransfer(ServerPlayer player,
                                                   @Nullable ResourceLocation targetChemicalId,
                                                   IdeaStorageChemicalTransfer firstTransfer, boolean bulk) {
        IdeaStorageContainerTransferBatch.continueAfterFirst(bulk, () -> {
            ItemStack carried = this.getCarried();
            if (carried.isEmpty()) {
                return false;
            }
            IdeaStorageChemicalTransfer next = MekanismCompat.planIdeaStorageChemicalTransfer(
                    storage, carried, targetChemicalId,
                    IdeaStorageTransferDirection.fixed(firstTransfer.intoStorage()));
            return commitChemicalContainerTransfer(player, carried, next);
        });
    }

    @Nullable
    private FluidContainerTransfer planFluidContainerTransfer(ItemStack container, FluidStack targetTemplate,
                                                              IdeaStorageTransferDirection direction) {
        ItemStack working = container.copyWithCount(1);
        IFluidHandlerItem handler = Capabilities.FluidHandler.ITEM.getCapability(working, null);
        if (handler == null) {
            return null;
        }

        Optional<FluidResourceKey> targetKey = FluidResourceKey.of(targetTemplate);
        if (direction.allowsIntoContainer() && targetKey.isPresent()) {
            long available = storage.simulateExtractFluid(targetKey.get(), Integer.MAX_VALUE);
            if (available > 0) {
                FluidStack offered = targetTemplate.copyWithAmount((int) available);
                int accepted = handler.fill(offered, IFluidHandler.FluidAction.SIMULATE);
                if (accepted > 0) {
                    int moved = handler.fill(offered.copyWithAmount(accepted), IFluidHandler.FluidAction.EXECUTE);
                    if (moved > 0) {
                        return new FluidContainerTransfer(handler.getContainer(), targetKey.get(), moved, false);
                    }
                }
            }
        }

        if (!direction.allowsIntoStorage()) {
            return null;
        }
        for (int tank = 0; tank < handler.getTanks(); tank++) {
            FluidStack contained = handler.getFluidInTank(tank);
            if (contained.isEmpty()) {
                continue;
            }
            FluidStack simulated = handler.drain(contained, IFluidHandler.FluidAction.SIMULATE);
            Optional<FluidResourceKey> key = FluidResourceKey.of(simulated);
            if (key.isEmpty()) {
                continue;
            }
            long accepted = storage.simulateInsertFluid(simulated, simulated.getAmount());
            if (accepted <= 0) {
                continue;
            }
            FluidStack drained = handler.drain(simulated.copyWithAmount((int) accepted),
                    IFluidHandler.FluidAction.EXECUTE);
            if (!drained.isEmpty()) {
                return new FluidContainerTransfer(handler.getContainer(), key.get(), drained.getAmount(), true);
            }
        }
        return null;
    }

    private boolean commitFluidContainerTransfer(ServerPlayer player, ItemStack carried,
                                                  @Nullable FluidContainerTransfer transfer) {
        if (transfer == null || transfer.amount() <= 0
                || !canReplaceOneCarriedContainer(player, carried, transfer.resultContainer())) {
            return false;
        }
        long moved = transfer.intoStorage()
                ? storage.insertFluid(transfer.key().template(), transfer.amount())
                : storage.extractFluid(transfer.key(), transfer.amount());
        if (moved != transfer.amount()) {
            if (moved > 0) {
                if (transfer.intoStorage()) {
                    storage.extractFluid(transfer.key(), moved);
                } else {
                    storage.insertFluid(transfer.key().template(), moved);
                }
            }
            return false;
        }
        replaceOneCarriedContainer(player, carried, transfer.resultContainer());
        return true;
    }

    private boolean commitChemicalContainerTransfer(ServerPlayer player, ItemStack carried,
                                                     @Nullable IdeaStorageChemicalTransfer transfer) {
        if (transfer == null || transfer.amount() <= 0
                || !canReplaceOneCarriedContainer(player, carried, transfer.resultContainer())) {
            return false;
        }
        long moved = transfer.intoStorage()
                ? storage.insertChemical(transfer.chemicalId(), transfer.amount())
                : storage.extractChemical(transfer.chemicalId(), transfer.amount());
        if (moved != transfer.amount()) {
            if (moved > 0) {
                if (transfer.intoStorage()) {
                    storage.extractChemical(transfer.chemicalId(), moved);
                } else {
                    storage.insertChemical(transfer.chemicalId(), moved);
                }
            }
            return false;
        }
        replaceOneCarriedContainer(player, carried, transfer.resultContainer());
        return true;
    }

    private static boolean canReplaceOneCarriedContainer(Player player, ItemStack carried, ItemStack result) {
        if (carried.getCount() <= 1 || result.isEmpty()) {
            return true;
        }
        PlayerMainInvWrapper inventory = new PlayerMainInvWrapper(player.getInventory());
        return ItemHandlerHelper.insertItem(inventory, result, true).isEmpty();
    }

    private void replaceOneCarriedContainer(Player player, ItemStack carried, ItemStack result) {
        if (carried.getCount() <= 1) {
            this.setCarried(result.isEmpty() ? ItemStack.EMPTY : result);
            return;
        }
        if (!result.isEmpty()) {
            PlayerMainInvWrapper inventory = new PlayerMainInvWrapper(player.getInventory());
            ItemHandlerHelper.insertItem(inventory, result, false);
        }
        carried.shrink(1);
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
            beginCraftingMutation();
            try {
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
            } finally {
                endCraftingMutation();
            }
        }
    }

    private record StorageOperation(ItemStack template, long delta,
                                    @Nullable IdeaStorageWithdrawal<ItemResourceKey> withdrawal) {
        private StorageOperation(ItemStack template, long delta) {
            this(template, delta, null);
        }
    }

    private record InventoryWithdrawal(int slot, ItemStack template) {
    }

    private record FluidContainerTransfer(ItemStack resultContainer, FluidResourceKey key,
                                          long amount, boolean intoStorage) {
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
