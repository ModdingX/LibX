package io.github.noeppi_noeppi.libx.inventory.container;

import com.mojang.datafixers.util.Function5;
import io.github.noeppi_noeppi.libx.fi.Function6;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A {@link DefaultContainer} for entities.
 */
public abstract class EntityContainer<T extends Entity> extends DefaultContainer {

    public final T entity;
    
    public EntityContainer(@Nullable ContainerType<?> type, int windowId, World world, int entityId, PlayerInventory playerInventory, PlayerEntity player, int firstOutputSlot, int firstInventorySlot) {
        super(type, windowId, world, playerInventory, player, firstOutputSlot, firstInventorySlot);
        //noinspection unchecked
        this.entity = (T) world.getEntityByID(entityId);
    }

    @Override
    public boolean canInteractWith(@Nonnull PlayerEntity player) {
        return isWithinUsableDistance(IWorldPosCallable.of(this.world, this.entity.getPosition()), this.player, this.world.getBlockState(this.entity.getPosition()).getBlock());
    }

    public T getEntity() {
        return this.entity;
    }

    /**
     * Creates a container type for a ContainerEntity.
     *
     * @param constructor A method reference to the container's constructor.
     */
    public static <T extends Container> ContainerType<T> createContainerType(Function5<Integer, World, Integer, PlayerInventory, PlayerEntity, T> constructor) {
        return IForgeContainerType.create((windowId1, inv, data) -> {
            int entityId1 = data.readInt();
            World world1 = inv.player.getEntityWorld();
            return constructor.apply(windowId1, world1, entityId1, inv, inv.player);
        });
    }

    /**
     * Creates a container type for a ContainerEntity.
     *
     * @param constructor A method reference to the container's constructor.
     */
    public static <T extends Container> ContainerType<T> createContainerType(Function6<ContainerType<T>, Integer, World, Integer, PlayerInventory, PlayerEntity, T> constructor) {
        AtomicReference<ContainerType<T>> typeRef = new AtomicReference<>(null);
        ContainerType<T> type = IForgeContainerType.create((windowId1, inv, data) -> {
            int entityId = data.readInt();
            World world = inv.player.getEntityWorld();
            return constructor.apply(typeRef.get(), windowId1, world, entityId, inv, inv.player);
        });
        typeRef.set(type);
        return type;
    }

    /**
     * Opens an EntityContainer for a player.
     */
    public static void openContainer(ServerPlayerEntity player, ContainerType<? extends TileContainer<?>> container, ITextComponent title, Entity entity) {
        INamedContainerProvider containerProvider = new INamedContainerProvider() {
            @Nonnull
            @Override
            public ITextComponent getDisplayName() {
                return title;
            }

            @Override
            public Container createMenu(int windowId, @Nonnull PlayerInventory playerInventory, @Nonnull PlayerEntity player) {
                PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
                buffer.writeInt(entity.getEntityId());
                return container.create(windowId, playerInventory, buffer);
            }
        };
        NetworkHooks.openGui(player, containerProvider, buffer -> buffer.writeInt(entity.getEntityId()));
    }
}
