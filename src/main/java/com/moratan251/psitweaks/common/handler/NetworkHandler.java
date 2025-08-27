package com.moratan251.psitweaks.common.handler;

import com.moratan251.psitweaks.client.guis.MessageFlashRingSync;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    public static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath("psitweaks", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void registerMessages() {
        int id = 0;
        CHANNEL.registerMessage(
                id++,
                MessageFlashRingSync.class,
                MessageFlashRingSync::encode,
                MessageFlashRingSync::decode,
                MessageFlashRingSync::handle
        );
    }
}