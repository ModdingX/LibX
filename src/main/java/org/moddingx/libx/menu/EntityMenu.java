package org.moddingx.libx.menu;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A {@link DefaultMenu} for entities.
 */
public abstract class EntityMenu<T extends Entity> extends DefaultMenu {

    public final T entity;
    
    public EntityMenu(@Nullable MenuType<?> type, int windowId, Level level, int entityId, Player player, Inventory inventory, int firstOutputSlot, int firstInventorySlot) {
        super(type, windowId, level, player, inventory, firstOutputSlot, firstInventorySlot);
        //noinspection unchecked
        this.entity = (T) level.getEntity(entityId);
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return stillValid(ContainerLevelAccess.create(this.level, this.entity.blockPosition()), this.player, this.level.getBlockState(this.entity.blockPosition()).getBlock());
    }

    public T getEntity() {
        return this.entity;
    }
}
