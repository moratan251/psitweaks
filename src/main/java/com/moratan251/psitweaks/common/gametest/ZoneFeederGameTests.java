package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.blocks.PsitweaksBlocks;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIndex;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityChargedZone;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.SpellGramZone;
import com.moratan251.psitweaks.common.items.PsitweaksDataComponents;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickChargedZone;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickTurbulenceZone;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickZoneBase;
import com.moratan251.psitweaks.common.tile.ZoneFeederBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.jetbrains.annotations.Nullable;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

import java.util.List;
import java.util.UUID;

/**
 * 領域給電機: FE の窓口（受け取りも取り出しも 1 tick に究極ユニバーサルケーブルの量まで）と、壊しても FE が残ること、荷電領域が
 * 自分の充電能力のあとに給電機の FE を引いて配ること。受け手に Mekanism のエネルギータブレットを使うテストは、Mekanism がないときは中身を飛ばして通す。
 * <p>
 * プレイヤーをワールドに入れるテストは、他のテストと同時に動かないようバッチを分ける。
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ZoneFeederGameTests {
    private static final String TEMPLATE = "connector_empty";

    private ZoneFeederGameTests() {
    }

    /**
     * 給電機の窓口: 1 tick に受け取るのは究極ユニバーサルケーブルの量（既定の設定で 3,276,800 FE）まで、ためるのはその 5 tick ぶん。
     * 取り出しも別枠で 1 tick に同じ量まで。Mekanism がなくても同じ。
     */
    @GameTest(template = TEMPLATE)
    public static void zoneFeederTakesUpToTheCableRate(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, PsitweaksBlocks.ZONE_FEEDER.get());
        ServerLevel level = helper.getLevel();
        IEnergyStorage storage = level.getCapability(Capabilities.EnergyStorage.BLOCK, helper.absolutePos(pos), Direction.NORTH);
        helper.assertTrue(storage != null, "The Zone Feeder must expose an FE storage");
        int rate = ZoneFeederBlockEntity.receiveRate();
        helper.assertTrue(rate == ZoneFeederBlockEntity.DEFAULT_RECEIVE_RATE,
                "With the default settings the rate must be the Ultimate Universal Cable's 3,276,800 FE/t, got " + rate);
        helper.assertTrue(ZoneFeederBlockEntity.capacity() == rate * ZoneFeederBlockEntity.BUFFER_TICKS,
                "The buffer must hold " + ZoneFeederBlockEntity.BUFFER_TICKS + " ticks of input");
        int first = storage.receiveEnergy(Integer.MAX_VALUE, false);
        int again = storage.receiveEnergy(Integer.MAX_VALUE, false);
        helper.assertTrue(first == rate, "The first insert of a tick must take the whole rate " + rate + ", took " + first);
        helper.assertTrue(again == 0, "A second insert in the same tick must take nothing, took " + again);
        IEnergyStorage sideless = level.getCapability(Capabilities.EnergyStorage.BLOCK, helper.absolutePos(pos), null);
        helper.assertTrue(sideless != null, "The Zone Feeder must also answer a sideless query");
        helper.runAfterDelay(1, () -> {
            int next = storage.receiveEnergy(Integer.MAX_VALUE, false);
            helper.assertTrue(next == rate, "The next tick must take the rate again, took " + next);
            helper.assertTrue(storage.canExtract(), "Cables must be able to draw FE out of the Zone Feeder");
            int simulated = storage.extractEnergy(Integer.MAX_VALUE, true);
            helper.assertTrue(simulated == rate && storage.getEnergyStored() == rate * 2,
                    "A simulated draw must report the rate and change nothing, reported " + simulated);
            int drawn = storage.extractEnergy(Integer.MAX_VALUE, false);
            int drawnAgain = storage.extractEnergy(Integer.MAX_VALUE, false);
            helper.assertTrue(drawn == rate, "The first draw of a tick must give the whole rate " + rate + ", gave " + drawn);
            helper.assertTrue(drawnAgain == 0, "A second draw in the same tick must give nothing, gave " + drawnAgain);
            helper.assertTrue(storage.receiveEnergy(1, true) == 0,
                    "Drawing FE must not reopen the insert limit of the tick");
            helper.runAfterDelay(1, () -> {
                int rest = storage.extractEnergy(Integer.MAX_VALUE, false);
                helper.assertTrue(rest == rate && storage.getEnergyStored() == 0,
                        "The next tick must give the rest (" + rate + " FE), gave " + rest);
                helper.assertTrue(storage.extractEnergy(1, false) == 0, "An empty Zone Feeder must give nothing");
                helper.succeed();
            });
        });
    }

    /**
     * 壊して落ちたアイテムに FE が残り、置き直すと戻る。空の給電機のアイテムには何も付かない。ブロックエンティティの NBT には FE を
     * 重ねて残さない（ピックブロックでも付くのはデータコンポーネントだけ）。
     */
    @GameTest(template = TEMPLATE)
    public static void brokenZoneFeederKeepsItsEnergy(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = new BlockPos(1, 1, 1);
        BlockPos absolute = helper.absolutePos(pos);
        helper.setBlock(pos, PsitweaksBlocks.ZONE_FEEDER.get());
        ZoneFeederBlockEntity empty = feeder(helper, pos);
        ItemStack emptyDrop = dropOf(helper, level, absolute, empty);
        helper.assertTrue(!emptyDrop.has(PsitweaksDataComponents.ZONE_FEEDER_ENERGY.get()),
                "An empty Zone Feeder must drop an item that stacks with new ones");

        long filled = empty.energyStorage().receiveEnergy(12_345, false);
        helper.assertTrue(filled == 12_345, "The feeder must take the FE, took " + filled);
        ItemStack drop = dropOf(helper, level, absolute, empty);
        Long kept = drop.get(PsitweaksDataComponents.ZONE_FEEDER_ENERGY.get());
        helper.assertTrue(kept != null && kept == filled, "The dropped item must keep " + filled + " FE, keeps " + kept);
        helper.assertTrue(filled == empty.collectComponents().getOrDefault(PsitweaksDataComponents.ZONE_FEEDER_ENERGY.get(), 0L),
                "Pick block must get the stored FE from the components");
        CompoundTag tag = empty.saveCustomOnly(level.registryAccess());
        empty.removeComponentsFromTag(tag);
        helper.assertTrue(!tag.contains("Energy"), "The stored FE must not stay in the block entity data of an item");

        level.removeBlock(absolute, false);
        helper.setBlock(pos, PsitweaksBlocks.ZONE_FEEDER.get());
        ZoneFeederBlockEntity placed = feeder(helper, pos);
        helper.assertTrue(placed.stored() == 0L, "A new feeder must start empty, has " + placed.stored());
        placed.applyComponentsFromItemStack(drop);
        helper.assertTrue(placed.stored() == filled, "Placing the item must bring back " + filled + " FE, got " + placed.stored());
        helper.succeed();
    }

    private static ItemStack dropOf(GameTestHelper helper, ServerLevel level, BlockPos absolute, ZoneFeederBlockEntity feeder) {
        List<ItemStack> drops = Block.getDrops(level.getBlockState(absolute), level, absolute, feeder);
        helper.assertTrue(drops.size() == 1 && drops.get(0).is(PsitweaksBlocks.ZONE_FEEDER.get().asItem()),
                "The Zone Feeder must drop itself, dropped " + drops);
        return drops.get(0);
    }

    /**
     * 荷電領域は給電機に FE を送らず、受け手がいなければ給電機から引かない（荷電領域の中に給電機だけを置く）。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_feeder_idle", timeoutTicks = 80)
    public static void chargedZoneNeitherFillsNorDrainsAnIdleFeeder(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, PsitweaksBlocks.ZONE_FEEDER.get());
        ZoneFeederBlockEntity feeder = feeder(helper, pos);
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_feeder_idle_caster", new Vec3(4.5, 1.0, 4.5));
        feeder.setOwner(caster.getUUID());
        feeder.energyStorage().receiveEnergy(1000, false);
        Runnable cleanup = () -> {
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(Entity::discard);
            GameTestPlayers.remove(helper, caster);
        };
        try {
            placeCharged(caster, helper.absolutePos(pos), 3, 5.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(30, () -> {
            long stored = feeder.stored();
            cleanup.run();
            helper.assertTrue(stored == 1000,
                    "A Charged Zone must neither send FE to a Zone Feeder nor draw from it with nothing to fill, it holds " + stored);
            helper.succeed();
        });
    }

    /**
     * 自分の充電能力で足りない分を給電機から引いて配る: 落ちているエネルギータブレットは充電能力 × 時間よりずっと多く受け取り、
     * 給電機から減った量は、タブレットが受け取った量から充電能力の分を引いたものに合う。照準の表示に「受け取り」が出る。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_feeder_draw", timeoutTicks = 100)
    public static void chargedZoneSendsFeederEnergy(GameTestHelper helper) {
        Item tablet = ModList.get().isLoaded("mekanism") ? item("mekanism:energy_tablet") : null;
        if (tablet == null) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, PsitweaksBlocks.ZONE_FEEDER.get());
        ZoneFeederBlockEntity feeder = feeder(helper, pos);
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_feeder_draw_caster", new Vec3(4.5, 1.0, 4.5));
        feeder.setOwner(caster.getUUID());
        long filled = feeder.energyStorage().receiveEnergy(Integer.MAX_VALUE, false);
        ItemEntity dropped = dropTablet(helper, tablet, new Vec3(1.5, 1.2, 0.5));
        Runnable cleanup = () -> {
            dropped.discard();
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(Entity::discard);
            GameTestPlayers.remove(helper, caster);
        };
        EntityChargedZone[] zone = new EntityChargedZone[1];
        try {
            zone[0] = placeCharged(caster, helper.absolutePos(pos), 3, 1.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        int before = stored(dropped.getItem());
        // 照準の表示は 10 tick ごとにならして送る。タブレットがすぐ満タンになっても見えるよう、早めに読む。
        float[] shown = new float[1];
        helper.runAfterDelay(15, () -> shown[0] = zone[0].getResourceMoved());
        helper.runAfterDelay(31, () -> {
            int gained = stored(dropped.getItem()) - before;
            long drawn = filled - feeder.stored();
            double own = zone[0].capacityPerTick() * 30;
            long fedTotal = zone[0].fedTotal();
            float shownReceived = Math.max(shown[0], zone[0].getResourceMoved());
            cleanup.run();
            helper.assertTrue(gained > own * 10, "The tablet must get far more than the zone's own " + own + " FE, got " + gained);
            helper.assertTrue(drawn == fedTotal, "The zone must count what it drew (" + drawn + " FE), counted " + fedTotal);
            helper.assertTrue(drawn <= gained && drawn >= gained - own - 16,
                    "The feeder must lose what the tablet got beyond the zone's own capacity (" + (gained - own) + " FE), lost " + drawn);
            helper.assertTrue(shownReceived > 0.0F, "The zone must show the FE received from feeders, shows " + shownReceived);
            helper.succeed();
        });
    }

    /**
     * 雷雲でも、給電機から引いた FE は増えない（増えるのは自分の充電能力の分だけ）。タブレットが受け取った量から給電機が減った量を
     * 引いた残りは、自分の充電能力 × 時間の 2 倍（乱流の威力 10）を超えない。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_feeder_storm", timeoutTicks = 100)
    public static void thundercloudDoesNotRaiseFeederEnergy(GameTestHelper helper) {
        Item tablet = ModList.get().isLoaded("mekanism") ? item("mekanism:energy_tablet") : null;
        if (tablet == null) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, PsitweaksBlocks.ZONE_FEEDER.get());
        ZoneFeederBlockEntity feeder = feeder(helper, pos);
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_feeder_storm_caster", new Vec3(4.5, 1.0, 4.5));
        feeder.setOwner(caster.getUUID());
        long filled = feeder.energyStorage().receiveEnergy(Integer.MAX_VALUE, false);
        ItemEntity dropped = dropTablet(helper, tablet, new Vec3(1.5, 1.2, 0.5));
        Runnable cleanup = () -> {
            dropped.discard();
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(Entity::discard);
            GameTestPlayers.remove(helper, caster);
        };
        EntityChargedZone[] zone = new EntityChargedZone[1];
        try {
            zone[0] = placeCharged(caster, helper.absolutePos(pos), 3, 1.0);
            placeTurbulence(caster, helper.absolutePos(pos), 3, 10.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        int before = stored(dropped.getItem());
        helper.runAfterDelay(31, () -> {
            int gained = stored(dropped.getItem()) - before;
            long drawn = filled - feeder.stored();
            double own = zone[0].capacityPerTick() * 30;
            cleanup.run();
            helper.assertTrue(drawn > 0, "The zone must draw from the feeder under the Thundercloud");
            helper.assertTrue(gained - drawn <= own * 2.0 + 16,
                    "Only the zone's own capacity may be raised by the Thundercloud: got " + gained + " FE for " + drawn
                            + " FE drawn and " + own + " FE of its own");
            helper.succeed();
        });
    }

    /** ほかの人が置いた給電機からは引かない。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_feeder_owner", timeoutTicks = 80)
    public static void chargedZoneLeavesOtherPlayersFeeders(GameTestHelper helper) {
        Item tablet = ModList.get().isLoaded("mekanism") ? item("mekanism:energy_tablet") : null;
        if (tablet == null) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, PsitweaksBlocks.ZONE_FEEDER.get());
        ZoneFeederBlockEntity feeder = feeder(helper, pos);
        feeder.setOwner(UUID.randomUUID());
        long filled = feeder.energyStorage().receiveEnergy(Integer.MAX_VALUE, false);
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_feeder_owner_caster", new Vec3(4.5, 1.0, 4.5));
        ItemEntity dropped = dropTablet(helper, tablet, new Vec3(1.5, 1.2, 0.5));
        Runnable cleanup = () -> {
            dropped.discard();
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(Entity::discard);
            GameTestPlayers.remove(helper, caster);
        };
        try {
            placeCharged(caster, helper.absolutePos(pos), 3, 1.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(30, () -> {
            long stored = feeder.stored();
            cleanup.run();
            helper.assertTrue(stored == filled, "A zone must not draw from another player's Zone Feeder, it went from "
                    + filled + " to " + stored);
            helper.succeed();
        });
    }

    private static ZoneFeederBlockEntity feeder(GameTestHelper helper, BlockPos relative) {
        if (helper.getLevel().getBlockEntity(helper.absolutePos(relative)) instanceof ZoneFeederBlockEntity feeder) {
            return feeder;
        }
        throw new IllegalStateException("No Zone Feeder at " + relative);
    }

    private static ItemEntity dropTablet(GameTestHelper helper, Item tablet, Vec3 relative) {
        Vec3 at = helper.absoluteVec(relative);
        ItemEntity dropped = new ItemEntity(helper.getLevel(), at.x, at.y, at.z, new ItemStack(tablet));
        dropped.setNoGravity(true);
        dropped.setDeltaMovement(Vec3.ZERO);
        dropped.setPickUpDelay(32767);
        helper.getLevel().addFreshEntity(dropped);
        return dropped;
    }

    private static int stored(ItemStack stack) {
        IEnergyStorage storage = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        return storage == null ? -1 : storage.getEnergyStored();
    }

    @Nullable
    private static Item item(String id) {
        ResourceLocation key = ResourceLocation.parse(id);
        return BuiltInRegistries.ITEM.containsKey(key) ? BuiltInRegistries.ITEM.get(key) : null;
    }

    private static MatrixValue cube(BlockPos center, int size) {
        double low = -(size / 2) - 0.5;
        return new MatrixValue(4, 4, new double[]{
                size, 0, 0, center.getX() + low,
                0, size, 0, center.getY() + low,
                0, 0, size, center.getZ() + low,
                0, 0, 0, 1});
    }

    /** {@code center} のブロックを中心にした一辺 {@code size}（奇数）、威力 {@code power} の荷電領域を置き、それを返す。 */
    private static EntityChargedZone placeCharged(ServerPlayer caster, BlockPos center, int size, double power)
            throws SpellRuntimeException {
        MatrixValue matrix = cube(center, size);
        var piece = new PieceTrickChargedZone(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                if (param.name.equals(PsitweaksSpellParams.MATRIX)) {
                    return matrix;
                }
                if (param.name.equals(PsitweaksSpellParams.SIZE)) {
                    return (double) size;
                }
                return param.name.equals(SpellParam.GENERIC_NAME_POWER) ? power : null;
            }
        };
        List<EntityChargedZone> before = SpellGramIndex.all(caster.level(), EntityChargedZone.class);
        execute(piece, caster);
        return SpellGramIndex.all(caster.level(), EntityChargedZone.class).stream().filter(zone -> !before.contains(zone))
                .findFirst().orElseThrow();
    }

    /** {@code center} のブロックを中心にした一辺 {@code size}（奇数）、威力 {@code power} の乱流領域を置く。 */
    private static void placeTurbulence(ServerPlayer caster, BlockPos center, int size, double power) throws SpellRuntimeException {
        MatrixValue matrix = cube(center, size);
        var piece = new PieceTrickTurbulenceZone(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                if (param.name.equals(PsitweaksSpellParams.MATRIX)) {
                    return matrix;
                }
                if (param.name.equals(PsitweaksSpellParams.SIZE)) {
                    return (double) size;
                }
                return param.name.equals(SpellParam.GENERIC_NAME_POWER) ? power : null;
            }
        };
        execute(piece, caster);
    }

    private static void execute(PieceTrickZoneBase<? extends SpellGramZone> piece, ServerPlayer caster) throws SpellRuntimeException {
        piece.execute(new SpellContext().setPlayer(caster));
    }
}
