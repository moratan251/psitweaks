package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.caster.SpamlessChatBridge;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import vazkii.psi.common.network.message.MessageSpamlessChat;

/** MessageSpamlessChat の非公開メッセージと置換キーを自律詠唱機の作動ログから読めるようにする。 */
@Mixin(value = MessageSpamlessChat.class, remap = false)
public abstract class MessageSpamlessChatMixin implements SpamlessChatBridge {
    @Shadow
    @Final
    private Component message;

    @Shadow
    @Final
    private int magic;

    @Override
    public Component psitweaks$message() {
        return message;
    }

    @Override
    public int psitweaks$magic() {
        return magic;
    }
}
