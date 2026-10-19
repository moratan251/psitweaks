package com.moratan251.psitweaks.common.spells.combat;

import com.mojang.logging.LogUtils;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

/**
 * Hits dealt with a sword that is not in anyone's hand (Trick: Dancing Blades), made to look like a melee swing as far
 * as other code can tell: the damage of a bare player swinging it (attack damage modifiers and damage enchantments such
 * as Sharpness and Smite), a damage source whose weapon is the sword, the sword's own on-hit behaviour
 * ({@code hurtEnemy} / {@code postHurtEnemy}, where Psi's psimetal sword casts its spell and other mods' swords do
 * their tricks), and its after-hit enchantments (Fire Aspect, Knockback). The sword used is a copy, so nothing wears
 * the real one down. Effects that only look at what the player holds in their hand cannot see it.
 */
public final class SwordStrikes {
    private static final Logger LOGGER = LogUtils.getLogger();
    /** Items whose on-hit effect already failed once: logged in full the first time only. Bounded by the item registry. */
    private static final Set<Item> FAILED_ITEMS = ConcurrentHashMap.newKeySet();
    /** A player's base attack damage, which the sword's modifiers build on. */
    public static final double PLAYER_BASE_ATTACK_DAMAGE = 1.0;

    private SwordStrikes() {
    }

    /**
     * What a player with nothing else boosting their attack would deal to {@code target} with {@code sword}: as in
     * {@code Player.attack}, the damage enchantments and the item's own bonus ({@code getAttackDamageBonus}, as the
     * mace and some modded swords use) both build on the attribute damage.
     */
    public static float meleeDamage(ServerLevel level, ItemStack sword, Entity target, DamageSource source) {
        double[] added = {0.0};
        double[] multipliedBase = {0.0};
        double[] multipliedTotal = {1.0};
        sword.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            if (attribute.value() != Attributes.ATTACK_DAMAGE.value()) {
                return;
            }
            switch (modifier.operation()) {
                case ADD_VALUE -> added[0] += modifier.amount();
                case ADD_MULTIPLIED_BASE -> multipliedBase[0] += modifier.amount();
                case ADD_MULTIPLIED_TOTAL -> multipliedTotal[0] *= 1.0 + modifier.amount();
            }
        });
        double attribute = PLAYER_BASE_ATTACK_DAMAGE + added[0];
        double damage = (attribute + attribute * multipliedBase[0]) * multipliedTotal[0];
        float base = (float) Math.max(0.0, damage);
        float bonus = sword.getItem().getAttackDamageBonus(target, base, source);
        return EnchantmentHelper.modifyDamage(level, sword, target, source, base) + bonus;
    }

    /** A throwaway copy of the sword to strike with: whatever wear the hit causes falls on the copy. */
    public static ItemStack swingCopy(ItemStack sword) {
        ItemStack copy = sword.copyWithCount(1);
        if (copy.isDamageableItem()) {
            copy.setDamageValue(0);
        }
        return copy;
    }

    /** A damage source of {@code type} dealt by {@code attacker} directly, with {@code weapon} as the weapon used. */
    public static DamageSource weaponSource(ServerLevel level, ResourceKey<DamageType> type, LivingEntity attacker, ItemStack weapon) {
        Holder<DamageType> holder = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type);
        return new DamageSource(holder, attacker, attacker) {
            @Override
            public ItemStack getWeaponItem() {
                return weapon;
            }
        };
    }

    /**
     * After a landed hit: the sword's own on-hit behaviour, its post-attack enchantments, and its Knockback pushing
     * along {@code direction}. A sword that throws while hitting is skipped, so one odd item cannot break the spell;
     * the first failure of each item is logged with its stack trace, later ones only at debug level.
     */
    public static void afterHit(ServerLevel level, LivingEntity attacker, ItemStack sword, LivingEntity target, DamageSource source,
                                Vec3 direction) {
        try {
            if (sword.getItem().hurtEnemy(sword, target, attacker)) {
                sword.getItem().postHurtEnemy(sword, target, attacker);
            }
        } catch (RuntimeException e) {
            if (FAILED_ITEMS.add(sword.getItem())) {
                LOGGER.warn("On-hit effect of {} failed when struck by a dancing blade; further failures are logged at debug level",
                        sword.getItem(), e);
            } else {
                LOGGER.debug("On-hit effect of {} failed again when struck by a dancing blade: {}", sword.getItem(), e.toString());
            }
        }
        EnchantmentHelper.doPostAttackEffectsWithItemSource(level, target, source, sword);
        float knockback = EnchantmentHelper.modifyKnockback(level, sword, target, source, 0.0F);
        if (knockback > 0.0F && direction.horizontalDistanceSqr() > 1.0E-6) {
            target.knockback(knockback * 0.5F, -direction.x, -direction.z);
        }
    }
}
