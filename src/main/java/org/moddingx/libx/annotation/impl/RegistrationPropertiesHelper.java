package org.moddingx.libx.annotation.impl;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;

/**
 * Helper methods injected by the {@code RegisterClassIds} coremod transformer into the static
 * initializer of every {@code @RegisterClass}-annotated class. Each call sets the registry
 * {@link ResourceKey} on the {@code Properties} object <em>before</em> the {@code Item} or
 * {@code Block} constructor runs, satisfying the requirement that
 * {@code Properties.effectiveDescriptionId()} is only called after {@code setId} has been set.
 */
public class RegistrationPropertiesHelper {

    public static void setItemId(Item.Properties props, String id) {
        props.setId(ResourceKey.create(Registries.ITEM, Identifier.parse(id)));
    }

    public static void setBlockId(BlockBehaviour.Properties props, String id) {
        props.setId(ResourceKey.create(Registries.BLOCK, Identifier.parse(id)));
    }

    public static void cleanupItemHolder(Item item) {
        if (BuiltInRegistries.ITEM instanceof MappedRegistry<?> mr && mr.unregisteredIntrusiveHolders != null) {
            ((MappedRegistry<Item>) mr).unregisteredIntrusiveHolders.remove(item);
        }
    }

    public static void cleanupBlockHolder(Block block) {
        if (BuiltInRegistries.BLOCK instanceof MappedRegistry<?> mr && mr.unregisteredIntrusiveHolders != null) {
            ((MappedRegistry<Block>) mr).unregisteredIntrusiveHolders.remove(block);
        }
    }

    public static void cleanupFluidHolder(Fluid fluid) {
        if (BuiltInRegistries.FLUID instanceof MappedRegistry<?> mr && mr.unregisteredIntrusiveHolders != null) {
            ((MappedRegistry<Fluid>) mr).unregisteredIntrusiveHolders.remove(fluid);
        }
    }

    public static void cleanupEntityTypeHolder(EntityType<?> entityType) {
        if (BuiltInRegistries.ENTITY_TYPE instanceof MappedRegistry<?> mr && mr.unregisteredIntrusiveHolders != null) {
            ((MappedRegistry<EntityType<?>>) mr).unregisteredIntrusiveHolders.remove(entityType);
        }
    }
}
