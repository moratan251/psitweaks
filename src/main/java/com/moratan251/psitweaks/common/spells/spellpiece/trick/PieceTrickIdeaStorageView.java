package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.menu.IdeaStorageMenu;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageService;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.piece.PieceTrick;

/**
 * 作動式: イデアストレージ閲覧。
 * コスト0・規模0。詠唱者がプレイヤーの場合のみ本人のイデアストレージ GUI を開く。
 */
public class PieceTrickIdeaStorageView extends PieceTrick {
    public PieceTrickIdeaStorageView(Spell spell) {
        super(spell);
        this.setStatLabel(EnumSpellStat.COMPLEXITY, new StatLabel(1.0D));
    }

    @Override
    public void initParams() {
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        meta.addStat(EnumSpellStat.COMPLEXITY, 1);
    }

    @Override
    public Object execute(SpellContext context) {
        if (context.caster instanceof ServerPlayer serverPlayer) {
            UUID owner = serverPlayer.getUUID();
            int rows = IdeaStorageService.get(serverPlayer.server, owner).getGridRows();
            IdeaStorageMenu.open(serverPlayer, owner, rows);
        }
        return null;
    }
}
