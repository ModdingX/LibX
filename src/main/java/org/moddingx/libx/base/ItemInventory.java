package org.moddingx.libx.base;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.moddingx.libx.inventory.StackItemResourceHandler;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.registration.Registerable;
import org.moddingx.libx.registration.RegistrationContext;
import org.moddingx.libx.registration.util.CapabilityInfo;

import javax.annotation.OverridingMethodsMustInvokeSuper;

/**
 * Base class for {@link Item items} which have an inventory. This will provide the capability to the item.
 */
public class ItemInventory extends ItemBase implements Registerable {

    private final int inventorySize;

    public ItemInventory(ModX mod, int inventorySize, Properties properties) {
        super(mod, properties);
        if (inventorySize < 1 || inventorySize > StackItemResourceHandler.MAX_SIZE) {
            throw new IllegalArgumentException("Invalid item inventory size: " + inventorySize + ", must be in [1," + StackItemResourceHandler.MAX_SIZE + "]");
        }
        this.inventorySize = inventorySize;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void registerAdditional(RegistrationContext ctx, EntryCollector builder) {
        builder.register(null, new CapabilityInfo.Item<>(this, Capabilities.Item.ITEM, (_, access) -> new StackItemResourceHandler(this.inventorySize, access)));
    }
}
