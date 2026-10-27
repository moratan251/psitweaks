package com.moratan251.psitweaks.common.caster;

import com.moratan251.psitweaks.common.storage.idea.IdeaStorageOwnerProvider;
import com.mojang.authlib.GameProfile;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jetbrains.annotations.Nullable;
import vazkii.psi.common.network.message.MessageSpellError;

/**
 * 自律詠唱機専用の Caster。
 * <p>
 * GameProfile の UUID は機械の所有者の UUID（所有者がいなければ機械ごとのランダムな内部キー）にする。
 * 保護 Mod やキル・ドロップの帰属は所有者として扱う。名前は FakePlayer とわかる {@link #NAME} のまま。
 * 所有者がオンラインでも術者として本人と取り違えないよう、術者を後から引き直すための保存と
 * {@link AutonomousCasterRegistry} は機械ごとの内部キー {@link #casterKey()} を使う（{@link CasterIds}）。
 * Psi の FakePlayer 拒否はこのクラスだけを mixin で許可し、Caster 宛てのメッセージは
 * 実プレイヤーへ送らず機械の作動ログへ渡す。
 */
public final class AutonomousCasterPlayer extends FakePlayer implements IdeaStorageOwnerProvider {
    /** Psi と多くの Mod が FakePlayer とみなす角括弧付きの名前。 */
    public static final String NAME = "[PsiAutoCaster]";
    /**
     * 仮想 CAD を置く枠（{@link Inventory#getItem} の番号）。通常インベントリの36枠は
     * すべて機械のアイテム枠に写すので、頭の防具枠に置く。術式の対象スロット（ホットバー・オフハンド）にもならない。
     */
    public static final int CAD_SLOT = Inventory.INVENTORY_SIZE + EquipmentSlot.HEAD.getIndex();

    private final UUID casterKey;
    @Nullable
    private AutonomousCasterHost host;
    /** 機械のブロックの位置。機械を撤去した後も、遅延中の術式が読めるように残す。 */
    private final BlockPos machinePos;

    /**
     * @param casterKey 機械ごとのランダムな内部キー。実プレイヤーの UUID とは重ならない
     * @param owner     名乗る UUID。機械の所有者。不明なら null で、内部キーを名乗る
     */
    public AutonomousCasterPlayer(ServerLevel level, UUID casterKey, @Nullable UUID owner, AutonomousCasterHost host) {
        super(level, new GameProfile(owner != null ? owner : casterKey, NAME));
        this.casterKey = casterKey;
        this.host = host;
        this.machinePos = host.machinePos().immutable();
        AutonomousCasterRegistry.register(this);
    }

    /** 「取得子: 詠唱機位置」が読む、機械のブロックの位置。 */
    public BlockPos machinePos() {
        return machinePos;
    }

    /** 機械ごとの内部キー。術者を UUID で引き直す保存とレジストリに使う。{@link #getUUID()} は所有者の UUID。 */
    public UUID casterKey() {
        return casterKey;
    }

    /** 機械の撤去・unload 後に呼ぶ。以降の遅延術式や術式エンティティは所有者もログ先も持たない。 */
    public void detach() {
        host = null;
        AutonomousCasterRegistry.unregister(this);
    }

    public boolean isAttached() {
        return host != null;
    }

    public int availablePsi() {
        AutonomousCasterHost current = host;
        return current == null ? 0 : Math.max(0, current.availablePsi());
    }

    /** Trick: Break Loop の mixin から呼ばれる。機械のループ回数を 0 に戻す。 */
    public void breakLoop() {
        AutonomousCasterHost current = host;
        if (current != null) {
            current.onBreakLoop();
        }
    }

    /** {@link AutonomousCasterSpellHandler} から呼ばれる。 */
    void reportInsufficientPsi(int cost) {
        AutonomousCasterHost current = host;
        if (current != null) {
            current.onInsufficientPsi(cost, Math.max(0, current.availablePsi()));
        }
    }

    @Override
    public Optional<UUID> ideaStorageOwner() {
        AutonomousCasterHost current = host;
        return current == null ? Optional.empty() : current.ideaStorageOwner();
    }

    /** 表示名などの言語。FakePlayer の既定（en_us）ではなく、機械所有者の言語を使う。 */
    @Override
    public String getLanguage() {
        AutonomousCasterHost current = host;
        Optional<String> language = current == null ? Optional.empty() : current.ownerLanguage();
        return language.orElseGet(super::getLanguage);
    }

    /**
     * 所有者のスコアボードのチーム。攻撃術式の味方の判定（{@code isAlliedTo}）と、攻撃を受けるプレイヤー側の判定
     * （{@code canHarmPlayer}）で、機械を所有者のチームの一員として扱う。所有者が不明なら自分の名前のチーム（通常はなし）。
     */
    @Override
    @Nullable
    public PlayerTeam getTeam() {
        Optional<String> owner = ownerName();
        return owner.isPresent() ? level().getScoreboard().getPlayersTeam(owner.get()) : super.getTeam();
    }

    /** 機械の所有者の名前。チームを引くのと、作動式: 仮装行列の分身が名乗るのに使う。所有者が不明なら空。 */
    public Optional<String> ownerName() {
        AutonomousCasterHost current = host;
        return current == null ? Optional.empty() : current.ownerName();
    }

    /**
     * 機械から外れた後は、手と防具の枠に物を受け取らない。Trick: Break Block などは壊す間だけ手を内蔵 CAD に持ち替え、
     * 終わってから元の物を戻すので、壊したのが機械自身だと、機械のアイテムと一緒に落ちた物の写しがここへ戻ってくる。
     * 遅延中の術式がその写しを使えないよう、内蔵 CAD 以外は空にする。
     */
    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        super.setItemSlot(slot, isAttached() || VirtualCad.isVirtualCad(stack) ? stack : ItemStack.EMPTY);
    }

    /** ワールドスポーン周辺の安全地点探索を省く。位置は詠唱前に必ず設定し直す。 */
    @Override
    public BlockPos adjustSpawnLocation(ServerLevel level, BlockPos pos) {
        return pos;
    }

    @Override
    public void sendSystemMessage(Component component) {
        capture(component);
    }

    @Override
    public void sendSystemMessage(Component component, boolean bypassHiddenChat) {
        capture(component);
    }

    @Override
    public void displayClientMessage(Component component, boolean actionBar) {
        capture(component);
    }

    /** PsiNetwork.sendToPlayer の宛先がこの Caster のときに mixin から呼ばれる。 */
    public void capturePsiPayload(CustomPacketPayload payload) {
        AutonomousCasterHost current = host;
        if (current == null || payload == null) {
            return;
        }
        if (payload instanceof MessageSpellError error) {
            current.onCasterMessage(spellErrorPosition(error.message(), error.x(), error.y()));
        } else if (payload instanceof SpamlessChatBridge spamless && spamless.psitweaks$message() != null) {
            current.onSpamlessMessage(spamless.psitweaks$message(), spamless.psitweaks$magic());
        }
    }

    /** クライアント側 GuiProgrammer を参照せず、Psi のエラー位置表示（列は A〜I）を再現する。 */
    public static Component spellErrorPosition(String translationKey, int x, int y) {
        return gridPosition(translationKey, x, y).withStyle(ChatFormatting.RED);
    }

    /** 術式グリッドの位置の行（x・y は 1 始まり、列は A〜I）。術式の警告の位置にも使う。 */
    public static MutableComponent gridPosition(String translationKey, int x, int y) {
        return Component.translatable(translationKey, gridColumn(x), y);
    }

    static String gridColumn(int x) {
        return String.valueOf((char) (Math.floorMod(x, 27) + 64));
    }

    private void capture(@Nullable Component component) {
        AutonomousCasterHost current = host;
        if (current != null && component != null) {
            current.onCasterMessage(component);
        }
    }
}
