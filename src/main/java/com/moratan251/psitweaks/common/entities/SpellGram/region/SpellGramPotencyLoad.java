package com.moratan251.psitweaks.common.entities.SpellGram.region;

import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.network.MessageSpellGramLoad;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.cad.EnumCADStat;
import vazkii.psi.api.cad.ICAD;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 「CAD の規模で数える版」の試作。領域型を置く・性質を足す作動式の規模を枠として使い、合計は術者の CAD の規模
 * × {@code capacityMultiplier} まで。超える分は古い魔法式から解く。Psi の回復には関わらない。
 * CAD のない術者（CAD を持たない自律詠唱機など）と規模が無限の CAD は、数の上限だけを見る。
 */
public final class SpellGramPotencyLoad {
    /** サーバー: 術者ごとの使用量。 */
    private static final Map<UUID, Integer> LOADS = new HashMap<>();
    /** クライアント: 自分の使用量（{@link MessageSpellGramLoad} で届く）。 */
    private static int clientLoad;

    private SpellGramPotencyLoad() {
    }

    public static int loadOf(Player player) {
        if (player.level().isClientSide) {
            return clientLoad;
        }
        return LOADS.getOrDefault(player.getUUID(), 0);
    }

    public static void setClientLoad(int load) {
        clientLoad = Math.max(0, load);
    }

    /** 術者の枠の大きさ。上限がない（CAD がない・規模が無限）ときは -1。 */
    public static int capacityOf(Player caster) {
        ItemStack cad = PsiAPI.getPlayerCAD(caster);
        if (cad.isEmpty() || !(cad.getItem() instanceof ICAD icad)) {
            return -1;
        }
        int potency = icad.getStatValue(cad, EnumCADStat.POTENCY);
        if (potency < 0) {
            return -1;
        }
        return (int) (potency * PsitweaksConfig.COMMON.spellGramCapacityMultiplier.get());
    }

    /** 使用量を数え直し、本人に知らせる。 */
    static void refresh(@Nullable MinecraftServer server, @Nullable UUID caster) {
        if (caster == null) {
            return;
        }
        int load = SpellGramRegionIndex.totalUse(caster);
        Integer previous = load > 0 ? LOADS.put(caster, load) : LOADS.remove(caster);
        if (server == null || (previous == null ? 0 : previous) == load) {
            return;
        }
        ServerPlayer player = server.getPlayerList().getPlayer(caster);
        if (player != null) {
            PacketDistributor.sendToPlayer(player, new MessageSpellGramLoad(load));
        }
    }

    public static void onServerStopped(ServerStoppedEvent event) {
        LOADS.clear();
    }
}
