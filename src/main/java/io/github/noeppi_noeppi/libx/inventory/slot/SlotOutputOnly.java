package io.github.noeppi_noeppi.libx.inventory.slot;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

/**
 * A {@link SlotItemHandler slot} that no items can be inserted in.
 */
public class SlotOutputOnly extends SlotItemHandler {

    public SlotOutputOnly(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        return false;
    }
}
