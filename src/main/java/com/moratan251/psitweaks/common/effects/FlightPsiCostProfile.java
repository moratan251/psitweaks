package com.moratan251.psitweaks.common.effects;

import com.moratan251.psitweaks.Psitweaks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import vazkii.psi.api.cad.EnumCADStat;
import vazkii.psi.api.cad.ICAD;
import vazkii.psi.api.spell.ISpellAcceptor;
import vazkii.psi.api.spell.SpellContext;

public record FlightPsiCostProfile(int cadEfficiency, double bulletCostModifier) {
    private static final int DEFAULT_CAD_EFFICIENCY = 100;
    private static final double DEFAULT_BULLET_COST_MODIFIER = 1.0;

    public static final String CONTEXT_KEY = Psitweaks.MOD_ID + ":flight_psi_cost_profile";
    public static final FlightPsiCostProfile DEFAULT = new FlightPsiCostProfile(
            DEFAULT_CAD_EFFICIENCY,
            DEFAULT_BULLET_COST_MODIFIER
    );

    private static final String PERSISTENT_DATA_KEY = Psitweaks.MOD_ID + ":flight_psi_cost_profile";
    private static final String CAD_EFFICIENCY_KEY = "cad_efficiency";
    private static final String BULLET_COST_MODIFIER_KEY = "bullet_cost_modifier";

    public FlightPsiCostProfile {
        if (cadEfficiency <= 0) {
            cadEfficiency = DEFAULT_CAD_EFFICIENCY;
        }
        if (!Double.isFinite(bulletCostModifier) || bulletCostModifier < 0.0) {
            bulletCostModifier = DEFAULT_BULLET_COST_MODIFIER;
        }
    }

    public static FlightPsiCostProfile capture(ItemStack cad, ItemStack bullet) {
        int efficiency = DEFAULT_CAD_EFFICIENCY;
        if (!cad.isEmpty() && cad.getItem() instanceof ICAD cadItem) {
            efficiency = cadItem.getStatValue(cad, EnumCADStat.EFFICIENCY);
        }

        double costModifier = DEFAULT_BULLET_COST_MODIFIER;
        if (!bullet.isEmpty() && ISpellAcceptor.isContainer(bullet)) {
            costModifier = ISpellAcceptor.acceptor(bullet).getCostModifier();
        }
        return new FlightPsiCostProfile(efficiency, costModifier);
    }

    public static FlightPsiCostProfile fromContext(SpellContext context) {
        if (context != null
                && context.customData.get(CONTEXT_KEY) instanceof FlightPsiCostProfile profile) {
            return profile;
        }
        return DEFAULT;
    }

    public static FlightPsiCostProfile load(LivingEntity entity) {
        CompoundTag persistentData = entity.getPersistentData();
        if (!persistentData.contains(PERSISTENT_DATA_KEY, Tag.TAG_COMPOUND)) {
            return DEFAULT;
        }

        CompoundTag profileTag = persistentData.getCompound(PERSISTENT_DATA_KEY);
        int efficiency = profileTag.contains(CAD_EFFICIENCY_KEY, Tag.TAG_ANY_NUMERIC)
                ? profileTag.getInt(CAD_EFFICIENCY_KEY)
                : DEFAULT_CAD_EFFICIENCY;
        double costModifier = profileTag.contains(BULLET_COST_MODIFIER_KEY, Tag.TAG_ANY_NUMERIC)
                ? profileTag.getDouble(BULLET_COST_MODIFIER_KEY)
                : DEFAULT_BULLET_COST_MODIFIER;
        return new FlightPsiCostProfile(
                efficiency,
                costModifier
        );
    }

    public void save(LivingEntity entity) {
        CompoundTag profileTag = new CompoundTag();
        profileTag.putInt(CAD_EFFICIENCY_KEY, cadEfficiency);
        profileTag.putDouble(BULLET_COST_MODIFIER_KEY, bulletCostModifier);
        entity.getPersistentData().put(PERSISTENT_DATA_KEY, profileTag);
    }

    public static void clear(LivingEntity entity) {
        entity.getPersistentData().remove(PERSISTENT_DATA_KEY);
    }
}
