package com.moratan251.psitweaks.client.combat;

import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickGramDemolition;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

/**
 * The snow of Trick: Cocytus's blizzard: for a few ticks, snowflakes, clumps of snow and puffs of mist blow out of the
 * caster's eyes through Trick: Gram Demolition's cone. Each particle flies off on its own line inside the cone, at a
 * speed that carries it somewhere between a third of the way and the far end, so the snow fills the whole cone. The
 * faint frost front drawn with it is a {@link ConeWave}.
 * <p>
 * Particles are added the usual way, so the client's particle setting thins them out.
 */
final class ConeBlizzard {
    /** How many ticks the snow keeps blowing. */
    private static final int BLOW_TICKS = 8;
    /** How far in front of the eyes the snow starts, so it does not cover the caster's own view. */
    private static final double START = 0.9;
    /** Mist starts farther out, as its puffs are big. */
    private static final double MIST_START = 1.8;
    private static final int FLAKES_PER_TICK = 26;
    private static final int CLUMPS_PER_TICK = 5;
    private static final int MIST_PER_TICK = 4;
    /**
     * How far a snowflake flies for each block per tick it starts with: vanilla snowflakes keep 95% of their speed every
     * tick, which adds up to 20 times the starting speed.
     */
    private static final double FLAKE_REACH_PER_SPEED = 20.0;
    /** The same for the mist (vanilla poof particles keep 90% of their speed every tick). */
    private static final double MIST_REACH_PER_SPEED = 10.0;

    private final Vec3 tip;
    private final Vec3 axis;
    private final Vec3 side;
    private final Vec3 up;
    private final double reach;
    private final double halfAngle;
    private final RandomSource random = RandomSource.create();
    private int age;

    /**
     * @param tip    the caster's eyes
     * @param axis   the way the cone opens
     * @param length how far the cone reaches from its tip
     */
    ConeBlizzard(Vec3 tip, Vec3 axis, float length) {
        this.tip = tip;
        this.axis = axis.lengthSqr() > 1.0E-6 ? axis.normalize() : new Vec3(0.0, 1.0, 0.0);
        Vec3 helper = Math.abs(this.axis.y) < 0.95 ? new Vec3(0.0, 1.0, 0.0) : new Vec3(1.0, 0.0, 0.0);
        this.side = helper.cross(this.axis).normalize();
        this.up = this.axis.cross(side).normalize();
        this.reach = Math.max(1.0, length - START);
        this.halfAngle = Math.toRadians(PieceTrickGramDemolition.HALF_ANGLE_DEGREES);
    }

    /** Blows this tick's snow. Returns false once the blizzard is over. */
    boolean tick(ClientLevel level) {
        if (age >= BLOW_TICKS) {
            return false;
        }
        // Strongest at first, easing off towards the end of the gust.
        float left = 1.0F - (float) age / BLOW_TICKS;
        float amount = 0.45F + 0.55F * left;
        for (int i = 0; i < Math.round(FLAKES_PER_TICK * amount); i++) {
            blow(level, ParticleTypes.SNOWFLAKE, START, reach / FLAKE_REACH_PER_SPEED * (0.3 + 0.75 * random.nextDouble()), 0.0);
        }
        for (int i = 0; i < Math.round(CLUMPS_PER_TICK * amount); i++) {
            blow(level, ParticleTypes.ITEM_SNOWBALL, START, 0.35 + 0.45 * random.nextDouble(), 0.08);
        }
        for (int i = 0; i < Math.round(MIST_PER_TICK * amount); i++) {
            blow(level, ParticleTypes.POOF, MIST_START,
                    (reach - MIST_START) / MIST_REACH_PER_SPEED * (0.25 + 0.6 * random.nextDouble()), 0.0);
        }
        age++;
        return true;
    }

    /** Adds one particle {@code start} blocks out along a random line inside the cone, flying along it. */
    private void blow(ClientLevel level, ParticleOptions particle, double start, double speed, double lift) {
        Vec3 direction = randomDirection();
        Vec3 from = tip.add(direction.scale(start + 0.4 * random.nextDouble()));
        Vec3 velocity = direction.scale(speed).add(0.0, lift, 0.0);
        level.addParticle(particle, from.x, from.y, from.z, velocity.x, velocity.y, velocity.z);
    }

    /** A unit direction inside the cone, spread evenly over its cross-section. */
    private Vec3 randomDirection() {
        double polar = halfAngle * Math.sqrt(random.nextDouble());
        double azimuth = random.nextDouble() * Mth.TWO_PI;
        Vec3 radial = side.scale(Math.cos(azimuth)).add(up.scale(Math.sin(azimuth)));
        return axis.scale(Math.cos(polar)).add(radial.scale(Math.sin(polar)));
    }
}
