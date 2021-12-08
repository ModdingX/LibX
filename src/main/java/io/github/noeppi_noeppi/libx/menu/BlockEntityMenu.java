package io.github.noeppi_noeppi.libx.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;

/**
 * A {@link BlockMenu} for blocks with block entities.
 */
public class BlockEntityMenu<T extends BlockEntity> extends BlockMenu {

    protected final T blockEntity;
    
    public BlockEntityMenu(@Nullable MenuType<? extends BlockEntityMenu<?>> type, int windowId, Level level, BlockPos pos, Inventory playerContainer, Player player, int firstOutputSlot, int firstInventorySlot) {
        super(type, windowId, level, pos, playerContainer, player, firstOutputSlot, firstInventorySlot);
        //noinspection unchecked
        this.blockEntity = (T) level.getBlockEntity(pos);
    }

    public T getBlockEntity() {
        return this.blockEntity;
    }
}
