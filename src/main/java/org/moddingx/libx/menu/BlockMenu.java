package org.moddingx.libx.menu;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.network.NetworkHooks;
import org.moddingx.libx.fi.Function5;
import org.moddingx.libx.fi.Function6;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A {@link DefaultMenu} for menus related to a block in the world.
 */
public class BlockMenu extends DefaultMenu {

    protected final BlockPos pos;

    public BlockMenu(@Nullable MenuType<? extends BlockMenu> type, int windowId, Level level, BlockPos pos, Inventory playerContainer, Player player, int firstOutputSlot, int firstInventorySlot) {
        super(type, windowId, level, playerContainer, player, firstOutputSlot, firstInventorySlot);
        this.pos = pos;
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return stillValid(ContainerLevelAccess.create(this.level, this.pos), this.player, this.level.getBlockState(this.pos).getBlock());
    }

    public BlockPos getPos() {
        return this.pos;
    }

    /**
     * Creates a menu type for a {@link BlockMenu}.
     *
     * @param constructor A method reference to the menus constructor.
     */
    public static <T extends BlockMenu> MenuType<T> createMenuType(Function5<Integer, Level, BlockPos, Inventory, Player, T> constructor) {
        return IForgeMenuType.create((windowId, inv, data) -> constructor.apply(windowId, inv.player.level, data.readBlockPos(), inv, inv.player));
    }

    /**
     * Creates a menu type for a {@link BlockMenu}.
     *
     * @param constructor A method reference to the menus constructor.
     */
    public static <T extends BlockMenu> MenuType<T> createMenuType(Function6<MenuType<T>, Integer, Level, BlockPos, Inventory, Player, T> constructor) {
        AtomicReference<MenuType<T>> typeRef = new AtomicReference<>(null);
        MenuType<T> type = IForgeMenuType.create((windowId, inv, data) -> constructor.apply(typeRef.get(), windowId, inv.player.level, data.readBlockPos(), inv, inv.player));
        typeRef.set(type);
        return type;
    }

    /**
     * Opens a {@link BlockMenu} for a player.
     */
    public static void openMenu(ServerPlayer player, MenuType<? extends BlockMenu> menu, Component title, BlockPos pos) {
        MenuProvider containerProvider = new MenuProvider() {

            @Nonnull
            @Override
            public Component getDisplayName() {
                return title;
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, @Nonnull Inventory inventory, @Nonnull Player player) {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                buffer.writeBlockPos(pos);
                return menu.create(containerId, inventory, buffer);
            }
        };
        NetworkHooks.openScreen(player, containerProvider, pos);
    }
}
