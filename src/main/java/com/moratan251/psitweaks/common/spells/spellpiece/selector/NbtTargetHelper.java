package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.moratan251.psitweaks.api.value.ContextualValue;
import com.moratan251.psitweaks.common.spells.util.StringSpellHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;

final class NbtTargetHelper {
    private static final TargetData EMPTY = new TargetData(new CompoundTag());

    private NbtTargetHelper() {
    }

    static Optional<TargetData> getNbt(SpellContext context, Object target) throws SpellRuntimeException {
        if (target instanceof Entity entity) {
            context.verifyEntity(entity);
            return Optional.of(new TargetData(NbtPredicate.getEntityTagToCompare(entity)));
        }

        if (target instanceof ContextualValue contextualValue) {
            return contextualValue.getNbt(context).map(TargetData::new);
        }

        return Optional.empty();
    }

    static TargetData empty() {
        return EMPTY;
    }

    static List<String> sortedKeys(TargetData data) {
        List<String> keys = new ArrayList<>();
        for (String key : data.tag().getAllKeys()) {
            keys.add(key);
        }
        Collections.sort(keys);
        return keys;
    }

    static String stringValue(TargetData data, String query) {
        String path = query == null ? "" : query.trim();
        if (path.isEmpty()) {
            return "";
        }

        Tag directValue = data.tag().get(path);
        if (directValue != null) {
            return stringify(directValue);
        }

        try {
            return stringify(NbtPathArgument.NbtPath.of(path).get(data.tag()));
        } catch (CommandSyntaxException ignored) {
            return "";
        }
    }

    private static String stringify(List<Tag> values) {
        if (values.isEmpty()) {
            return "";
        }

        if (values.size() == 1) {
            return stringify(values.get(0));
        }

        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(values.get(i));
        }
        builder.append(']');
        return StringSpellHelper.sanitize(builder.toString());
    }

    private static String stringify(Tag value) {
        return StringSpellHelper.sanitize(value.toString());
    }

    record TargetData(CompoundTag tag) {
    }
}
