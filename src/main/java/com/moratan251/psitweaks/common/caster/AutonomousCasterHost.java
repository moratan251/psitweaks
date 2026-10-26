package com.moratan251.psitweaks.common.caster;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.chat.Component;

/**
 * 自律詠唱機の専用 Caster が機械側へ問い合わせ・通知する窓口。
 * Mekanism 型を含めないため、common 側から参照できる。
 */
public interface AutonomousCasterHost {
    /** Caster 宛てのチャット相当メッセージ（デバッグ出力、詠唱エラーなど）。 */
    void onCasterMessage(Component message);

    /** 「作動式: デバッグ（スパム抑制）」の出力。同じ置換キーの過去出力を置き換える。 */
    void onSpamlessMessage(Component message, int replacementKey);

    /** 詠唱コストの支払いに使える内部 Psi。 */
    int availablePsi();

    /** 確定した詠唱コストが内部 Psi を超えたため、詠唱を取り消したとき。 */
    void onInsufficientPsi(int cost, int available);

    /** 詠唱時点の機械所有者。未設定や機械の撤去後は空。 */
    Optional<UUID> ideaStorageOwner();

    /** 機械所有者の言語コード。オンラインならその設定、オフラインなら最後に見えた値。一度も見ていなければ空。 */
    Optional<String> ownerLanguage();

    /** 機械所有者のプレイヤー名（スコアボードのチームを引く名前）。オンラインなら今の名前、オフラインなら最後に見えた名前。不明なら空。 */
    Optional<String> ownerName();
}
