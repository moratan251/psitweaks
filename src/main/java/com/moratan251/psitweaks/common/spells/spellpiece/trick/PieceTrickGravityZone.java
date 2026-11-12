package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityGravityZone;
import com.moratan251.psitweaksqol.api.PsitweaksModeConfigurable;
import com.moratan251.psitweaksqol.api.PsitweaksModeOption;
import com.moratan251.psitweaksqol.api.PsitweaksModeOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 作動式: 加重領域（仮、Gravity Zone）。範囲の中の生物に、押す向きへ「威力」に応じた運動を毎 tick 足し続ける。威力はコストを
 * 決めるため定数。入力は 4 つまでなので、モードで組み替える（2026-11-12 ユーザー、演算子: 直方体領域と同じ仕組み）:
 * <ul>
 *   <li>ベクトルモード（既定、モードのなかった頃の術式もこれ）: 範囲・サイズ・方向・威力。向きは「方向」（長さは関係ない）。
 *   時間は持たず 30 秒。</li>
 *   <li>行列モード: 範囲・サイズ・威力・時間。向きは範囲の行列の Y 軸（その範囲の「上」）、行列を省けば真上。威力が負なら逆向き
 *   （コストは絶対値）。</li>
 * </ul>
 * モードを変えても、同じ名前の入力（範囲・サイズ・威力）のつなぎ先は残る。
 */
public class PieceTrickGravityZone extends PieceTrickZoneBase<EntityGravityZone> implements PsitweaksModeConfigurable {
    /** 威力の上限（仮）。 */
    public static final double MAX_POWER = 10.0;
    // 仮の数値: 規模 = 10 + 広さ × 威力 / 10、コスト = 40 + 広さ × 威力（広さ = サイズ²）
    private static final int POTENCY_BASE = 10;
    private static final int POTENCY_AREA_DIVISOR = 10;
    private static final int COST_BASE = 40;
    private static final String TAG_MODE = "psitweaksMode";
    private static final List<PsitweaksModeOption> MODES = List.of(PsitweaksModeOptions.VECTOR, PsitweaksModeOptions.MATRIX);

    private PsitweaksModeOption mode = PsitweaksModeOptions.VECTOR;
    private SpellParam<Vector3> direction;
    private SpellParam<Number> power;

    public PieceTrickGravityZone(Spell spell) {
        super(spell, EntityGravityZone.class);
        applyStatLabels();
    }

    private void applyStatLabels() {
        setStatLabel(EnumSpellStat.POTENCY, areaLabel().mul(new StatLabel(SpellParam.GENERIC_NAME_POWER, true))
                .div(POTENCY_AREA_DIVISOR).add(POTENCY_BASE).floor());
        setStatLabel(EnumSpellStat.COST, areaLabel().mul(new StatLabel(SpellParam.GENERIC_NAME_POWER, true))
                .add(COST_BASE).floor());
    }

    @Override
    public void initParams() {
        rebuildParams(null);
    }

    @Override
    public List<PsitweaksModeOption> getAvailableModeOptions() {
        return MODES;
    }

    @Override
    public PsitweaksModeOption getModeOption() {
        return normalizeModeOption(mode);
    }

    @Override
    public void setModeOption(PsitweaksModeOption modeOption) {
        PsitweaksModeOption nextMode = normalizeModeOption(modeOption);
        if (nextMode.id().equals(getModeOption().id())) {
            return;
        }
        Map<String, SpellParam.Side> savedSides = new LinkedHashMap<>();
        paramSides.forEach((param, side) -> savedSides.put(param.name, side));
        mode = nextMode;
        rebuildParams(savedSides);
        applyStatLabels();
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        mode = normalizeModeOption(PsitweaksModeOptions.byId(tag.getString(TAG_MODE)).orElse(null));
        rebuildParams(null);
        applyStatLabels();
        super.readFromNBT(tag);
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putString(TAG_MODE, getModeOption().serializedId());
    }

    /** 行列モードか（向きを範囲の行列の Y 軸から取り、時間を持つ）。 */
    public boolean isMatrixMode() {
        return getModeOption().id().equals(PsitweaksModeOptions.MATRIX.id());
    }

    private void rebuildParams(Map<String, SpellParam.Side> savedSides) {
        params.clear();
        paramSides.clear();
        direction = null;
        power = null;
        super.initParams();
        if (savedSides != null) {
            for (SpellParam<?> param : params.values()) {
                SpellParam.Side side = savedSides.get(param.name);
                if (side != null) {
                    paramSides.put(param, side);
                }
            }
        }
    }

    @Override
    protected void addZoneParams() {
        if (!isMatrixMode()) {
            addParam(direction = new ParamVector(SpellParam.GENERIC_NAME_DIRECTION, SpellParam.BLUE, false, false));
        }
        addParam(power = new ParamNumber(SpellParam.GENERIC_NAME_POWER, SpellParam.RED, false, true));
    }

    /** ベクトルモードは範囲・サイズ・方向・威力で入力の 4 つが埋まるので時間を持たない（30 秒）。行列モードは持つ。 */
    @Override
    protected boolean hasTimeParam() {
        return isMatrixMode();
    }

    @Override
    protected int potency(int area) throws SpellCompilationException {
        return POTENCY_BASE + (int) Math.floor(area * constantPower() / POTENCY_AREA_DIVISOR);
    }

    @Override
    protected int cost(int area) throws SpellCompilationException {
        return COST_BASE + (int) Math.floor(area * constantPower());
    }

    /** 術式を組んだ時点の威力の大きさ。ベクトルモードは正、行列モードは 0 でなければよい（負は逆向き）。 */
    private double constantPower() throws SpellCompilationException {
        Object value = getParamEvaluation(power);
        if (!(value instanceof Number number) || (isMatrixMode() ? number.doubleValue() == 0.0 : number.doubleValue() <= 0.0)) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_VALUE, x, y);
        }
        return Math.min(MAX_POWER, Math.abs(number.doubleValue()));
    }

    @Override
    protected EntityGravityZone createZone(ServerLevel level) {
        return new EntityGravityZone(level);
    }

    @Override
    protected void configure(EntityGravityZone zone, SpellContext context) throws SpellRuntimeException {
        double powerValue = getNotNullParamValue(context, power).doubleValue();
        Vec3 push;
        if (isMatrixMode()) {
            if (powerValue == 0.0) {
                throw new SpellRuntimeException(SpellRuntimeException.NON_POSITIVE_VALUE);
            }
            push = matrixUp(regionValue(context));
            if (powerValue < 0.0) {
                push = push.scale(-1.0);
            }
        } else {
            Vector3 dir = getNotNullParamValue(context, direction);
            if (dir.isZero()) {
                throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
            }
            if (powerValue <= 0.0) {
                throw new SpellRuntimeException(SpellRuntimeException.NON_POSITIVE_VALUE);
            }
            push = dir.toVec3D();
        }
        zone.configurePush(push, (float) Math.min(MAX_POWER, Math.abs(powerValue)));
    }

    /** 行列の Y 軸（2 列目の上 3 行、長さは関係ない）。行列がない・Y 軸が潰れていれば真上。 */
    public static Vec3 matrixUp(MatrixValue matrix) {
        if (matrix == null || matrix.rows() < 3 || matrix.cols() < 2) {
            return new Vec3(0.0, 1.0, 0.0);
        }
        Vec3 axis = new Vec3(matrix.get(0, 1), matrix.get(1, 1), matrix.get(2, 1));
        return axis.lengthSqr() < 1.0E-12 ? new Vec3(0.0, 1.0, 0.0) : axis.normalize();
    }
}
