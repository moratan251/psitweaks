package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.compat.CreateCompat;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.compat.MekanismGameplayCompat;
import com.moratan251.psitweaks.common.compat.SablePhysicsCompat;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIndex;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.SpellGramZone;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickHeatZone;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickSoakZone;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickZoneBase;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.common.spell.operator.vector.PieceOperatorVectorRaycast;

/**
 * Sable の機体（サブレベル）に向けて置いた領域が、置いた時点の機体の見た目の場所で効くこと（2026-11-11、方針 A）。
 * Sable がなければ飛ばす。Sable の型（companion）はコンパイル時に見えないので、機体を組む呼び出しはリフレクションにする。
 * 開発クライアントを開いたまま Sable 入りの GameTest サーバーを起動すると、Sable の DLL が使われていて起動直後に落ちる。
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SableZoneGameTests {
    private static final String TEMPLATE = "connector_empty";
    /** 領域のサイズ。機体の床の上に立つ生き物の中心（床のブロックの中心から 1.5 ほど上）が入る大きさ。 */
    private static final double SIZE = 5.0;
    /** 機体に載せるもの（テストの場所の位置）: 床の上に焚き火、ブレイズバーナー、抵抗型発熱器。 */
    private static final BlockPos CAMPFIRE_ON_SHIP = new BlockPos(2, 2, 1);
    private static final BlockPos BURNER_ON_SHIP = new BlockPos(3, 2, 3);
    private static final BlockPos HEATER_ON_SHIP = new BlockPos(1, 2, 3);

    private SableZoneGameTests() {
    }

    /**
     * 3×3 の石の機体を 30 度回し、真上から演算子: ベクトルレイキャストで狙った点（機体の座標）に加熱領域を置く。領域は世界の座標へ
     * 写され、機体の見た目の床を覆い、床の上の生き物を燃やし、狙った機体を（加重領域と同じ呼び出しで）押せる。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_sable_projection", timeoutTicks = 100)
    public static void zoneAimedAtShipIsPlacedWhereTheShipIsSeen(GameTestHelper helper) {
        if (!ModList.get().isLoaded("sable")) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        Object ship;
        Object sable;
        try {
            sable = Class.forName("dev.ryanhcode.sable.Sable").getField("HELPER").get(null);
            ship = assembleTurnedPlatform(helper, level, 30.0);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Could not assemble a Sable sub-level for the test", e);
        }
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_sable_caster", new Vec3(0.5, 1.0, 0.5));
        Husk[] husk = new Husk[1];
        SpellGramZone[] zone = new SpellGramZone[1];

        helper.runAtTickTime(10, () -> {
            try {
                Vector3d shipCenter = (Vector3d) call(call(ship, "logicalPose"), "position");
                Vec3 from = new Vec3(shipCenter.x, shipCenter.y + 3.0, shipCenter.z);
                BlockHitResult hit = PieceOperatorVectorRaycast.raycast(caster, new Vector3(from.x, from.y, from.z),
                        new Vector3(0, -1, 0), 32.0);
                helper.assertTrue(hit.getType() == HitResult.Type.BLOCK, "The raycast must hit the ship");
                BlockPos plotHit = hit.getBlockPos().immutable();
                Vec3 seenHit = project(sable, level, Vec3.atCenterOf(plotHit));
                helper.assertTrue(seenHit.distanceTo(Vec3.atCenterOf(plotHit)) > 1000.0,
                        "Precondition: the raycast returns the ship's own (plot) coordinates, got " + plotHit);

                husk[0] = helper.spawnWithNoFreeWill(EntityType.HUSK, helper.relativeVec(seenHit.add(0.0, 0.6, 0.0)));
                castHeat(caster, plotHit);
                for (SpellGramObject object : SpellGramIndex.ofCaster(level, caster.getUUID())) {
                    if (object instanceof SpellGramZone found) {
                        zone[0] = found;
                    }
                }
                helper.assertTrue(zone[0] != null, "The Heat Zone must be placed");
                Vec3 zoneCenter = Vec3.atCenterOf(zone[0].getArea().center());
                helper.assertTrue(zoneCenter.distanceTo(seenHit) < 1.0,
                        "The zone must be centered where the ship is seen " + seenHit + ", got " + zoneCenter);
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        Vec3 floor = project(sable, level, Vec3.atCenterOf(plotHit.offset(dx, 0, dz)));
                        helper.assertTrue(zone[0].getArea().contains(BlockPos.containing(floor)),
                                "The zone must cover the ship's floor where it is seen (" + dx + ", " + dz + ")");
                    }
                }
            } catch (ReflectiveOperationException | SpellRuntimeException e) {
                throw new RuntimeException(e);
            }
        });

        helper.runAtTickTime(12, () -> {
            helper.assertTrue(zone[0].entitiesInside().contains(husk[0]),
                    "The husk standing on the ship must be inside the zone");
            int pushed = SablePhysicsCompat.accelerateSubLevels(level, zone[0].getOutline(), new Vec3(0.0, 1.0, 0.0),
                    0.001, zone[0]::isInsideAndActive);
            helper.assertTrue(pushed == 1, "The Gravity Zone's push must find the aimed ship, pushed " + pushed);
        });

        helper.runAtTickTime(25, () -> {
            boolean burning = husk[0].getRemainingFireTicks() > 0;
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(SpellGramObject::discard);
            GameTestPlayers.remove(helper, caster);
            helper.assertTrue(burning, "The Heat Zone aimed at the ship must burn the husk standing on it");
            helper.succeed();
        });
    }

    /**
     * 世界に置いた加熱領域が、範囲に重なった機体（30 度回した 3×3 の石の床）の機械にも効く（2026-11-11、範囲を機体の座標へ
     * 写して見る）: 床の上のブレイズバーナー（Create があれば）が「加熱」になり、抵抗型発熱器（Mekanism があれば）が温まる。
     * どちらもなければ飛ばす。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_sable_ship_machines", timeoutTicks = 120)
    public static void worldHeatZoneReachesMachinesOnShips(GameTestHelper helper) {
        Optional<Block> burner = block("create:blaze_burner");
        Optional<Block> heater = block("mekanism:resistive_heater");
        boolean withBurner = CreateCompat.isCreateLoaded() && burner.isPresent();
        boolean withHeater = MekanismCompat.isMekanismLoaded() && heater.isPresent();
        if (!ModList.get().isLoaded("sable") || (!withBurner && !withHeater)) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        Map<BlockPos, BlockState> extra = new HashMap<>();
        if (withBurner) {
            extra.put(BURNER_ON_SHIP, withProperty(burner.get().defaultBlockState(), "blaze", "smouldering"));
        }
        if (withHeater) {
            extra.put(HEATER_ON_SHIP, heater.get().defaultBlockState());
        }
        Object ship;
        BlockPos plotAnchor;
        try {
            ship = assembleTurnedPlatform(helper, level, 30.0, extra);
            plotAnchor = plotAnchorOf(ship);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Could not assemble a Sable sub-level for the test", e);
        }
        BlockPos burnerPlot = onPlot(plotAnchor, BURNER_ON_SHIP);
        BlockPos heaterPlot = onPlot(plotAnchor, HEATER_ON_SHIP);
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_sable_machines_caster", new Vec3(0.5, 1.0, 0.5));
        double[] heaterBefore = new double[1];

        helper.runAtTickTime(10, () -> {
            try {
                if (withBurner) {
                    helper.assertTrue(burner.get() == level.getBlockState(burnerPlot).getBlock(),
                            "Precondition: the Blaze Burner must be on the ship at " + burnerPlot);
                }
                if (withHeater) {
                    heaterBefore[0] = MekanismGameplayCompat.temperatureAt(level, heaterPlot);
                    helper.assertTrue(!Double.isNaN(heaterBefore[0]), "Precondition: the heater must be on the ship");
                }
                castOverShip(caster, ship, heatPiece(), 5.0);
            } catch (ReflectiveOperationException | SpellRuntimeException e) {
                throw new RuntimeException(e);
            }
        });

        helper.runAtTickTime(60, () -> {
            String burnerLevel = withBurner ? propertyValue(level.getBlockState(burnerPlot), "blaze") : "kindled";
            double heaterAfter = withHeater ? MekanismGameplayCompat.temperatureAt(level, heaterPlot) : Double.NaN;
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(SpellGramObject::discard);
            GameTestPlayers.remove(helper, caster);
            helper.assertTrue("kindled".equals(burnerLevel),
                    "The Heat Zone must kindle the Blaze Burner on the ship, was " + burnerLevel);
            if (withHeater) {
                helper.assertTrue(heaterAfter > heaterBefore[0] + 5.0, "The Heat Zone must heat the Resistive Heater on"
                        + " the ship, " + heaterBefore[0] + " K -> " + heaterAfter + " K");
            }
            helper.succeed();
        });
    }

    /**
     * 世界に置いた湿潤領域が、範囲に重なった機体（30 度回した 3×3 の石の床）のブロックにも効く（ブロックを 1 段ずつ見る走査も
     * 機体の座標へ写した範囲で回る）: 床の上の火のついた焚き火が消える。氷は機体が床に当たると割れることがあるので使わない。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_sable_ship_blocks", timeoutTicks = 120)
    public static void worldSoakZoneReachesBlocksOnShips(GameTestHelper helper) {
        if (!ModList.get().isLoaded("sable")) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        Object ship;
        BlockPos plotAnchor;
        try {
            ship = assembleTurnedPlatform(helper, level, 30.0,
                    Map.of(CAMPFIRE_ON_SHIP, Blocks.CAMPFIRE.defaultBlockState()));
            plotAnchor = plotAnchorOf(ship);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Could not assemble a Sable sub-level for the test", e);
        }
        BlockPos campfirePlot = onPlot(plotAnchor, CAMPFIRE_ON_SHIP);
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_sable_blocks_caster", new Vec3(0.5, 1.0, 0.5));

        helper.runAtTickTime(10, () -> {
            try {
                helper.assertTrue(CampfireBlock.isLitCampfire(level.getBlockState(campfirePlot)),
                        "Precondition: the lit campfire must be on the ship at " + campfirePlot);
                castOverShip(caster, ship, soakPiece(), 1.0);
            } catch (ReflectiveOperationException | SpellRuntimeException e) {
                throw new RuntimeException(e);
            }
        });

        helper.runAtTickTime(40, () -> {
            BlockState campfire = level.getBlockState(campfirePlot);
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(SpellGramObject::discard);
            GameTestPlayers.remove(helper, caster);
            helper.assertTrue(campfire.is(Blocks.CAMPFIRE) && !CampfireBlock.isLitCampfire(campfire),
                    "The Soak Zone must put out the campfire on the ship, got " + campfire);
            helper.succeed();
        });
    }

    /** 機体用の空間の中心のブロック（組むとき、床の角（テストの場所の (1, 1, 1)）が向きを変えずにここへ移る）。 */
    private static BlockPos plotAnchorOf(Object ship) throws ReflectiveOperationException {
        return ((BlockPos) call(call(ship, "getPlot"), "getCenterBlock")).immutable();
    }

    /** テストの場所の位置 {@code relative} に置いて組んだブロックの、機体用の空間での位置。 */
    private static BlockPos onPlot(BlockPos plotAnchor, BlockPos relative) {
        return plotAnchor.offset(relative.subtract(new BlockPos(1, 1, 1)));
    }

    /** 世界の座標で、機体の今見えている場所を覆う一辺 7 の領域を {@code piece} で置く（威力の入力があれば {@code power}）。 */
    private static void castOverShip(ServerPlayer caster, Object ship, PieceTrickZoneBase<?> template, double power)
            throws ReflectiveOperationException, SpellRuntimeException {
        Vector3d shipCenter = (Vector3d) call(call(ship, "logicalPose"), "position");
        castZone(caster, template, BlockPos.containing(shipCenter.x, shipCenter.y, shipCenter.z), 7.0, power);
    }

    /** テストの場所の (1..3, 1, 1..3) に石を並べて機体に組み、床の中心を回転の中心にして {@code yawDegrees} 度回す。 */
    private static Object assembleTurnedPlatform(GameTestHelper helper, ServerLevel level, double yawDegrees)
            throws ReflectiveOperationException {
        return assembleTurnedPlatform(helper, level, yawDegrees, Map.of());
    }

    /**
     * 石の床（テストの場所の (1..3, 1, 1..3)）に、{@code extra}（テストの場所の位置 → ブロック。床の上 y = 2 か、床の置き換え）を
     * 足して機体に組み、床の中心を回転の中心にして {@code yawDegrees} 度回す。
     */
    private static Object assembleTurnedPlatform(GameTestHelper helper, ServerLevel level, double yawDegrees,
                                                 Map<BlockPos, BlockState> extra) throws ReflectiveOperationException {
        BlockPos min = helper.absolutePos(new BlockPos(1, 1, 1));
        BlockPos floorMax = helper.absolutePos(new BlockPos(3, 1, 3));
        List<BlockPos> blocks = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(min, floorMax)) {
            level.setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
            blocks.add(pos.immutable());
        }
        int maxY = floorMax.getY();
        for (Map.Entry<BlockPos, BlockState> entry : extra.entrySet()) {
            BlockPos pos = helper.absolutePos(entry.getKey());
            level.setBlock(pos, entry.getValue(), 3);
            if (!blocks.contains(pos)) {
                blocks.add(pos);
            }
            maxY = Math.max(maxY, pos.getY());
        }
        Class<?> boxClass = Class.forName("dev.ryanhcode.sable.companion.math.BoundingBox3i");
        Object bounds = boxClass.getConstructor(int.class, int.class, int.class, int.class, int.class, int.class)
                .newInstance(min.getX(), min.getY(), min.getZ(), floorMax.getX(), maxY, floorMax.getZ());
        Object ship = Class.forName("dev.ryanhcode.sable.api.SubLevelAssemblyHelper")
                .getMethod("assembleBlocks", ServerLevel.class, BlockPos.class, Iterable.class,
                        Class.forName("dev.ryanhcode.sable.companion.math.BoundingBox3ic"))
                .invoke(null, level, min, blocks, bounds);
        // SubLevelContainer の getMethod は専用サーバーにないクライアントの型を読み込むので、物理は SubLevelPhysicsSystem から取る。
        Object physics = Class.forName("dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem")
                .getMethod("get", Level.class).invoke(null, level);
        Object pipeline = call(physics, "getPipeline");
        for (Method method : pipeline.getClass().getMethods()) {
            if (method.getName().equals("teleport") && method.getParameterCount() == 3) {
                method.invoke(pipeline, ship, new Vector3d(min.getX() + 1.5, min.getY() + 0.5, min.getZ() + 1.5),
                        new Quaterniond().rotateY(Math.toRadians(yawDegrees)));
                return ship;
            }
        }
        throw new NoSuchMethodException("PhysicsPipeline.teleport");
    }

    private static void castHeat(ServerPlayer caster, BlockPos center) throws SpellRuntimeException {
        castZone(caster, heatPiece(), center, SIZE, 5.0);
    }

    /** 入力を castZone の値に差し替えた作動式: 加熱領域。 */
    private static PieceTrickZoneBase<?> heatPiece() {
        return new PieceTrickHeatZone(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return zoneInput(param);
            }
        };
    }

    /** 入力を castZone の値に差し替えた作動式: 湿潤領域。 */
    private static PieceTrickZoneBase<?> soakPiece() {
        return new PieceTrickSoakZone(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return zoneInput(param);
            }
        };
    }

    /**
     * {@code template} と同じ種類の領域を、{@code center} を中心にした一辺 {@code size}（演算子: 直方体領域の Number モードと
     * 同じ行列）で置く。威力の入力があれば {@code power}。
     */
    private static void castZone(ServerPlayer caster, PieceTrickZoneBase<?> template, BlockPos center, double size,
                                 double power) throws SpellRuntimeException {
        ZONE_INPUT.set(new Object[]{center, size, power});
        try {
            template.execute(new SpellContext().setPlayer(caster));
        } finally {
            ZONE_INPUT.remove();
        }
    }

    /** castZone の入力（中心、サイズ、威力）。作動式の入力を差し替えるため、置いている間だけ持つ。 */
    private static final ThreadLocal<Object[]> ZONE_INPUT = new ThreadLocal<>();

    /** 作動式の入力の値（castZone から使う）。 */
    static Object zoneInput(SpellParam<?> param) {
        Object[] input = ZONE_INPUT.get();
        BlockPos center = (BlockPos) input[0];
        double size = (Double) input[1];
        if (param.name.equals(PsitweaksSpellParams.MATRIX)) {
            double low = -Math.floor((size - 1) / 2.0) - 0.5;
            return new MatrixValue(4, 4, new double[]{
                    size, 0, 0, center.getX() + low,
                    0, size, 0, center.getY() + low,
                    0, 0, size, center.getZ() + low,
                    0, 0, 0, 1});
        }
        if (param.name.equals(PsitweaksSpellParams.SIZE)) {
            return size;
        }
        return param.name.equals(SpellParam.GENERIC_NAME_POWER) ? input[2] : null;
    }

    private static Vec3 project(Object sable, Level level, Vec3 position) throws ReflectiveOperationException {
        return (Vec3) sable.getClass().getMethod("projectOutOfSubLevel", Level.class, Position.class)
                .invoke(sable, level, position);
    }

    private static Optional<Block> block(String id) {
        return BuiltInRegistries.BLOCK.getOptional(ResourceLocation.parse(id));
    }

    private static BlockState withProperty(BlockState state, String name, String value) {
        Property<?> property = state.getBlock().getStateDefinition().getProperty(name);
        return property == null ? state : withValue(state, property, value);
    }

    private static <T extends Comparable<T>> BlockState withValue(BlockState state, Property<T> property, String value) {
        return property.getValue(value).map(v -> state.setValue(property, v)).orElse(state);
    }

    private static String propertyValue(BlockState state, String name) {
        Property<?> property = state.getBlock().getStateDefinition().getProperty(name);
        return property == null ? "(none)" : valueName(state, property);
    }

    private static <T extends Comparable<T>> String valueName(BlockState state, Property<T> property) {
        return property.getName(state.getValue(property));
    }

    private static Object call(Object target, String name) throws ReflectiveOperationException {
        Method method = target.getClass().getMethod(name);
        method.setAccessible(true);
        return method.invoke(target);
    }
}
