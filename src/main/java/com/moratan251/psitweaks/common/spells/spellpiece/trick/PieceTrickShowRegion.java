package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.caster.AutonomousCasterPlayer;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamMatrix;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.RegionMatrix;
import com.moratan251.psitweaksqol.api.PsitweaksModeConfigurable;
import com.moratan251.psitweaksqol.api.PsitweaksModeOption;
import com.moratan251.psitweaksqol.api.PsitweaksModeOptions;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceTrick;
import vazkii.psi.common.network.PsiNetwork;
import vazkii.psi.common.network.message.MessageParticleTrail;

/**
 * Draws the 12 edges of a region matrix with Psi's particle trail in the CAD colour. Block mode moves the outline by
 * half a block so it wraps the blocks whose positions are in the region, which are exactly the blocks Operator: Region
 * Vector List and the Block mode of Operator: Inside Region pick.
 */
public class PieceTrickShowRegion extends PieceTrick implements PsitweaksModeConfigurable {
    private static final String TAG_MODE = "psitweaksMode";
    private static final List<PsitweaksModeOption> MODES = List.of(PsitweaksModeOptions.BLOCK, PsitweaksModeOptions.VECTOR);
    private static final int POTENCY = 10;
    private static final int DEFAULT_TIME = 20;
    private static final int MAX_TIME = 1200;

    private PsitweaksModeOption mode = PsitweaksModeOptions.BLOCK;
    private SpellParam<MatrixValue> region;
    private SpellParam<Number> time;

    public PieceTrickShowRegion(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(POTENCY));
    }

    @Override
    public void initParams() {
        addParam(region = new ParamMatrix(PsitweaksSpellParams.MATRIX, PsitweaksSpellParams.MATRIX_COLOR, false, false));
        addParam(time = new ParamNumber(SpellParam.GENERIC_NAME_TIME, SpellParam.PURPLE, true, true));
    }

    @Override
    public List<PsitweaksModeOption> getAvailableModeOptions() {
        return MODES;
    }

    @Override
    public PsitweaksModeOption getModeOption() {
        return normalizeModeOption(mode);
    }

    /** The parameters are the same in both modes, so only the mode changes. */
    @Override
    public void setModeOption(PsitweaksModeOption modeOption) {
        mode = normalizeModeOption(modeOption);
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        mode = normalizeModeOption(PsitweaksModeOptions.byId(tag.getString(TAG_MODE)).orElse(null));
        super.readFromNBT(tag);
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putString(TAG_MODE, getModeOption().serializedId());
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        meta.addStat(EnumSpellStat.POTENCY, POTENCY);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        RegionMatrix area = RegionMatrix.parse(getNotNullParamValue(context, region));
        int ticks = Math.min(this.<Number>getParamValueOrDefault(context, time, DEFAULT_TIME).intValue(), MAX_TIME);
        if (ticks <= 0) {
            throw new SpellRuntimeException(SpellRuntimeException.NON_POSITIVE_VALUE);
        }

        List<Vector3[]> edges = outline(area, getModeOption().id().equals(PsitweaksModeOptions.BLOCK.id()));
        for (Vector3[] edge : edges) {
            if (!context.isInRadius(edge[0]) || !context.isInRadius(edge[1])) {
                throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
            }
        }

        ItemStack cad = PsiAPI.getPlayerCAD(context.caster);
        for (Vector3[] edge : edges) {
            Vector3 direction = edge[1].copy().sub(edge[0]);
            // The trail stops a quarter block short of its length, so extend it to reach the far corner.
            send(context.caster, new MessageParticleTrail(edge[0].toVec3D(),
                    direction.toVec3D(), direction.mag() + 0.25, ticks / 6, cad));
        }
        return null;
    }

    /**
     * Sends the outline to the players who see the caster. The autonomous caster's player is not in the world, so
     * nobody tracks it; the players who see its chunk get the outline instead.
     */
    private static void send(Player caster, MessageParticleTrail message) {
        if (caster instanceof AutonomousCasterPlayer && caster.level() instanceof ServerLevel level) {
            PacketDistributor.sendToPlayersTrackingChunk(level, caster.chunkPosition(), message);
        } else {
            PsiNetwork.sendToPlayersTrackingAndSelf(caster, message);
        }
    }

    /** The 12 edges as {from, to} pairs: four along each edge vector of the region. */
    public static List<Vector3[]> outline(RegionMatrix area, boolean blocks) {
        Vector3 shift = blocks ? new Vector3(0.5, 0.5, 0.5) : new Vector3();
        List<Vector3[]> edges = new ArrayList<>(12);
        for (int axis = 0; axis < 3; axis++) {
            for (int corner = 0; corner < 4; corner++) {
                boolean[] from = new boolean[3];
                from[(axis + 1) % 3] = (corner & 1) != 0;
                from[(axis + 2) % 3] = (corner & 2) != 0;
                boolean[] to = from.clone();
                to[axis] = true;
                edges.add(new Vector3[]{
                        area.corner(from[0], from[1], from[2]).add(shift),
                        area.corner(to[0], to[1], to[2]).add(shift)
                });
            }
        }
        return edges;
    }
}
