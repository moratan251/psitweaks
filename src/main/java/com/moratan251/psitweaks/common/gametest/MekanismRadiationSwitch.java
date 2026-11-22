package com.moratan251.psitweaks.common.gametest;

/**
 * GameTest 用: Mekanism の設定「放射線を有効にする」を切り替える。Mekanism のクラスを読み込むので、Mekanism があるときだけ
 * 呼ぶこと。
 */
final class MekanismRadiationSwitch {
    private MekanismRadiationSwitch() {
    }

    static boolean isEnabled() {
        return mekanism.common.config.MekanismConfig.general.radiationEnabled.get();
    }

    static void set(boolean enabled) {
        mekanism.common.config.MekanismConfig.general.radiationEnabled.set(enabled);
    }
}
