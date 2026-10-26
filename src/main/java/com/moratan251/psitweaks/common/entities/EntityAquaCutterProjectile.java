package com.moratan251.psitweaks.common.entities;

import com.moratan251.psitweaks.common.items.PsitweaksItems;
import com.moratan251.psitweaks.common.spells.SpellSafetyUtils;
import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class EntityAquaCutterProjectile extends ThrowableItemProjectile {

    public EntityAquaCutterProjectile(EntityType<? extends EntityAquaCutterProjectile> type, Level level) {
        super(type, level);
    }

    public EntityAquaCutterProjectile(Level level, LivingEntity owner) {
        super(PsitweaksEntities.AQUA_CUTTER_PROJECTILE.get(), owner, level);
    }

    @Override
    protected Item getDefaultItem() {
        return PsitweaksItems.AQUA_CUTTER_PROJECTILE.get();
    }

    /** 術者側（術者・術者のペット・味方）と、プレイヤーに安全のときのプレイヤーはすり抜ける。 */
    @Override
    protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target) && AttackRules.canProjectileHarm(this, target,
                AttackRules.Options.of(getPersistentData().getBoolean(SpellSafetyUtils.NBT_SAFE_TO_PLAYERS)));
    }
}
