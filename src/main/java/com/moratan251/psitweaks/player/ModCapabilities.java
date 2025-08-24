package com.moratan251.psitweaks.player;

import com.moratan251.psitweaks.player.interfaces.IFlightData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class ModCapabilities {
    public static final Capability<IFlightData> FLIGHT_DATA =
            CapabilityManager.get(new CapabilityToken<>() {});
}