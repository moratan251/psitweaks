package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIndex;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.SpellGramZone;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickZoneBase;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

import java.util.Comparator;
import java.util.List;

/**
 * 領域のピースを術式の盤面なしで置く GameTest 用の道具。ピースは {@code getRawParamValue} を {@link #paramValue} で
 * 上書きして作り、{@link #cast} が決めたサイズ・威力と、中心のまわりの立方体の行列を返す。
 */
final class ZoneCasting {
    /** 置く前に {@link #cast} が決める入力: サイズ、威力、時間（秒、0 以下なら省く）。 */
    private static final ThreadLocal<double[]> SIZE_AND_POWER = ThreadLocal.withInitial(() -> new double[]{5.0, 1.0, 0.0});

    private ZoneCasting() {
    }

    /**
     * 入力 {@code param} の値: 行列は中心 {@code center}（絶対座標）のまわりの一辺サイズの立方体、サイズ・威力・時間は
     * {@link #cast} の値（時間を決めなければ省く）。
     */
    static Object paramValue(SpellParam<?> param, BlockPos center) {
        double[] sizeAndPower = SIZE_AND_POWER.get();
        if (param.name.equals(PsitweaksSpellParams.MATRIX)) {
            double edge = sizeAndPower[0];
            double low = -Math.floor((edge - 1) / 2.0) - 0.5;
            return new MatrixValue(4, 4, new double[]{
                    edge, 0, 0, center.getX() + low,
                    0, edge, 0, center.getY() + low,
                    0, 0, edge, center.getZ() + low,
                    0, 0, 0, 1});
        }
        if (param.name.equals(PsitweaksSpellParams.SIZE)) {
            return sizeAndPower[0];
        }
        if (param.name.equals(SpellParam.GENERIC_NAME_TIME)) {
            return sizeAndPower[2] > 0.0 ? sizeAndPower[2] : null;
        }
        return param.name.equals(SpellParam.GENERIC_NAME_POWER) ? sizeAndPower[1] : null;
    }

    /** 術者 {@code caster} として、サイズ {@code size}・威力 {@code power} でピースを唱える。 */
    static void cast(ServerPlayer caster, PieceTrickZoneBase<?> piece, double size, double power) throws SpellRuntimeException {
        cast(caster, piece, size, power, 0.0);
    }

    /** {@link #cast} に「時間」（秒）も入れる。 */
    static void cast(ServerPlayer caster, PieceTrickZoneBase<?> piece, double size, double power, double seconds)
            throws SpellRuntimeException {
        SIZE_AND_POWER.set(new double[]{size, power, seconds});
        try {
            piece.execute(new SpellContext().setPlayer(caster));
        } finally {
            SIZE_AND_POWER.remove();
        }
    }

    /** {@link #cast} し、失敗したら {@code cleanup} を走らせてから投げ直す。 */
    static void castOrClean(ServerPlayer caster, PieceTrickZoneBase<?> piece, double size, double power, Runnable cleanup) {
        try {
            cast(caster, piece, size, power);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
    }

    /** 術者 {@code caster} の {@code type} の領域（置いた順）。 */
    static <T extends SpellGramZone> List<T> zonesOf(GameTestHelper helper, ServerPlayer caster, Class<T> type) {
        return SpellGramIndex.ofCaster(helper.getLevel(), caster.getUUID()).stream()
                .filter(type::isInstance).map(type::cast).sorted(Comparator.comparingInt(SpellGramZone::getId)).toList();
    }

    /** 術者 {@code caster} の魔法式オブジェクトを消し、術者をワールドから出す。 */
    static void cleanup(GameTestHelper helper, ServerPlayer caster) {
        SpellGramIndex.ofCaster(helper.getLevel(), caster.getUUID()).forEach(zone -> zone.discard());
        GameTestPlayers.remove(helper, caster);
    }
}
