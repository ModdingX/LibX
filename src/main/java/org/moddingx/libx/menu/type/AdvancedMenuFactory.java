package org.moddingx.libx.menu.type;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;

/**
 * A factory for {@link AbstractContainerMenu menus} that can take some kind of extra payload.
 * 
 * @param <T> The type of {@link AbstractContainerMenu menu} created by this factory.
 * @param <S> The type of the extra payload used to create menus. This can be {@link Void}.
 */
public interface AdvancedMenuFactory<T extends AbstractContainerMenu, S> {

    /**
     * Creates a new menu. This is called on both the logical server and the logical client.
     */
    T createMenu(MenuType<T> menuType, int windowId, Level level, S payload, Player player, Inventory inventory);
}
