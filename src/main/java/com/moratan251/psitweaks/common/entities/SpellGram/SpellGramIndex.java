package com.moratan251.psitweaks.common.entities.SpellGram;

import com.moratan251.psitweaks.common.caster.CasterIds;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * ディメンションごとの、ワールドに読み込まれている魔法式オブジェクトの台帳（サーバー側のみ）。
 * オブジェクトがワールドに入る・出るときに {@link SpellGramObject} が出し入れするので、全エンティティを調べずに
 * 場所や術者から引ける。読み込まれていないチャンクにあるものは載らない。サーバーが止まったら空にする。
 */
public final class SpellGramIndex {
    private static final Map<ResourceKey<Level>, Set<SpellGramObject>> BY_LEVEL = new HashMap<>();

    private SpellGramIndex() {
    }

    static void add(SpellGramObject object) {
        BY_LEVEL.computeIfAbsent(object.level().dimension(), ignored -> new LinkedHashSet<>()).add(object);
    }

    static void remove(SpellGramObject object) {
        ResourceKey<Level> dimension = object.level().dimension();
        Set<SpellGramObject> objects = BY_LEVEL.get(dimension);
        if (objects != null && objects.remove(object) && objects.isEmpty()) {
            BY_LEVEL.remove(dimension);
        }
    }

    /** {@code level} にあり、中心が {@code center} から {@code radius} 以内の魔法式オブジェクト。 */
    public static List<SpellGramObject> near(Level level, Vec3 center, double radius) {
        return near(level, center, radius, SpellGramObject.class);
    }

    /** {@link #near(Level, Vec3, double)} のうち {@code type} のもの。 */
    public static <T extends SpellGramObject> List<T> near(Level level, Vec3 center, double radius, Class<T> type) {
        Set<SpellGramObject> objects = BY_LEVEL.get(level.dimension());
        if (objects == null) {
            return List.of();
        }
        double radiusSqr = radius * radius;
        List<T> found = new ArrayList<>();
        for (SpellGramObject object : objects) {
            if (type.isInstance(object) && !object.isRemoved() && object.position().distanceToSqr(center) <= radiusSqr) {
                found.add(type.cast(object));
            }
        }
        return found;
    }

    /** {@code level} にある、術者（{@link CasterIds#idOf} の ID）の魔法式オブジェクト。 */
    public static List<SpellGramObject> ofCaster(Level level, @Nullable UUID caster) {
        Set<SpellGramObject> objects = BY_LEVEL.get(level.dimension());
        if (objects == null || caster == null) {
            return List.of();
        }
        List<SpellGramObject> found = new ArrayList<>();
        for (SpellGramObject object : objects) {
            if (!object.isRemoved() && caster.equals(object.getCasterUuid())) {
                found.add(object);
            }
        }
        return found;
    }

    public static void onServerStopped(ServerStoppedEvent event) {
        BY_LEVEL.clear();
    }
}
