package org.moddingx.libx.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A {@link DefaultMenu} for menus related to a block in the world.
 */
public class BlockMenu extends DefaultMenu {

    protected final BlockPos pos;

    public BlockMenu(@Nullable MenuType<? extends BlockMenu> type, int windowId, Level level, BlockPos pos, Player player, Inventory inventory, int firstOutputSlot, int firstInventorySlot) {
        super(type, windowId, level, player, inventory, firstOutputSlot, firstInventorySlot);
        this.pos = pos;
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return stillValid(ContainerLevelAccess.create(this.level, this.pos), this.player, this.level.getBlockState(this.pos).getBlock());
    }

    public BlockPos getPos() {
        return this.pos;
    }
}
