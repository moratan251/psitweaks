package com.moratan251.psitweaks.common.compat;

import com.moratan251.psitweaks.client.compat.ParadeJadeDisguise;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Jade の任意連携。Jade が入っているときだけ Jade が見つけて読み込む（Jade の API だけを使い、mixin は使わない）。
 * クライアントの処理は {@link ParadeJadeDisguise} に分け、専用サーバーではクライアントのクラスを読まない。
 */
@WailaPlugin
public class PsitweaksJadePlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        ParadeJadeDisguise.register(registration);
    }
}
