package com.moratan251.psitweaks.client.particles;

import com.moratan251.psitweaks.common.particles.PsitweaksParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.PlayerCloudParticle;
import net.minecraft.client.particle.SpriteSet;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

/** Psitweaks の粒の見た目（クライアント側だけ）。 */
public final class PsitweaksParticleProviders {
    private PsitweaksParticleProviders() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(PsitweaksParticleProviders::onRegisterParticleProviders);
    }

    private static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(PsitweaksParticles.TINTED_CLOUD.get(),
                sprites -> (options, level, x, y, z, xSpeed, ySpeed, zSpeed) -> new TintedCloudParticle(level, x, y, z,
                        xSpeed, ySpeed, zSpeed, sprites, options.getRed(), options.getGreen(), options.getBlue()));
    }

    /** バニラの雲と同じ見た目と動きで、色だけ変えた粒（明るさに少しむらを付ける）。 */
    private static final class TintedCloudParticle extends PlayerCloudParticle {
        TintedCloudParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed,
                            SpriteSet sprites, float red, float green, float blue) {
            super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
            float shade = 0.85F + 0.15F * random.nextFloat();
            setColor(red * shade, green * shade, blue * shade);
        }
    }
}
