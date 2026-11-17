package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.attributes.PsitweaksAttributes;
import com.moratan251.psitweaks.common.compat.ImmersiveEngineeringCompat;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIndex;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityTurbulenceZone;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickTurbulenceZone;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickZoneBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

import java.util.Optional;

/**
 * 作動式: 乱流領域と Immersive Engineering の風車。IE がなければ飛ばす。
 * <p>
 * 区画（9×10×9、ブロックなし）の中に、一辺 9 の乱流領域を (4, 5, 4) を中心に置く（x 0〜8・y 1〜9・z 0〜8）。風車はどれも南向き
 * （前が +z、動力ダイナモは後ろの -z）。
 * <ul>
 *   <li>風車 1（(4, 5, 2)）: 羽の円が空いていて、後ろに動力ダイナモ、その下に低電圧キャパシタ（上の面が入力）。前の z 3・4 を
 *   石の壁で塞ぐので、IE の自然の風では回らない（前の空気が足りない）。</li>
 *   <li>風車 2（(4, 5, 4)）: 後ろに動力ダイナモがあるが、羽の円が石の壁（z 4）で塞がっている。</li>
 *   <li>風車 3（(4, 5, 0)）: 羽の円は空いているが、後ろに動力ダイナモがない。</li>
 * </ul>
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ImmersiveEngineeringZoneGameTests {
    private static final String TEMPLATE = "zone_ie_windmills";
    private static final BlockPos CENTER = new BlockPos(4, 5, 4);
    private static final BlockPos WINDMILL = new BlockPos(4, 5, 2);
    private static final BlockPos DYNAMO = WINDMILL.north();
    private static final BlockPos CAPACITOR = DYNAMO.below();
    private static final BlockPos BLOCKED_WINDMILL = new BlockPos(4, 5, 4);
    private static final BlockPos BLOCKED_DYNAMO = BLOCKED_WINDMILL.north();
    private static final BlockPos LONE_WINDMILL = new BlockPos(4, 5, 0);
    /** Mekanism の風力発電機（高さ 5）。風車 1 の前（自然の風は壁で止まっている）。 */
    private static final BlockPos WIND_GENERATOR = new BlockPos(1, 1, 7);
    private static final int ZONE_SIZE = 9;
    /** 床の広さ 81 × 40 ÷ 240 × 事象干渉力 1 = 13.5 WU。 */
    private static final double ZONE_POWER = 40.0;
    private static final double ZONE_WIND = 81.0 * ZONE_POWER / 240.0;
    /** キャパシタの増え方を測る長さ（tick）。 */
    private static final int MEASURE_TICKS = 40;

    private ImmersiveEngineeringZoneGameTests() {
    }

    /**
     * 乱流領域の中の風車は、羽の円が空いていて後ろに動力ダイナモがあれば、前が塞がっていても乱流の風で回り、1 台は 4 WU
     * （60 FE/t）まで受け取る。羽の円が塞がった風車・動力ダイナモのない風車は風を受け取らない（風を分ける台数にも入らない）。
     * Mekanism Generators があれば風力発電機も置き、上限 4 WU の風車の余りが風力発電機に回ること（13.5 − 4 = 9.5 WU）を確かめる。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_turbulence_ie_windmill", timeoutTicks = 200)
    public static void turbulenceZoneTurnsWindmillsUpToFourUnits(GameTestHelper helper) {
        Optional<Block> windmill = block("immersiveengineering:windmill");
        Optional<Block> dynamo = block("immersiveengineering:dynamo");
        Optional<Block> capacitor = block("immersiveengineering:capacitor_lv");
        if (!ImmersiveEngineeringCompat.isLoaded() || windmill.isEmpty() || dynamo.isEmpty() || capacitor.isEmpty()) {
            helper.succeed();
            return;
        }
        Optional<Block> windGenerator = block("mekanismgenerators:wind_generator");
        boolean withGenerator = MekanismCompat.isGeneratorsLoaded() && windGenerator.isPresent();
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_ie_windmill", new Vec3(8.5, 1.0, 8.5));
        // 風車 1 の前を塞ぐ壁（z 3・4）。風車 2 の羽の円は z 4 の壁の中。
        for (int x = 0; x <= 8; x++) {
            for (int y = 1; y <= 9; y++) {
                helper.setBlock(new BlockPos(x, y, 3), Blocks.STONE);
                helper.setBlock(new BlockPos(x, y, 4), Blocks.STONE);
            }
        }
        helper.setBlock(CAPACITOR, capacitor.get());
        helper.setBlock(DYNAMO, facing(dynamo.get(), Direction.SOUTH));
        helper.setBlock(WINDMILL, facing(windmill.get(), Direction.SOUTH));
        helper.setBlock(BLOCKED_DYNAMO, facing(dynamo.get(), Direction.SOUTH));
        helper.setBlock(BLOCKED_WINDMILL, facing(windmill.get(), Direction.SOUTH));
        helper.setBlock(LONE_WINDMILL, facing(windmill.get(), Direction.SOUTH));
        if (withGenerator) {
            helper.setBlock(WIND_GENERATOR, windGenerator.get());
        }
        Runnable cleanup = () -> {
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(zone -> zone.discard());
            GameTestPlayers.remove(helper, caster);
        };
        try {
            caster.getAttribute(PsitweaksAttributes.SPELL_DAMAGE_FACTOR).setBaseValue(1.0D);
            cast(helper, caster);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        long[] stored = new long[1];
        // 乱流領域は 20 tick ごとに風を分け直し、風車は 20 tick ごとに受け取る風量を読み直す。
        helper.runAfterDelay(60, () -> stored[0] = energyAt(helper, CAPACITOR));
        helper.runAfterDelay(60 + MEASURE_TICKS, () -> {
            long gained = energyAt(helper, CAPACITOR) - stored[0];
            double windmillUnits = EntityTurbulenceZone.windUnitsAt(level, helper.absolutePos(WINDMILL));
            double blockedUnits = EntityTurbulenceZone.windUnitsAt(level, helper.absolutePos(BLOCKED_WINDMILL));
            double loneUnits = EntityTurbulenceZone.windUnitsAt(level, helper.absolutePos(LONE_WINDMILL));
            double generatorUnits = withGenerator ? EntityTurbulenceZone.windUnitsAt(level, helper.absolutePos(WIND_GENERATOR)) : 0.0;
            double used = SpellGramIndex.ofCaster(level, caster.getUUID()).stream()
                    .filter(zone -> zone instanceof EntityTurbulenceZone)
                    .mapToDouble(zone -> ((EntityTurbulenceZone) zone).getResourceUsed()).sum();
            cleanup.run();
            helper.assertTrue(Math.abs(windmillUnits - EntityTurbulenceZone.MAX_WU_PER_WINDMILL) < 1.0E-9,
                    "A windmill must take 4 WU at most, got " + windmillUnits);
            helper.assertTrue(blockedUnits == 0.0, "A windmill whose rotor is blocked must take no wind, got " + blockedUnits);
            helper.assertTrue(loneUnits == 0.0, "A windmill without a Kinetic Dynamo must take no wind, got " + loneUnits);
            long expected = Math.round(EntityTurbulenceZone.MAX_WU_PER_WINDMILL * EntityTurbulenceZone.WIND_FE_PER_WU) * MEASURE_TICKS;
            helper.assertTrue(gained == expected,
                    "The Kinetic Dynamo must make 60 FE/t from the zone's wind alone: " + gained + " FE in " + MEASURE_TICKS
                            + " ticks, expected " + expected);
            if (withGenerator) {
                double rest = ZONE_WIND - EntityTurbulenceZone.MAX_WU_PER_WINDMILL;
                helper.assertTrue(Math.abs(generatorUnits - rest) < 1.0E-9,
                        "The wind the windmill cannot take must go to the Wind Generator: " + generatorUnits + " vs " + rest);
                helper.assertTrue(Math.abs(used - ZONE_WIND) < 1.0E-3, "The zone must count all its wind in use, got " + used);
            } else {
                helper.assertTrue(Math.abs(used - EntityTurbulenceZone.MAX_WU_PER_WINDMILL) < 1.0E-3,
                        "The zone must count only the windmill's 4 WU in use, got " + used);
            }
            helper.succeed();
        });
    }

    private static void cast(GameTestHelper helper, ServerPlayer caster) throws SpellRuntimeException {
        BlockPos center = helper.absolutePos(CENTER);
        PieceTrickZoneBase<?> piece = new PieceTrickTurbulenceZone(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                if (param.name.equals(PsitweaksSpellParams.MATRIX)) {
                    double low = -Math.floor((ZONE_SIZE - 1) / 2.0) - 0.5;
                    return new MatrixValue(4, 4, new double[]{
                            ZONE_SIZE, 0, 0, center.getX() + low,
                            0, ZONE_SIZE, 0, center.getY() + low,
                            0, 0, ZONE_SIZE, center.getZ() + low,
                            0, 0, 0, 1});
                }
                if (param.name.equals(PsitweaksSpellParams.SIZE)) {
                    return (double) ZONE_SIZE;
                }
                return param.name.equals(SpellParam.GENERIC_NAME_POWER) ? ZONE_POWER : null;
            }
        };
        piece.execute(new SpellContext().setPlayer(caster));
    }

    private static long energyAt(GameTestHelper helper, BlockPos relative) {
        IEnergyStorage storage = helper.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, helper.absolutePos(relative), null);
        return storage == null ? -1L : storage.getEnergyStored();
    }

    /** {@code block} の既定の状態の向き（名前が facing のもの）を {@code direction} にする。 */
    @SuppressWarnings("unchecked")
    private static BlockState facing(Block block, Direction direction) {
        BlockState state = block.defaultBlockState();
        for (Property<?> property : state.getProperties()) {
            if (property.getName().equals("facing") && property.getPossibleValues().contains(direction)) {
                return state.setValue((Property<Direction>) property, direction);
            }
        }
        return state;
    }

    private static Optional<Block> block(String id) {
        return BuiltInRegistries.BLOCK.getOptional(ResourceLocation.parse(id));
    }
}
