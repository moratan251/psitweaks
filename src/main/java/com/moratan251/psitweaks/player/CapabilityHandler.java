package com.moratan251.psitweaks.player;

import com.moratan251.psitweaks.player.interfaces.IFlightData;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import net.minecraft.world.entity.Entity;


@Mod.EventBusSubscriber(modid = "psitweaks")
public class CapabilityHandler {

    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(ResourceLocation.fromNamespaceAndPath("psitweaks", "flight_data"),
                    new ICapabilityProvider() {
                        private final IFlightData inst = new FlightData();
                        @Override
                        public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
                            return cap == ModCapabilities.FLIGHT_DATA ? LazyOptional.of(() -> inst).cast() : LazyOptional.empty();
                        }
                    });
        }
    }
}