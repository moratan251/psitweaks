package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIndex;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntitySilenceZone;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickSilenceZone;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.entity.SculkShriekerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * 作動式: 遮音領域（2026-11-11）。振動は羊毛と同じく範囲を通れず、範囲の中のスカルクシュリーカーは叫ばない。音を鳴らさない
 * 処理はクライアント側なので、ここでは「点が範囲の中か」だけを確かめる。
 * <p>
 * スカルクセンサーは近くのどの振動にも反応するので、テストごとにバッチを分け、同時に動かさない。
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SilenceZoneGameTests {
    private static final String TEMPLATE = "connector_empty";
    /** 振動が届いてスカルクセンサーが動き出すまで待つ tick（距離 6 ブロックほどで 6 tick ほど）。 */
    private static final int HEAR_TICKS = 20;

    private SilenceZoneGameTests() {
    }

    /** 比べるための土台: 遮音領域がなければ、5 ブロックほど先の振動でスカルクセンサーが動く。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_silence_control", timeoutTicks = 60)
    public static void sculkSensorHearsWithoutSilenceZone(GameTestHelper helper) {
        BlockPos sensor = new BlockPos(0, 1, 0);
        helper.setBlock(sensor, Blocks.SCULK_SENSOR);
        helper.runAfterDelay(2, () -> {
            helper.getLevel().gameEvent(null, GameEvent.BLOCK_PLACE, helper.absolutePos(new BlockPos(4, 1, 4)));
            helper.runAfterDelay(HEAR_TICKS, () -> {
                helper.assertTrue(isActive(helper, sensor), "The Sculk Sensor must hear a vibration 5 blocks away");
                helper.succeed();
            });
        });
    }

    /** 範囲の中で起きた振動は、外のスカルクセンサーに届かない。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_silence_inside", timeoutTicks = 60)
    public static void silenceZoneStopsVibrationsMadeInside(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos sensor = new BlockPos(0, 1, 0);
        BlockPos source = new BlockPos(4, 1, 4);
        helper.setBlock(sensor, Blocks.SCULK_SENSOR);
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "silence_inside_caster", new Vec3(4.5, 1.0, 4.5));
        Runnable cleanup = cleanup(helper, caster);
        try {
            placeSilence(caster, helper.absolutePos(source), 1);
            Vec3 sourceAt = Vec3.atCenterOf(helper.absolutePos(source));
            Vec3 sensorAt = Vec3.atCenterOf(helper.absolutePos(sensor));
            helper.assertTrue(EntitySilenceZone.silences(level, sourceAt) && !EntitySilenceZone.silences(level, sensorAt),
                    "Only the block of the zone must be silenced");
            helper.assertTrue(EntitySilenceZone.blocksPath(level, sourceAt, sensorAt), "A vibration from inside must be blocked");
            Vec3 besideA = Vec3.atCenterOf(helper.absolutePos(new BlockPos(3, 1, 4)));
            Vec3 besideB = Vec3.atCenterOf(helper.absolutePos(new BlockPos(3, 1, 0)));
            helper.assertFalse(EntitySilenceZone.blocksPath(level, besideA, besideB), "A path that misses the zone must stay open");
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(2, () -> {
            level.gameEvent(null, GameEvent.BLOCK_PLACE, helper.absolutePos(source));
            helper.runAfterDelay(HEAR_TICKS, () -> {
                boolean active = isActive(helper, sensor);
                cleanup.run();
                helper.assertFalse(active, "A vibration made inside a Silence Zone must not reach the Sculk Sensor");
                helper.succeed();
            });
        });
    }

    /** 範囲の中のスカルクセンサーには、外の振動も、踏んだときの振動も届かない。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_silence_listener", timeoutTicks = 80)
    public static void silenceZoneDeafensTheSensorInside(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos sensor = new BlockPos(0, 1, 0);
        helper.setBlock(sensor, Blocks.SCULK_SENSOR);
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "silence_listener_caster", new Vec3(1.5, 1.0, 1.5));
        Husk walker = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(0.5, 2.0, 0.5));
        walker.setNoAi(true);
        Runnable cleanup = () -> {
            walker.discard();
            cleanup(helper, caster).run();
        };
        try {
            // 中心のブロックのまわり 3×3×3（センサーと、その上と、術者の足もと）。
            placeSilence(caster, helper.absolutePos(sensor), 3);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(2, () -> {
            level.gameEvent(null, GameEvent.BLOCK_PLACE, helper.absolutePos(new BlockPos(4, 1, 4)));
            BlockPos at = helper.absolutePos(sensor);
            BlockState state = level.getBlockState(at);
            state.getBlock().stepOn(level, at, state, walker);
            helper.runAfterDelay(HEAR_TICKS, () -> {
                boolean active = isActive(helper, sensor);
                cleanup.run();
                helper.assertFalse(active, "A Sculk Sensor inside a Silence Zone must hear neither outside vibrations nor steps");
                helper.succeed();
            });
        });
    }

    /** 範囲の中のスカルクシュリーカーは叫ばず、範囲の中のプレイヤーに踏まれたシュリーカーも叫ばない。外なら叫ぶ。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_silence_shrieker", timeoutTicks = 40)
    public static void silenceZoneStopsShriekers(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos inside = new BlockPos(0, 1, 0);
        BlockPos outside = new BlockPos(4, 1, 4);
        helper.setBlock(inside, Blocks.SCULK_SHRIEKER);
        helper.setBlock(outside, Blocks.SCULK_SHRIEKER);
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "silence_shriek_caster", new Vec3(0.5, 2.0, 0.5));
        ServerPlayer stranger = GameTestPlayers.inLevel(helper, "silence_shriek_stranger", new Vec3(4.5, 2.0, 4.5));
        Runnable cleanup = () -> {
            cleanup(helper, caster).run();
            GameTestPlayers.remove(helper, stranger);
        };
        try {
            placeSilence(caster, helper.absolutePos(inside), 3);
            shrieker(helper, inside).tryShriek(level, stranger);
            helper.assertFalse(isShrieking(helper, inside), "A Sculk Shrieker inside a Silence Zone must not shriek");
            shrieker(helper, outside).tryShriek(level, caster);
            helper.assertFalse(isShrieking(helper, outside), "A player inside a Silence Zone must not set off a Sculk Shrieker");
            shrieker(helper, outside).tryShriek(level, stranger);
            helper.assertTrue(isShrieking(helper, outside), "A Sculk Shrieker outside the zone must still shriek");
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        cleanup.run();
        helper.succeed();
    }

    private static Runnable cleanup(GameTestHelper helper, ServerPlayer caster) {
        return () -> {
            SpellGramIndex.ofCaster(helper.getLevel(), caster.getUUID()).forEach(Entity::discard);
            GameTestPlayers.remove(helper, caster);
        };
    }

    private static boolean isActive(GameTestHelper helper, BlockPos sensor) {
        BlockState state = helper.getBlockState(sensor);
        return state.is(Blocks.SCULK_SENSOR) && SculkSensorBlock.getPhase(state) != SculkSensorPhase.INACTIVE;
    }

    private static boolean isShrieking(GameTestHelper helper, BlockPos pos) {
        return helper.getBlockState(pos).getValue(SculkShriekerBlock.SHRIEKING);
    }

    private static SculkShriekerBlockEntity shrieker(GameTestHelper helper, BlockPos pos) {
        return (SculkShriekerBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
    }

    /** {@code center} のブロックを中心にした一辺 {@code size}（奇数）の遮音領域を置く。 */
    private static void placeSilence(ServerPlayer caster, BlockPos center, int size) throws SpellRuntimeException {
        double low = -(size / 2) - 0.5;
        MatrixValue matrix = new MatrixValue(4, 4, new double[]{
                size, 0, 0, center.getX() + low,
                0, size, 0, center.getY() + low,
                0, 0, size, center.getZ() + low,
                0, 0, 0, 1});
        var piece = new PieceTrickSilenceZone(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                if (param.name.equals(PsitweaksSpellParams.MATRIX)) {
                    return matrix;
                }
                return param.name.equals(PsitweaksSpellParams.SIZE) ? (double) size : null;
            }
        };
        piece.execute(new SpellContext().setPlayer(caster));
    }
}
