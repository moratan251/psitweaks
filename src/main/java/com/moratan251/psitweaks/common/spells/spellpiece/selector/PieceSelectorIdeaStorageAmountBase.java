package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamString;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageService;
import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
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

abstract class PieceSelectorIdeaStorageAmountBase extends PieceSelector {
    private SpellParam<String> resourceId;

    protected PieceSelectorIdeaStorageAmountBase(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(0));
        setStatLabel(EnumSpellStat.COST, new StatLabel(0));
    }

    @Override
    public void initParams() {
        addParam(resourceId = new ParamString(PsitweaksSpellParams.STRING,
                PsitweaksSpellParams.STRING_COLOR, false, false));
    }

    protected ResourceLocation resourceId(SpellContext context) throws SpellRuntimeException {
        String value = getParamValue(context, resourceId);
        return value == null ? null : ResourceLocation.tryParse(value);
    }

    @Override
    public final Object execute(SpellContext context) throws SpellRuntimeException {
        if (!(context.caster instanceof ServerPlayer player)) {
            return 0.0D;
        }
        ResourceLocation id = resourceId(context);
        if (id == null) {
            return 0.0D;
        }
        var storage = IdeaStorageService.get(player.server, player.getUUID());
        return storage.isLoadFailed() ? 0.0D : storedAmount(storage, id);
    }

    protected abstract double storedAmount(PlayerIdeaStorage storage, ResourceLocation id);

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psi.datatype.number");
    }
}
