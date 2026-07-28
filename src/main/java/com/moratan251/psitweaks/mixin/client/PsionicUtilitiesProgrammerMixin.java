package com.moratan251.psitweaks.mixin.client;

import com.moratan251.psitweaks.client.gui.PsionicUtilitiesProgrammerExtension;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.client.gui.GuiProgrammer;

@Mixin(value = GuiProgrammer.class, priority = 900, remap = false)
public abstract class PsionicUtilitiesProgrammerMixin
        implements PsionicUtilitiesProgrammerExtension {
    @Unique
    private static final int psitweaks$GRID_SIZE = 9;
    @Unique
    private static final int psitweaks$MAX_UNDO_HISTORY = 25;
    @Unique
    private static final Map<String, Field> psitweaks$PU_FIELDS = new ConcurrentHashMap<>();

    @Shadow
    public Spell spell;
    @Shadow
    @Final
    public Stack<Spell> undoSteps;
    @Shadow
    @Final
    public Stack<Spell> redoSteps;

    @Unique
    private Spell psitweaks$dragUndoSnapshot;
    @Unique
    private CompoundTag psitweaks$dragUndoFingerprint;

    @Override
    public void psitweaks$resetPsionicUtilitiesConnectorStart() {
        psitweaks$setIntField("dragX", -1);
        psitweaks$setIntField("dragY", -1);
        psitweaks$setBooleanField("isMoving", false);
    }

    @Override
    public void psitweaks$beginPsionicUtilitiesDragHistory() {
        psitweaks$dragUndoSnapshot = null;
        psitweaks$dragUndoFingerprint = null;
        int dragX = psitweaks$getIntField("dragX");
        int dragY = psitweaks$getIntField("dragY");
        if (spell == null || !psitweaks$isGridPosition(dragX, dragY)) {
            return;
        }

        psitweaks$dragUndoSnapshot = spell.copy();
        psitweaks$dragUndoFingerprint = psitweaks$serializeSpell(spell);
    }

    @Override
    public void psitweaks$finishPsionicUtilitiesDragHistory() {
        Spell snapshot = psitweaks$dragUndoSnapshot;
        CompoundTag fingerprint = psitweaks$dragUndoFingerprint;
        psitweaks$dragUndoSnapshot = null;
        psitweaks$dragUndoFingerprint = null;
        if (snapshot == null
                || fingerprint == null
                || spell == null
                || fingerprint.equals(psitweaks$serializeSpell(spell))) {
            return;
        }

        redoSteps.clear();
        undoSteps.push(snapshot);
        while (undoSteps.size() > psitweaks$MAX_UNDO_HISTORY) {
            undoSteps.remove(0);
        }
    }

    @Unique
    private int psitweaks$getIntField(String name) {
        try {
            return psitweaks$getPuField(name).getInt(this);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException(
                    "Cannot read Psionic Utilities programmer field " + name,
                    exception
            );
        }
    }

    @Unique
    private void psitweaks$setIntField(String name, int value) {
        try {
            psitweaks$getPuField(name).setInt(this, value);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException(
                    "Cannot write Psionic Utilities programmer field " + name,
                    exception
            );
        }
    }

    @Unique
    private void psitweaks$setBooleanField(String name, boolean value) {
        try {
            psitweaks$getPuField(name).setBoolean(this, value);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException(
                    "Cannot write Psionic Utilities programmer field " + name,
                    exception
            );
        }
    }

    @Unique
    private static Field psitweaks$getPuField(String name) {
        return psitweaks$PU_FIELDS.computeIfAbsent(name, fieldName -> {
            try {
                Field field = GuiProgrammer.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException(
                        "Unsupported Psionic Utilities programmer field " + fieldName,
                        exception
                );
            }
        });
    }

    @Unique
    private static boolean psitweaks$isGridPosition(int x, int y) {
        return x >= 0
                && x < psitweaks$GRID_SIZE
                && y >= 0
                && y < psitweaks$GRID_SIZE;
    }

    @Unique
    private static CompoundTag psitweaks$serializeSpell(Spell spell) {
        CompoundTag tag = new CompoundTag();
        spell.writeToNBT(tag);
        return tag;
    }
}
