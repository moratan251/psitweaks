package com.moratan251.psitweaks.client;

import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntitySilenceZone;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * 遮音領域（{@link EntitySilenceZone}）の中で鳴る音を鳴らさない（クライアント側）。サーバーから届いた音も、クライアントだけで
 * 鳴る音（足音、ブロックの破壊、ウォーデンの鼓動など）も、鳴らす直前に NeoForge の {@link PlaySoundEvent} を通るので、
 * 音の位置が遮音領域の中なら取り消す。音楽や画面の音（位置を持たない音）と、どこにいても聞こえる音（減衰しない音）は
 * そのまま。鳴り始めたあとで範囲に入った音（動くトロッコなど）は止めない。
 */
public final class SilenceZoneSounds {
    private SilenceZoneSounds() {
    }

    public static void registerEvents() {
        NeoForge.EVENT_BUS.addListener(SilenceZoneSounds::onPlaySound);
    }

    private static void onPlaySound(PlaySoundEvent event) {
        SoundInstance sound = event.getSound();
        if (sound == null || sound.isRelative() || sound.getAttenuation() == SoundInstance.Attenuation.NONE) {
            return;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null && EntitySilenceZone.silences(level, new Vec3(sound.getX(), sound.getY(), sound.getZ()))) {
            event.setSound(null);
        }
    }
}
