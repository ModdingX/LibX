package org.moddingx.libx.util.game;

import net.neoforged.neoforge.transfer.energy.EnergyHandler;

/**
 * An {@link EnergyHandler} that allows storing the energy value as long.
 */
public interface LongEnergyStorage extends EnergyHandler {

    /**
     * Gets the energy stored as a long.
     */
    long getLongEnergyStored();

    /**
     * Gets the maximum energy stored as a long.
     */
    long getLongMaxEnergyStored();

    @Override
    default long getAmountAsLong() {
        return this.getLongEnergyStored();
    }

    @Override
    default long getCapacityAsLong() {
        return this.getLongMaxEnergyStored();
    }
}
