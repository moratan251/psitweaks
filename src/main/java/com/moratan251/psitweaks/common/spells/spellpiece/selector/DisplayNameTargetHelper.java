package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.api.PsitweaksListAdapter;
import com.moratan251.psitweaks.api.PsitweaksListAdapters;
import com.moratan251.psitweaks.api.value.BlockValue;
import com.moratan251.psitweaks.api.value.BlockValueHelper;
import com.moratan251.psitweaks.api.value.ContextualValue;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.util.StringSpellHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;

public final class DisplayNameTargetHelper {
    private DisplayNameTargetHelper() {
    }

    public static String getDisplayName(SpellContext context, Object target) throws SpellRuntimeException {
        if (target instanceof Entity entity) {
            context.verifyEntity(entity);
            return entityName(entity);
        }
        if (target instanceof SpellItemValue item) {
            return item.isEmpty() ? "" : componentToString(item.copyStack().getHoverName());
        }
        if (target instanceof BlockValue block) {
            return componentToString(block.displayName());
        }
        if (target instanceof Vector3 vector) {
            return componentToString(BlockValueHelper.snapshotVector(context, vector).displayName());
        }
        if (target instanceof ContextualValue) {
            return "";
        }
        return "";
    }

    public static List<String> getDisplayNames(SpellContext context, Object source) throws SpellRuntimeException {
        if (source == null) {
            return List.of();
        }

        PsitweaksListAdapter<Object> adapter = PsitweaksListAdapters.findAdapter(source.getClass()).orElseThrow(
                () -> new SpellRuntimeException(SpellRuntimeException.NULL_TARGET)
        );
        List<String> result = new ArrayList<>();
        int size = adapter.size(source);
        for (int i = 0; i < size; i++) {
            result.add(getDisplayName(context, adapter.get(source, i)));
        }
        return List.copyOf(result);
    }

    private static String entityName(Entity entity) {
        if (entity instanceof Player player) {
            return StringSpellHelper.sanitize(player.getGameProfile().getName());
        }
        if (entity.hasCustomName() && entity.getCustomName() != null) {
            return componentToString(entity.getCustomName());
        }
        return componentToString(entity.getType().getDescription());
    }

    private static String componentToString(Component component) {
        return component == null ? "" : StringSpellHelper.sanitize(component.getString());
    }
}
