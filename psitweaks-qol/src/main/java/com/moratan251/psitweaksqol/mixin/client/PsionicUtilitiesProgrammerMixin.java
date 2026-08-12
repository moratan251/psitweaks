package com.moratan251.psitweaksqol.mixin.client;

import com.moratan251.psitweaksqol.client.gui.PsionicUtilitiesProgrammerExtension;
import java.util.Stack;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.client.gui.GuiProgrammer;

@Mixin(value = GuiProgrammer.class, priority = 1100, remap = false)
public abstract class PsionicUtilitiesProgrammerMixin implements PsionicUtilitiesProgrammerExtension {
    @Unique
    private static final int psitweaks$GRID_SIZE = 9;
    @Unique
    private static final int psitweaks$MAX_UNDO_HISTORY = 25;

    @Shadow
    private int dragX;
    @Shadow
    private int dragY;
    @Shadow
    private boolean isMoving;
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
        dragX = -1;
        dragY = -1;
        isMoving = false;
    }

    @Override
    public void psitweaks$beginPsionicUtilitiesDragHistory() {
        psitweaks$dragUndoSnapshot = null;
        psitweaks$dragUndoFingerprint = null;
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
        if (snapshot == null || fingerprint == null || spell == null
                || fingerprint.equals(psitweaks$serializeSpell(spell))) {
            return;
        }

        redoSteps.clear();
        undoSteps.push(snapshot);
        while (undoSteps.size() > psitweaks$MAX_UNDO_HISTORY) {
            undoSteps.removeFirst();
        }
    }

    @Unique
    private static boolean psitweaks$isGridPosition(int x, int y) {
        return x >= 0 && x < psitweaks$GRID_SIZE && y >= 0 && y < psitweaks$GRID_SIZE;
    }

    @Unique
    private static CompoundTag psitweaks$serializeSpell(Spell spell) {
        CompoundTag tag = new CompoundTag();
        spell.writeToNBT(tag);
        return tag;
    }
}
