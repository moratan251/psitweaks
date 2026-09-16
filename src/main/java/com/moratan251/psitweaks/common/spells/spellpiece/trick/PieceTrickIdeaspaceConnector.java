package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.blocks.PsitweaksBlocks;
import com.moratan251.psitweaks.common.tile.IdeaspaceConnectorBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.cad.EnumCADComponent;
import vazkii.psi.api.cad.ICAD;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;
import vazkii.psi.common.spell.trick.block.PieceTrickConjureBlock;

public class PieceTrickIdeaspaceConnector extends PieceTrick {
    private SpellParam<Vector3> position;
    public PieceTrickIdeaspaceConnector(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(100));
        setStatLabel(EnumSpellStat.COST, new StatLabel(500));
    }

    @Override public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
    }

    @Override public void addToMetadata(SpellMetadata metadata) throws SpellCompilationException {
        super.addToMetadata(metadata);
        metadata.addStat(EnumSpellStat.POTENCY, 100);
        metadata.addStat(EnumSpellStat.COST, 500);
    }

    @Override public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 vector = getParamValue(context, position);
        if (vector == null) throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        if (!Double.isFinite(vector.x) || !Double.isFinite(vector.y) || !Double.isFinite(vector.z))
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        if (!context.isInRadius(vector)) throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        if (!(context.caster instanceof ServerPlayer player) || context.focalPoint == null) return null;
        var level = context.focalPoint.level();
        var pos = vector.toBlockPos();
        if (level.isClientSide || !level.hasChunkAt(pos) || !level.isInWorldBounds(pos)
                || !level.getWorldBorder().isWithinBounds(pos) || !level.mayInteract(player, pos)
                || level.getBlockEntity(pos) != null) return null;
        BlockSnapshot snapshot = BlockSnapshot.create(level.dimension(), level, pos);
        if (!PieceTrickConjureBlock.conjure(level, pos, player, PsitweaksBlocks.IDEASPACE_CONNECTOR.get().defaultBlockState())) return null;
        if (ForgeEventFactory.onBlockPlace(player, snapshot, Direction.UP)) {
            snapshot.restore(true);
            return null;
        }
        if (level.getBlockEntity(pos) instanceof IdeaspaceConnectorBlockEntity connector) {
            ItemStack cad = PsiAPI.getPlayerCAD(player);
            ItemStack colorizer = !cad.isEmpty() && cad.getItem() instanceof ICAD item
                    ? item.getComponentInSlot(cad, EnumCADComponent.DYE) : ItemStack.EMPTY;
            connector.initialize(player.getUUID(), colorizer);
        }
        return null;
    }
}
