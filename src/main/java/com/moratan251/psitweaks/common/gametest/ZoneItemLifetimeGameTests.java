package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.compat.CreateCompat;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIndex;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickChargedZone;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickSoakZone;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickZoneBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

import java.util.function.Function;

/**
 * 領域が加工している落ちているアイテムは、加工している間は消えるまでの時間が進まない（2026-11-12）。消える寸前（残り 10 tick）に
 * したアイテムを領域の中に置き、加工が終わる前の 40 tick 後にまだ残っていることを確かめる。Create・AE2 がないときは飛ばして通す。
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ZoneItemLifetimeGameTests {
    private static final String TEMPLATE = "connector_empty";

    private ZoneItemLifetimeGameTests() {
    }

    /** 湿潤領域の一括洗浄（Create）: 砂利は洗っている間は消えない。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_item_lifetime_wash", timeoutTicks = 80)
    public static void washingKeepsItemsFromDespawning(GameTestHelper helper) {
        if (!CreateCompat.isCreateLoaded()) {
            helper.succeed();
            return;
        }
        check(helper, "zone_lifetime_wash", new ItemStack(Items.GRAVEL), PieceTrickSoakZone::new);
    }

    /** 荷電領域の AE2 のチャージ: ケルタスクォーツの水晶はチャージしている間は消えない。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_item_lifetime_charge", timeoutTicks = 80)
    public static void chargingKeepsItemsFromDespawning(GameTestHelper helper) {
        ResourceLocation certusId = ResourceLocation.fromNamespaceAndPath("ae2", "certus_quartz_crystal");
        if (!ModList.get().isLoaded("ae2") || !BuiltInRegistries.ITEM.containsKey(certusId)) {
            helper.succeed();
            return;
        }
        check(helper, "zone_lifetime_charge", new ItemStack(BuiltInRegistries.ITEM.get(certusId)), PieceTrickChargedZone::new);
    }

    private static void check(GameTestHelper helper, String name, ItemStack stack, Function<Spell, PieceTrickZoneBase<?>> pieceType) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, name, new Vec3(4.5, 1.0, 4.5));
        Vec3 at = helper.absoluteVec(new Vec3(1.5, 1.2, 1.5));
        ItemEntity item = new ItemEntity(level, at.x, at.y, at.z, stack);
        item.setNoGravity(true);
        item.setDeltaMovement(Vec3.ZERO);
        item.setPickUpDelay(32767);
        item.lifespan = 10;
        level.addFreshEntity(item);
        Runnable cleanup = () -> {
            item.discard();
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(Entity::discard);
            GameTestPlayers.remove(helper, caster);
        };
        try {
            place(pieceType, caster, helper.absolutePos(new BlockPos(2, 1, 2)), 5);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(40, () -> {
            boolean alive = item.isAlive();
            int lifespan = item.lifespan;
            int age = item.getAge();
            cleanup.run();
            helper.assertTrue(alive && lifespan > 10 && age > 10,
                    "An item being processed must not despawn: alive " + alive + ", lifespan " + lifespan + ", age " + age);
            helper.succeed();
        });
    }

    private static void place(Function<Spell, PieceTrickZoneBase<?>> pieceType, ServerPlayer caster, BlockPos center, int size)
            throws SpellRuntimeException {
        double low = -(size / 2) - 0.5;
        MatrixValue matrix = new MatrixValue(4, 4, new double[]{
                size, 0, 0, center.getX() + low,
                0, size, 0, center.getY() + low,
                0, 0, size, center.getZ() + low,
                0, 0, 0, 1});
        wrap(pieceType.apply(new Spell()), matrix, size).execute(new SpellContext().setPlayer(caster));
    }

    /** 入力を差し替えた同じ種類の作動式（湿潤領域は行列・サイズ・時間、荷電領域はさらに威力を持つ）。 */
    private static PieceTrickZoneBase<?> wrap(PieceTrickZoneBase<?> piece, MatrixValue matrix, int size) {
        if (piece instanceof PieceTrickSoakZone) {
            return new PieceTrickSoakZone(new Spell()) {
                @Override
                public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                    return input(param, matrix, size);
                }
            };
        }
        return new PieceTrickChargedZone(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return input(param, matrix, size);
            }
        };
    }

    private static Object input(SpellParam<?> param, MatrixValue matrix, int size) {
        if (param.name.equals(PsitweaksSpellParams.MATRIX)) {
            return matrix;
        }
        if (param.name.equals(PsitweaksSpellParams.SIZE)) {
            return (double) size;
        }
        // 荷電領域の威力（2026-11-14 から必須）。湿潤領域は持たない。
        return param.name.equals(SpellParam.GENERIC_NAME_POWER) ? 5.0 : null;
    }
}
