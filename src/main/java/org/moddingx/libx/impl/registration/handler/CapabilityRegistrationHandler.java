package org.moddingx.libx.impl.registration.handler;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.moddingx.libx.registration.util.CapabilityInfo;

import java.util.HashMap;
import java.util.Map;

public class CapabilityRegistrationHandler extends SpecialRegistrationHandler {
    
    private final Map<Identifier, CapabilityInfo.Item<?, ?>> items;
    private final Map<Identifier, CapabilityInfo.Block<?, ?>> blocks;
    private final Map<Identifier, CapabilityInfo.BlockEntity<?, ?, ?>> blockEntities;
    private final Map<Identifier, CapabilityInfo.Entity<?, ?, ?>> entities;

    public CapabilityRegistrationHandler(Runnable runRegistration) {
        super(runRegistration);
        this.items = new HashMap<>();
        this.blocks = new HashMap<>();
        this.blockEntities = new HashMap<>();
        this.entities = new HashMap<>();
    }

    @Override
    public void handle(Identifier id, Object object) {
        if (object instanceof CapabilityInfo.Item<?, ?> itemInfo) {
            this.addToMap("CapabilityInfo.Item", this.items, id, itemInfo);
        }
        if (object instanceof CapabilityInfo.Block<?, ?> blockInfo) {
            this.addToMap("CapabilityInfo.Block", this.blocks, id, blockInfo);
        }
        if (object instanceof CapabilityInfo.BlockEntity<?, ?, ?> blockEntityInfo) {
            this.addToMap("CapabilityInfo.BlockEntity", this.blockEntities, id, blockEntityInfo);
        }
        if (object instanceof CapabilityInfo.Entity<?, ?, ?> entityInfo) {
            this.addToMap("CapabilityInfo.Entity", this.entities, id, entityInfo);
        }
    }
    
    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (CapabilityInfo.Item<?, ?> info : this.items.values()) registerItemTo(event, info);
        for (CapabilityInfo.Block<?, ?> info : this.blocks.values()) registerBlockTo(event, info);
        for (CapabilityInfo.BlockEntity<?, ?, ?> info : this.blockEntities.values()) registerBlockEntityTo(event, info);
        for (CapabilityInfo.Entity<?, ?, ?> info : this.entities.values()) registerEntityTo(event, info);
    }
    
    private static <T, C> void registerItemTo(RegisterCapabilitiesEvent event, CapabilityInfo.Item<T, C> info) {
        event.registerItem(info.capability(), info.provider(), info.item());
    }
    
    private static <T, C> void registerBlockTo(RegisterCapabilitiesEvent event, CapabilityInfo.Block<T, C> info) {
        event.registerBlock(info.capability(), info.provider(), info.block());
    }
    
    private static <BE extends BlockEntity, T, C> void registerBlockEntityTo(RegisterCapabilitiesEvent event, CapabilityInfo.BlockEntity<BE, T, C> info) {
        event.registerBlockEntity(info.capability(), info.blockEntityType(), info.provider());
    }
    
    private static <E extends Entity, T, C> void registerEntityTo(RegisterCapabilitiesEvent event, CapabilityInfo.Entity<E, T, C> info) {
        event.registerEntity(info.capability(), info.entityType(), info.provider());
    }
}
