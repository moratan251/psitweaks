package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.attachments.PsitweaksAttachments;
import com.moratan251.psitweaks.common.entities.combat.EntityChainLightning;
import com.moratan251.psitweaks.common.handler.AquaCutterProjectileHandler;
import com.moratan251.psitweaks.common.registries.PsitweaksDamageTypes;
import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import com.moratan251.psitweaks.common.spells.combat.CombatStatuses;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The wet state Trick: Aqua Cutter leaves ({@link CombatStatuses}): it counts as in water or rain, keeps fire off, dries
 * on time, and makes Trick: Chain Lightning hit harder; a burning creature takes more from the blade instead and stays
 * dry. Rain is allowed for, as it makes every creature wet.
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WetStateGameTests {
    private static final String TEMPLATE = "connector_empty";
    private static final String BATCH = "psitweaks_wet";
    private static final float HEALTH = 200.0F;

    private WetStateGameTests() {
    }

    /** A goal-less, unarmoured husk with {@link #HEALTH} health, so damage arrives exactly. */
    private static Husk husk(GameTestHelper helper, BlockPos relative) {
        Husk husk = helper.spawnWithNoFreeWill(EntityType.HUSK, Vec3.atBottomCenterOf(relative));
        husk.getAttribute(Attributes.ARMOR).setBaseValue(0.0);
        husk.getAttribute(Attributes.MAX_HEALTH).setBaseValue(HEALTH);
        husk.setHealth(HEALTH);
        husk.setNoAi(true);
        return husk;
    }

    @GameTest(template = TEMPLATE, batch = BATCH)
    public static void soakedCreatureIsWetDoesNotBurnAndDries(GameTestHelper helper) {
        var husk = husk(helper, new BlockPos(2, 0, 2));
        husk.setRemainingFireTicks(100);
        CombatStatuses.soak(husk, 3);
        helper.assertTrue(CombatStatuses.isSoaked(husk) && husk.isInWaterOrRain(),
                "Aqua Cutter's wet state must count as in water or rain");
        helper.assertTrue(husk.getRemainingFireTicks() <= 0, "Getting wet must put the fire out");
        husk.igniteForSeconds(5);
        helper.assertTrue(husk.getRemainingFireTicks() <= 0, "A wet creature must not catch fire");
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(!husk.hasData(PsitweaksAttachments.SOAK),
                        "The wet state must dry and be removed on time"))
                .thenExecute(() -> {
                    husk.igniteForSeconds(5);
                    helper.assertTrue(husk.getRemainingFireTicks() > 0, "A dried creature must catch fire again");
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE, batch = BATCH)
    public static void aquaCutterHitsBurningCreaturesHarderAndLeavesThemBurning(GameTestHelper helper) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "aqua-caster"));
        player.moveTo(Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(0, 0, 0))));
        var burning = husk(helper, new BlockPos(1, 0, 2));
        var plain = husk(helper, new BlockPos(3, 0, 2));
        burning.setRemainingFireTicks(200);
        var blade = new Snowball(helper.getLevel(), player);
        AquaCutterProjectileHandler.strike(blade, burning, 10.0F, AttackRules.Options.DEFAULT);
        AquaCutterProjectileHandler.strike(blade, plain, 10.0F, AttackRules.Options.DEFAULT);
        helper.assertTrue(Math.abs(HEALTH - burning.getHealth() - 10.0F * AquaCutterProjectileHandler.BURNING_FACTOR) < 1.0E-3F,
                "Aqua Cutter must deal 25% more to a burning creature: " + (HEALTH - burning.getHealth()));
        helper.assertTrue(burning.isOnFire() && !CombatStatuses.isSoaked(burning),
                "A burning creature must keep burning and stay dry");
        helper.assertTrue(Math.abs(HEALTH - plain.getHealth() - 10.0F) < 1.0E-3F && CombatStatuses.isSoaked(plain),
                "A creature not on fire takes the plain damage and gets wet: " + (HEALTH - plain.getHealth()));
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = BATCH)
    public static void chainLightningHitsSoakedCreaturesHarder(GameTestHelper helper) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "wet-caster"));
        player.moveTo(Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(0, 0, 0))));
        var soaked = husk(helper, new BlockPos(1, 0, 2));
        var dry = husk(helper, new BlockPos(3, 0, 2));
        CombatStatuses.soak(soaked, CombatStatuses.AQUA_CUTTER_WET_TICKS);
        boolean raining = dry.isInWaterOrRain();
        var source = AttackRules.source(helper.getLevel(), PsitweaksDamageTypes.CHAIN_LIGHTNING, player, player);
        EntityChainLightning.strike(helper.getLevel(), player, AttackRules.Options.DEFAULT, soaked, source, 10.0F, 0);
        EntityChainLightning.strike(helper.getLevel(), player, AttackRules.Options.DEFAULT, dry, source, 10.0F, 0);
        helper.assertTrue(Math.abs(HEALTH - soaked.getHealth() - 15.0F) < 1.0E-3F,
                "Chain Lightning must deal 50% more to a soaked creature: " + (HEALTH - soaked.getHealth()));
        helper.assertTrue(raining || Math.abs(HEALTH - dry.getHealth() - 10.0F) < 1.0E-3F,
                "Chain Lightning must deal its plain damage to a dry creature: " + (HEALTH - dry.getHealth()));
        helper.succeed();
    }
}
