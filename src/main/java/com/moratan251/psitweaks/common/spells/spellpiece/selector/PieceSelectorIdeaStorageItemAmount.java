package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.mode.ModeConfigurableSpellPiece;
import com.moratan251.psitweaks.common.spells.param.ParamSpellItemValue;
import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import com.moratan251.psitweaksqol.api.PsitweaksModeOption;
import com.moratan251.psitweaksqol.api.PsitweaksModeOptions;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

public class PieceSelectorIdeaStorageItemAmount extends PieceSelectorIdeaStorageAmountBase
        implements ModeConfigurableSpellPiece {
    private static final String TAG_MODE = "psitweaksMode";
    private static final List<PsitweaksModeOption> MODES = List.of(PsitweaksModeOptions.STRING, PsitweaksModeOptions.ITEM);
    private PsitweaksModeOption mode = PsitweaksModeOptions.STRING;
    private SpellParam<SpellItemValue> item;

    public PieceSelectorIdeaStorageItemAmount(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        if (getModeOption().id().equals(PsitweaksModeOptions.ITEM.id())) {
            addParam(item = new ParamSpellItemValue(PsitweaksSpellParams.ITEM,
                    PsitweaksSpellParams.ITEM_COLOR, false, false));
        } else {
            super.initParams();
        }
    }

    @Override
    public List<PsitweaksModeOption> getAvailableModeOptions() {
        return MODES;
    }

    @Override
    public PsitweaksModeOption getModeOption() {
        return normalizeModeOption(mode);
    }

    @Override
    public void setModeOption(PsitweaksModeOption option) {
        PsitweaksModeOption next = normalizeModeOption(option);
        if (next.id().equals(getModeOption().id())) {
            return;
        }
        // The sole input keeps its connection even when its name/type changes.
        SpellParam.Side side = paramSides.get(params.values().iterator().next());
        mode = next;
        rebuildParams();
        if (side != null) {
            paramSides.put(params.values().iterator().next(), side);
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
        mode = normalizeModeOption(PsitweaksModeOptions.byId(tag.getString(TAG_MODE)).orElse(null));
        rebuildParams();
        super.readFromNBT(tag);
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putString(TAG_MODE, getModeOption().serializedId());
    }

    @Override
    protected ResourceLocation resourceId(SpellContext context) throws SpellRuntimeException {
        if (!getModeOption().id().equals(PsitweaksModeOptions.ITEM.id())) {
            return super.resourceId(context);
        }
        SpellItemValue value = getParamValue(context, item);
        return value == null || value.isEmpty() ? null : BuiltInRegistries.ITEM.getKey(value.copyStack().getItem());
    }

    @Override
    protected double storedAmount(PlayerIdeaStorage storage, ResourceLocation id) {
        return storage.itemAmountById(id);
    }
}
