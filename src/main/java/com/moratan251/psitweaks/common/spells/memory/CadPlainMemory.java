package com.moratan251.psitweaks.common.spells.memory;

import com.moratan251.psitweaks.api.PsitweaksPlainValues;
import com.moratan251.psitweaks.api.value.PlainMemoryValue;
import com.moratan251.psitweaks.api.value.PlainValueType;
import com.moratan251.psitweaks.common.spells.util.StringSpellHelper;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.cad.ICAD;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;

public final class CadPlainMemory {
    private static final String WARNING_STRING_TRUNCATED = "psitweaks.spellwarning.cad_memory_string_truncated";
    private static final String TAG_MEMORY = "psitweaks_plain_memory";
    private static final String TAG_LEGACY_ENTITY_MAP = "psitweaks_entity_map";
    private static final String SLOT_LOCKED_PREFIX = "psi:SlotLocked";

    private CadPlainMemory() {
    }

    public static int internalSlot(Number slotNumber) {
        return slotNumber == null ? -1 : slotNumber.intValue() - 1;
    }

    public static boolean isSlotLocked(SpellContext context, int internalSlot) {
        return context != null && context.customData.containsKey(lockKey(internalSlot));
    }

    public static void markSlotLocked(SpellContext context, int internalSlot) {
        if (context != null) {
            context.customData.put(lockKey(internalSlot), 0);
        }
    }

    public static void store(SpellContext context, int internalSlot, Object value) throws SpellRuntimeException {
        CadAccess cadAccess = cadAccess(context);
        validateSlot(cadAccess.cad(), cadAccess.stack(), internalSlot);

        PlainMemoryValue<?> memoryValue = PsitweaksPlainValues.memoryValue(
                truncateStringForCadMemory(context, internalSlot, value));
        store(cadAccess.stack(), cadAccess.cad(), internalSlot, memoryValue);
    }

    public static Object read(SpellContext context, int internalSlot, PlainValueType<?> requestedType)
            throws SpellRuntimeException {
        CadAccess cadAccess = cadAccess(context);
        validateSlot(cadAccess.cad(), cadAccess.stack(), internalSlot);

        PlainMemoryValue<?> value = storedOrVectorFallback(cadAccess.stack(), cadAccess.cad(), internalSlot);
        return PsitweaksPlainValues.convert(value, requestedType);
    }

    public static Optional<PlainMemoryValue<?>> stored(ItemStack cadStack, int internalSlot) {
        if (cadStack == null || cadStack.isEmpty()) {
            return Optional.empty();
        }

        CompoundTag root = cadStack.getTag();
        if (root == null || !root.contains(TAG_MEMORY, Tag.TAG_COMPOUND)) {
            return Optional.empty();
        }

        CompoundTag memory = root.getCompound(TAG_MEMORY);
        String slotKey = slotKey(internalSlot);
        if (!memory.contains(slotKey, Tag.TAG_COMPOUND)) {
            return Optional.empty();
        }

        return PlainMemoryValue.deserialize(memory.getCompound(slotKey));
    }

    public static void storeVectorFromPsi(ItemStack cadStack, int internalSlot, Vector3 value) {
        if (cadStack == null || cadStack.isEmpty() || value == null) {
            return;
        }
        storeRaw(cadStack, internalSlot, PsitweaksPlainValues.memoryValueUnchecked(PsitweaksPlainValues.VECTOR, value));
    }

    public static Optional<Vector3> storedVectorForPsi(ItemStack cadStack, int internalSlot) throws SpellRuntimeException {
        Optional<PlainMemoryValue<?>> storedValue = stored(cadStack, internalSlot);
        if (storedValue.isEmpty()) {
            return Optional.empty();
        }

        Object value = PsitweaksPlainValues.convert(storedValue.get(), PsitweaksPlainValues.VECTOR);
        return Optional.of((Vector3) value);
    }

    private static void store(ItemStack cadStack, ICAD cad, int internalSlot, PlainMemoryValue<?> value)
            throws SpellRuntimeException {
        storeRaw(cadStack, internalSlot, value);
        if (value.type().id().equals(PsitweaksPlainValues.VECTOR.id())) {
            cad.setStoredVector(cadStack, internalSlot, (Vector3) value.copiedValue());
        }
    }

    private static void storeRaw(ItemStack cadStack, int internalSlot, PlainMemoryValue<?> value) {
        CompoundTag root = cadStack.getOrCreateTag();
        root.remove(TAG_LEGACY_ENTITY_MAP);
        CompoundTag memory = root.getCompound(TAG_MEMORY);
        memory.put(slotKey(internalSlot), value.serialize());
        root.put(TAG_MEMORY, memory);
        cadStack.setTag(root);
    }

    private static PlainMemoryValue<?> storedOrVectorFallback(ItemStack cadStack, ICAD cad, int internalSlot)
            throws SpellRuntimeException {
        Optional<PlainMemoryValue<?>> storedValue = stored(cadStack, internalSlot);
        if (storedValue.isPresent()) {
            return storedValue.get();
        }
        return PsitweaksPlainValues.memoryValueUnchecked(PsitweaksPlainValues.VECTOR,
                cad.getStoredVector(cadStack, internalSlot));
    }

    private static CadAccess cadAccess(SpellContext context) throws SpellRuntimeException {
        ItemStack cadStack = PsiAPI.getPlayerCAD(context.caster);
        if (cadStack != null && cadStack.getItem() instanceof ICAD cad) {
            return new CadAccess(cadStack, cad);
        }
        throw new SpellRuntimeException("psi.spellerror.nocad");
    }

    private static void validateSlot(ICAD cad, ItemStack cadStack, int internalSlot) throws SpellRuntimeException {
        int size = cad.getMemorySize(cadStack);
        if (internalSlot < 0 || internalSlot >= size) {
            throw new SpellRuntimeException("psi.spellerror.memoryoutofbounds");
        }
    }

    private static Object truncateStringForCadMemory(SpellContext context, int internalSlot, Object value) {
        if (!(value instanceof String string)) {
            return value;
        }

        String sanitized = StringSpellHelper.sanitize(string);
        if (sanitized.length() <= StringSpellHelper.MAX_CAD_STORED_STRING_LENGTH) {
            return sanitized;
        }

        String truncated = StringSpellHelper.sanitize(sanitized, StringSpellHelper.MAX_CAD_STORED_STRING_LENGTH);
        notifyStringTruncated(context, internalSlot, sanitized.length(), truncated.length());
        return truncated;
    }

    private static void notifyStringTruncated(
            SpellContext context,
            int internalSlot,
            int originalLength,
            int truncatedLength
    ) {
        if (context != null && context.caster != null) {
            context.caster.sendSystemMessage(Component.translatable(
                    WARNING_STRING_TRUNCATED,
                    internalSlot + 1,
                    originalLength,
                    truncatedLength
            ).withStyle(ChatFormatting.YELLOW));
        }
    }

    private static String slotKey(int internalSlot) {
        return String.valueOf(internalSlot);
    }

    private static String lockKey(int internalSlot) {
        return SLOT_LOCKED_PREFIX + internalSlot;
    }

    private record CadAccess(ItemStack stack, ICAD cad) {
    }
}