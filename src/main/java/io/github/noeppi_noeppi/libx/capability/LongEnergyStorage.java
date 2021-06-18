package io.github.noeppi_noeppi.libx.capability;

import io.github.noeppi_noeppi.libx.util.LongAmountToIntUtil;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * An {@link IEnergyStorage} that allows storing the energy value as long. 
 */
public interface LongEnergyStorage extends IEnergyStorage {

    /**
     * Gets the long energy stored best represented as an {@code int}.
     * This uses {@link LongAmountToIntUtil}.
     */
    @Override
    default int getEnergyStored() {
        return LongAmountToIntUtil.getValue(this.getLongEnergyStored(), this.getLongMaxEnergyStored());
    }
    
    /**
     * Gets the long maximum energy stored best represented as an {@code int}.
     * This uses {@link LongAmountToIntUtil}.
     */
    @Override
    default int getMaxEnergyStored() {
        return LongAmountToIntUtil.getMaxValue(this.getLongMaxEnergyStored());
    }

    /**
     * Gets the energy stored as a long.
     */
    long getLongEnergyStored();
    
    /**
     * Gets the maximum energy stored as a long.
     */
    long getLongMaxEnergyStored();
}
