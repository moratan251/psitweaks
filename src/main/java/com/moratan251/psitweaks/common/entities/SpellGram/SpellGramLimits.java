package com.moratan251.psitweaks.common.entities.SpellGram;

import com.moratan251.psitweaks.common.caster.AutonomousCasterPlayer;
import com.moratan251.psitweaks.common.caster.CasterIds;
import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.SpellGramZone;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * 1 人（術者の {@code CasterIds.idOf}、自律詠唱機は機械ごと）が同時に置ける魔法式オブジェクトの数の上限
 * （2026-11-11 ユーザー）。同じ種類をいくつ置いてもよく、サークル（ファイアサークル・アイスサークル）と領域を合わせて
 * 上限まで。超えて置くと、いちばん古いものから解ける（DISPELLED）。上限はプレイヤーと自律詠唱機で別々に設定できる
 * （{@code spellgram.maxObjectsPerPlayer}・{@code spellgram.maxObjectsPerAutonomousCaster}、どちらも既定 32）。
 * 流星群の夜（目印）は、ほかとは違う扱いにする予定なので数えない。
 */
public final class SpellGramLimits {
    /** 設定の既定値（プレイヤー・自律詠唱機とも）。 */
    public static final int DEFAULT_MAX_PER_CASTER = 32;

    private SpellGramLimits() {
    }

    /** 上限に数えるか: サークルと領域。 */
    public static boolean counts(SpellGramObject object) {
        return object instanceof SpellGramCircle || object instanceof SpellGramZone;
    }

    /** プレイヤー 1 人が同時に置ける数。 */
    public static int maxPerPlayer() {
        return PsitweaksConfig.COMMON.spellGramMaxObjectsPerPlayer.get();
    }

    /** 自律詠唱機 1 台が同時に置ける数。 */
    public static int maxPerAutonomousCaster() {
        return PsitweaksConfig.COMMON.spellGramMaxObjectsPerAutonomousCaster.get();
    }

    /** 術者 {@code caster} が同時に置ける数（自律詠唱機か、プレイヤーか）。 */
    public static int maxFor(Entity caster) {
        return caster instanceof AutonomousCasterPlayer ? maxPerAutonomousCaster() : maxPerPlayer();
    }

    /**
     * 術者 {@code caster} が新しく 1 つ置く直前に呼ぶ。置いた後も上限に収まるよう、読み込まれているすべてのディメンションの
     * その術者のオブジェクトを、古いものから解く。
     */
    public static void makeRoom(ServerLevel castingLevel, Entity caster) {
        List<SpellGramObject> placed = placedBy(castingLevel, CasterIds.idOf(caster));
        int excess = placed.size() - (maxFor(caster) - 1);
        if (excess <= 0) {
            return;
        }
        // 古いものから。エンティティの番号はサーバー全体で置いた順に増えるので、同じ tick に置いたものも順に並ぶ。
        placed.sort(Comparator.comparingInt(SpellGramObject::getId));
        for (int i = 0; i < excess && i < placed.size(); i++) {
            placed.get(i).end(SpellGramObject.EndReason.DISPELLED);
        }
    }

    /** 術者 {@code casterId} が今置いている、上限に数えるオブジェクト（終わったものは除く）。 */
    public static List<SpellGramObject> placedBy(ServerLevel anyLevel, UUID casterId) {
        List<SpellGramObject> placed = new ArrayList<>();
        for (ServerLevel level : anyLevel.getServer().getAllLevels()) {
            for (SpellGramObject object : SpellGramIndex.ofCaster(level, casterId)) {
                if (counts(object) && !object.isEnded()) {
                    placed.add(object);
                }
            }
        }
        return placed;
    }
}
