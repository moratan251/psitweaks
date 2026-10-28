package com.moratan251.psitweaks.common.entities.SpellGram.region;

import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.network.MessageSpellGramPsiLock;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.cad.ISocketable;
import vazkii.psi.api.cad.RegenPsiEvent;
import vazkii.psi.common.core.handler.PlayerDataHandler;
import vazkii.psi.common.item.ItemCAD;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 「Psi を確保する版」の試作。領域型を置く・性質を足すのに払った Psi は、その魔法式が解けるまで回復しない。
 * <p>
 * 確保した分は Psi の上限から引いた「回復できる上限」として扱う（{@link #onRegenPsi}）。回復の速さも、確保していない
 * 割合に合わせて遅くする（設定 {@code psiLockRegenScale}。1 で割合どおり、0 で遅くしない）。確保の合計は
 * Psi の上限 × {@code psiLockMaxRatio} までで、超える分は古い魔法式から解く。
 * 自律詠唱機など本物でないプレイヤーの魔法式は確保しない（数の上限だけ）。
 */
public final class SpellGramPsiLock {
    /** サーバー: 術者ごとの確保量。 */
    private static final Map<UUID, Integer> LOCKS = new HashMap<>();
    /** クライアント: 自分の確保量（{@link MessageSpellGramPsiLock} で届く）。 */
    private static int clientLock;

    private SpellGramPsiLock() {
    }

    public static int lockOf(Player player) {
        if (player.level().isClientSide) {
            return player.isLocalPlayer() ? clientLock : 0;
        }
        return LOCKS.getOrDefault(player.getUUID(), 0);
    }

    public static void setClientLock(int lock) {
        clientLock = Math.max(0, lock);
    }

    /** 確保の対象になる術者か（本物のプレイヤーだけ）。 */
    public static boolean locksPsi(@Nullable Player caster) {
        return caster instanceof ServerPlayer && !(caster instanceof FakePlayer);
    }

    /** 作動式のコストを、術者の CAD の効率と選んでいる術式弾の倍率で、実際に払う Psi に直す。 */
    public static int realCost(Player caster, int pieceCost) {
        ItemStack cad = PsiAPI.getPlayerCAD(caster);
        ItemStack bullet = ItemStack.EMPTY;
        if (!cad.isEmpty()) {
            ISocketable socketable = ISocketable.socketable(cad);
            if (socketable != null) {
                bullet = socketable.getSelectedBullet();
            }
        }
        // 効率が無限の CAD（クリエイティブ）は -1 を返し、Psi を払わないので確保もしない。
        return Math.max(0, ItemCAD.getRealCost(cad, bullet, pieceCost));
    }

    public static int totalPsi(Player caster) {
        return PlayerDataHandler.get(caster).getTotalPsi();
    }

    public static int maxLock(Player caster) {
        return (int) (totalPsi(caster) * PsitweaksConfig.COMMON.spellGramPsiLockMaxRatio.get());
    }

    /** 確保量を数え直し、本人に知らせる。 */
    static void refresh(@Nullable MinecraftServer server, @Nullable UUID caster) {
        if (caster == null) {
            return;
        }
        int lock = SpellGramRegionIndex.totalUse(caster);
        Integer previous = lock > 0 ? LOCKS.put(caster, lock) : LOCKS.remove(caster);
        if (server == null || (previous == null ? 0 : previous) == lock) {
            return;
        }
        ServerPlayer player = server.getPlayerList().getPlayer(caster);
        if (player != null) {
            PacketDistributor.sendToPlayer(player, new MessageSpellGramPsiLock(lock));
        }
    }

    /** 確保した分は回復できる上限から引き、回復も確保していない割合に合わせて遅くする。 */
    public static void onRegenPsi(RegenPsiEvent event) {
        Player player = event.getPlayer();
        int lock = lockOf(player);
        if (lock <= 0) {
            return;
        }
        int total = event.getPlayerPsiCapacity();
        int cap = Math.max(0, total - lock);
        double regenScale = PsitweaksConfig.COMMON.spellGramPsiLockRegenScale.get();
        if (regenScale > 0.0D && total > 0) {
            int slower = (int) Math.round(event.getRegenRate() * Math.min(1.0D, (double) lock / total) * regenScale);
            if (slower > 0) {
                event.removeRegen(slower);
            }
        }
        int room = Math.max(0, cap - event.getPlayerPsi());
        int max = event.getMaxPlayerRegen();
        event.setMaxPlayerRegen(max >= 0 ? Math.min(max, room) : room);
        // 払い戻しなどで確保した分まで入り込んだときは、確保した分の外まで下げる（サーバーで下げて同期する）。
        if (!player.level().isClientSide && event.getPlayerPsi() > cap) {
            PlayerDataHandler.get(player).deductPsi(event.getPlayerPsi() - cap, 0, true);
        }
    }

    public static void onServerStopped(ServerStoppedEvent event) {
        LOCKS.clear();
    }
}
