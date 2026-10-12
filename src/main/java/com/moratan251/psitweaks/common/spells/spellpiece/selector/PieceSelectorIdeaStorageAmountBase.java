package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamString;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageOwnerResolver;
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

/** Ideaspace queries about one resource given by ID; amounts and free capacity share the input and the 0 fallbacks. */
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
        StorageQuery query = prepareQuery(context);
        if (query == null) {
            return 0.0D;
        }
        var owner = IdeaStorageOwnerResolver.resolve(player);
        if (owner.isEmpty()) {
            return 0.0D;
        }
        var storage = IdeaStorageService.get(player.server, owner.get());
        return storage.isLoadFailed() ? 0.0D : query.apply(storage);
    }

    /** Reads the inputs before the storage is touched; null (no usable resource) evaluates to 0. */
    protected StorageQuery prepareQuery(SpellContext context) throws SpellRuntimeException {
        ResourceLocation id = resourceId(context);
        return id == null ? null : storage -> storedAmount(storage, id);
    }

    /** The value for a resource ID: a stored amount, or a free capacity for the capacity selectors. */
    protected abstract double storedAmount(PlayerIdeaStorage storage, ResourceLocation id);

    @FunctionalInterface
    protected interface StorageQuery {
        double apply(PlayerIdeaStorage storage);
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
