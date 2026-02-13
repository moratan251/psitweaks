package com.moratan251.psitweaks.common.spells;

import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.param.ParamEntity;
import vazkii.psi.api.spell.piece.PieceTrick;

public class PieceTrickGuillotine extends PieceTrick {

    private static final int POTENCY = 500;
    private static final int COST = 2400;
    private static final float BASE_DAMAGE = 24.0F;

    private SpellParam<Entity> target;

    public PieceTrickGuillotine(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel((double) POTENCY));
        setStatLabel(EnumSpellStat.COST, new StatLabel((double) COST));
    }

    @Override
    public void initParams() {
        addParam(target = new ParamEntity("psi.spellparam.target", SpellParam.YELLOW, false, false));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        meta.addStat(EnumSpellStat.POTENCY, POTENCY);
        meta.addStat(EnumSpellStat.COST, COST);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Entity targetVal = getParamValue(context, target);
        context.verifyEntity(targetVal);

        if (!(targetVal instanceof LivingEntity livingEntity)) {
            throw new SpellRuntimeException("psi.spellerror.nulltarget");
        }
        if (!context.isInRadius(targetVal)) {
            throw new SpellRuntimeException("psi.spellerror.outsideradius");
        }
        if (context.caster.level().isClientSide) {
            return null;
        }

        double perSpellDamageMultiplier = PsitweaksConfig.COMMON.guillotineDamageMultiplier.get();
        double globalDamageMultiplier = PsitweaksConfig.COMMON.globalSpellPowerMultiplier.get();
        float damage = (float) (BASE_DAMAGE * perSpellDamageMultiplier * globalDamageMultiplier);
        playSlashEffect(livingEntity);
        boolean wasAlive = livingEntity.isAlive();
        boolean damaged = livingEntity.hurt(context.caster.damageSources().indirectMagic(context.caster, context.caster), damage);
        if (damaged && wasAlive && !livingEntity.isAlive() && livingEntity instanceof Mob mob) {
            Item headItem = resolveMobHeadItem(mob);
            if (headItem != null) {
                mob.spawnAtLocation(new ItemStack(headItem));
            }
        }

        return null;
    }

    private void playSlashEffect(LivingEntity target) {
        if (target.level() instanceof ServerLevel serverLevel) {
            double x = target.getX();
            double y = target.getY() + target.getBbHeight() * 0.6;
            double z = target.getZ();

            serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, x, y, z, 3, 0.2, 0.1, 0.2, 0.0);
            serverLevel.sendParticles(ParticleTypes.CRIT, x, y, z, 16, 0.35, 0.25, 0.35, 0.08);
            serverLevel.playSound(
                    null,
                    x,
                    y,
                    z,
                    SoundEvents.PLAYER_ATTACK_SWEEP,
                    SoundSource.PLAYERS,
                    1.0F,
                    0.8F
            );
        }
    }

    @Nullable
    private Item resolveMobHeadItem(Mob mob) {
        Item vanillaHead = resolveVanillaHeadItem(mob);
        if (vanillaHead != null) {
            return vanillaHead;
        }

        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(mob.getType());
        if (entityId == null) {
            return null;
        }

        Item dynamicHead = ForgeRegistries.ITEMS.getValue(
                ResourceLocation.fromNamespaceAndPath(entityId.getNamespace(), entityId.getPath() + "_head")
        );
        if (isValidHeadItem(dynamicHead)) {
            return dynamicHead;
        }

        Item dynamicSkull = ForgeRegistries.ITEMS.getValue(
                ResourceLocation.fromNamespaceAndPath(entityId.getNamespace(), entityId.getPath() + "_skull")
        );
        if (isValidHeadItem(dynamicSkull)) {
            return dynamicSkull;
        }

        return null;
    }

    @Nullable
    private Item resolveVanillaHeadItem(Mob mob) {
        if (mob instanceof WitherSkeleton) {
            return Items.WITHER_SKELETON_SKULL;
        }
        if (mob instanceof AbstractSkeleton) {
            return Items.SKELETON_SKULL;
        }
        if (mob instanceof Zombie) {
            return Items.ZOMBIE_HEAD;
        }
        if (mob instanceof Creeper) {
            return Items.CREEPER_HEAD;
        }
        if (mob instanceof AbstractPiglin) {
            return Items.PIGLIN_HEAD;
        }
        if (mob instanceof EnderDragon) {
            return Items.DRAGON_HEAD;
        }
        return null;
    }

    private boolean isValidHeadItem(@Nullable Item item) {
        return item != null && item != Items.AIR;
    }
}
