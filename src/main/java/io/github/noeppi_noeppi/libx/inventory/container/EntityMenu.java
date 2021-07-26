package io.github.noeppi_noeppi.libx.inventory.container;

import com.mojang.datafixers.util.Function5;
import io.github.noeppi_noeppi.libx.fi.Function6;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A {@link DefaultMenu} for entities.
 */
public abstract class EntityMenu<T extends Entity> extends DefaultMenu {

    public final T entity;
    
    public EntityMenu(@Nullable MenuType<?> type, int windowId, Level level, int entityId, Inventory playerContainer, Player player, int firstOutputSlot, int firstInventorySlot) {
        super(type, windowId, level, playerContainer, player, firstOutputSlot, firstInventorySlot);
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

    /**
     * Creates a container type for an {@link EntityMenu}.
     *
     * @param constructor A method reference to the menus constructor.
     */
    public static <T extends AbstractContainerMenu> MenuType<T> createMenuType(Function5<Integer, Level, Integer, Inventory, Player, T> constructor) {
        return IForgeContainerType.create((windowId1, inv, data) -> {
            int entityId1 = data.readInt();
            Level level1 = inv.player.getCommandSenderWorld();
            return constructor.apply(windowId1, level1, entityId1, inv, inv.player);
        });
    }

    /**
     * Creates a menu type for an {@link EntityMenu}.
     *
     * @param constructor A method reference to the menus constructor.
     */
    public static <T extends AbstractContainerMenu> MenuType<T> createMenuType(Function6<MenuType<T>, Integer, Level, Integer, Inventory, Player, T> constructor) {
        AtomicReference<MenuType<T>> typeRef = new AtomicReference<>(null);
        MenuType<T> type = IForgeContainerType.create((windowId1, inv, data) -> {
            int entityId = data.readInt();
            Level world = inv.player.getCommandSenderWorld();
            return constructor.apply(typeRef.get(), windowId1, world, entityId, inv, inv.player);
        });
        typeRef.set(type);
        return type;
    }

    /**
     * Opens an {@link EntityMenu} for a player.
     */
    public static void openMenu(ServerPlayer player, MenuType<? extends BlockEntityMenu<?>> menu, Component title, Entity entity) {
        MenuProvider containerProvider = new MenuProvider() {
            @Nonnull
            @Override
            public Component getDisplayName() {
                return title;
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, @Nonnull Inventory inventory, @Nonnull Player player) {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                buffer.writeInt(entity.getId());
                return menu.create(containerId, inventory, buffer);
            }
        };
        NetworkHooks.openGui(player, containerProvider, buffer -> buffer.writeInt(entity.getId()));
    }
}
