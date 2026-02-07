package com.moratan251.psitweaks.common.spells;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamEntity;
import vazkii.psi.api.spell.piece.PieceTrick;

public class PieceTrickCocytus extends PieceTrick {
    SpellParam<Entity> target;

    public PieceTrickCocytus(Spell spell) {
        super(spell);
        this.setStatLabel(EnumSpellStat.POTENCY,new StatLabel((double)1250));
        this.setStatLabel(EnumSpellStat.COST, new StatLabel((double)5000));
    }

    public void initParams() {
        this.addParam(this.target = new ParamEntity("psi.spellparam.target", SpellParam.YELLOW, false, false));
    }

    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        meta.addStat(EnumSpellStat.POTENCY, 1250);
        meta.addStat(EnumSpellStat.COST, 5000);
    }

    public Object execute(SpellContext context) throws SpellRuntimeException {
        Entity targetVal = (Entity)this.getParamValue(context, this.target);
        context.verifyEntity(targetVal);
        if (!(targetVal instanceof LivingEntity)) {
            throw new SpellRuntimeException("psi.spellerror.nulltarget");
        } else if (!context.isInRadius(targetVal)) {
            throw new SpellRuntimeException("psi.spellerror.outsideradius");
        } else {
            if (!(targetVal instanceof Player)) {

                if(targetVal instanceof Mob mob){
                    mob.setNoAi(true);
                    if (context.caster.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(
                                ParticleTypes.SNOWFLAKE,  // パーティクルの種類
                                mob.getX(), mob.getY(), mob.getZ(),              // 座標
                                30,                   // パーティクルの数
                                0.5, 0.5, 0.5,       // X, Y, Z方向の拡散範囲
                                0.02                  // 速度
                        );
                    }
                    if (!context.caster.level().isClientSide) {
                        context.caster.level().playSound(
                                null,
                                mob.getX(), mob.getY(), mob.getZ(),
                                SoundEvents.GLASS_BREAK,
                                SoundSource.PLAYERS,
                                1.0F,
                                1.0F
                        );
                    }

                }

            }
            return null;
        }
    }
}