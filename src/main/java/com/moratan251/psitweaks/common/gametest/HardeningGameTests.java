package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.effects.PsitweaksEffects;
import com.moratan251.psitweaks.common.handler.EffectHandler;
import java.util.List;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Trick: Hardening caps one hit at 12 − 2 × level (1 from level 6). It caps damage that armor reduces plus falling and
 * flying into a wall, and lets poison, magic, /kill and other armor-bypassing damage through.
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HardeningGameTests {
    private static final float HEALTH = 200.0F;
    private static final float HIT = 30.0F;

    private HardeningGameTests() {
    }

    private record Hit(String name, DamageSource source, boolean capped) {
    }

    @GameTest(template = "psi110_empty")
    public static void hardeningCapsArmorDamageFallAndWallsOnly(GameTestHelper helper) {
        helper.assertTrue(EffectHandler.hardeningCap(0) == 10.0F && EffectHandler.hardeningCap(4) == 2.0F
                        && EffectHandler.hardeningCap(5) == 1.0F && EffectHandler.hardeningCap(9) == 1.0F,
                "The cap must be 12 - 2 x level up to level 5 and 1 from level 6");

        Husk husk = GameTestPlayers.tough(helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(1.5, 1.0, 1.5)), HEALTH);
        try {
            husk.setNoAi(true);
            husk.addEffect(new MobEffectInstance(PsitweaksEffects.HARDENING, 20 * 60, 0));
            float cap = EffectHandler.hardeningCap(0);
            DamageSources sources = helper.getLevel().damageSources();
            List<Hit> hits = List.of(
                    new Hit("cactus", sources.cactus(), true),
                    new Hit("fall", sources.fall(), true),
                    new Hit("fly_into_wall", sources.flyIntoWall(), true),
                    new Hit("magic", sources.magic(), false),
                    new Hit("wither", sources.wither(), false),
                    new Hit("generic_kill", sources.genericKill(), false),
                    new Hit("fell_out_of_world", sources.fellOutOfWorld(), false));
            for (Hit hit : hits) {
                husk.setHealth(HEALTH);
                husk.invulnerableTime = 0;
                husk.hurt(hit.source(), HIT);
                float lost = HEALTH - husk.getHealth();
                helper.assertTrue(hit.capped() ? lost > 0.0F && lost <= cap + 0.001F : lost > cap,
                        hit.name() + " took " + lost + " under Hardening; expected " + (hit.capped() ? "at most " + cap : "more than " + cap));
            }
            helper.succeed();
        } finally {
            husk.discard();
        }
    }
}
