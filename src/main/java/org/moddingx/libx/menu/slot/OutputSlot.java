package org.moddingx.libx.menu.slot;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.moddingx.libx.inventory.IAdvancedItemHandlerModifiable;

import javax.annotation.Nonnull;

/**
 * A {@link ResourceHandlerSlot slot} that no items can be inserted in.
 */
public class OutputSlot extends ResourceHandlerSlot {

    public OutputSlot(IAdvancedItemHandlerModifiable handler, int index, int xPosition, int yPosition) {
        super(handler, handler::set, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        return false;
    }
}
