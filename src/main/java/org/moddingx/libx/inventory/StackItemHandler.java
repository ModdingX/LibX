package org.moddingx.libx.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.MutableDataComponentHolder;

/**
 * An {@link IAdvancedItemHandlerModifiable} with a {@link MutableDataComponentHolder} as backend.
 */
public class StackItemHandler extends StackItemResourceHandler implements IAdvancedItemHandlerModifiable {

    /**
     * Backward-compat reference to the INVENTORY_DATA constant from {@link StackItemResourceHandler}.
     */
    public static final DataComponentType<NonNullList<ItemStack>> INVENTORY_DATA = StackItemResourceHandler.INVENTORY_DATA;

    public StackItemHandler(int size, MutableDataComponentHolder dataComponentHolder) {
        super(size, dataComponentHolder);
    }
}
