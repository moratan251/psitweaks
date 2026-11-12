package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIndex;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityGravityZone;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityHeatZone;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntitySoakZone;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.SpellGramZone;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickGravityZone;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickHeatZone;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickSoakZone;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickZoneBase;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

import java.util.List;

/**
 * 同じ種類の領域を重ねても効き目は増えない（2026-11-12 ユーザー）: 重なったところでは一番強い（威力が高い、並べば干渉力、それも
 * 並べば古い）領域だけが効く。加重領域だけは押す向きを足し合わせ、威力 10 で頭打ちにする。
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ZoneStackingGameTests {
    private static final String TEMPLATE = "connector_empty";

    private ZoneStackingGameTests() {
    }

    /** 威力の高い加熱領域だけが重なったところで効き、威力の同じ湿潤領域どうしは古いほうが効く。重ならないところはそれぞれ効く。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_stacking", timeoutTicks = 40)
    public static void onlyTheStrongestZoneOfAKindWorks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_stacking_caster", new Vec3(4.5, 1.0, 4.5));
        Runnable cleanup = () -> {
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(Entity::discard);
            GameTestPlayers.remove(helper, caster);
        };
        try {
            BlockPos a = helper.absolutePos(new BlockPos(1, 1, 1));
            BlockPos b = helper.absolutePos(new BlockPos(2, 1, 1));
            BlockPos overlap = helper.absolutePos(new BlockPos(1, 1, 1)).east();
            EntityHeatZone weak = place(caster, a, 3, 3.0, EntityHeatZone.class);
            EntityHeatZone strong = place(caster, b, 3, 8.0, EntityHeatZone.class);
            helper.assertTrue(strong.isStrongestAt(overlap) && !weak.isStrongestAt(overlap),
                    "Where two Heat Zones overlap, only the one with the higher Power may work");
            BlockPos weakOnly = helper.absolutePos(new BlockPos(0, 1, 1));
            helper.assertTrue(weak.isStrongestAt(weakOnly), "Outside the overlap, the weaker zone still works");

            EntitySoakZone older = place(caster, a, 3, 0.0, EntitySoakZone.class);
            EntitySoakZone newer = place(caster, b, 3, 0.0, EntitySoakZone.class);
            helper.assertTrue(older.isStrongestAt(overlap) && !newer.isStrongestAt(overlap),
                    "Equal zones must leave the overlap to the older one");
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        cleanup.run();
        helper.succeed();
    }

    /** 重なった加重領域は押す向きを足し合わせ（大きさは威力 10 で頭打ち）、代表の 1 つだけが押す。逆向きなら打ち消し合う。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_stacking_gravity", timeoutTicks = 40)
    public static void gravityZonesAddUpToPowerTen(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_stacking_gravity", new Vec3(4.5, 1.0, 4.5));
        Runnable cleanup = () -> {
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(Entity::discard);
            GameTestPlayers.remove(helper, caster);
        };
        try {
            BlockPos center = helper.absolutePos(new BlockPos(1, 1, 1));
            EntityGravityZone first = placeGravity(caster, center, new Vector3(0, 1, 0), 8.0);
            EntityGravityZone second = placeGravity(caster, center, new Vector3(0, 1, 0), 8.0);
            Vec3 combined = first.combinedPushAt(center);
            helper.assertTrue(combined != null && Math.abs(combined.length() - EntityGravityZone.MAX_COMBINED_POWER) < 1.0E-6
                            && combined.y > 0.0, "Two pushes of Power 8 must add up and stop at Power 10: " + combined);
            helper.assertTrue(second.combinedPushAt(center) == null, "Only one zone may push where they overlap");
            EntityGravityZone opposite = placeGravity(caster, center, new Vector3(0, -1, 0), 8.0);
            Vec3 cancelled = first.combinedPushAt(center);
            helper.assertTrue(cancelled != null && Math.abs(cancelled.y - 8.0) < 1.0E-6,
                    "Up 8 + up 8 + down 8 must leave up 8: " + cancelled);
            helper.assertTrue(EntityGravityZone.capCombined(new Vec3(3.0, 4.0, 0.0)).length() == 5.0,
                    "A sum below the cap must stay as it is");
            opposite.discard();
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        cleanup.run();
        helper.succeed();
    }

    private static MatrixValue cube(BlockPos center, int size) {
        double low = -(size / 2) - 0.5;
        return new MatrixValue(4, 4, new double[]{
                size, 0, 0, center.getX() + low,
                0, size, 0, center.getY() + low,
                0, 0, size, center.getZ() + low,
                0, 0, 0, 1});
    }

    /** 置いた領域（{@code power} が 0 なら威力の入力なし）。 */
    private static <T extends SpellGramZone> T place(ServerPlayer caster, BlockPos center, int size, double power, Class<T> type)
            throws SpellRuntimeException {
        MatrixValue matrix = cube(center, size);
        PieceTrickZoneBase<?> piece = type == EntityHeatZone.class
                ? new PieceTrickHeatZone(new Spell()) {
                    @Override
                    public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                        return input(param, matrix, size, power);
                    }
                }
                : new PieceTrickSoakZone(new Spell()) {
                    @Override
                    public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                        return input(param, matrix, size, power);
                    }
                };
        List<? extends SpellGramZone> before = SpellGramIndex.all(caster.level(), type);
        piece.execute(new SpellContext().setPlayer(caster));
        return SpellGramIndex.all(caster.level(), type).stream().filter(zone -> !before.contains(zone)).findFirst().orElseThrow();
    }

    private static EntityGravityZone placeGravity(ServerPlayer caster, BlockPos center, Vector3 direction, double power)
            throws SpellRuntimeException {
        MatrixValue matrix = cube(center, 3);
        var piece = new PieceTrickGravityZone(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                if (param.name.equals(SpellParam.GENERIC_NAME_DIRECTION)) {
                    return direction;
                }
                return input(param, matrix, 3, power);
            }
        };
        List<EntityGravityZone> before = SpellGramIndex.all(caster.level(), EntityGravityZone.class);
        piece.execute(new SpellContext().setPlayer(caster));
        return SpellGramIndex.all(caster.level(), EntityGravityZone.class).stream().filter(zone -> !before.contains(zone))
                .findFirst().orElseThrow();
    }

    private static Object input(SpellParam<?> param, MatrixValue matrix, int size, double power) {
        if (param.name.equals(PsitweaksSpellParams.MATRIX)) {
            return matrix;
        }
        if (param.name.equals(PsitweaksSpellParams.SIZE)) {
            return (double) size;
        }
        return param.name.equals(SpellParam.GENERIC_NAME_POWER) && power > 0.0 ? power : null;
    }
}
