package com.moratan251.psitweaks.common.gametest;

import com.mojang.logging.LogUtils;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityHeatZone;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityInterferenceZone;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntitySoakZone;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityTurbulenceZone;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.SpellGramZone;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.ZoneArea;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.slf4j.Logger;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * 領域の重さを測る（2026-11-13、領域干渉と機械連携の軽量化の前後比較）。ふだんの GameTest では何もしない。ゲームのフォルダ
 * （run/）に {@code psitweaks-zone-benchmark} という名前のファイルを置いたとき（または環境変数
 * {@code PSITWEAKS_ZONE_BENCHMARK=1}、システムプロパティ {@code psitweaks.zoneBenchmark=true}）だけ測り、ログに
 * {@code [ZoneBench]} で出す。時間は機械の速さで変わるので合否は見ない。
 * <p>
 * ほかのテストから離れた場所（テストの位置から x に 200,000）の 2×2 チャンクに、ブロックエンティティ（チェスト）を 2 段
 * 敷き詰め、一辺 16 の加熱領域を 4 チャンクにまたがるように置く。範囲の中には、入っていれば Mekanism の抵抗型発熱器と
 * Create のデポを並べる。領域の tick は手で回し、そのたびにゲーム時刻を 1 進める（tick ごとの覚え書きが本物と同じように
 * 作り直されるように。最後に元へ戻す）。
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ZoneLoadBenchmarkGameTests {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int WARMUP_TICKS = 2000;
    private static final int MEASURED_TICKS = 1000;
    private static final int ROUNDS = 3;
    /** 台帳を増やすための、加熱領域と重ならない小さな領域の数。 */
    private static final int OTHER_OBJECTS = 60;
    private static final int ZONE_SIZE = 16;
    /** ゲームのフォルダ（run/）にこの名前のファイルがあれば測る（Gradle の実行に環境変数が渡らないときのため）。 */
    private static final String FLAG_FILE = "psitweaks-zone-benchmark";

    private ZoneLoadBenchmarkGameTests() {
    }

    static boolean enabled() {
        return "1".equals(System.getenv("PSITWEAKS_ZONE_BENCHMARK")) || Boolean.getBoolean("psitweaks.zoneBenchmark")
                || java.nio.file.Files.exists(net.neoforged.fml.loading.FMLPaths.GAMEDIR.get().resolve(FLAG_FILE));
    }

    @GameTest(template = "psi110_empty", batch = "psitweaks_zone_benchmark", timeoutTicks = 100)
    public static void zoneLoad(GameTestHelper helper) throws SpellRuntimeException {
        if (!enabled()) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_bench", new Vec3(0.5, 1.0, 0.5));
        ServerPlayer other = GameTestPlayers.inLevel(helper, "zone_bench_other", new Vec3(0.5, 1.0, 0.5));
        BlockPos base = helper.absolutePos(BlockPos.ZERO);
        // チャンクの角に中心を置き、範囲を 2×2 チャンクにまたがらせる。
        int cx = ((base.getX() + 200_000) >> 4) << 4;
        int cz = (base.getZ() >> 4) << 4;
        BlockPos center = new BlockPos(cx, base.getY() + 16, cz);
        List<SpellGramObject> placed = new ArrayList<>();
        List<ChunkPos> forced = new ArrayList<>();
        ServerLevelData data = (ServerLevelData) level.getLevelData();
        long savedTime = data.getGameTime();
        try {
            for (int dx = -1; dx <= 0; dx++) {
                for (int dz = -1; dz <= 0; dz++) {
                    ChunkPos chunk = new ChunkPos((cx >> 4) + dx, (cz >> 4) + dz);
                    level.setChunkForced(chunk.x, chunk.z, true);
                    forced.add(chunk);
                }
            }
            int fillers = fill(level, center);
            int machines = placeMachines(level, center);

            EntityHeatZone heat = new EntityHeatZone(level);
            heat.setPower(5.0F);
            place(level, heat, ZoneArea.cube(center, ZONE_SIZE), caster, 2.0, placed);
            double alone = measure(data, heat);

            for (int i = 0; i < OTHER_OBJECTS; i++) {
                // 加熱領域より上に置く、一辺 1 の湿潤領域（重ならない）。
                BlockPos at = center.offset(-15 + (i % 10) * 3, ZONE_SIZE + 2 + (i / 10) * 2, -15);
                place(level, new EntitySoakZone(level), ZoneArea.cube(at, 1), other, 1.0, placed);
            }
            double withObjects = measure(data, heat);

            // 他者の、置いた時点の事象干渉力が低い（止めはしないが判定には出てくる）領域干渉を重ねる。
            place(level, new EntityInterferenceZone(level), ZoneArea.cube(center, ZONE_SIZE), other, 1.0, placed);
            double withInterference = measure(data, heat);

            // 同じ術者の乱流領域を重ねる（熱乱流: 機械への上乗せと、乱流の判定が増える）。
            EntityTurbulenceZone turbulence = new EntityTurbulenceZone(level);
            turbulence.configureWind(10.0F);
            place(level, turbulence, ZoneArea.cube(center, ZONE_SIZE), caster, 2.0, placed);
            double withTurbulence = measure(data, heat);

            LOGGER.info("[ZoneBench] block entities in 4 chunks: {}, machines in the zone: {}, objects: {}",
                    fillers + machines, machines, placed.size());
            LOGGER.info(String.format("[ZoneBench] heat zone tick (us): alone %.1f, +%d objects %.1f, +interference %.1f,"
                    + " +own turbulence %.1f", alone, OTHER_OBJECTS, withObjects, withInterference, withTurbulence));
        } finally {
            data.setGameTime(savedTime);
            placed.forEach(Entity::discard);
            forced.forEach(chunk -> level.setChunkForced(chunk.x, chunk.z, false));
            GameTestPlayers.remove(helper, caster);
            GameTestPlayers.remove(helper, other);
        }
        helper.succeed();
    }

    private static void place(ServerLevel level, SpellGramZone zone, ZoneArea area, ServerPlayer caster, double interference,
                              List<SpellGramObject> placed) {
        zone.configureZone(area, 0xFFFFFF);
        zone.setMachineSpeed(SpellGramZone.machineSpeedFor(interference));
        zone.setPlacement(caster.getUUID(), interference);
        zone.setCaster(caster);
        zone.setLifetimeTicks(1_000_000);
        zone.setMaxIntegrity(1.0E9F);
        level.addFreshEntity(zone);
        placed.add(zone);
    }

    /**
     * 領域の tick を、ゲーム時刻を 1 ずつ進めながら回し、1 tick あたりの時間（マイクロ秒）を返す。慣らしのあと
     * {@value #ROUNDS} 回測って、いちばん速い回を使う（JIT やほかのスレッドの揺れを減らす）。
     */
    private static double measure(ServerLevelData data, SpellGramZone zone) {
        for (int i = 0; i < WARMUP_TICKS; i++) {
            data.setGameTime(data.getGameTime() + 1);
            zone.tick();
        }
        double best = Double.MAX_VALUE;
        for (int round = 0; round < ROUNDS; round++) {
            long start = System.nanoTime();
            for (int i = 0; i < MEASURED_TICKS; i++) {
                data.setGameTime(data.getGameTime() + 1);
                zone.tick();
            }
            best = Math.min(best, (System.nanoTime() - start) / 1000.0 / MEASURED_TICKS);
        }
        return best;
    }

    /** 4 チャンク（32×32）に、領域の底の高さからチェストを 2 段敷き詰める。置いた数を返す。 */
    private static int fill(ServerLevel level, BlockPos center) {
        int count = 0;
        int y0 = center.getY() - ZONE_SIZE / 2;
        for (int x = center.getX() - 16; x < center.getX() + 16; x++) {
            for (int z = center.getZ() - 16; z < center.getZ() + 16; z++) {
                for (int y = y0; y < y0 + 2; y++) {
                    level.setBlock(new BlockPos(x, y, z), Blocks.CHEST.defaultBlockState(), 2);
                    count++;
                }
            }
        }
        return count;
    }

    /** 範囲の中に、入っていれば Mekanism の抵抗型発熱器と Create のデポを 10 個ずつ並べる。置いた数を返す。 */
    private static int placeMachines(ServerLevel level, BlockPos center) {
        int count = 0;
        int y = center.getY() - ZONE_SIZE / 2 + 2;
        Optional<Block> heater = block("mekanism:resistive_heater");
        Optional<Block> depot = block("create:depot");
        for (int i = 0; i < 10; i++) {
            if (heater.isPresent()) {
                level.setBlock(new BlockPos(center.getX() - 7 + i, y, center.getZ() - 6), heater.get().defaultBlockState(), 2);
                count++;
            }
            if (depot.isPresent()) {
                level.setBlock(new BlockPos(center.getX() - 7 + i, y, center.getZ() + 6), depot.get().defaultBlockState(), 2);
                count++;
            }
        }
        return count;
    }

    private static Optional<Block> block(String id) {
        return BuiltInRegistries.BLOCK.getOptional(ResourceLocation.parse(id));
    }
}
