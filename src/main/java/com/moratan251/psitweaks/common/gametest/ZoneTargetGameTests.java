package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIndex;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickFreezeZone;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickGravityZone;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickHeatZone;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickZoneBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariants;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

import java.util.List;

/**
 * 領域の効き目の相手。加熱・凍結領域の火と凍えは攻撃術式の共通ルール（{@code AttackRules#canHarm}）で相手を選ぶ。
 * 加重領域は、壁や柵に掛けるもの（額縁・絵画・リードの杭）を押さない（押すと外れてアイテムになるため）。
 * 領域はどれもテンプレートの (2, 1, 2) を中心にした一辺 5（威力 5）。
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ZoneTargetGameTests {
    private static final String TEMPLATE = "connector_empty";
    private static final BlockPos CENTER = new BlockPos(2, 1, 2);
    private static final int WAIT = 25;

    private ZoneTargetGameTests() {
    }

    /** 加熱領域（PvP が切ってある）: 中のハスクは燃え、ほかのプレイヤー・術者のペット・術者は燃えない。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_target_heat_pvp_off", timeoutTicks = 100)
    public static void heatZoneSparesPlayersAndPetsWithPvpOff(GameTestHelper helper) {
        sparesWithPvpOff(helper, true);
    }

    /** 凍結領域（PvP が切ってある）: 中のハスクは凍え、ほかのプレイヤー・術者のペット・術者は凍えない。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_target_freeze_pvp_off", timeoutTicks = 100)
    public static void freezeZoneSparesPlayersAndPetsWithPvpOff(GameTestHelper helper) {
        sparesWithPvpOff(helper, false);
    }

    /** 加熱領域（PvP が入っている）: チームの外のプレイヤーは燃え、同じチームの味方は友好的な攻撃が入っていても燃えない。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_target_heat_team", timeoutTicks = 100)
    public static void heatZoneBurnsStrangersButNotTeamMates(GameTestHelper helper) {
        sparesTeamWithPvpOn(helper, true);
    }

    /** 凍結領域（PvP が入っている）: チームの外のプレイヤーは凍え、同じチームの味方は友好的な攻撃が入っていても凍えない。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_target_freeze_team", timeoutTicks = 100)
    public static void freezeZoneFreezesStrangersButNotTeamMates(GameTestHelper helper) {
        sparesTeamWithPvpOn(helper, false);
    }

    private static void sparesWithPvpOff(GameTestHelper helper, boolean heat) {
        MinecraftServer server = helper.getLevel().getServer();
        boolean pvp = server.isPvpAllowed();
        String kind = heat ? "ht" : "fz";
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zt_" + kind + "_caster", new Vec3(4.5, 1.0, 4.5));
        ServerPlayer other = GameTestPlayers.inLevel(helper, "zt_" + kind + "_other", new Vec3(0.5, 1.0, 0.5));
        Wolf pet = GameTestPlayers.pet(helper, caster.getUUID(), new Vec3(4.5, 1.0, 0.5));
        Husk husk = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(0.5, 1.0, 4.5));
        Runnable cleanup = () -> {
            SpellGramIndex.ofCaster(helper.getLevel(), caster.getUUID()).forEach(Entity::discard);
            pet.discard();
            husk.discard();
            GameTestPlayers.remove(helper, other);
            GameTestPlayers.remove(helper, caster);
            server.setPvpAllowed(pvp);
        };
        server.setPvpAllowed(false);
        place(helper, caster, heat, cleanup);
        helper.runAfterDelay(WAIT, () -> {
            boolean huskHit = affected(husk, heat);
            boolean otherHit = affected(other, heat);
            boolean petHit = affected(pet, heat);
            boolean casterHit = affected(caster, heat);
            cleanup.run();
            String what = heat ? "burn" : "freeze";
            helper.assertTrue(huskHit, "The husk inside must " + what);
            helper.assertFalse(otherHit, "Another player must not " + what + " while PvP is off");
            helper.assertFalse(petHit, "The caster's pet must not " + what);
            helper.assertFalse(casterHit, "The caster must not " + what);
            helper.succeed();
        });
    }

    private static void sparesTeamWithPvpOn(GameTestHelper helper, boolean heat) {
        MinecraftServer server = helper.getLevel().getServer();
        boolean pvp = server.isPvpAllowed();
        String kind = heat ? "ht" : "fz";
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zt_" + kind + "_tcaster", new Vec3(4.5, 1.0, 4.5));
        ServerPlayer mate = GameTestPlayers.inLevel(helper, "zt_" + kind + "_mate", new Vec3(0.5, 1.0, 0.5));
        ServerPlayer stranger = GameTestPlayers.inLevel(helper, "zt_" + kind + "_stranger", new Vec3(0.5, 1.0, 4.5));
        PlayerTeam team = GameTestPlayers.team(helper, "zt_" + kind + "_team", true,
                caster.getScoreboardName(), mate.getScoreboardName());
        Runnable cleanup = () -> {
            SpellGramIndex.ofCaster(helper.getLevel(), caster.getUUID()).forEach(Entity::discard);
            GameTestPlayers.removeTeam(helper, team);
            List.of(mate, stranger, caster).forEach(player -> GameTestPlayers.remove(helper, player));
            server.setPvpAllowed(pvp);
        };
        server.setPvpAllowed(true);
        place(helper, caster, heat, cleanup);
        helper.runAfterDelay(WAIT, () -> {
            boolean strangerHit = affected(stranger, heat);
            boolean mateHit = affected(mate, heat);
            boolean casterHit = affected(caster, heat);
            cleanup.run();
            String what = heat ? "burn" : "freeze";
            helper.assertTrue(strangerHit, "A player outside the caster's team must " + what + " while PvP is on");
            helper.assertFalse(mateHit, "A team mate must not " + what + ", even with friendly fire on");
            helper.assertFalse(casterHit, "The caster must not " + what);
            helper.succeed();
        });
    }

    /** 燃えているか（加熱）、凍えの値がたまっているか（凍結）。 */
    private static boolean affected(LivingEntity entity, boolean heat) {
        return heat ? entity.getRemainingFireTicks() > 0 : entity.getTicksFrozen() > 0;
    }

    private static void place(GameTestHelper helper, ServerPlayer caster, boolean heat, Runnable cleanup) {
        BlockPos center = helper.absolutePos(CENTER);
        PieceTrickZoneBase<?> piece = heat
                ? new PieceTrickHeatZone(new Spell()) {
                    @Override
                    public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                        return cube(param, center);
                    }
                }
                : new PieceTrickFreezeZone(new Spell()) {
                    @Override
                    public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                        return cube(param, center);
                    }
                };
        try {
            piece.execute(new SpellContext().setPlayer(caster));
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
    }

    /** {@code center} のブロックを中心にした一辺 5 の立方体、威力 5。 */
    private static Object cube(SpellParam<?> param, BlockPos center) {
        if (param.name.equals(PsitweaksSpellParams.MATRIX)) {
            return new MatrixValue(4, 4, new double[]{
                    5, 0, 0, center.getX() - 2.5,
                    0, 5, 0, center.getY() - 2.5,
                    0, 0, 5, center.getZ() - 2.5,
                    0, 0, 0, 1});
        }
        if (param.name.equals(PsitweaksSpellParams.SIZE)) {
            return 5.0;
        }
        return param.name.equals(SpellParam.GENERIC_NAME_POWER) ? 5.0 : null;
    }

    /**
     * 加重領域（+X 向き、威力 5）: 中の額縁（ダイヤ入り）・絵画・リードの杭は押されず、外れてアイテムにならない。同じ範囲の
     * 落ちているアイテムは押される。額縁と絵画は z = 4 の石の壁に、杭は (2, 1, 0) の柵に掛ける。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_target_gravity_hanging", timeoutTicks = 100)
    public static void gravityZoneLeavesHangingEntitiesInPlace(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x <= 4; x++) {
            for (int y = 1; y <= 3; y++) {
                helper.setBlock(new BlockPos(x, y, 4), Blocks.STONE);
            }
        }
        BlockPos fence = new BlockPos(2, 1, 0);
        helper.setBlock(fence, Blocks.OAK_FENCE);
        ItemFrame frame = new ItemFrame(level, helper.absolutePos(new BlockPos(1, 2, 3)), Direction.NORTH);
        frame.setItem(new ItemStack(Items.DIAMOND));
        Painting painting = new Painting(level, helper.absolutePos(new BlockPos(3, 2, 3)), Direction.NORTH,
                level.registryAccess().registryOrThrow(Registries.PAINTING_VARIANT).getHolderOrThrow(PaintingVariants.KEBAB));
        boolean hung = level.addFreshEntity(frame) & level.addFreshEntity(painting);
        LeashFenceKnotEntity knot = LeashFenceKnotEntity.getOrCreateKnot(level, helper.absolutePos(fence));
        Vec3 looseAt = helper.absoluteVec(new Vec3(0.5, 1.0, 2.5));
        ItemEntity loose = new ItemEntity(level, looseAt.x, looseAt.y, looseAt.z, new ItemStack(Items.COBBLESTONE));
        loose.setDeltaMovement(Vec3.ZERO);
        loose.setNeverPickUp();
        level.addFreshEntity(loose);
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zt_gravity", new Vec3(0.5, 1.0, 0.5));
        AABB around = new AABB(helper.absolutePos(BlockPos.ZERO)).expandTowards(5.0, 5.0, 5.0).inflate(2.0);
        Runnable cleanup = () -> {
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(Entity::discard);
            List.of(frame, painting, knot).forEach(Entity::discard);
            level.getEntitiesOfClass(ItemEntity.class, around).forEach(Entity::discard);
            GameTestPlayers.remove(helper, caster);
            for (int x = 0; x <= 4; x++) {
                for (int y = 1; y <= 3; y++) {
                    helper.setBlock(new BlockPos(x, y, 4), Blocks.AIR);
                }
            }
            helper.setBlock(fence, Blocks.AIR);
        };
        if (!hung) {
            cleanup.run();
            helper.fail("The item frame and the painting could not be hung on the wall");
            return;
        }
        BlockPos center = helper.absolutePos(CENTER);
        PieceTrickGravityZone piece = new PieceTrickGravityZone(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                if (param.name.equals(SpellParam.GENERIC_NAME_DIRECTION)) {
                    return new Vector3(1.0, 0.0, 0.0);
                }
                return cube(param, center);
            }
        };
        try {
            piece.execute(new SpellContext().setPlayer(caster));
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        double startX = loose.getX();
        helper.runAfterDelay(20, () -> {
            boolean frameKept = frame.isAlive() && frame.getItem().is(Items.DIAMOND);
            boolean paintingKept = painting.isAlive();
            boolean knotKept = knot.isAlive();
            long dropped = level.getEntitiesOfClass(ItemEntity.class, around, item -> item != loose).size();
            double moved = loose.getX() - startX;
            cleanup.run();
            helper.assertTrue(frameKept, "The item frame must stay on the wall with its diamond");
            helper.assertTrue(paintingKept, "The painting must stay on the wall");
            helper.assertTrue(knotKept, "The leash knot must stay on the fence");
            helper.assertTrue(dropped == 0, "Nothing must drop from hanging entities, dropped " + dropped);
            helper.assertTrue(moved > 0.5, "A dropped item inside must still be pushed, moved " + moved);
            helper.succeed();
        });
    }
}
