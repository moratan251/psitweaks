package com.moratan251.psitweaks.common.gametest;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.common.item.ItemSpellDrive;
import vazkii.psi.common.item.base.ModItems;
import vazkii.psi.common.spell.constant.PieceConstantNumber;

/** 自律詠唱機の GameTest で使う術式弾。GameTest holder ではない。 */
final class AutonomousCasterTestSpells {
    private AutonomousCasterTestSpells() {
    }

    /** 「作動式: デバッグ」で定数を出力するだけの術式（コスト 0）。 */
    static ItemStack debugBullet(double value) {
        Spell spell = new Spell();
        spell.name = "autonomous_caster_debug";
        SpellPiece constant = number(spell, value);
        SpellPiece debug = piece(spell, "trick_debug");
        connect(debug, SpellParam.GENERIC_NAME_TARGET, SpellParam.Side.LEFT);
        place(spell, constant, 3, 4);
        place(spell, debug, 4, 4);
        return bullet(spell);
    }

    /** 術者自身を前方へ瞬間移動させる術式。距離に比例した Psi コストを持つ。 */
    static ItemStack blinkBullet(double distance) {
        Spell spell = new Spell();
        spell.name = "autonomous_caster_blink";
        SpellPiece caster = piece(spell, "selector_caster");
        SpellPiece constant = number(spell, distance);
        SpellPiece blink = piece(spell, "trick_blink");
        connect(blink, SpellParam.GENERIC_NAME_TARGET, SpellParam.Side.LEFT);
        connect(blink, "psi.spellparam.distance", SpellParam.Side.TOP);
        place(spell, caster, 3, 4);
        place(spell, constant, 4, 3);
        place(spell, blink, 4, 4);
        return bullet(spell);
    }

    /** Trick: Spin Chamber で選択スロットを {@code offset} の向きへ1つ動かす術式。 */
    static ItemStack spinChamberBullet(double offset) {
        Spell spell = new Spell();
        spell.name = "autonomous_caster_spin_chamber";
        SpellPiece constant = number(spell, offset);
        SpellPiece spin = piece(spell, "trick_spin_chamber");
        connect(spin, SpellParam.GENERIC_NAME_NUMBER, SpellParam.Side.LEFT);
        place(spell, constant, 3, 4);
        place(spell, spin, 4, 4);
        return bullet(spell);
    }

    /** 「取得子: ループ回数」の値を「作動式: デバッグ」で出力するだけの術式（コスト 0）。 */
    static ItemStack loopIndexBullet() {
        Spell spell = new Spell();
        spell.name = "autonomous_caster_loop_index";
        placeLoopIndexDebug(spell);
        return bullet(spell);
    }

    /** ループ回数を出力し、Trick: Spin Chamber で選択スロットを {@code offset} の向きへ1つ動かす術式。 */
    static ItemStack loopIndexSpinChamberBullet(double offset) {
        Spell spell = new Spell();
        spell.name = "autonomous_caster_loop_index_spin_chamber";
        placeLoopIndexDebug(spell);
        SpellPiece constant = number(spell, offset);
        SpellPiece spin = piece(spell, "trick_spin_chamber");
        connect(spin, SpellParam.GENERIC_NAME_NUMBER, SpellParam.Side.LEFT);
        place(spell, constant, 3, 5);
        place(spell, spin, 4, 5);
        return bullet(spell);
    }

    /** ループ回数を出力し、Trick: Break Loop を 0 で実行する術式。 */
    static ItemStack loopIndexBreakLoopBullet() {
        Spell spell = new Spell();
        spell.name = "autonomous_caster_loop_index_break_loop";
        placeLoopIndexDebug(spell);
        SpellPiece constant = number(spell, 0);
        SpellPiece breakLoop = piece(spell, "trick_break_loop");
        connect(breakLoop, SpellParam.GENERIC_NAME_NUMBER, SpellParam.Side.LEFT);
        place(spell, constant, 3, 5);
        place(spell, breakLoop, 4, 5);
        return bullet(spell);
    }

    /** ループ回数を出力する術式を書き込んだループ型術式弾。 */
    static ItemStack loopcastBullet() {
        Spell spell = new Spell();
        spell.name = "autonomous_caster_loopcast";
        placeLoopIndexDebug(spell);
        ItemStack bullet = new ItemStack(ModItems.loopSpellBullet.get());
        ItemSpellDrive.setSpell(bullet, spell);
        return bullet;
    }

    /** Trick: Russian Roulette で選択スロットをランダムに選び直す術式。 */
    static ItemStack russianRouletteBullet() {
        Spell spell = new Spell();
        spell.name = "autonomous_caster_russian_roulette";
        place(spell, piece(spell, "trick_russian_roulette"), 4, 4);
        return bullet(spell);
    }

    private static void placeLoopIndexDebug(Spell spell) {
        SpellPiece index = piece(spell, "selector_loopcast_index");
        SpellPiece debug = piece(spell, "trick_debug");
        connect(debug, SpellParam.GENERIC_NAME_TARGET, SpellParam.Side.LEFT);
        place(spell, index, 3, 4);
        place(spell, debug, 4, 4);
    }

    private static ItemStack bullet(Spell spell) {
        ItemStack bullet = new ItemStack(ModItems.spellBullet.get());
        ItemSpellDrive.setSpell(bullet, spell);
        return bullet;
    }

    private static SpellPiece number(Spell spell, double value) {
        PieceConstantNumber constant = (PieceConstantNumber) piece(spell, "constant_number");
        constant.valueStr = value == Math.rint(value) ? Integer.toString((int) value) : Double.toString(value);
        return constant;
    }

    private static SpellPiece piece(Spell spell, String id) {
        return PsiAPI.SPELL_PIECE_REGISTRY.get(ResourceLocation.fromNamespaceAndPath("psi", id)).create(spell);
    }

    private static void connect(SpellPiece piece, String param, SpellParam.Side side) {
        SpellParam<?> target = piece.params.get(param);
        if (target == null) {
            throw new IllegalStateException(piece.getRegistryKey() + " has no parameter " + param);
        }
        piece.paramSides.put(target, side);
    }

    private static void place(Spell spell, SpellPiece piece, int x, int y) {
        piece.x = x;
        piece.y = y;
        piece.isInGrid = true;
        spell.grid.gridData[x][y] = piece;
    }
}
