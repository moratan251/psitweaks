package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.spells.util.IdeaStorageLastTransfer;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageEnergyTransfer;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageOwnerResolver;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageService;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.capabilities.Capabilities;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;

public abstract class PieceTrickIdeaStorageFEBase extends PieceTrick {
    protected SpellParam<Vector3> position;
    protected SpellParam<Number> power;
    protected SpellParam<Vector3> direction;

    protected PieceTrickIdeaStorageFEBase(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(50));
        setStatLabel(EnumSpellStat.COST, new StatLabel(100));
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector("psi.spellparam.position", SpellParam.BLUE, false, false));
        addParam(power = new ParamNumber("psi.spellparam.power", SpellParam.RED, false, false));
        addParam(direction = new ParamVector("psi.spellparam.direction", SpellParam.GREEN, false, false));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        meta.addStat(EnumSpellStat.POTENCY, 50);
        meta.addStat(EnumSpellStat.COST, 100);
    }

    protected abstract boolean absorbs();

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        IdeaStorageLastTransfer.record(context, 0);
        Vector3 pos = getNotNullParamValue(context, position);
        Vector3 face = getNotNullParamValue(context, direction);
        double strength = this.<Number>getNotNullParamValue(context, power).doubleValue();
        if (!Double.isFinite(strength) || strength <= 0) {
            throw new SpellRuntimeException("psi.spellerror.nonpositivevalue");
        }
        if (!Double.isFinite(pos.x) || !Double.isFinite(pos.y) || !Double.isFinite(pos.z)
                || !Double.isFinite(face.x) || !Double.isFinite(face.y) || !Double.isFinite(face.z)) {
            throw new SpellRuntimeException("psi.spellerror.nullvector");
        }
        if (!context.isInRadius(pos)) {
            throw new SpellRuntimeException("psi.spellerror.outsideradius");
        }
        if (!(context.caster instanceof ServerPlayer player)) return null;
        var owner = IdeaStorageOwnerResolver.requireOwner(player);
        var level = player.serverLevel();
        var blockPos = pos.toBlockPos();
        if (!level.hasChunkAt(blockPos) || !level.mayInteract(player, blockPos)) return null;
        var handler = level.getCapability(Capabilities.EnergyStorage.BLOCK, blockPos,
                Direction.getNearest(face.x, face.y, face.z));
        if (handler == null) return null;
        var storage = IdeaStorageService.get(player.server, owner);
        int amount = IdeaStorageEnergyTransfer.amountForPower(strength);
        int moved = absorbs()
                ? IdeaStorageEnergyTransfer.absorb(storage, handler, amount)
                : IdeaStorageEnergyTransfer.supply(storage, handler, amount);
        IdeaStorageLastTransfer.record(context, moved);
        return null;
    }
}
