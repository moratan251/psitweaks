package com.moratan251.psitweaks.common.entities;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.entities.SpellGram.EntityFlareCircle;
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

    public static void register(IEventBus bus) {
        ENTITY_TYPES.register(bus);
    }
}
