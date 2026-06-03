package com.moratan251.psitweaks.common.spells.operator;

import com.moratan251.psitweaks.api.value.ContextualValue;
import com.moratan251.psitweaks.common.spells.wrapper.StringListWrapper;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;

final class TagTargetHelper {
    private TagTargetHelper() {
    }

    static StringListWrapper getTagList(SpellContext context, Object target) throws SpellRuntimeException {
        List<String> tags = getTagIds(context, target).stream()
                .filter(id -> id != null)
                .sorted(Comparator.comparing(ResourceLocation::toString))
                .map(ResourceLocation::toString)
                .toList();
        return StringListWrapper.make(tags);
    }

    private static Collection<ResourceLocation> getTagIds(SpellContext context, Object target)
            throws SpellRuntimeException {
        if (target instanceof Entity entity) {
            context.verifyEntity(entity);
            return entity.getType().builtInRegistryHolder().tags()
                    .map(TagKey::location)
                    .toList();
        }

        if (target instanceof ContextualValue contextualValue) {
            return contextualValue.getTagIds(context);
        }

        return List.of();
    }
}
