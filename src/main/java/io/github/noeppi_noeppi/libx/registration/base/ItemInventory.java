package io.github.noeppi_noeppi.libx.registration.base;

import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class ItemInventory<T extends IItemHandlerModifiable & INBTSerializable<CompoundTag>> extends ItemBase {

    private final Function<Runnable, T> inventoryFactory;
    
    public ItemInventory(ModX mod, Properties properties, Function<Runnable, T> inventoryFactory) {
        super(mod, properties);
        this.inventoryFactory = inventoryFactory;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag capTag) {
        ICapabilityProvider parent = super.initCapabilities(stack, capTag);
        
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
                if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                    return inventoryCapability.cast();
                } else {
                    return parent == null ? LazyOptional.empty() : parent.getCapability(cap, side);
                }
            }
        };
    }

    @Nullable
    public static IItemHandlerModifiable getInventory(ItemStack stack) {
        IItemHandler handler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).resolve().orElse(null);
        if (handler instanceof IItemHandlerModifiable modifiable) {
            return modifiable;
        } else {
            return null;
        }
    }

    public static Optional<IItemHandlerModifiable> getInventoryOption(ItemStack stack) {
        IItemHandler handler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).resolve().orElse(null);
        if (handler instanceof IItemHandlerModifiable modifiable) {
            return Optional.of(modifiable);
        } else {
            return Optional.empty();
        }
    }
}
