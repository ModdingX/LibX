package io.github.noeppi_noeppi.libx.base;

import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Base class for {@link Item items} which have an inventory. This will provide the capability to the item.
 */
public class ItemInventory<T extends IItemHandlerModifiable & INBTSerializable<CompoundTag>> extends ItemBase {

    private final Function<Runnable, T> inventoryFactory;

    /**
     * Creates a new item with inventory.
     * 
     * @param inventoryFactory A factory that creates new item handler for an item stack. The runnable
     *                         given to that function should be called in {@code onContentsChanged}
     */
    public ItemInventory(ModX mod, Properties properties, Function<Runnable, T> inventoryFactory) {
        super(mod, properties);
        this.inventoryFactory = inventoryFactory;
    }
    
    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag capTag) {
        LazyOptional<IItemHandlerModifiable> inventoryCapability = LazyOptional.of(() -> {
            AtomicReference<T> handler = new AtomicReference<>(null);
            handler.set(this.inventoryFactory.apply(() -> {
                CompoundTag nbt = stack.getOrCreateTag();
                nbt.put("Inventory", handler.get().serializeNBT());
                stack.setTag(nbt);
            }));
            CompoundTag nbt = stack.getTag();
            if (nbt != null && nbt.contains("Inventory", Tag.TAG_COMPOUND)) {
                handler.get().deserializeNBT(nbt.getCompound("Inventory"));
            }
            return handler.get();
        });
        
        return new ICapabilityProvider() {
            @Nonnull
            @Override
            public <C> LazyOptional<C> getCapability(@Nonnull Capability<C> cap, @Nullable Direction side) {
                //noinspection unchecked
                return cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? (LazyOptional<C>) inventoryCapability : LazyOptional.empty();
            }
        };
    }

    /**
     * Gets the inventory of an {@link ItemStack} or null if the ItemStack doesn't have the item handler capability
     * or the item handler is not an instance of {@link IItemHandlerModifiable}.
     */
    @Nullable
    public static IItemHandlerModifiable getInventory(ItemStack stack) {
        IItemHandler handler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).resolve().orElse(null);
        if (handler instanceof IItemHandlerModifiable modifiable) {
            return modifiable;
        } else {
            return null;
        }
    }
    
    /**
     * Gets a stream containing the inventory of an {@link ItemStack} or an empty stream if the ItemStack
     * doesn't have the item handler capability or the item handler is not an instance of
     * {@link IItemHandlerModifiable}.
     */
    public static Stream<IItemHandlerModifiable> getInventoryStream(ItemStack stack) {
        IItemHandler handler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).resolve().orElse(null);
        if (handler instanceof IItemHandlerModifiable modifiable) {
            return Stream.of(modifiable);
        } else {
            return Stream.empty();
        }
    }
}
