package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.storage.idea.IdeaStorageService;
import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.piece.PieceSelector;

/** Input-free ideaspace readings (FE, free FE, type counts); 0 outside a server player cast or for a broken save. */
abstract class PieceSelectorIdeaStorageStatusBase extends PieceSelector {
    protected PieceSelectorIdeaStorageStatusBase(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(0));
        setStatLabel(EnumSpellStat.COST, new StatLabel(0));
    }

    @Override
    public void initParams() {
    }

    @Override
    public final Object execute(SpellContext context) {
        if (!(context.caster instanceof ServerPlayer player)) {
            return 0.0D;
        }
        var storage = IdeaStorageService.get(player.server, player.getUUID());
        return storage.isLoadFailed() ? 0.0D : status(storage);
    }

    protected abstract double status(PlayerIdeaStorage storage);

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psi.datatype.number");
    }
}
