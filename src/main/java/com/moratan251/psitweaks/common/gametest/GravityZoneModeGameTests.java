package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIndex;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityGravityZone;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickGravityZone;
import com.moratan251.psitweaksqol.api.PsitweaksModeOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

import java.util.List;

/**
 * 作動式: 加重領域のモード（2026-11-12 ユーザー）: ベクトルモード（既定、範囲・サイズ・方向・威力）と、行列モード（範囲・
 * サイズ・威力・時間、向きは行列の Y 軸、行列を省けば真上、負の威力は逆向き）。
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class GravityZoneModeGameTests {
    private static final String TEMPLATE = "connector_empty";

    private GravityZoneModeGameTests() {
    }

    /** 既定はベクトルモードで、行列モードに変えると方向の代わりに時間の入力が出る。同じ名前の入力のつなぎ先は残り、保存もされる。 */
    @GameTest(template = TEMPLATE)
    public static void gravityZoneModesSwapDirectionForTime(GameTestHelper helper) {
        PieceTrickGravityZone piece = (PieceTrickGravityZone) vazkii.psi.api.PsiAPI.SPELL_PIECE_REGISTRY
                .get(Psitweaks.location("trick_gravity_zone")).create(new Spell());
        helper.assertTrue(!piece.isMatrixMode() && piece.params.containsKey(SpellParam.GENERIC_NAME_DIRECTION)
                        && !piece.params.containsKey(SpellParam.GENERIC_NAME_TIME),
                "The default (vector) mode must keep the Direction input and have no Time");
        SpellParam<?> power = piece.params.get(SpellParam.GENERIC_NAME_POWER);
        piece.paramSides.put(power, SpellParam.Side.LEFT);
        piece.setModeOption(PsitweaksModeOptions.MATRIX);
        helper.assertTrue(piece.isMatrixMode() && !piece.params.containsKey(SpellParam.GENERIC_NAME_DIRECTION)
                        && piece.params.containsKey(SpellParam.GENERIC_NAME_TIME) && piece.params.size() == 4,
                "The matrix mode must swap Direction for Time, inputs " + piece.params.keySet());
        helper.assertTrue(piece.paramSides.get(piece.params.get(SpellParam.GENERIC_NAME_POWER)) == SpellParam.Side.LEFT,
                "The Power wire must stay when the mode changes");
        CompoundTag tag = new CompoundTag();
        piece.writeToNBT(tag);
        PieceTrickGravityZone loaded = (PieceTrickGravityZone) vazkii.psi.api.spell.SpellPiece.createFromNBT(new Spell(), tag);
        helper.assertTrue(loaded.isMatrixMode() && loaded.params.containsKey(SpellParam.GENERIC_NAME_TIME),
                "The mode must be saved with the spell");

        helper.assertTrue(PieceTrickGravityZone.matrixUp(null).equals(new Vec3(0.0, 1.0, 0.0)),
                "Without a matrix the zone must push straight up");
        MatrixValue turned = new MatrixValue(4, 4, new double[]{
                0, -3, 0, 0,
                3, 0, 0, 0,
                0, 0, 3, 0,
                0, 0, 0, 1});
        Vec3 up = PieceTrickGravityZone.matrixUp(turned);
        helper.assertTrue(up.distanceTo(new Vec3(-1.0, 0.0, 0.0)) < 1.0E-9, "The push must follow the matrix's Y axis, got " + up);
        helper.succeed();
    }

    /**
     * 行列モードに回した立方体を渡すと、押す向きは回した Y 軸で、効く範囲は輪郭の箱のとおり（角が欠けない、2026-11-12 ユーザー）。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_gravity_mode", timeoutTicks = 40)
    public static void gravityZoneWithTurnedMatrixKeepsItsOutline(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_gravity_turned", new Vec3(4.5, 1.0, 4.5));
        Runnable cleanup = () -> {
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(Entity::discard);
            GameTestPlayers.remove(helper, caster);
        };
        try {
            BlockPos center = helper.absolutePos(new BlockPos(2, 2, 2));
            double c = Math.cos(Math.PI / 4) * 5.0;
            double s = Math.sin(Math.PI / 4) * 5.0;
            // 中心のブロックの角の点（整数の座標）を中心にした一辺 5 の立方体を、Z 軸のまわりに 45° 回したもの。
            MatrixValue matrix = new MatrixValue(4, 4, new double[]{
                    c, -s, 0, center.getX() - (c - s) * 0.5,
                    s, c, 0, center.getY() - (s + c) * 0.5,
                    0, 0, 5, center.getZ() - 2.5,
                    0, 0, 0, 1});
            PieceTrickGravityZone piece = new PieceTrickGravityZone(new Spell()) {
                @Override
                public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                    if (param.name.equals(PsitweaksSpellParams.MATRIX)) {
                        return matrix;
                    }
                    if (param.name.equals(PsitweaksSpellParams.SIZE)) {
                        return 5.0;
                    }
                    return param.name.equals(SpellParam.GENERIC_NAME_POWER) ? 3.0 : null;
                }
            };
            piece.setModeOption(PsitweaksModeOptions.MATRIX);
            piece.execute(new SpellContext().setPlayer(caster));
            List<EntityGravityZone> zones = SpellGramIndex.all(level, EntityGravityZone.class).stream()
                    .filter(zone -> caster.getUUID().equals(zone.getCasterUuid())).toList();
            helper.assertTrue(zones.size() == 1, "One Gravity Zone expected, found " + zones.size());
            EntityGravityZone zone = zones.get(0);
            for (int dx = -2; dx <= 2; dx += 4) {
                for (int dy = -2; dy <= 2; dy += 4) {
                    for (int dz = -2; dz <= 2; dz += 4) {
                        helper.assertTrue(zone.getArea().contains(center.offset(dx, dy, dz)),
                                "The corners of the outline must be inside: " + dx + "," + dy + "," + dz);
                    }
                }
            }
            helper.assertFalse(zone.getArea().contains(center.offset(3, 0, 0)), "Blocks outside the outline must stay outside");
            Vec3 push = zone.getPushDirection();
            helper.assertTrue(push.distanceTo(new Vec3(-Math.sqrt(0.5), Math.sqrt(0.5), 0.0)) < 1.0E-6,
                    "The push must follow the turned Y axis, got " + push);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        cleanup.run();
        helper.succeed();
    }

    /** 行列モードは時間を受け、負の威力で逆向きに押す。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_gravity_mode", timeoutTicks = 40)
    public static void gravityZoneMatrixModeTakesTimeAndSignedPower(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_gravity_mode", new Vec3(4.5, 1.0, 4.5));
        Runnable cleanup = () -> {
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(Entity::discard);
            GameTestPlayers.remove(helper, caster);
        };
        try {
            BlockPos center = helper.absolutePos(new BlockPos(1, 1, 1));
            MatrixValue matrix = new MatrixValue(4, 4, new double[]{
                    3, 0, 0, center.getX() - 1.5,
                    0, 3, 0, center.getY() - 1.5,
                    0, 0, 3, center.getZ() - 1.5,
                    0, 0, 0, 1});
            PieceTrickGravityZone piece = new PieceTrickGravityZone(new Spell()) {
                @Override
                public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                    if (param.name.equals(PsitweaksSpellParams.MATRIX)) {
                        return matrix;
                    }
                    if (param.name.equals(PsitweaksSpellParams.SIZE)) {
                        return 3.0;
                    }
                    if (param.name.equals(SpellParam.GENERIC_NAME_TIME)) {
                        return 60.0;
                    }
                    return param.name.equals(SpellParam.GENERIC_NAME_POWER) ? -6.0 : null;
                }
            };
            piece.setModeOption(PsitweaksModeOptions.MATRIX);
            piece.execute(new SpellContext().setPlayer(caster));
            List<EntityGravityZone> zones = SpellGramIndex.all(level, EntityGravityZone.class).stream()
                    .filter(zone -> caster.getUUID().equals(zone.getCasterUuid())).toList();
            helper.assertTrue(zones.size() == 1, "One Gravity Zone expected, found " + zones.size());
            EntityGravityZone zone = zones.get(0);
            helper.assertTrue(zone.getLifetimeTicks() == 60 * 20, "The Time must set the lifetime, was " + zone.getLifetimeTicks());
            helper.assertTrue(zone.getPower() == 6.0F && zone.getPushDirection().y < -0.99,
                    "A negative Power must push down with its size: power " + zone.getPower() + ", direction " + zone.getPushDirection());
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        cleanup.run();
        helper.succeed();
    }
}
