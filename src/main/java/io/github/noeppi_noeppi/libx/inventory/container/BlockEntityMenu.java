package io.github.noeppi_noeppi.libx.inventory.container;

import io.github.noeppi_noeppi.libx.fi.Function5;
import io.github.noeppi_noeppi.libx.fi.Function6;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A {@link DefaultContainerMenu} for blocks with block entities.
 */
public class BlockEntityMenu<T extends BlockEntity> extends DefaultContainerMenu {

    protected final BlockPos pos;
    protected final T tile;
    
    public BlockEntityMenu(@Nullable MenuType<?> type, int windowId, Level level, BlockPos pos, Inventory playerContainer, Player player, int firstOutputSlot, int firstInventorySlot) {
        super(type, windowId, level, playerContainer, player, firstOutputSlot, firstInventorySlot);
        this.pos = pos;
        //noinspection unchecked
        this.tile = (T) level.getBlockEntity(pos);
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        //noinspection ConstantConditions
        return stillValid(ContainerLevelAccess.create(this.tile.getLevel(), this.tile.getBlockPos()), this.player, this.tile.getBlockState().getBlock());
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public T getTile() {
        return this.tile;
    }


    /**
     * Creates a container type for a ContainerTile.
     *
     * @param constructor A method reference to the container's constructor.
     */
    public static <T extends BlockEntityMenu<?>> MenuType<T> createMenuType(Function5<Integer, Level, BlockPos, Inventory, Player, T> constructor) {
        return IForgeContainerType.create((windowId1, inv, data) -> {
            BlockPos pos = data.readBlockPos();
            Level level = inv.player.getCommandSenderWorld();
            return constructor.apply(windowId1, level, pos, inv, inv.player);
        });
    }

    /**
     * Creates a container type for a ContainerTile.
     *
     * @param constructor A method reference to the container's constructor.
     */
    public static <T extends AbstractContainerMenu> MenuType<T> createMenuType(Function6<MenuType<T>, Integer, Level, BlockPos, Inventory, Player, T> constructor) {
        AtomicReference<MenuType<T>> typeRef = new AtomicReference<>(null);
        MenuType<T> type = IForgeContainerType.create((windowId1, inv, data) -> {
            BlockPos pos1 = data.readBlockPos();
            Level world1 = inv.player.getCommandSenderWorld();
            return constructor.apply(typeRef.get(), windowId1, world1, pos1, inv, inv.player);
        });
        typeRef.set(type);
        return type;
    }

    /**
     * Opens a TileContainer for a player.
     */
    public static void openMenu(ServerPlayer player, MenuType<? extends BlockEntityMenu<?>> menu, Component title, BlockPos pos) {
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
        NetworkHooks.openGui(player, containerProvider, pos);
    }
}
