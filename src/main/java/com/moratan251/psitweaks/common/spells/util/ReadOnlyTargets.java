package com.moratan251.psitweaks.common.spells.util;

import com.moratan251.psitweaks.common.spells.combat.Parades;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * The target check for pieces that only read an entity (its ID, display name, tags, NBT, effects or items) or only
 * remember it, and leave the entity itself unchanged.
 *
 * <p>Psi's {@code verifyEntity} reports every entity that cannot use a portal as immune: bosses such as the Wither and
 * the Ender Dragon, creatures riding something, sleeping creatures and creatures playing their death animation. That
 * suits tricks that move or rewrite an entity, but Psi's own reading operators (Entity Health, Entity Position and so
 * on) do not call it, and with it a single boss in an Entity List stops a whole list filter. So a read stops only when
 * there is no target, when the target is already gone from the world (picked up, unloaded or removed after dying), or
 * when the target is a Trick: Parade caster hiding from this caster.
 */
public final class ReadOnlyTargets {
    private ReadOnlyTargets() {
    }

    public static void verify(SpellContext context, @Nullable Entity entity) throws SpellRuntimeException {
        if (entity == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_TARGET);
        }
        if (entity.isRemoved()) {
            // The same error verifyEntity gives a removed entity, so spells that relied on it keep failing alike.
            throw new SpellRuntimeException(SpellRuntimeException.IMMUNE_TARGET);
        }
        if (Parades.decoyFacing(entity, context.caster) != null) {
            throw new SpellRuntimeException(Parades.ERROR_DISGUISED);
        }
    }
}
