package com.moratan251.psitweaks.common.caster;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.jetbrains.annotations.Nullable;

/**
 * 稼働中の専用 Caster を UUID から引く弱参照表。
 * 専用 Caster はワールドのプレイヤー一覧に入らないため、術式円などが UUID で術者を探す経路だけに使う。
 */
public final class AutonomousCasterRegistry {
    private static final Map<UUID, WeakReference<AutonomousCasterPlayer>> CASTERS = new ConcurrentHashMap<>();

    private AutonomousCasterRegistry() {
    }

    static void register(AutonomousCasterPlayer caster) {
        CASTERS.put(caster.getUUID(), new WeakReference<>(caster));
    }

    static void unregister(AutonomousCasterPlayer caster) {
        CASTERS.computeIfPresent(caster.getUUID(), (id, reference) -> reference.get() == caster ? null : reference);
    }

    @Nullable
    public static Player find(@Nullable Level level, @Nullable UUID casterId) {
        if (level == null || casterId == null || level.isClientSide) {
            return null;
        }
        WeakReference<AutonomousCasterPlayer> reference = CASTERS.get(casterId);
        AutonomousCasterPlayer caster = reference == null ? null : reference.get();
        if (caster == null) {
            if (reference != null) {
                CASTERS.remove(casterId, reference);
            }
            return null;
        }
        return caster.isAttached() && caster.level() == level ? caster : null;
    }

    /** 構造物のコピーなどで同じ Caster UUID を持つ機械が並んだ時に、後から来た側が UUID を作り直すために使う。 */
    public static boolean isInUse(@Nullable UUID casterId) {
        if (casterId == null) {
            return false;
        }
        WeakReference<AutonomousCasterPlayer> reference = CASTERS.get(casterId);
        AutonomousCasterPlayer caster = reference == null ? null : reference.get();
        return caster != null && caster.isAttached();
    }

    public static void clear() {
        CASTERS.clear();
    }

    public static void onServerStopped(ServerStoppedEvent event) {
        clear();
    }
}
