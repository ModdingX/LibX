package org.moddingx.libx.base;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.moddingx.libx.codec.MoreStreamCodecs;
import org.moddingx.libx.inventory.StackItemHandler;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.registration.Registerable;
import org.moddingx.libx.registration.RegistrationContext;
import org.moddingx.libx.registration.util.CapabilityInfo;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.function.Function;

/**
 * Base class for {@link Item items} which have an inventory. This will provide the capability to the item.
 */
public class ItemInventory extends ItemBase implements Registerable {

    public static final DataComponentType<NonNullList<ItemStack>> INVENTORY_DATA = new DataComponentType.Builder<NonNullList<ItemStack>>()
            .persistent(NonNullList.codecOf(ItemStack.OPTIONAL_CODEC))
            .networkSynchronized(MoreStreamCodecs.listOf(ItemStack.OPTIONAL_STREAM_CODEC).map(NonNullList::copyOf, Function.identity()))
            .build();

    private final int inventorySize;
    
    public ItemInventory(ModX mod, int inventorySize, Properties properties) {
        super(mod, properties);
        this.inventorySize = inventorySize;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void registerAdditional(RegistrationContext ctx, EntryCollector builder) {
        builder.register(null, new CapabilityInfo.Item<>(this, Capabilities.ItemHandler.ITEM, (stack, ignored) -> new StackItemHandler(this.inventorySize, stack)));
    }
}
