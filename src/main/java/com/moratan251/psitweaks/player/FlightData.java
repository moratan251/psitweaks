package com.moratan251.psitweaks.player;

import com.moratan251.psitweaks.player.interfaces.IFlightData;


public class FlightData implements IFlightData {
    private boolean hasFlightArmor = false;

    @Override
    public boolean hasFlightArmor() {
        return hasFlightArmor;
    }

    @Override
    public void setHasFlightArmor(boolean value) {
        this.hasFlightArmor = value;
    }
}
