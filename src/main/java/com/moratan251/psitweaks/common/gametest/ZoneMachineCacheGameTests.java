package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.compat.MekanismGameplayCompat;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityHeatZone;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.SpellGramZone;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.ZoneArea;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * 領域が覚えている機械の一覧（2026-11-13 の軽量化 B2）: 一覧は {@link SpellGramZone#MACHINE_REFRESH_TICKS} ごとに作り直すので、
 * 領域を置いたあとに置いた機械にも、その間隔のうちに効き始める。
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ZoneMachineCacheGameTests {
    private static final String TEMPLATE = "connector_empty";
    private static final BlockPos HEATER = new BlockPos(1, 1, 1);

    private ZoneMachineCacheGameTests() {
    }

    /** 加熱領域を置いてから置いた抵抗型発熱器も、作り直しの間隔のうちに温まり始める。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_machine_cache", timeoutTicks = 120)
    public static void heatZoneReachesMachinesPlacedAfterIt(GameTestHelper helper) throws SpellRuntimeException {
        Optional<Block> heater = BuiltInRegistries.BLOCK.getOptional(ResourceLocation.parse("mekanism:resistive_heater"));
        if (!MekanismCompat.isMekanismLoaded() || heater.isEmpty()) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_machine_cache", new Vec3(4.5, 1.0, 4.5));
        EntityHeatZone zone = new EntityHeatZone(level);
        zone.setPower(5.0F);
        zone.configureZone(ZoneArea.cube(helper.absolutePos(new BlockPos(2, 1, 2)), 5), 0xFFFFFF);
        zone.setMachineSpeed(SpellGramZone.machineSpeedFor(2.0));
        zone.setPlacement(caster.getUUID(), 2.0);
        zone.setCaster(caster);
        zone.setLifetimeTicks(400);
        // 熱の総量は置いたときのコストに比例する（2026-11-14）: コスト 1000（400 tick）で 900 J/t（360 J/Psi）。
        zone.setPlacementCost(1000.0F);
        zone.setMaxIntegrity(1.0E6F);
        level.addFreshEntity(zone);
        Runnable cleanup = () -> {
            zone.discard();
            helper.setBlock(HEATER, Blocks.AIR);
            GameTestPlayers.remove(helper, caster);
        };
        BlockPos absolute = helper.absolutePos(HEATER);
        // 領域が一度機械の一覧を作ってから、発熱器を置く。
        helper.runAfterDelay(3, () -> helper.setBlock(HEATER, heater.get()));
        double[] before = new double[1];
        helper.runAfterDelay(4, () -> before[0] = MekanismGameplayCompat.temperatureAt(level, absolute));
        helper.runAfterDelay(4 + SpellGramZone.MACHINE_REFRESH_TICKS + 30, () -> {
            double after = MekanismGameplayCompat.temperatureAt(level, absolute);
            cleanup.run();
            helper.assertTrue(!Double.isNaN(before[0]), "The Resistive Heater must hold heat");
            helper.assertTrue(after > before[0] + 3.0,
                    "A heater placed after the Heat Zone must be heated, " + before[0] + " K -> " + after + " K");
            helper.succeed();
        });
    }
}
