package com.moratan251.psitweaks.common.tile.machine;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import com.moratan251.psitweaks.common.caster.AutonomousCasterHost;
import com.moratan251.psitweaks.common.caster.AutonomousCasterPlayer;
import com.moratan251.psitweaks.common.caster.AutonomousCasterRegistry;
import com.moratan251.psitweaks.common.caster.AutonomousCasterSettings;
import com.moratan251.psitweaks.common.caster.CasterLog;
import com.moratan251.psitweaks.common.caster.VirtualCad;
import com.moratan251.psitweaks.common.chemicals.PsitweaksChemicals;
import com.moratan251.psitweaks.common.items.PsitweaksDataComponents;
import com.moratan251.psitweaks.common.network.MessageAutonomousCasterAction;
import com.moratan251.psitweaks.common.network.MessageAutonomousCasterLog;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.attachments.containers.item.AttachedItems;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.slot.chemical.ChemicalInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.tile.interfaces.IHasVisualization;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.util.MekanismUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import vazkii.psi.api.cad.ICAD;
import vazkii.psi.api.spell.ISpellAcceptor;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.common.attribute.base.ModAttributes;
import vazkii.psi.common.core.handler.PlayerData;
import vazkii.psi.common.core.handler.PlayerDataHandler;
import vazkii.psi.common.item.ItemCAD;
import vazkii.psi.common.item.ItemLoopcastSpellBullet;

/**
 * 自律詠唱機。レッドストーン入力をトリガーに、選択した術式弾を専用 Caster と内蔵仮想 CAD で詠唱する。
 * <p>
 * 詠唱コストは気化サイオニックエコーから回復する内部 Psi だけで払う。機械の36スロットは Caster の通常インベントリ
 * （ホットバーと残りの27枠）へ写像し、術式による消費・変化を毎 tick 書き戻す。Caster 宛てのメッセージは作動ログへ集める。
 */
public class AutonomousCasterBlockEntity extends TileEntityConfigurableMachine implements AutonomousCasterHost, IHasVisualization {
    private static final Logger LOGGER = LogUtils.getLogger();

    /** 術式弾スロットの数。内蔵 CAD の装弾数と同じで、選択中のスロットは内蔵 CAD の選択ソケットと同期する。 */
    public static final int BULLET_SLOTS = VirtualCad.SOCKETS;
    /** アイテムスロットの数。Caster の通常インベントリと同じで、番号も同じ（0〜8 がホットバー）。 */
    public static final int ITEM_SLOTS = Inventory.INVENTORY_SIZE;
    /** 手に持つスロットとして選べるホットバーの数。 */
    public static final int HOTBAR_SLOTS = Inventory.getSelectionSize();
    public static final int MIN_YAW = -180;
    public static final int MAX_YAW = 180;
    public static final int MIN_PITCH = -90;
    public static final int MAX_PITCH = 90;

    // GUI 上のスロット位置（アイテム描画位置）
    /** 術式弾スロットは左上に横6個×2段で並べる。 */
    public static final int BULLET_COLUMNS = 6;
    public static final int BULLET_X = 8;
    public static final int BULLET_Y = 18;
    public static final int ITEM_X = 8;
    /** ホットバー以外の27スロット（3段）の上端。 */
    public static final int ITEM_Y = 58;
    /** ホットバーの段。プレイヤーのインベントリと同じく、上の3段から 4 px 離す。 */
    public static final int HOTBAR_Y = 116;
    /** エコー補充スロットと着色器スロットは、右上のゲージの右に縦に並べる。 */
    public static final int ECHO_SLOT_X = 152;
    public static final int ECHO_SLOT_Y = 18;
    public static final int DYE_SLOT_X = 152;
    public static final int DYE_SLOT_Y = 36;

    /** 詠唱に成功した後、稼働表示を続ける tick 数。 */
    private static final int ACTIVE_TICKS_AFTER_CAST = 10;
    /** Caster の目の位置。ブロック中心から前面方向へずらす距離（前面から 0.25 ブロック外）。 */
    private static final double EYE_FORWARD_OFFSET = 0.75D;

    private static final String TAG_PSI = "Psi";
    private static final String TAG_SELECTED_BULLET = "SelectedBullet";
    private static final String TAG_SELECTED_ITEM = "SelectedItem";
    private static final String TAG_TRIGGER_MODE = "TriggerMode";
    private static final String TAG_CAST_INTERVAL = "CastInterval";
    private static final String TAG_YAW = "YawOffset";
    private static final String TAG_PITCH = "Pitch";
    private static final String TAG_LOOP_COUNTER = "LoopCounter";
    private static final String TAG_LOOP_SPELLS = "LoopSpells";
    private static final String TAG_SIGNAL = "Signal";
    private static final String TAG_COOLDOWN = "Cooldown";
    private static final String TAG_CASTER_ID = "CasterId";
    private static final String TAG_VIRTUAL_CAD = "VirtualCad";
    private static final String TAG_LOG = "Log";
    private static final String TAG_LAST_OUTCOME = "LastOutcome";
    private static final String TAG_LAST_FAILURE = "LastFailure";
    private static final String TAG_OWNER_LANGUAGE = "OwnerLanguage";
    private static final String TAG_OWNER_NAME = "OwnerName";
    private static final String TAG_OWNER_NAME_OF = "OwnerNameOf";
    private static final String TAG_BULLET_SLOTS = "BulletSlots";
    /** 保存時の術式弾スロットの数を記録していなかった頃（術式弾が8個だった頃）の数。 */
    private static final int UNRECORDED_BULLET_SLOTS = 8;

    private static final String ERROR_PREFIX = "psitweaks.autonomous_caster.error.";

    public enum TriggerMode {
        /** 立ち上がりエッジで1回詠唱する。 */
        PULSE,
        /** 信号が続く間、詠唱間隔ごとに詠唱する。 */
        LEVEL;

        public static TriggerMode byIndex(int index) {
            TriggerMode[] values = values();
            return index >= 0 && index < values.length ? values[index] : PULSE;
        }
    }

    public enum Outcome {
        NONE,
        SUCCESS,
        FAILURE;

        public static Outcome byIndex(int index) {
            Outcome[] values = values();
            return index >= 0 && index < values.length ? values[index] : NONE;
        }
    }

    // getInitial* はスーパークラスのコンストラクタから呼ばれるため、そこで代入するフィールドに初期化子を付けない。
    private List<IInventorySlot> bulletSlots;
    private IInventorySlot dyeSlot;
    private List<IInventorySlot> itemSlots;
    private ChemicalInventorySlot echoSlot;
    private IChemicalTank echoTank;

    private int psi;
    private int selectedBullet;
    private int selectedItemSlot;
    private TriggerMode triggerMode = TriggerMode.PULSE;
    private int castInterval = AutonomousCasterSettings.defaultCastInterval();
    private int yawOffset;
    private int pitch;
    /** 機械に1つのループ回数。詠唱が成立するたびに増え、「取得子: ループ回数」に渡す。 */
    private int loopCounter;
    private final UUID[] loopSpellIds = new UUID[BULLET_SLOTS];
    private boolean signal;
    private int cooldown;
    @Nullable
    private UUID casterId;
    private ItemStack virtualCad = ItemStack.EMPTY;
    private final CasterLog log = new CasterLog(AutonomousCasterSettings::logCapacity, AutonomousCasterSettings::maxLogMessageLength);
    private Outcome lastOutcome = Outcome.NONE;
    private long lastFailureSequence;
    @Nullable
    private String ownerLanguage;
    /** 最後に見えた所有者の名前と、その時の所有者。所有者のチームを引くのに使う。 */
    @Nullable
    private String ownerName;
    @Nullable
    private UUID ownerNameOf;

    // 保存しない状態
    private boolean pendingPulse;
    private boolean signalChecked;
    private boolean virtualCadDirty = true;
    private boolean bulletsDirty = true;
    private boolean manualCastRequested;
    /** 今回の詠唱で Trick: Break Loop がループ回数を 0 に戻した。 */
    private boolean loopBroken;
    @Nullable
    private AutonomousCasterPlayer caster;
    private final ItemStack[] mirrorSnapshot = emptyStacks(ITEM_SLOTS);
    private int activeTicks;
    private int recoveryPerTick;
    /** 最後に Caster の Psi へ書いた値。休眠の後の返却など、詠唱の外で増減した分を機械に足すのに使う。 */
    private int casterPsiWritten = -1;
    private final Map<UUID, Long> viewerRevisions = new HashMap<>();

    // クライアントへ同期する値（サーバーのコンフィグを正とする）
    private int clientPsiCapacity = AutonomousCasterSettings.psiCapacity();
    private int clientMinCastInterval = AutonomousCasterSettings.minCastInterval();
    @Nullable
    private CasterLog clientLog;
    /** GUI の左のタブで切り替える、視線の表示。クライアントだけの状態で保存しない。 */
    private boolean clientRendering;

    public AutonomousCasterBlockEntity(BlockPos pos, BlockState state) {
        super(PsitweaksMekanismBlocks.AUTONOMOUS_CASTER, pos, state);

        ConfigInfo itemConfig = configComponent.getConfig(TransmissionType.ITEM);
        if (itemConfig != null) {
            itemConfig.addSlotInfo(DataType.INPUT, new InventorySlotInfo(true, false, itemSlots));
            itemConfig.addSlotInfo(DataType.OUTPUT, new InventorySlotInfo(false, true, itemSlots));
            itemConfig.addSlotInfo(DataType.INPUT_OUTPUT, new InventorySlotInfo(true, true, itemSlots));
            itemConfig.addSlotInfo(DataType.EXTRA, new InventorySlotInfo(true, true, List.of(echoSlot)));
            // 術式弾スロットは搬入(2)の面からだけ搬入でき、搬出はしない。
            itemConfig.addSlotInfo(DataType.INPUT_2, new InventorySlotInfo(true, false, bulletSlots));
        }
        configComponent.setupInputConfig(TransmissionType.CHEMICAL, echoTank);
        ejectorComponent = new TileComponentEjector(this).setOutputData(configComponent, TransmissionType.ITEM);
    }

    @NotNull
    @Override
    public IChemicalTankHolder getInitialChemicalTanks(IContentsListener listener) {
        ChemicalTankHelper builder = ChemicalTankHelper.forSideWithConfig(this);
        echoTank = builder.addTank(BasicChemicalTank.inputModern(AutonomousCasterSettings.echoTankCapacity(),
                AutonomousCasterBlockEntity::isPsionicEcho, listener));
        return builder.build();
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this);

        IContentsListener bulletListener = () -> {
            listener.onContentsChanged();
            bulletsDirty = true;
        };
        List<IInventorySlot> bullets = new ArrayList<>(BULLET_SLOTS);
        for (int i = 0; i < BULLET_SLOTS; i++) {
            bullets.add(builder.addSlot(BasicInventorySlot.at(AutonomousCasterBlockEntity::isBulletCandidate, bulletListener,
                    bulletSlotX(i), bulletSlotY(i), 1)));
        }
        bulletSlots = List.copyOf(bullets);

        IContentsListener dyeListener = () -> {
            listener.onContentsChanged();
            virtualCadDirty = true;
        };
        dyeSlot = builder.addSlot(BasicInventorySlot.at(stack -> VirtualCad.acceptsDye(registriesOrNull(), stack),
                dyeListener, DYE_SLOT_X, DYE_SLOT_Y, 1));

        // 9スロットだった頃の機械の保存データと番号が合うよう、ホットバー・エコー補充スロット・残りの27スロットの順に足す。
        List<IInventorySlot> items = new ArrayList<>(ITEM_SLOTS);
        for (int i = 0; i < HOTBAR_SLOTS; i++) {
            items.add(builder.addSlot(BasicInventorySlot.at(AutonomousCasterBlockEntity::isMirrorableItem, listener,
                    ITEM_X + i * 18, HOTBAR_Y)));
        }
        echoSlot = builder.addSlot(ChemicalInventorySlot.fill(echoTank, listener, ECHO_SLOT_X, ECHO_SLOT_Y));
        for (int i = HOTBAR_SLOTS; i < ITEM_SLOTS; i++) {
            int row = i / HOTBAR_SLOTS - 1;
            int column = i % HOTBAR_SLOTS;
            items.add(builder.addSlot(BasicInventorySlot.at(AutonomousCasterBlockEntity::isMirrorableItem, listener,
                    ITEM_X + column * 18, ITEM_Y + row * 18)));
        }
        itemSlots = List.copyOf(items);
        return builder.build();
    }

    // ---- tick ----

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        if (!signalChecked) {
            // 読み込み直後の信号は記録だけして、チャンク読み込みで詠唱しない。
            refreshSignal(false);
            signalChecked = true;
        }
        echoSlot.fillTank();
        if (bulletsDirty) {
            refreshBulletState();
        }
        if (virtualCadDirty) {
            refreshVirtualCad();
        }
        ensureCaster();
        adoptSelectedSlot();
        if (virtualCad.getItem() instanceof ICAD cad) {
            cad.incrementTime(virtualCad);
            markForSave();
        }
        reconcileMirror();
        syncCasterPsi();
        if (gameTime() % 20 == 0) {
            refreshOwnerLanguage();
        }

        recoveryPerTick = recoverPsi();
        if (cooldown > 0) {
            cooldown--;
        }
        if (activeTicks > 0) {
            activeTicks--;
        }

        boolean pulse = pendingPulse;
        pendingPulse = false;
        if (cooldown <= 0) {
            boolean trigger = manualCastRequested
                    || (triggerMode == TriggerMode.PULSE && pulse)
                    || (triggerMode == TriggerMode.LEVEL && signal);
            if (trigger) {
                // クールダウン中のパルスは捨てる（レート制限）。手動詠唱だけはクールダウン明けまで待つ。
                manualCastRequested = false;
                performCast();
                cooldown = AutonomousCasterSettings.clampCastInterval(castInterval);
                markForSave();
            }
        }

        syncLogToViewers();
        setActive(recoveryPerTick > 0 || activeTicks > 0);
        return sendUpdatePacket;
    }

    private void refreshSignal(boolean allowPulse) {
        if (level == null) {
            return;
        }
        boolean powered = level.hasNeighborSignal(worldPosition);
        if (powered == signal) {
            return;
        }
        signal = powered;
        if (powered && allowPulse) {
            pendingPulse = true;
        }
        markForSave();
    }

    @Override
    public void onNeighborChange(Block block, BlockPos neighborPos) {
        super.onNeighborChange(block, neighborPos);
        if (!isRemote()) {
            refreshSignal(signalChecked);
        }
    }

    @Override
    public void onAdded() {
        super.onAdded();
        if (!isRemote()) {
            // 設置時に既に入っている信号では詠唱しない。
            refreshSignal(false);
            signalChecked = true;
        }
    }

    /** 選択中のスロットの弾や術式を差し替えたら、ループ回数を 0 に戻す。ほかのスロットの差し替えでは戻さない。 */
    private void refreshBulletState() {
        bulletsDirty = false;
        for (int i = 0; i < BULLET_SLOTS; i++) {
            UUID spellId = spellIdOf(bulletSlots.get(i).getStack());
            if (!Objects.equals(spellId, loopSpellIds[i])) {
                loopSpellIds[i] = spellId;
                if (i == selectedBullet) {
                    loopCounter = 0;
                }
                markForSave();
            }
        }
    }

    @Nullable
    private static UUID spellIdOf(ItemStack stack) {
        if (stack.isEmpty() || !ISpellAcceptor.hasSpell(stack)) {
            return null;
        }
        try {
            Spell spell = ISpellAcceptor.acceptor(stack).getSpell();
            return spell == null ? null : spell.uuid;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private void refreshVirtualCad() {
        virtualCadDirty = false;
        virtualCad = VirtualCad.rebuild(registriesOrNull(), dyeSlot.getStack(), virtualCad);
        VirtualCad.select(virtualCad, selectedBullet);
        AutonomousCasterPlayer current = caster;
        if (current != null) {
            current.getInventory().setItem(AutonomousCasterPlayer.CAD_SLOT, virtualCad);
        }
        markForSave();
    }

    /**
     * 専用 Caster を用意する。Caster は所有者の UUID を名乗り（保護 Mod やキルの帰属のため）、
     * {@link #casterId} は術者を引き直すための機械ごとの内部キーにする。
     */
    private void ensureCaster() {
        AutonomousCasterPlayer current = caster;
        UUID owner = getOwnerUUID();
        if (current != null && current.isAttached() && current.level() == level) {
            if (current.getUUID().equals(owner != null ? owner : current.casterKey())) {
                return;
            }
            // 所有者が後から決まった・変わった時は名乗る UUID を合わせて作り直す。手持ちは先に機械へ戻す。
            reconcileMirror();
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (current != null) {
            current.detach();
        }
        if (casterId == null || AutonomousCasterRegistry.isInUse(casterId)) {
            // 構造物コピーなどで同じキーの機械が並んだ場合も、Caster ごとの状態を分けるため作り直す。
            casterId = UUID.randomUUID();
            markForSave();
        }
        AutonomousCasterPlayer created = new AutonomousCasterPlayer(serverLevel, casterId, owner, this);
        Inventory inventory = created.getInventory();
        for (int i = 0; i < ITEM_SLOTS; i++) {
            ItemStack stack = itemSlots.get(i).getStack();
            inventory.items.set(i, stack.copy());
            mirrorSnapshot[i] = stack.copy();
        }
        inventory.setItem(AutonomousCasterPlayer.CAD_SLOT, virtualCad);
        inventory.selected = selectedItemSlot;
        positionCaster(created);
        caster = created;
        casterPsiWritten = -1;
    }

    /**
     * 術式（Trick: Spin Chamber・Trick: Russian Roulette）が内蔵 CAD の選択ソケットを変えていたら、選択中の術式弾スロットに
     * 取り込む。術式円など後から動く術式の分も拾えるよう、詠唱の後と毎 tick に見る。
     */
    private void adoptSelectedSlot() {
        int selected = VirtualCad.selectedSlot(virtualCad);
        if (selected < 0 || selected == selectedBullet) {
            return;
        }
        if (selected < BULLET_SLOTS) {
            selectedBullet = selected;
            markForSave();
        } else {
            VirtualCad.select(virtualCad, selectedBullet);
        }
    }

    /** 視点を機械の前面に置き、設定された向きを向かせる。 */
    private void positionCaster(AutonomousCasterPlayer target) {
        Vec3 eye = eyePosition();
        float yaw = lookYaw();
        float xRot = lookPitch();
        target.moveTo(eye.x, eye.y - target.getEyeHeight(), eye.z, yaw, xRot);
        target.setYHeadRot(yaw);
        target.yHeadRotO = yaw;
        target.yBodyRot = yaw;
        target.yBodyRotO = yaw;
        target.setDeltaMovement(Vec3.ZERO);
    }

    /** 詠唱主体の目の位置。機械の前面から少し前に出たところ。視線の表示もここから引く。 */
    public Vec3 eyePosition() {
        Direction facing = getDirection();
        return Vec3.atCenterOf(worldPosition).add(
                facing.getStepX() * EYE_FORWARD_OFFSET,
                facing.getStepY() * EYE_FORWARD_OFFSET,
                facing.getStepZ() * EYE_FORWARD_OFFSET);
    }

    /** 詠唱主体の水平の向き（正面からの Yaw を足した角度）。 */
    public float lookYaw() {
        return Mth.wrapDegrees(getDirection().toYRot() + yawOffset);
    }

    public float lookPitch() {
        return Mth.clamp(pitch, MIN_PITCH, MAX_PITCH);
    }

    /**
     * 詠唱主体の視線の向き（長さ1）。Entity#calculateViewVector と同じ式で求め、詠唱主体の getLookAngle と一致させる。
     * Vec3.directionFromRotation は Mth の sin/cos の表引きがずれ、1.0E-4 ほど違う向きになる。
     */
    public Vec3 lookVector() {
        float xRot = lookPitch() * Mth.DEG_TO_RAD;
        float yRot = -lookYaw() * Mth.DEG_TO_RAD;
        float horizontal = Mth.cos(xRot);
        return new Vec3(Mth.sin(yRot) * horizontal, -Mth.sin(xRot), Mth.cos(yRot) * horizontal);
    }

    /** Psi 回復。1 tick に回復1回分を、コンフィグの比率で気化サイオニックエコーを使って回復する。アップグレードは載せられない。 */
    private int recoverPsi() {
        int capacity = AutonomousCasterSettings.psiCapacity();
        if (psi > capacity) {
            psi = capacity;
            markDirtyComparator();
            markForSave();
        }
        long echo = echoTank.getStored();
        int missing = capacity - psi;
        if (missing <= 0 || echo <= 0) {
            return 0;
        }
        int perOperation = AutonomousCasterSettings.psiPerOperation();
        double echoPerPsi = (double) AutonomousCasterSettings.echoPerOperation() / perOperation;
        long byEcho = (long) Math.floor(echo / echoPerPsi + 1.0E-9D);
        int gain = (int) Math.max(0L, Math.min(Math.min(perOperation, missing), byEcho));
        if (gain <= 0) {
            return 0;
        }
        long used = Math.min(echo, Math.max(1L, (long) Math.ceil(gain * echoPerPsi - 1.0E-9D)));
        echoTank.extract(used, Action.EXECUTE, AutomationType.INTERNAL);
        psi += gain;
        markDirtyComparator();
        markForSave();
        return gain;
    }

    /**
     * Caster の Psi を機械に合わせる。前回書いた後に Caster 側で増減した分（休眠の後の返却など）は機械に足す。
     * Psi の返却は Caster の最大 Psi までなので、最大 Psi も機械の容量に合わせる。
     */
    private void syncCasterPsi() {
        AutonomousCasterPlayer current = caster;
        if (current == null) {
            return;
        }
        int capacity = AutonomousCasterSettings.psiCapacity();
        AttributeInstance totalPsi = current.getAttribute(ModAttributes.TOTAL_PSI.holder());
        if (totalPsi != null && totalPsi.getBaseValue() != capacity) {
            totalPsi.setBaseValue(capacity);
        }
        PlayerData data = PlayerDataHandler.get(current);
        if (casterPsiWritten >= 0 && data.availablePsi != casterPsiWritten) {
            psi = (int) Mth.clamp((long) psi + data.availablePsi - casterPsiWritten, 0L, capacity);
            markDirtyComparator();
            markForSave();
        }
        data.availablePsi = psi;
        casterPsiWritten = psi;
    }

    // ---- 詠唱 ----

    private void performCast() {
        if (level == null) {
            return;
        }
        int slot = selectedBullet;
        ItemStack bullet = bulletSlots.get(slot).getStack();
        if (bullet.isEmpty() || !ISpellAcceptor.hasSpell(bullet)) {
            fail(error("no_spell", slot + 1));
            return;
        }
        if (isLoopcastBullet(bullet)) {
            // 入れられないが、以前のワールドで入っていた弾は撃たない。
            fail(error("loopcast_bullet", slot + 1));
            return;
        }
        if (psi <= 0) {
            fail(error("no_psi"));
            return;
        }
        if (!VirtualCad.isUsable(virtualCad)) {
            fail(error("no_cad"));
            return;
        }
        AutonomousCasterPlayer current = caster;
        if (current == null || !current.isAttached()) {
            fail(error("no_caster"));
            return;
        }

        reconcileMirror();
        positionCaster(current);
        current.getInventory().selected = selectedItemSlot;
        syncCasterPsi();
        PlayerData data = PlayerDataHandler.get(current);
        data.overflowed = false;
        data.regenCooldown = 0;
        data.deductions.clear();
        resetLoopcast(data);

        long revisionBefore = log.revision();
        int loopIndex = loopCounter;
        loopBroken = false;
        VirtualCad.select(virtualCad, slot);
        Optional<ArrayList<Entity>> result;
        boolean crashed = false;
        try {
            result = ItemCAD.cast(level, current, data, bullet, virtualCad, 0, 0, AutonomousCasterSettings.castVolume(), context -> {
                context.castFrom = InteractionHand.MAIN_HAND;
                context.loopcastIndex = loopIndex;
                // CAD が hotbar 外にあるため、0 で選択スロットそのものを対象にする。
                context.targetSlot = 0;
            });
        } catch (RuntimeException e) {
            LOGGER.warn("Autonomous caster at {} failed while casting", worldPosition, e);
            result = Optional.empty();
            crashed = true;
        }

        psi = Mth.clamp(data.availablePsi, 0, AutonomousCasterSettings.psiCapacity());
        data.availablePsi = psi;
        casterPsiWritten = psi;
        data.overflowed = false;
        data.deductions.clear();
        // Psi の持続 loopcast 状態は残さない。
        resetLoopcast(data);
        markDirtyComparator();
        reconcileMirror();
        adoptSelectedSlot();

        if (result.isPresent()) {
            // Trick: Spin Chamber などで選択スロットが変わっても、ループ回数は引き継ぐ。
            if (!loopBroken && loopCounter < Integer.MAX_VALUE) {
                loopCounter++;
            }
            lastOutcome = Outcome.SUCCESS;
            activeTicks = ACTIVE_TICKS_AFTER_CAST;
        } else if (crashed) {
            fail(error("exception"));
        } else if (log.revision() == revisionBefore || log.isEmpty()) {
            fail(error("cast_failed"));
        } else {
            // Psi が Caster へ送った理由（CAD 性能不足、Psi 不足など）が既にログにある。
            lastOutcome = Outcome.FAILURE;
            lastFailureSequence = log.latest().map(CasterLog.Entry::sequence).orElse(0L);
        }
        markForSave();
    }

    private void fail(Component reason) {
        log.add(CasterLog.Source.MACHINE_ERROR, reason, gameTime());
        lastOutcome = Outcome.FAILURE;
        lastFailureSequence = log.latest().map(CasterLog.Entry::sequence).orElse(0L);
        markForSave();
    }

    private static Component error(String key, Object... args) {
        return Component.translatable(ERROR_PREFIX + key, args).withStyle(ChatFormatting.RED);
    }

    private static void resetLoopcast(PlayerData data) {
        data.loopcasting = false;
        data.loopcastHand = null;
        data.lastTickLoopcastStack = null;
        data.loopcastTime = 1;
        data.loopcastAmount = 0;
        data.loopcastFadeTime = 0;
    }

    // ---- Caster inventory の写像 ----

    /**
     * 機械の36スロットと Caster の通常インベントリを突き合わせる。片方だけ変わった側を正とし、両方が同じ種類で変わった場合は
     * 増減を合算する。防具枠（CAD の枠を除く）・オフハンドに入ったものと収まらないアイテムは空きスロットへ移し、
     * それでも残れば前面に落とす。
     */
    private void reconcileMirror() {
        AutonomousCasterPlayer current = caster;
        if (current == null) {
            return;
        }
        Inventory inventory = current.getInventory();
        List<ItemStack> overflow = new ArrayList<>();
        for (int i = 0; i < ITEM_SLOTS; i++) {
            reconcileSlot(i, inventory, overflow);
        }

        ItemStack cadSlot = inventory.getItem(AutonomousCasterPlayer.CAD_SLOT);
        if (cadSlot != virtualCad) {
            if (!cadSlot.isEmpty() && !ItemStack.isSameItemSameComponents(cadSlot, virtualCad)) {
                overflow.add(cadSlot);
            }
            inventory.setItem(AutonomousCasterPlayer.CAD_SLOT, virtualCad);
        }
        for (int i = 0; i < inventory.armor.size(); i++) {
            if (Inventory.INVENTORY_SIZE + i != AutonomousCasterPlayer.CAD_SLOT) {
                collectForeign(inventory.armor, i, overflow);
            }
        }
        for (int i = 0; i < inventory.offhand.size(); i++) {
            collectForeign(inventory.offhand, i, overflow);
        }
        inventory.selected = selectedItemSlot;

        for (ItemStack stack : overflow) {
            storeOverflow(stack);
        }
    }

    private void reconcileSlot(int index, Inventory inventory, List<ItemStack> overflow) {
        IInventorySlot slot = itemSlots.get(index);
        ItemStack machine = slot.getStack();
        ItemStack mirrored = inventory.items.get(index);
        ItemStack snapshot = mirrorSnapshot[index];
        boolean machineChanged = !ItemStack.matches(machine, snapshot);
        boolean casterChanged = !ItemStack.matches(mirrored, snapshot);
        if (!machineChanged && !casterChanged) {
            return;
        }

        ItemStack result;
        if (!casterChanged) {
            result = machine.copy();
        } else if (!machineChanged) {
            result = fitToSlot(slot, mirrored, overflow);
        } else if (sameKind(machine, mirrored) && sameKind(machine, snapshot) && sameKind(mirrored, snapshot)) {
            // 搬入出と術式が同じ tick に同じアイテムを動かした場合は、両方の増減を残す。
            long count = (long) machine.getCount() + mirrored.getCount() - snapshot.getCount();
            ItemStack template = !machine.isEmpty() ? machine : !mirrored.isEmpty() ? mirrored : snapshot;
            result = count <= 0 ? ItemStack.EMPTY
                    : fitToSlot(slot, template.copyWithCount((int) Math.min(Integer.MAX_VALUE, count)), overflow);
        } else {
            result = machine.copy();
            if (!mirrored.isEmpty()) {
                overflow.add(mirrored.copy());
            }
        }

        if (!ItemStack.matches(result, machine)) {
            slot.setStack(result);
        }
        inventory.items.set(index, result.copy());
        mirrorSnapshot[index] = result.copy();
    }

    private static boolean sameKind(ItemStack first, ItemStack second) {
        return first.isEmpty() || second.isEmpty() || ItemStack.isSameItemSameComponents(first, second);
    }

    private static ItemStack fitToSlot(IInventorySlot slot, ItemStack stack, List<ItemStack> overflow) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (!slot.isItemValid(stack)) {
            overflow.add(stack.copy());
            return ItemStack.EMPTY;
        }
        int limit = slot.getLimit(stack);
        if (stack.getCount() <= limit) {
            return stack.copy();
        }
        overflow.add(stack.copyWithCount(stack.getCount() - limit));
        return stack.copyWithCount(limit);
    }

    private static void collectForeign(NonNullList<ItemStack> stacks, int index, List<ItemStack> overflow) {
        ItemStack stack = stacks.get(index);
        if (!stack.isEmpty()) {
            stacks.set(index, ItemStack.EMPTY);
            overflow.add(stack);
        }
    }

    private void storeOverflow(ItemStack stack) {
        ItemStack remaining = stack;
        for (IInventorySlot slot : itemSlots) {
            if (remaining.isEmpty()) {
                break;
            }
            remaining = slot.insertItem(remaining, Action.EXECUTE, AutomationType.INTERNAL);
        }
        if (remaining.isEmpty()) {
            return;
        }
        if (level != null && !level.isClientSide) {
            Direction facing = getDirection();
            Vec3 drop = Vec3.atCenterOf(worldPosition).add(facing.getStepX() * 0.75D, facing.getStepY() * 0.75D - 0.25D,
                    facing.getStepZ() * 0.75D);
            ItemEntity entity = new ItemEntity(level, drop.x, drop.y, drop.z, remaining.copy(), 0.0D, 0.0D, 0.0D);
            entity.setDefaultPickUpDelay();
            level.addFreshEntity(entity);
        } else {
            LOGGER.warn("Autonomous caster at {} discarded {} that did not fit", worldPosition, remaining);
        }
    }

    // ---- 片付け ----

    @Override
    public void blockRemoved() {
        super.blockRemoved();
        if (!isRemote()) {
            // ドロップ計算の前に、Caster 側の変化を機械のスロットへ戻しておく。
            reconcileMirror();
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        releaseCaster();
    }

    private void releaseCaster() {
        AutonomousCasterPlayer current = caster;
        caster = null;
        if (current != null) {
            current.getInventory().clearContent();
            current.detach();
        }
        viewerRevisions.clear();
    }

    // ---- AutonomousCasterHost ----

    @Override
    public BlockPos machinePos() {
        return worldPosition;
    }

    @Override
    public void onBreakLoop() {
        if (level != null && !level.isClientSide) {
            loopCounter = 0;
            loopBroken = true;
            markForSave();
        }
    }

    @Override
    public void onCasterMessage(Component message) {
        if (level != null && !level.isClientSide) {
            log.add(CasterLog.Source.CASTER_MESSAGE, message, gameTime());
            markForSave();
        }
    }

    @Override
    public void onSpamlessMessage(Component message, int replacementKey) {
        if (level != null && !level.isClientSide) {
            log.addSpamless(message, replacementKey, gameTime());
            markForSave();
        }
    }

    @Override
    public int availablePsi() {
        return psi;
    }

    @Override
    public void onInsufficientPsi(int cost, int available) {
        if (level != null && !level.isClientSide) {
            log.add(CasterLog.Source.MACHINE_ERROR, error("not_enough_psi", cost, available), gameTime());
            markForSave();
        }
    }

    @Override
    public Optional<UUID> ideaStorageOwner() {
        return Optional.ofNullable(getOwnerUUID());
    }

    @Override
    public Optional<String> ownerLanguage() {
        refreshOwnerLanguage();
        return Optional.ofNullable(ownerLanguage);
    }

    /** 所有者がオンラインなら、その言語を覚える。オフラインの間は最後に覚えた言語を使う。 */
    private void refreshOwnerLanguage() {
        UUID owner = getOwnerUUID();
        if (owner == null || level == null || level.getServer() == null) {
            return;
        }
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
        String language = player == null ? null : player.getLanguage();
        if (language != null && !language.isBlank() && !language.equals(ownerLanguage)) {
            ownerLanguage = language;
            markForSave();
        }
    }

    /**
     * 所有者の名前。Caster が所有者のチームに入るのに使う。オンラインなら今の名前を覚え、オフラインの間は覚えた名前、
     * 覚えていなければサーバーのプロフィールキャッシュから引く。覚えた名前は所有者が変わったら使わない。
     */
    @Override
    public Optional<String> ownerName() {
        UUID owner = getOwnerUUID();
        if (owner == null || level == null || level.getServer() == null) {
            return Optional.empty();
        }
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
        String name = player != null ? player.getScoreboardName() : null;
        if (name == null && owner.equals(ownerNameOf) && ownerName != null) {
            return Optional.of(ownerName);
        }
        if (name == null && level.getServer().getProfileCache() != null) {
            name = level.getServer().getProfileCache().get(owner).map(GameProfile::getName).orElse(null);
        }
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        if (!name.equals(ownerName) || !owner.equals(ownerNameOf)) {
            ownerName = name;
            ownerNameOf = owner;
            markForSave();
        }
        return Optional.of(name);
    }

    private long gameTime() {
        return level == null ? 0L : level.getGameTime();
    }

    // ---- 作動ログの同期 ----

    private void syncLogToViewers() {
        if (playersUsing.isEmpty()) {
            viewerRevisions.clear();
            return;
        }
        Set<UUID> viewing = new HashSet<>();
        for (Player player : new ArrayList<>(playersUsing)) {
            if (!(player instanceof ServerPlayer serverPlayer)
                    || !(serverPlayer.containerMenu instanceof MekanismTileContainer<?> menu)
                    || menu.getTileEntity() != this) {
                continue;
            }
            UUID id = serverPlayer.getUUID();
            viewing.add(id);
            long known = viewerRevisions.getOrDefault(id, 0L);
            if (known == log.revision()) {
                continue;
            }
            CasterLog.Delta delta = log.deltaSince(known);
            viewerRevisions.put(id, log.revision());
            if (!delta.isEmpty()) {
                PacketDistributor.sendToPlayer(serverPlayer, new MessageAutonomousCasterLog(menu.containerId, delta));
            }
        }
        viewerRevisions.keySet().retainAll(viewing);
    }

    /** クライアント側で受け取った差分を反映する。起点が合わなければ false（全体の再送を頼む）。 */
    public static boolean applyClientLog(Player player, int containerId, CasterLog.Delta delta) {
        if (player.containerMenu instanceof MekanismTileContainer<?> menu && menu.containerId == containerId
                && menu.getTileEntity() instanceof AutonomousCasterBlockEntity tile) {
            return tile.getClientLog().apply(delta);
        }
        return true;
    }

    public CasterLog getClientLog() {
        CasterLog current = clientLog;
        if (current == null) {
            current = CasterLog.clientCopy();
            clientLog = current;
        }
        return current;
    }

    /** GUI を閉じた時に呼ぶ。クライアント側コピーは画面ごとに作り直す。 */
    public void releaseClientLog() {
        clientLog = null;
    }

    // ---- GUI からの操作 ----

    /** サーバー側。開いている menu・対象・セキュリティを確認してから設定を変える。 */
    public static void handleAction(Player player, int containerId, MessageAutonomousCasterAction.Action action, int value) {
        if (!(player instanceof ServerPlayer)
                || !(player.containerMenu instanceof MekanismTileContainer<?> menu)
                || menu.containerId != containerId
                || !(menu.getTileEntity() instanceof AutonomousCasterBlockEntity tile)
                || !menu.stillValid(player)
                || !menu.canPlayerAccess(player)) {
            return;
        }
        tile.applyAction(player, action, value);
    }

    private void applyAction(Player player, MessageAutonomousCasterAction.Action action, int value) {
        switch (action) {
            case SELECT_BULLET -> {
                int selected = Mth.clamp(value, 0, BULLET_SLOTS - 1);
                if (selected != selectedBullet) {
                    // 手で切り替えたときだけ 0 に戻す。術式による切り替え（adoptSelectedSlot）では引き継ぐ。
                    loopCounter = 0;
                }
                selectedBullet = selected;
                VirtualCad.select(virtualCad, selectedBullet);
            }
            case SELECT_ITEM_SLOT -> {
                selectedItemSlot = Mth.clamp(value, 0, HOTBAR_SLOTS - 1);
                if (caster != null) {
                    caster.getInventory().selected = selectedItemSlot;
                }
            }
            case SET_TRIGGER_MODE -> triggerMode = TriggerMode.byIndex(value);
            case SET_INTERVAL -> castInterval = AutonomousCasterSettings.clampCastInterval(value);
            case SET_YAW -> {
                yawOffset = Mth.clamp(value, MIN_YAW, MAX_YAW);
                sendUpdatePacket();
            }
            case SET_PITCH -> {
                pitch = Mth.clamp(value, MIN_PITCH, MAX_PITCH);
                sendUpdatePacket();
            }
            case RESET_LOOP_COUNTER -> loopCounter = 0;
            case CLEAR_LOG -> {
                log.clear();
                lastFailureSequence = 0L;
            }
            case REQUEST_LOG -> viewerRevisions.put(player.getUUID(), 0L);
            case CAST_NOW -> manualCastRequested = true;
        }
        markForSave();
    }

    // ---- 保存・同期 ----

    @NotNull
    @Override
    public CompoundTag getReducedUpdateTag(@NotNull HolderLookup.Provider provider) {
        CompoundTag updateTag = super.getReducedUpdateTag(provider);
        // 視線の表示は GUI を閉じた後も描くので、向きは画面の同期とは別にブロックの更新でも送る。
        updateTag.putInt(TAG_YAW, yawOffset);
        updateTag.putInt(TAG_PITCH, pitch);
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.handleUpdateTag(tag, provider);
        yawOffset = Mth.clamp(tag.getInt(TAG_YAW), MIN_YAW, MAX_YAW);
        pitch = Mth.clamp(tag.getInt(TAG_PITCH), MIN_PITCH, MAX_PITCH);
    }

    /** 回収したブロックアイテムに内部 Psi を残す。空なら付けず、新品の機械と重ねられるようにする。 */
    @Override
    protected void collectImplicitComponents(@NotNull DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        if (psi > 0) {
            builder.set(PsitweaksDataComponents.AUTONOMOUS_CASTER_PSI.get(), psi);
        }
    }

    /** 置き直したときに、ブロックアイテムに残した内部 Psi を戻す。 */
    @Override
    protected void applyImplicitComponents(@NotNull BlockEntity.DataComponentInput input) {
        super.applyImplicitComponents(input);
        Integer stored = input.get(PsitweaksDataComponents.AUTONOMOUS_CASTER_PSI.get());
        if (stored != null) {
            psi = Mth.clamp(stored, 0, AutonomousCasterSettings.psiCapacity());
            markDirtyComparator();
            markForSave();
        }
    }

    @Override
    public void removeComponentsFromTag(@NotNull CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove(TAG_PSI);
    }

    /** 術式弾スロットが今より少なかった頃に回収した機械は、足りない術式弾スロットの分だけ空きを挟んで番号を合わせる。 */
    @Override
    public void applyInventorySlots(@NotNull BlockEntity.DataComponentInput input, @NotNull List<IInventorySlot> slots,
            @NotNull AttachedItems attachedItems) {
        int missing = slots.size() - attachedItems.size();
        if (missing > 0 && missing < BULLET_SLOTS) {
            List<ItemStack> stacks = new ArrayList<>(attachedItems.containers());
            stacks.addAll(BULLET_SLOTS - missing, Collections.nCopies(missing, ItemStack.EMPTY));
            attachedItems = new AttachedItems(stacks);
        }
        super.applyInventorySlots(input, slots, attachedItems);
    }

    /** 術式弾スロットが今より少なかった頃に保存した機械は、術式弾より後ろのスロットの番号を増えた分だけずらして読む。 */
    private static CompoundTag withCurrentSlotNumbers(CompoundTag tag) {
        int saved = tag.contains(TAG_BULLET_SLOTS) ? tag.getInt(TAG_BULLET_SLOTS) : UNRECORDED_BULLET_SLOTS;
        if (saved >= BULLET_SLOTS || !tag.contains(SerializationConstants.ITEMS, Tag.TAG_LIST)) {
            return tag;
        }
        CompoundTag moved = tag.copy();
        for (Tag entry : moved.getList(SerializationConstants.ITEMS, Tag.TAG_COMPOUND)) {
            CompoundTag item = (CompoundTag) entry;
            int slot = item.getByte(SerializationConstants.SLOT);
            if (slot >= saved) {
                item.putByte(SerializationConstants.SLOT, (byte) (slot + BULLET_SLOTS - saved));
            }
        }
        return moved;
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt(TAG_PSI, psi);
        tag.putInt(TAG_BULLET_SLOTS, BULLET_SLOTS);
        tag.putInt(TAG_SELECTED_BULLET, selectedBullet);
        tag.putInt(TAG_SELECTED_ITEM, selectedItemSlot);
        tag.putInt(TAG_TRIGGER_MODE, triggerMode.ordinal());
        tag.putInt(TAG_CAST_INTERVAL, castInterval);
        tag.putInt(TAG_YAW, yawOffset);
        tag.putInt(TAG_PITCH, pitch);
        tag.putInt(TAG_LOOP_COUNTER, loopCounter);
        CompoundTag spells = new CompoundTag();
        for (int i = 0; i < BULLET_SLOTS; i++) {
            if (loopSpellIds[i] != null) {
                spells.putUUID(Integer.toString(i), loopSpellIds[i]);
            }
        }
        tag.put(TAG_LOOP_SPELLS, spells);
        tag.putBoolean(TAG_SIGNAL, signal);
        tag.putInt(TAG_COOLDOWN, cooldown);
        if (casterId != null) {
            tag.putUUID(TAG_CASTER_ID, casterId);
        }
        if (!virtualCad.isEmpty()) {
            tag.put(TAG_VIRTUAL_CAD, virtualCad.save(registries));
        }
        CompoundTag logTag = new CompoundTag();
        log.save(logTag, registries);
        tag.put(TAG_LOG, logTag);
        tag.putInt(TAG_LAST_OUTCOME, lastOutcome.ordinal());
        tag.putLong(TAG_LAST_FAILURE, lastFailureSequence);
        if (ownerLanguage != null) {
            tag.putString(TAG_OWNER_LANGUAGE, ownerLanguage);
        }
        if (ownerName != null && ownerNameOf != null) {
            tag.putString(TAG_OWNER_NAME, ownerName);
            tag.putUUID(TAG_OWNER_NAME_OF, ownerNameOf);
        }
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(withCurrentSlotNumbers(tag), registries);
        psi = Math.max(0, tag.getInt(TAG_PSI));
        selectedBullet = Mth.clamp(tag.getInt(TAG_SELECTED_BULLET), 0, BULLET_SLOTS - 1);
        selectedItemSlot = Mth.clamp(tag.getInt(TAG_SELECTED_ITEM), 0, HOTBAR_SLOTS - 1);
        triggerMode = TriggerMode.byIndex(tag.getInt(TAG_TRIGGER_MODE));
        castInterval = tag.contains(TAG_CAST_INTERVAL)
                ? AutonomousCasterSettings.clampCastInterval(tag.getInt(TAG_CAST_INTERVAL))
                : AutonomousCasterSettings.defaultCastInterval();
        yawOffset = Mth.clamp(tag.getInt(TAG_YAW), MIN_YAW, MAX_YAW);
        pitch = Mth.clamp(tag.getInt(TAG_PITCH), MIN_PITCH, MAX_PITCH);
        loopCounter = Math.max(0, tag.getInt(TAG_LOOP_COUNTER));
        CompoundTag spells = tag.getCompound(TAG_LOOP_SPELLS);
        for (int i = 0; i < BULLET_SLOTS; i++) {
            String key = Integer.toString(i);
            loopSpellIds[i] = spells.hasUUID(key) ? spells.getUUID(key) : null;
        }
        signal = tag.getBoolean(TAG_SIGNAL);
        cooldown = Math.max(0, tag.getInt(TAG_COOLDOWN));
        casterId = tag.hasUUID(TAG_CASTER_ID) ? tag.getUUID(TAG_CASTER_ID) : null;
        virtualCad = tag.contains(TAG_VIRTUAL_CAD)
                ? ItemStack.parseOptional(registries, tag.getCompound(TAG_VIRTUAL_CAD))
                : ItemStack.EMPTY;
        log.load(tag.getCompound(TAG_LOG), registries);
        lastOutcome = Outcome.byIndex(tag.getInt(TAG_LAST_OUTCOME));
        lastFailureSequence = tag.getLong(TAG_LAST_FAILURE);
        ownerLanguage = tag.contains(TAG_OWNER_LANGUAGE) ? tag.getString(TAG_OWNER_LANGUAGE) : null;
        boolean hasOwnerName = tag.contains(TAG_OWNER_NAME) && tag.hasUUID(TAG_OWNER_NAME_OF);
        ownerName = hasOwnerName ? tag.getString(TAG_OWNER_NAME) : null;
        ownerNameOf = hasOwnerName ? tag.getUUID(TAG_OWNER_NAME_OF) : null;
        virtualCadDirty = true;
        bulletsDirty = true;
        signalChecked = false;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableInt.create(() -> psi, value -> psi = value));
        container.track(SyncableInt.create(AutonomousCasterSettings::psiCapacity, value -> clientPsiCapacity = Math.max(1, value)));
        container.track(SyncableInt.create(AutonomousCasterSettings::minCastInterval, value -> clientMinCastInterval = value));
        container.track(SyncableInt.create(() -> selectedBullet, value -> selectedBullet = Mth.clamp(value, 0, BULLET_SLOTS - 1)));
        container.track(SyncableInt.create(() -> selectedItemSlot, value -> selectedItemSlot = Mth.clamp(value, 0, HOTBAR_SLOTS - 1)));
        container.track(SyncableEnum.create(TriggerMode::byIndex, TriggerMode.PULSE, () -> triggerMode, value -> triggerMode = value));
        container.track(SyncableInt.create(() -> castInterval, value -> castInterval = value));
        container.track(SyncableInt.create(() -> yawOffset, value -> yawOffset = value));
        container.track(SyncableInt.create(() -> pitch, value -> pitch = value));
        container.track(SyncableInt.create(() -> loopCounter, value -> loopCounter = value));
        container.track(SyncableInt.create(() -> cooldown, value -> cooldown = value));
        container.track(SyncableBoolean.create(() -> signal, value -> signal = value));
        container.track(SyncableEnum.create(Outcome::byIndex, Outcome.NONE, () -> lastOutcome, value -> lastOutcome = value));
        container.track(SyncableLong.create(() -> lastFailureSequence, value -> lastFailureSequence = value));
        container.track(SyncableInt.create(() -> recoveryPerTick, value -> recoveryPerTick = value));
    }

    @Override
    protected boolean makesComparatorDirty(ContainerType<?, ?, ?> type) {
        return false;
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(psi, AutonomousCasterSettings.psiCapacity());
    }

    // ---- 判定 ----

    private static boolean isPsionicEcho(ChemicalStack stack) {
        return stack.is(PsitweaksChemicals.GAS_PSIONIC_ECHO.get());
    }

    /** 弾スロット: ソケットから詠唱できる術式容器（弾）だけ。CAD 自体とループ型術式弾は入れない。 */
    public static boolean isBulletCandidate(ItemStack stack) {
        return !stack.isEmpty() && !(stack.getItem() instanceof ICAD) && !isLoopcastBullet(stack)
                && ISpellAcceptor.isContainer(stack);
    }

    private static boolean isLoopcastBullet(ItemStack stack) {
        return stack.getItem() instanceof ItemLoopcastSpellBullet;
    }

    /** Caster へ写すスロット: CAD を入れると Caster が CAD を一意に特定できなくなるため拒否する。 */
    public static boolean isMirrorableItem(ItemStack stack) {
        return !(stack.getItem() instanceof ICAD);
    }

    @Nullable
    private HolderLookup.Provider registriesOrNull() {
        return level == null ? null : level.registryAccess();
    }

    private static ItemStack[] emptyStacks(int size) {
        ItemStack[] stacks = new ItemStack[size];
        Arrays.fill(stacks, ItemStack.EMPTY);
        return stacks;
    }

    // ---- GUI 用 ----

    public static int bulletSlotX(int slot) {
        return BULLET_X + slot % BULLET_COLUMNS * 18;
    }

    public static int bulletSlotY(int slot) {
        return BULLET_Y + slot / BULLET_COLUMNS * 18;
    }

    public IChemicalTank getEchoTank() {
        return echoTank;
    }

    public List<IInventorySlot> getBulletSlots() {
        return bulletSlots;
    }

    public IInventorySlot getDyeSlot() {
        return dyeSlot;
    }

    public List<IInventorySlot> getItemSlots() {
        return itemSlots;
    }

    public int getPsi() {
        return psi;
    }

    public int getPsiCapacity() {
        return isRemote() ? clientPsiCapacity : AutonomousCasterSettings.psiCapacity();
    }

    public int getMinCastInterval() {
        return isRemote() ? clientMinCastInterval : AutonomousCasterSettings.minCastInterval();
    }

    public int getSelectedBullet() {
        return selectedBullet;
    }

    public int getSelectedItemSlot() {
        return selectedItemSlot;
    }

    public TriggerMode getTriggerMode() {
        return triggerMode;
    }

    public int getCastInterval() {
        return castInterval;
    }

    public int getYawOffset() {
        return yawOffset;
    }

    public int getPitch() {
        return pitch;
    }

    public int getLoopCounter() {
        return loopCounter;
    }

    public int getCooldown() {
        return cooldown;
    }

    public boolean hasSignal() {
        return signal;
    }

    public Outcome getLastOutcome() {
        return lastOutcome;
    }

    public long getLastFailureSequence() {
        return lastFailureSequence;
    }

    public int getRecoveryPerTick() {
        return recoveryPerTick;
    }

    // ---- 視線の表示（クライアント） ----

    @Override
    public boolean isClientRendering() {
        return clientRendering;
    }

    @Override
    public void toggleClientRendering() {
        clientRendering = !clientRendering;
    }

    // ---- GameTest 用 ----

    public CasterLog getLog() {
        return log;
    }

    public ItemStack getVirtualCad() {
        return virtualCad;
    }

    @Nullable
    public AutonomousCasterPlayer getCaster() {
        return caster;
    }

    public void setPsi(int psi) {
        this.psi = Mth.clamp(psi, 0, AutonomousCasterSettings.psiCapacity());
        markDirtyComparator();
        markForSave();
    }

    /** GameTest 用。GUI と同じ経路で設定を変える。 */
    public void applySetting(MessageAutonomousCasterAction.Action action, int value, Player player) {
        applyAction(player, action, value);
    }

    /** 既定の側面設定（ブロックアイテムに付ける）。 */
    public static Map<RelativeSide, DataType> defaultItemSides() {
        return Map.of(
                RelativeSide.LEFT, DataType.INPUT,
                RelativeSide.RIGHT, DataType.INPUT,
                RelativeSide.TOP, DataType.INPUT,
                RelativeSide.BACK, DataType.INPUT,
                RelativeSide.BOTTOM, DataType.EXTRA);
    }
}
