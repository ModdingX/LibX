package org.moddingx.libx.annotation.impl;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;

import javax.annotation.Nullable;

/**
 * Helper methods injected by the {@code RegisterClassIds} coremod transformer into the static
 * initializer of every {@code @RegisterClass}-annotated class. Each call sets the registry
 * {@link ResourceKey} on the {@code Properties} object <em>before</em> the {@link Item} or
 * {@link net.minecraft.world.level.block.Block Block} constructor runs, satisfying the requirement that
 * {@code Properties.effectiveDescriptionId()} is only called after {@code setId} has been set.
 */
public class RegistrationPropertiesHelper {

    public static void setItemId(@Nullable Item.Properties props, String id) {
        if (props == null) return;
        props.setId(ResourceKey.create(Registries.ITEM, Identifier.parse(id)));
    }

    public static void setBlockId(@Nullable BlockBehaviour.Properties props, String id) {
        if (props == null) return;
        props.setId(ResourceKey.create(Registries.BLOCK, Identifier.parse(id)));
    }
}
