package com.moratan251.psitweaks.common.particles;

import com.mojang.serialization.MapCodec;
import com.moratan251.psitweaks.Psitweaks;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

/** Psitweaks の粒の種類。見た目はクライアント側（{@code PsitweaksParticleProviders}）で決める。 */
public final class PsitweaksParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, Psitweaks.MOD_ID);

    /** 色を付けた雲（バニラの雲と同じ見た目と動き）。熱乱流で乱流領域の煙をオレンジにするのに使う。 */
    public static final DeferredHolder<ParticleType<?>, ParticleType<ColorParticleOption>> TINTED_CLOUD =
            PARTICLE_TYPES.register("tinted_cloud", () -> new ParticleType<ColorParticleOption>(false) {
                @Override
                public @NotNull MapCodec<ColorParticleOption> codec() {
                    return ColorParticleOption.codec(this);
                }

                @Override
                public @NotNull StreamCodec<? super net.minecraft.network.RegistryFriendlyByteBuf, ColorParticleOption> streamCodec() {
                    StreamCodec<? super ByteBuf, ColorParticleOption> codec = ColorParticleOption.streamCodec(this);
                    return codec;
                }
            });

    private PsitweaksParticles() {
    }

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }
}
