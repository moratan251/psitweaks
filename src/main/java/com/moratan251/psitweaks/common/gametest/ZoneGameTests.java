package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIndex;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityGravityZone;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntitySoakZone;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.ZoneArea;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.combat.CombatStatuses;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickGravityZone;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickSoakZone;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickZoneBase;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Husk;
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
 * 領域型の魔法式オブジェクト（試作: 湿潤領域・加重領域）。範囲は領域行列を中心 ±大きさで切り詰めたブロックの集まりで、
 * 中の生物にだけ効く。
 * <p>
 * プレイヤーをワールドに入れるので、他のテストと同時に動かないようバッチを分ける。
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ZoneGameTests {
    private static final String TEMPLATE = "connector_empty";

    private ZoneGameTests() {
    }

    /**
     * 範囲は行列の中のブロックのうち、行列の中心のブロックを中心にした一辺サイズの立方体に入るもの（演算子: 直方体領域と
     * 同じ数え方で、偶数は正の向きに 1 つ多い）。行列が小さければ行列の範囲。
     */
    @GameTest(template = TEMPLATE)
    public static void zoneAreaIsTheMatrixClippedAroundItsCenter(GameTestHelper helper) throws SpellRuntimeException {
        // ブロック 0〜8 を含む 9×9×9 の行列（作動式: 領域表示のブロックのモードと同じく、整数の座標で判定する）。
        MatrixValue big = cube(new Vector3(-0.5, -0.5, -0.5), 9);
        ZoneArea clipped = ZoneArea.of(big, 5);
        helper.assertTrue(clipped.center().equals(new BlockPos(4, 4, 4)), "The center block should be 4,4,4: " + clipped.center());
        helper.assertTrue(clipped.contains(new BlockPos(2, 4, 6)) && clipped.contains(new BlockPos(6, 2, 2)),
                "Blocks of the 5-block cube around the center must be inside");
        helper.assertFalse(clipped.contains(new BlockPos(1, 4, 4)) || clipped.contains(new BlockPos(4, 7, 4)),
                "Blocks farther than the Size must be outside");
        helper.assertTrue(clipped.bounds().getXsize() == 5 && clipped.bounds().getYsize() == 5,
                "The outline must wrap 5 blocks per axis: " + clipped.bounds());

        ZoneArea even = ZoneArea.of(big, 4);
        helper.assertTrue(even.contains(new BlockPos(3, 4, 4)) && even.contains(new BlockPos(6, 4, 4))
                        && !even.contains(new BlockPos(2, 4, 4)) && !even.contains(new BlockPos(7, 4, 4)),
                "An even Size must reach one block farther on the positive side, like Operator: Cuboid Region");

        ZoneArea whole = ZoneArea.of(big, 65);
        helper.assertTrue(whole.contains(new BlockPos(0, 0, 0)) && whole.contains(new BlockPos(8, 8, 8))
                        && !whole.contains(new BlockPos(9, 4, 4)) && whole.bounds().getXsize() == 9,
                "A matrix smaller than the Size must be the zone as it is");
        helper.assertTrue(PieceTrickZoneBase.areaOf(5) == 25 && PieceTrickZoneBase.areaOf(17) == 289,
                "The area is the Size squared");
        helper.succeed();
    }

    /** 湿潤領域の中の生物は濡れ、外の生物は濡れない。置き直すと古い領域は消える（1 人 1 つ）。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_soak", timeoutTicks = 100)
    public static void soakZoneSoaksOnlyTheBeingsInside(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_soak_caster", new Vec3(0.5, 1.0, 0.5));
        Husk inside = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(2.5, 1.0, 2.5));
        Husk outside = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(6.5, 1.0, 6.5));
        inside.setNoAi(true);
        outside.setNoAi(true);
        BlockPos center = helper.absolutePos(new BlockPos(2, 1, 2));
        Runnable cleanup = () -> {
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(zone -> zone.discard());
            GameTestPlayers.remove(helper, caster);
        };
        try {
            // 中心のブロックを中心にした 3×3×3（サイズ 3）。行列は 7×7×7 なのでサイズで切り詰められる。
            castSoak(caster, cube(vector(center).add(-3.5, -3.5, -3.5), 7), 3.0);
            castSoak(caster, cube(vector(center).add(-3.5, -3.5, -3.5), 7), 3.0);
            List<EntitySoakZone> zones = SpellGramIndex.near(level, Vec3.atCenterOf(center), 0.5, EntitySoakZone.class);
            helper.assertTrue(zones.size() == 1, "Placing a Soak Zone again must end the old one, found " + zones.size());
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(15, () -> {
            boolean insideWet = CombatStatuses.isSoaked(inside);
            boolean outsideWet = CombatStatuses.isSoaked(outside);
            cleanup.run();
            helper.assertTrue(insideWet, "The husk inside the Soak Zone must be wet");
            helper.assertFalse(outsideWet, "The husk outside the Soak Zone must stay dry");
            helper.succeed();
        });
    }

    /** 加重領域は中の生物を指定の向きへ押し続ける（上向きなら持ち上がる）。外の生物は持ち上げない。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_gravity", timeoutTicks = 100)
    public static void gravityZonePushesTheBeingsInside(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_gravity_caster", new Vec3(0.5, 1.0, 0.5));
        Husk inside = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(2.5, 1.0, 2.5));
        Husk outside = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(6.5, 1.0, 6.5));
        BlockPos center = helper.absolutePos(new BlockPos(2, 2, 2));
        double insideY = inside.getY();
        double outsideY = outside.getY();
        Runnable cleanup = () -> {
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(zone -> zone.discard());
            GameTestPlayers.remove(helper, caster);
        };
        try {
            // 5×5×5（サイズ 5）。威力 10 で上向きに押す。
            var piece = new PieceTrickGravityZone(new Spell()) {
                @Override
                public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                    if (param.name.equals(PsitweaksSpellParams.MATRIX)) {
                        return cube(vector(center).add(-2.5, -2.5, -2.5), 5);
                    }
                    if (param.name.equals(PsitweaksSpellParams.SIZE)) {
                        return 5.0;
                    }
                    return param.name.equals(SpellParam.GENERIC_NAME_DIRECTION) ? new Vector3(0, 3, 0) : 10.0;
                }
            };
            piece.execute(new SpellContext().setPlayer(caster));
            List<EntityGravityZone> zones = SpellGramIndex.near(level, Vec3.atCenterOf(center), 0.5, EntityGravityZone.class);
            helper.assertTrue(zones.size() == 1 && zones.get(0).getPushDirection().y > 0.99,
                    "The Gravity Zone must be placed with an upward direction");
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(12, () -> {
            double insideRise = inside.getY() - insideY;
            double outsideRise = outside.getY() - outsideY;
            cleanup.run();
            helper.assertTrue(insideRise > 0.5, "The husk inside must be lifted, rose " + insideRise);
            helper.assertTrue(outsideRise < 0.1, "The husk outside must not be lifted, rose " + outsideRise);
            helper.succeed();
        });
    }

    private static void castSoak(ServerPlayer caster, MatrixValue matrix, double size) throws SpellRuntimeException {
        var piece = new PieceTrickSoakZone(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return param.name.equals(PsitweaksSpellParams.MATRIX) ? matrix : size;
            }
        };
        piece.execute(new SpellContext().setPlayer(caster));
    }

    private static Vector3 vector(BlockPos pos) {
        return new Vector3(pos.getX(), pos.getY(), pos.getZ());
    }

    private static MatrixValue cube(Vector3 start, double edge) {
        return new MatrixValue(4, 4, new double[]{
                edge, 0, 0, start.x,
                0, edge, 0, start.y,
                0, 0, edge, start.z,
                0, 0, 0, 1});
    }
}
