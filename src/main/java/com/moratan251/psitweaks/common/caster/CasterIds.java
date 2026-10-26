package com.moratan251.psitweaks.common.caster;

import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * 術者を後から UUID で引き直すための ID。
 * <p>
 * 自律詠唱機の専用 Caster は所有者の UUID を名乗るため、{@code getUUID()} で保存すると本物の所有者が見つかってしまう。
 * 術者の保存には {@link #idOf} を使い、引き直しでは {@link #find} のように {@link AutonomousCasterRegistry} を先に見る。
 */
public final class CasterIds {
    private CasterIds() {
    }

    /** 保存用の ID。専用 Caster は機械ごとの内部キー、それ以外は {@code getUUID()}。 */
    public static UUID idOf(Entity entity) {
        return entity instanceof AutonomousCasterPlayer caster ? caster.casterKey() : entity.getUUID();
    }

    /** {@link #idOf} で保存した ID がこのエンティティを指すか。 */
    public static boolean matches(@Nullable Entity entity, @Nullable UUID id) {
        return entity != null && id != null && idOf(entity).equals(id);
    }

    /** {@link #idOf} で保存した ID からエンティティを引く。専用 Caster のキーを先に見る。サーバー側だけ。 */
    @Nullable
    public static Entity find(@Nullable Level level, @Nullable UUID id) {
        if (!(level instanceof ServerLevel serverLevel) || id == null) {
            return null;
        }
        Entity caster = AutonomousCasterRegistry.find(level, id);
        return caster != null ? caster : serverLevel.getEntity(id);
    }

    /**
     * {@link #idOf} で保存した術者を引く。専用 Caster のキーを先に見て、次にワールドの生き物、最後に同じワールドにいる
     * プレイヤー一覧のプレイヤー。サーバー側だけ。
     */
    @Nullable
    public static LivingEntity findLiving(@Nullable Level level, @Nullable UUID id) {
        if (id == null || !(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        // 自律詠唱機の詠唱主体はワールドにもプレイヤー一覧にも入らず、所有者の UUID を名乗る。
        // 術者は idOf（機械ごとの内部キー）で保存するので、本物の所有者より先に引く。
        Player autonomous = AutonomousCasterRegistry.find(level, id);
        if (autonomous != null) {
            return autonomous;
        }
        Entity entity = serverLevel.getEntity(id);
        if (entity instanceof LivingEntity living) {
            return living;
        }
        Player player = serverLevel.getServer().getPlayerList().getPlayer(id);
        return player != null && player.level() == level ? player : null;
    }
}
