package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.neoforged.neoforge.common.util.FakePlayer;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.piece.PieceTrick;

/**
 * 作動式: エンダーチェスト。
 * コスト0・規模0。詠唱者が(機械ではない)プレイヤーの場合のみ本人のエンダーチェストの画面を開く。
 */
public class PieceTrickEnderChest extends PieceTrick {
    private static final Component TITLE = Component.translatable("container.enderchest");

    public PieceTrickEnderChest(Spell spell) {
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
            PlayerEnderChestContainer enderChest = player.getEnderChestInventory();
            // ブロックのエンダーチェストを開いていない状態にする(蓋のアニメーションと距離判定はブロック用)
            enderChest.setActiveChest(null);
            player.openMenu(new SimpleMenuProvider(
                    (containerId, inventory, ignored) -> ChestMenu.threeRows(containerId, inventory, enderChest), TITLE));
            player.awardStat(Stats.OPEN_ENDERCHEST);
        }
        return null;
    }
}
