package com.moratan251.psitweaks.common.spells;

import com.moratan251.psitweaks.common.attributes.PsitweaksAttributes;
import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.entities.SpellGram.EntityIceCircle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;

import java.util.UUID;

public class PieceTrickIceCircle extends PieceTrick {

    private static final double MIN_POWER = 0.5D;
    private static final float BASE_DAMAGE = 2.0F;
    private static final float DAMAGE_PER_POWER = 2.0F;
    private static final float FIXED_RADIUS = 8.0F;
    private static final float FIXED_VISUAL_SCALE = FIXED_RADIUS * 0.5F;
    private static final int LIFETIME_TICKS = 60 * 20;
    private static final int DAMAGE_INTERVAL_TICKS = 10;
    private static final double POTENCY_BASE = 200.0D;
    private static final double POTENCY_PER_POWER = 120.0D;
    private static final double COST_BASE = 2000.0D;
    private static final double COST_PER_POWER = 1200.0D;

    private static final String NBT_ICE_UUID = "PsitweaksIceCircleUUID";
    private static final String NBT_ICE_DIM  = "PsitweaksIceCircleDim";

    private SpellParam<Vector3> position;
    private SpellParam<Number> power;

    public PieceTrickIceCircle(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY,
                new StatLabel("psi.spellparam.power", true).max(MIN_POWER).mul(POTENCY_PER_POWER).add(POTENCY_BASE).floor());
        setStatLabel(EnumSpellStat.COST,
                new StatLabel("psi.spellparam.power", true).max(MIN_POWER).mul(COST_PER_POWER).add(COST_BASE).floor());
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
        addParam(power = new ParamNumber("psi.spellparam.power", SpellParam.RED, false, true));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);

        Double powerVal = getParamEvaluation(power);
        if (powerVal == null || powerVal <= 0.0D) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_VALUE, x, y);
        }

        powerVal = Math.max(MIN_POWER, powerVal);
        meta.addStat(EnumSpellStat.POTENCY, (int) (POTENCY_BASE + powerVal * POTENCY_PER_POWER));
        meta.addStat(EnumSpellStat.COST, (int) (COST_BASE + powerVal * COST_PER_POWER));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 positionVal = getParamValue(context, position);
        double powerVal = getParamValue(context, power).doubleValue();

        if (positionVal == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }
        if (powerVal <= 0.0D) {
            throw new SpellRuntimeException(SpellRuntimeException.NON_POSITIVE_VALUE);
        }
        if (!context.isInRadius(positionVal)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }
        if (!(context.caster.level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        double clampedPower = Math.max(MIN_POWER, powerVal);
        float baseDamage = calculateDamage(clampedPower);

        double spellPowerMultiplier = PsitweaksConfig.COMMON.iceCircleDamageMultiplier.get();
        double globalDamageMultiplier = PsitweaksConfig.COMMON.globalSpellPowerMultiplier.get();
        double spellDamageFactor = context.caster.getAttributeValue(PsitweaksAttributes.SPELL_DAMAGE_FACTOR.get());
        float finalDamage = (float) (baseDamage * spellPowerMultiplier * globalDamageMultiplier * spellDamageFactor);

        ItemStack cad = context.tool;
        removeExistingIceCircleByCAD(serverLevel, cad);

        EntityIceCircle iceCircle = new EntityIceCircle(serverLevel);
        iceCircle.setPos(positionVal.x, positionVal.y + 0.01D, positionVal.z);
        iceCircle.setCaster(context.caster);
        iceCircle.setSafeToPlayers(SpellSafetyUtils.hasSafeToPlayers(context));
        iceCircle.setVisualScale(FIXED_VISUAL_SCALE);
        iceCircle.setLifetimeTicks(LIFETIME_TICKS);
        iceCircle.configureAreaDamage(FIXED_RADIUS, finalDamage, DAMAGE_INTERVAL_TICKS);

        serverLevel.addFreshEntity(iceCircle);
        saveIceCircleToCAD(cad, iceCircle, serverLevel);

        Vec3 center = iceCircle.position();
        serverLevel.playSound(
                null,
                center.x,
                center.y,
                center.z,
                SoundEvents.GLASS_BREAK,
                SoundSource.PLAYERS,
                0.8F,
                1.2F
        );
        serverLevel.sendParticles(
                ParticleTypes.SNOWFLAKE,
                center.x,
                center.y + 0.05D,
                center.z,
                18,
                FIXED_RADIUS * 0.35D,
                0.08D,
                FIXED_RADIUS * 0.35D,
                0.01D
        );

        return null;
    }

    private static void removeExistingIceCircleByCAD(ServerLevel castingLevel, ItemStack cad) {
        CompoundTag nbt = cad.getOrCreateTag();
        if (!nbt.hasUUID(NBT_ICE_UUID)) {
            return;
        }

        UUID uuid = nbt.getUUID(NBT_ICE_UUID);
        String dimKey = nbt.getString(NBT_ICE_DIM);

        for (ServerLevel level : castingLevel.getServer().getAllLevels()) {
            if (level.dimension().location().toString().equals(dimKey)) {
                Entity entity = level.getEntity(uuid);
                if (entity instanceof EntityIceCircle circle) {
                    circle.destroyBySpell();
                }
                break;
            }
        }

        nbt.remove(NBT_ICE_UUID);
        nbt.remove(NBT_ICE_DIM);
    }

    private static void saveIceCircleToCAD(ItemStack cad, EntityIceCircle circle, ServerLevel level) {
        CompoundTag nbt = cad.getOrCreateTag();
        nbt.putUUID(NBT_ICE_UUID, circle.getUUID());
        nbt.putString(NBT_ICE_DIM, level.dimension().location().toString());
    }

    private static float calculateDamage(double powerVal) {
        return BASE_DAMAGE + (float) (powerVal * DAMAGE_PER_POWER);
    }
}
