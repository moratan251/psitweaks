package com.moratan251.psitweaks.common.entities.SpellGram.zone;

import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.spells.combat.CombatStatuses;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * 作動式: 湿潤領域（仮）の範囲。中の生物を濡らす（作動式: アクアカッターと同じ濡れ）。出てからも
 * {@link #SOAK_TICKS} の間は濡れたまま。
 */
public class EntitySoakZone extends SpellGramZone {
    /** 範囲の中の生物を濡らす長さ（出てから乾くまで、仮）。 */
    public static final int SOAK_TICKS = 60;
    private static final int INTERVAL = 10;
    private static final int MAX_DRIPS_PER_TICK = 6;

    public EntitySoakZone(EntityType<? extends EntitySoakZone> type, Level level) {
        super(type, level);
    }

    public EntitySoakZone(Level level) {
        this(PsitweaksEntities.SOAK_ZONE.get(), level);
    }

    /** 辺の色: 水の青。 */
    public static final int OUTLINE_COLOR = 0x3F8CFF;

    @Override
    public int getOutlineColor() {
        return OUTLINE_COLOR;
    }

    @Override
    protected int effectInterval() {
        return INTERVAL;
    }

    @Override
    protected void affect(LivingEntity target) {
        CombatStatuses.soak(target, SOAK_TICKS);
    }

    @Override
    protected void spawnClientParticles() {
        AABB box = getOutline();
        double volume = box.getXsize() * box.getYsize() * box.getZsize();
        int drips = (int) Math.min(MAX_DRIPS_PER_TICK, Math.max(1, volume / 150.0));
        for (int i = 0; i < drips; i++) {
            double x = box.minX + random.nextDouble() * box.getXsize();
            double y = box.minY + random.nextDouble() * box.getYsize();
            double z = box.minZ + random.nextDouble() * box.getZsize();
            if (getArea().contains(BlockPos.containing(x, y, z))) {
                level().addParticle(ParticleTypes.FALLING_WATER, x, y, z, 0.0, 0.0, 0.0);
            }
        }
    }
}
