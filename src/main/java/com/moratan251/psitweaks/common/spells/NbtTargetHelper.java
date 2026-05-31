package com.moratan251.psitweaks.common.spells;

import com.moratan251.psitweaks.api.value.ContextualValue;
import com.moratan251.psitweaks.common.spells.util.StringSpellHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;

final class NbtTargetHelper {
    private static final String ID_TAG = "id";
    private static final TargetData EMPTY = new TargetData(new CompoundTag(), false);

    private NbtTargetHelper() {
    }

    static Optional<TargetData> getNbt(SpellContext context, Object target) throws SpellRuntimeException {
        if (target instanceof Entity entity) {
            context.verifyEntity(entity);
            return Optional.of(new TargetData(NbtPredicate.getEntityTagToCompare(entity), true));
        }

        if (target instanceof ContextualValue contextualValue) {
            return contextualValue.getNbt(context).map(tag -> new TargetData(tag, false));
        }

        return Optional.empty();
    }

    static TargetData empty() {
        return EMPTY;
    }

    static List<String> sortedKeys(TargetData data) {
        List<String> keys = new ArrayList<>();
        for (String key : data.tag().getAllKeys()) {
            if (!data.omitEntityId() || !ID_TAG.equals(key)) {
                keys.add(key);
            }
        }
        Collections.sort(keys);
        return keys;
    }

    static String stringValue(TargetData data, String key) {
        if (data.omitEntityId() && ID_TAG.equals(key)) {
            return "";
        }

        Tag value = data.tag().get(key);
        return value == null ? "" : StringSpellHelper.sanitize(value.toString());
    }

    record TargetData(CompoundTag tag, boolean omitEntityId) {
    }
}
