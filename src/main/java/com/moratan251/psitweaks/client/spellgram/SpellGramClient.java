package com.moratan251.psitweaks.client.spellgram;

import com.mojang.blaze3d.platform.InputConstants;
import com.moratan251.psitweaks.common.network.MessageRecallSpellGrams;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

/** 領域型の魔法式オブジェクト（試作）のクライアント側: 回収キー（初期状態では割り当てない）と、確保した Psi の表示。 */
public final class SpellGramClient {
    public static final String KEY_CATEGORY = "key.categories.psitweaks";
    public static final KeyMapping RECALL_KEY = new KeyMapping("key.psitweaks.recall_spellgrams",
            KeyConflictContext.IN_GAME, InputConstants.UNKNOWN, KEY_CATEGORY);

    private SpellGramClient() {
    }

    public static void registerEvents(IEventBus modEventBus) {
        modEventBus.addListener(SpellGramClient::onRegisterKeyMappings);
        NeoForge.EVENT_BUS.addListener(SpellGramClient::onClientTick);
        SpellGramPsiLockHud.registerEvents();
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(RECALL_KEY);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().player == null) {
            return;
        }
        boolean pressed = false;
        while (RECALL_KEY.consumeClick()) {
            pressed = true;
        }
        if (pressed) {
            PacketDistributor.sendToServer(new MessageRecallSpellGrams());
        }
    }
}
