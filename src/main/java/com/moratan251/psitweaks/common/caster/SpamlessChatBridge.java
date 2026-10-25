package com.moratan251.psitweaks.common.caster;

import net.minecraft.network.chat.Component;

/** Psi の MessageSpamlessChat が持つ非公開フィールドを mixin 経由で読むための bridge。 */
public interface SpamlessChatBridge {
    Component psitweaks$message();

    int psitweaks$magic();
}
