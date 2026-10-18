package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.menu.SpellCraftingMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.neoforged.neoforge.common.util.FakePlayer;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.piece.PieceTrick;

/**
 * 作動式: 作業台。
 * コスト0・規模0。詠唱者が(機械ではない)プレイヤーの場合のみ3×3の作業台の画面を開く。閉じると作業欄のアイテムは術者に戻る。
 */
public class PieceTrickCraftingTable extends PieceTrick {
    private static final Component TITLE = Component.translatable("container.crafting");

    public PieceTrickCraftingTable(Spell spell) {
        super(spell);
        // 画面を開くだけで世界に作用しないため、Psi の Debug などと同じく投射を数えない
        setStatLabel(EnumSpellStat.PROJECTION, null);
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
        // 機械が詠唱するときの偽プレイヤーには画面を表示するクライアントがない
        if (context.caster instanceof ServerPlayer player && !(player instanceof FakePlayer)) {
            ContainerLevelAccess access = ContainerLevelAccess.create(player.level(), player.blockPosition());
            player.openMenu(new SimpleMenuProvider(
                    (containerId, inventory, ignored) -> new SpellCraftingMenu(containerId, inventory, access), TITLE));
            player.awardStat(Stats.INTERACT_WITH_CRAFTING_TABLE);
        }
        return null;
    }
}
