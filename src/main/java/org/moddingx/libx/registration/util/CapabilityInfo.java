package org.moddingx.libx.registration.util;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.*;
import org.moddingx.libx.registration.Registerable;
import org.moddingx.libx.registration.RegistrationContext;

/**
 * Auxiliary classes to register capability providers easily.
 */
public abstract class CapabilityInfo {
    
    private CapabilityInfo() {
        
    }

    /**
     * Wraps a {@link ICapabilityProvider capability provider} for an {@link ItemCapability item capability}. Using
     * the LibX registration system, an instance of this class can be registered to apply a capability to an
     * {@link net.minecraft.world.item.Item}.
     *
     * @see Registerable#registerAdditional(RegistrationContext, Registerable.EntryCollector)
     */
    public record Item<T, C>(
            ItemLike item,
            ItemCapability<T, C> capability,
            ICapabilityProvider<ItemStack, C, T> provider
    ) {}
    
    /**
     * Wraps a {@link IBlockCapabilityProvider capability provider} for a {@link BlockCapability block capability}.
     * Using the LibX registration system, an instance of this class can be registered to apply a capability to a
     * {@link net.minecraft.world.level.block.Block block}.
     *
     * @see Registerable#registerAdditional(RegistrationContext, Registerable.EntryCollector)
     */
    public record Block<T, C>(
            net.minecraft.world.level.block.Block block,
            BlockCapability<T, C> capability,
            IBlockCapabilityProvider<T, C> provider
    ) {}
    
    /**
     * Wraps a {@link ICapabilityProvider capability provider} for a {@link BlockCapability block capability}. Using
     * the LibX registration system, an instance of this class can be registered to apply a capability to a
     * {@link BlockEntityType}.
     *
     * @see Registerable#registerAdditional(RegistrationContext, Registerable.EntryCollector)
     */
    public record BlockEntity<BE extends net.minecraft.world.level.block.entity.BlockEntity, T, C>(
            BlockEntityType<BE> blockEntityType,
            BlockCapability<T, C> capability,
            ICapabilityProvider<? super BE, C, T> provider
    ) {}

    /**
     * Wraps a {@link ICapabilityProvider capability provider} for an {@link EntityCapability entity capability}. Using
     * the LibX registration system, an instance of this class can be registered to apply a capability to an
     * {@link EntityType entity type}.
     *
     * @see Registerable#registerAdditional(RegistrationContext, Registerable.EntryCollector)
     */
    public record Entity<E extends net.minecraft.world.entity.Entity, T, C>(
            EntityType<E> entityType,
            EntityCapability<T, C> capability,
            ICapabilityProvider<? super E, C, T> provider
    ) {}
}
