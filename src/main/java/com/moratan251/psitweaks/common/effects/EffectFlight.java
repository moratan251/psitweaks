package com.moratan251.psitweaks.common.effects;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.network.MessageFlightPsiCastEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.network.PacketDistributor;
import vazkii.psi.api.cad.ICAD;
import vazkii.psi.common.core.handler.PsiSoundHandler;
import vazkii.psi.common.core.handler.PlayerDataHandler;

public class EffectFlight extends MobEffect {
    public static final ResourceLocation FLIGHT_MODIFIER_ID = Psitweaks.location("effect.flight");

    private static final int PSI_CONSUMPTION_INTERVAL = 10;
    private static final int DEFAULT_PSI_SPELL_COLOR = -15481345;
    private static final float CAST_SOUND_VOLUME = 0.025F;

    public EffectFlight() {
        super(MobEffectCategory.BENEFICIAL, 0x98D982);
        addAttributeModifier(
                NeoForgeMod.CREATIVE_FLIGHT,
                FLIGHT_MODIFIER_ID,
                1.0,
                AttributeModifier.Operation.ADD_VALUE
        );
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!(entity instanceof ServerPlayer player)
                || !player.getAbilities().flying
                || hasOtherFlightSource(player)) {
            return true;
        }

        FlightPsiCostProfile costProfile = FlightPsiCostProfile.load(player);
        int cost = FlightPsiUpkeep.getAdjustedPsiCostForAmplifier(
                amplifier,
                costProfile.cadEfficiency(),
                costProfile.bulletCostModifier()
        );
        if (cost == 0) {
            return true;
        }

        PlayerDataHandler.PlayerData playerData = PlayerDataHandler.get(player);
        long availablePsi = playerData == null ? 0 : getAvailablePsi(playerData);
        if (availablePsi < cost) {
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
            return true;
        }

        playerData.deductPsi(cost, 0, true, true);
        if (getAvailablePsi(playerData) < availablePsi) {
            playPsiCastEffects(player, playerData.getCAD());
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % PSI_CONSUMPTION_INTERVAL == 0;
    }

    @SuppressWarnings("deprecation")
    private static boolean hasOtherFlightSource(ServerPlayer player) {
        if (player.getAbilities().mayfly) {
            return true;
        }

        AttributeInstance flight = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
        if (flight == null) {
            return false;
        }
        if (flight.getBaseValue() > 0.0) {
            return true;
        }

        return flight.getModifiers().stream()
                .filter(modifier -> !modifier.is(FLIGHT_MODIFIER_ID))
                .anyMatch(modifier -> modifier.operation() == AttributeModifier.Operation.ADD_VALUE
                        && modifier.amount() > 0.0);
    }

    private static long getAvailablePsi(PlayerDataHandler.PlayerData playerData) {
        long availablePsi = Math.max(0, playerData.getAvailablePsi());
        ItemStack cad = playerData.getCAD();
        if (!cad.isEmpty() && cad.getItem() instanceof ICAD cadItem) {
            int storedPsi = cadItem.getStoredPsi(cad);
            if (storedPsi == -1) {
                return Long.MAX_VALUE;
            }
            availablePsi += Math.max(0, storedPsi);
        }
        return availablePsi;
    }

    private static void playPsiCastEffects(ServerPlayer player, ItemStack cad) {
        int color = DEFAULT_PSI_SPELL_COLOR;
        if (!cad.isEmpty() && cad.getItem() instanceof ICAD cadItem) {
            color = cadItem.getSpellColor(cad);
        }

        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                PsiSoundHandler.cadShoot,
                SoundSource.PLAYERS,
                CAST_SOUND_VOLUME,
                0.5F + player.getRandom().nextFloat() * 0.5F
        );
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                player,
                new MessageFlightPsiCastEffect(player.getId(), color)
        );
    }
}
