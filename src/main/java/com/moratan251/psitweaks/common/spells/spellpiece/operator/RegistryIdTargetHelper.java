package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.value.BlockValue;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.util.StringSpellHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;

final class RegistryIdTargetHelper {
    private RegistryIdTargetHelper() {
    }

    static String getRegistryId(SpellContext context, Object target) throws SpellRuntimeException {
        if (target instanceof Entity entity) {
            context.verifyEntity(entity);
            return StringSpellHelper.clamp(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());
        }
        if (target instanceof SpellItemValue item) {
            if (item.isEmpty()) {
                return "";
            }
            return StringSpellHelper.clamp(BuiltInRegistries.ITEM.getKey(item.copyStack().getItem()).toString());
        }
        if (target instanceof BlockValue block) {
            return StringSpellHelper.clamp(block.blockId().toString());
        }
        return "";
    }
}
