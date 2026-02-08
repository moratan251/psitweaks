package com.moratan251.psitweaks.common.spells;

import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.handler.AquaCutterProjectileHandler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceTrick;

public class PieceTrickAquaCutter extends PieceTrick {

    private static final double MIN_POWER = 0.1;
    private static final float BASE_DAMAGE = 2.0F;
    private static final float DAMAGE_PER_POWER = 3.0F;

    private SpellParam<Number> power;

    public PieceTrickAquaCutter(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel("psi.spellparam.power", true).max(0.1).mul(100).floor());
        setStatLabel(EnumSpellStat.COST, new StatLabel("psi.spellparam.power", true).max(0.1).mul(200).add(200).floor());
    }

    @Override
    public void initParams() {
        addParam(power = new ParamNumber("psi.spellparam.power", SpellParam.RED, false, true));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        Double powerVal = getParamEvaluation(power);
        if (powerVal == null || powerVal <= 0.0) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_VALUE, x, y);
        }

        powerVal = Math.max(MIN_POWER, powerVal);
        meta.addStat(EnumSpellStat.POTENCY, (int) (powerVal * 100));
        meta.addStat(EnumSpellStat.COST, (int) (200 + powerVal * 200));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        double powerVal = getParamValue(context, power).doubleValue();
        if (powerVal <= 0.0) {
            throw new SpellRuntimeException(SpellRuntimeException.NON_POSITIVE_VALUE);
        }

        Level level = context.caster.level();
        if (level.isClientSide) {
            return null;
        }

        float damage = (float) (BASE_DAMAGE + powerVal * DAMAGE_PER_POWER);
        double perSpellDamageMul = PsitweaksConfig.COMMON.aquaCutterDamageMultiplier.get();
        double globalDamageMul = PsitweaksConfig.COMMON.globalSpellPowerMultiplier.get();
        float finalDamage = (float) (damage * perSpellDamageMul * globalDamageMul);
        float velocity = 1.8F + (float) (powerVal * 0.3);

        Snowball projectile = new Snowball(level, context.caster);
        projectile.setItem(new ItemStack(Items.PRISMARINE_SHARD));
        Vec3 look = context.caster.getLookAngle();
        projectile.setPos(context.caster.getX(), context.caster.getEyeY() - 0.1, context.caster.getZ());
        projectile.shoot(look.x, look.y, look.z, velocity, 0.0F);

        CompoundTag data = projectile.getPersistentData();
        data.putBoolean(AquaCutterProjectileHandler.TAG_AQUA_CUTTER, true);
        data.putFloat(AquaCutterProjectileHandler.TAG_AQUA_CUTTER_DAMAGE, finalDamage);

        level.addFreshEntity(projectile);
        level.playSound(null, context.caster.getX(), context.caster.getY(), context.caster.getZ(),
                SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 0.6F, 1.3F);

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SPLASH, context.caster.getX(), context.caster.getEyeY(), context.caster.getZ(),
                    8, 0.15, 0.15, 0.15, 0.05);
        }

        return null;
    }
}
