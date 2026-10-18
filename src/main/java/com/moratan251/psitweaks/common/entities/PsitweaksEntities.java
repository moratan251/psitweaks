package com.moratan251.psitweaks.common.entities;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.entities.SpellGram.EntityFlareCircle;
import com.moratan251.psitweaks.common.entities.combat.EntityChainLightning;
import com.moratan251.psitweaks.common.entities.combat.EntityDancingBlade;
import com.moratan251.psitweaks.common.entities.combat.EntityGravityWell;
import com.moratan251.psitweaks.common.entities.combat.EntityGroundShockwave;
import com.moratan251.psitweaks.common.entities.combat.EntityKamaitachiBlade;
import com.moratan251.psitweaks.common.entities.combat.EntityParadeDecoy;
import com.moratan251.psitweaks.common.entities.combat.EntityStarfallMeteor;
import com.moratan251.psitweaks.common.entities.SpellGram.EntityIceCircle;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PsitweaksEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Psitweaks.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<EntityTimeAccelerator>> TIME_ACCELERATOR =
            ENTITY_TYPES.register("time_accelerator",
                    () -> EntityType.Builder.<EntityTimeAccelerator>of(EntityTimeAccelerator::new, MobCategory.MISC)
                            // 当たり判定なし・描画不要の想定。必要ならサイズ調整
                            .sized(0.1f, 0.1f)
                            .clientTrackingRange(32)
                            .updateInterval(1)
                            .build(Psitweaks.MOD_ID + ":time_accelerator"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityPhononMaserBeam>> PHONON_MASER_BEAM =
            ENTITY_TYPES.register("phonon_maser_beam",
                    () -> EntityType.Builder.<EntityPhononMaserBeam>of(EntityPhononMaserBeam::new, MobCategory.MISC)
                            .sized(0.1f, 0.1f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .fireImmune()
                            .build(Psitweaks.MOD_ID + ":phonon_maser_beam"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityMeteorLineBeam>> METEOR_LINE_BEAM =
            ENTITY_TYPES.register("meteor_line_beam",
                    () -> EntityType.Builder.<EntityMeteorLineBeam>of(EntityMeteorLineBeam::new, MobCategory.MISC)
                            .sized(0.1f, 0.1f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .fireImmune()
                            .build(Psitweaks.MOD_ID + ":meteor_line_beam"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityMolecularDivider>> MOLECULAR_DIVIDER =
            ENTITY_TYPES.register("molecular_divider",
                    () -> EntityType.Builder.<EntityMolecularDivider>of(EntityMolecularDivider::new, MobCategory.MISC)
                            .sized(0.1f, 0.1f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .fireImmune()
                            .build(Psitweaks.MOD_ID + ":molecular_divider"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityAquaCutterProjectile>> AQUA_CUTTER_PROJECTILE =
            ENTITY_TYPES.register("aqua_cutter_projectile",
                    () -> EntityType.Builder.<EntityAquaCutterProjectile>of(EntityAquaCutterProjectile::new, MobCategory.MISC)
                            .sized(0.3125f, 0.3125f)
                            .clientTrackingRange(64)
                            .updateInterval(10)
                            .build(Psitweaks.MOD_ID + ":aqua_cutter_projectile"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityDryIceProjectile>> DRY_ICE_PROJECTILE =
            ENTITY_TYPES.register("dry_ice_projectile",
                    () -> EntityType.Builder.<EntityDryIceProjectile>of(EntityDryIceProjectile::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(64)
                            .updateInterval(10)
                            .build(Psitweaks.MOD_ID + ":dry_ice_projectile"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityBlazeBall>> BLAZE_BALL =
            ENTITY_TYPES.register("blaze_ball",
                    () -> EntityType.Builder.<EntityBlazeBall>of(EntityBlazeBall::new, MobCategory.MISC)
                            .sized(0.3125f, 0.3125f)
                            .clientTrackingRange(64)
                            .updateInterval(10)
                            .fireImmune()
                            .build(Psitweaks.MOD_ID + ":blaze_ball"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityFlareCircle>> FLARE_CIRCLE =
            ENTITY_TYPES.register("flare_circle",
                    () -> EntityType.Builder.<EntityFlareCircle>of(EntityFlareCircle::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .fireImmune()
                            .build(Psitweaks.MOD_ID + ":flare_circle"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityIceCircle>> ICE_CIRCLE =
            ENTITY_TYPES.register("ice_circle",
                    () -> EntityType.Builder.<EntityIceCircle>of(EntityIceCircle::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .fireImmune()
                            .build(Psitweaks.MOD_ID + ":ice_circle"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityTunnelerArrow>> TUNNELER_ARROW =
            ENTITY_TYPES.register("tunneler_arrow",
                    () -> EntityType.Builder.<EntityTunnelerArrow>of(EntityTunnelerArrow::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .eyeHeight(0.13f)
                            .clientTrackingRange(4)
                            .updateInterval(20)
                            .build(Psitweaks.MOD_ID + ":tunneler_arrow"));

    // Combat trick effects: short-lived, never saved (see CombatSpellEntity).
    public static final DeferredHolder<EntityType<?>, EntityType<EntityGroundShockwave>> GROUND_SHOCKWAVE =
            ENTITY_TYPES.register("ground_shockwave",
                    () -> EntityType.Builder.<EntityGroundShockwave>of(EntityGroundShockwave::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(8)
                            .updateInterval(20)
                            .fireImmune()
                            .noSave()
                            .build(Psitweaks.MOD_ID + ":ground_shockwave"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityKamaitachiBlade>> KAMAITACHI_BLADE =
            ENTITY_TYPES.register("kamaitachi_blade",
                    () -> EntityType.Builder.<EntityKamaitachiBlade>of(EntityKamaitachiBlade::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .fireImmune()
                            .noSave()
                            .build(Psitweaks.MOD_ID + ":kamaitachi_blade"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityDancingBlade>> DANCING_BLADE =
            ENTITY_TYPES.register("dancing_blade",
                    () -> EntityType.Builder.<EntityDancingBlade>of(EntityDancingBlade::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .fireImmune()
                            .noSave()
                            .build(Psitweaks.MOD_ID + ":dancing_blade"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityStarfallMeteor>> STARFALL_METEOR =
            ENTITY_TYPES.register("starfall_meteor",
                    () -> EntityType.Builder.<EntityStarfallMeteor>of(EntityStarfallMeteor::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(10)
                            .updateInterval(20)
                            .fireImmune()
                            .noSave()
                            .build(Psitweaks.MOD_ID + ":starfall_meteor"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityGravityWell>> GRAVITY_WELL =
            ENTITY_TYPES.register("gravity_well",
                    () -> EntityType.Builder.<EntityGravityWell>of(EntityGravityWell::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(8)
                            .updateInterval(20)
                            .fireImmune()
                            .noSave()
                            .build(Psitweaks.MOD_ID + ":gravity_well"));

    /** Deals the later jumps of Trick: Chain Lightning; it is never sent to clients. */
    public static final DeferredHolder<EntityType<?>, EntityType<EntityChainLightning>> CHAIN_LIGHTNING =
            ENTITY_TYPES.register("chain_lightning",
                    () -> EntityType.Builder.<EntityChainLightning>of(EntityChainLightning::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(4)
                            .updateInterval(20)
                            .fireImmune()
                            .noSave()
                            .noSummon()
                            .build(Psitweaks.MOD_ID + ":chain_lightning"));

    /** The lookalike of Trick: Parade: player-sized, never saved. */
    public static final DeferredHolder<EntityType<?>, EntityType<EntityParadeDecoy>> PARADE_DECOY =
            ENTITY_TYPES.register("parade_decoy",
                    () -> EntityType.Builder.<EntityParadeDecoy>of(EntityParadeDecoy::new, MobCategory.MISC)
                            .sized(0.6f, 1.8f)
                            .eyeHeight(1.62f)
                            .clientTrackingRange(10)
                            .noSave()
                            .noSummon()
                            .build(Psitweaks.MOD_ID + ":parade_decoy"));

    public static void register(IEventBus bus) {
        ENTITY_TYPES.register(bus);
    }
}
