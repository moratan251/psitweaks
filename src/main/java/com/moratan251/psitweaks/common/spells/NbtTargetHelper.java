package com.moratan251.psitweaks.common.spells;

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

    static String stringValue(TargetData data, String query) {
        String path = query == null ? "" : query.trim();
        if (path.isEmpty()) {
            return "";
        }

        if (data.omitEntityId() && isRootEntityIdPath(path)) {
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

    private static boolean isRootEntityIdPath(String path) {
        return ID_TAG.equals(path) || ("\"" + ID_TAG + "\"").equals(path);
    }

    record TargetData(CompoundTag tag, boolean omitEntityId) {
    }
}
