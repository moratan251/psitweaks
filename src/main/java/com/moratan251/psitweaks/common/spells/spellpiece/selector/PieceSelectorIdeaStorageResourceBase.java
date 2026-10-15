package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.mode.BlockResourceModes;
import com.moratan251.psitweaks.common.spells.mode.IdeaStorageResourceModes;
import com.moratan251.psitweaks.common.spells.mode.ModeConfigurableSpellPiece;
import com.moratan251.psitweaks.common.spells.param.ParamString;
import com.moratan251.psitweaks.common.spells.param.ParamStringOrItem;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageService;
import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import com.moratan251.psitweaksqol.api.PsitweaksModeOption;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.piece.PieceSelector;

/**
 * Ideaspace readings whose mode picks the resource kind. Item mode takes an ID String or an Item, fluids and chemicals
 * an ID String, and FE nothing; the one input keeps its wire across modes. Every unusable input, a cast without a
 * server player and a broken save read 0.
 */
public abstract class PieceSelectorIdeaStorageResourceBase extends PieceSelector implements ModeConfigurableSpellPiece {
    public static final String TAG_MODE = "psitweaksMode";
    private PsitweaksModeOption mode = BlockResourceModes.ITEM;
    private SpellParam<?> resource;

    protected PieceSelectorIdeaStorageResourceBase(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(0));
        setStatLabel(EnumSpellStat.COST, new StatLabel(0));
    }

    /** False for readings that never take a resource, such as type counts. */
    protected boolean takesResource() {
        return true;
    }

    @Override
    public List<PsitweaksModeOption> getAvailableModeOptions() {
        return BlockResourceModes.ALL;
    }

    @Override
    public PsitweaksModeOption getModeOption() {
        return normalizeModeOption(mode);
    }

    @Override
    public void setModeOption(PsitweaksModeOption option) {
        PsitweaksModeOption next = normalizeModeOption(option);
        if (next == getModeOption()) {
            return;
        }
        SpellParam.Side side = resource == null ? null : paramSides.get(resource);
        mode = next;
        rebuildParams();
        if (side != null && resource != null) {
            paramSides.put(resource, side);
        }
    }

    @Override
    public void initParams() {
        // Runs during construction too, before mode is assigned.
        PsitweaksModeOption kind = mode == null ? BlockResourceModes.ITEM : getModeOption();
        resource = null;
        if (!takesResource() || kind == BlockResourceModes.ENERGY) {
            return;
        }
        if (kind == BlockResourceModes.ITEM) {
            addParam(resource = new ParamStringOrItem(PsitweaksSpellParams.RESOURCE, PsitweaksSpellParams.ITEM_COLOR,
                    false, false));
        } else {
            addParam(resource = new ParamString(PsitweaksSpellParams.RESOURCE, PsitweaksSpellParams.STRING_COLOR,
                    false, false));
        }
    }

    private void rebuildParams() {
        params.clear();
        paramSides.clear();
        initParams();
    }

    @net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)
    @Override
    public void drawAdditional(com.mojang.blaze3d.vertex.PoseStack pose, net.minecraft.client.renderer.MultiBufferSource buffers, int light) {
        com.moratan251.psitweaks.client.spells.ModeOverlayRenderer.drawModeOverlay(pose, buffers, light, getModeOption());
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        mode = IdeaStorageResourceModes.bySerializedId(getAvailableModeOptions(), tag.getString(TAG_MODE));
        rebuildParams();
        super.readFromNBT(tag);
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putString(TAG_MODE, getModeOption().serializedId());
    }

    @Override
    public final Object execute(SpellContext context) throws SpellRuntimeException {
        if (!(context.caster instanceof ServerPlayer player)) {
            return 0.0D;
        }
        // Read the input before the storage is touched.
        Object input = resource == null ? null : getParamValue(context, (SpellParam<?>) resource);
        if (resource != null && input == null) {
            return 0.0D;
        }
        var storage = IdeaStorageService.get(player.server, player.getUUID());
        return storage.isLoadFailed() ? 0.0D : read(storage, getModeOption(), input);
    }

    /** The reading for a resource kind; {@code input} is null only for kinds without an input. */
    protected abstract double read(PlayerIdeaStorage storage, PsitweaksModeOption kind, Object input);

    /** The ID an input names: an ID String, or the item of a non-empty Item. Null when it names nothing valid. */
    protected static ResourceLocation idOf(Object input) {
        if (input instanceof String id) {
            return ResourceLocation.tryParse(id);
        }
        if (input instanceof SpellItemValue item && !item.isEmpty()) {
            return BuiltInRegistries.ITEM.getKey(item.snapshot().getItem());
        }
        return null;
    }

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psi.datatype.number");
    }
}
