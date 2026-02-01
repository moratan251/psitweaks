package com.moratan251.psitweaks.common.entities;

import com.moratan251.psitweaks.Psitweaks;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PsitweaksEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Psitweaks.MOD_ID);

    public static final RegistryObject<EntityType<com.moratan251.psitweaks.common.entities.EntityTimeAccelerator>> TIME_ACCELERATOR =
            ENTITY_TYPES.register("time_accelerator",
                    () -> EntityType.Builder.<EntityTimeAccelerator>of(EntityTimeAccelerator::new, MobCategory.MISC)
                            // 当たり判定なし・描画不要の想定。必要ならサイズ調整
                            .sized(0.1f, 0.1f)
                            .clientTrackingRange(32)
                            .updateInterval(1)
                            .build(Psitweaks.MOD_ID + ":time_accelerator"));

    public static final RegistryObject<EntityType<EntityPhononMaserBeam>> PHONON_MASER_BEAM =
            ENTITY_TYPES.register("phonon_maser_beam",
                    () -> EntityType.Builder.<EntityPhononMaserBeam>of(EntityPhononMaserBeam::new, MobCategory.MISC)
                            .sized(0.1f, 0.1f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .fireImmune()
                            .build(Psitweaks.MOD_ID + ":phonon_maser_beam"));

    public static void register(IEventBus bus) {
        ENTITY_TYPES.register(bus);
    }
}