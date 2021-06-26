package io.github.noeppi_noeppi.libx.inventory.container;

import io.github.noeppi_noeppi.libx.fi.Function5;
import io.github.noeppi_noeppi.libx.fi.Function6;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
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
 * A {@link DefaultContainer} for blocks with tile entities.
 */
public class TileContainer<T extends TileEntity> extends DefaultContainer {

    protected final BlockPos pos;
    protected final T tile;
    
    public TileContainer(@Nullable ContainerType<?> type, int windowId, World world, BlockPos pos, PlayerInventory playerInventory, PlayerEntity player, int firstOutputSlot, int firstInventorySlot) {
        super(type, windowId, world, playerInventory, player, firstOutputSlot, firstInventorySlot);
        this.pos = pos;
        //noinspection unchecked
        this.tile = (T) world.getTileEntity(pos);
    }

    @Override
    public boolean canInteractWith(@Nonnull PlayerEntity player) {
        //noinspection ConstantConditions
        return isWithinUsableDistance(IWorldPosCallable.of(this.tile.getWorld(), this.tile.getPos()), this.player, this.tile.getBlockState().getBlock());
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
    public static <T extends TileContainer<?>> ContainerType<T> createContainerType(Function5<Integer, World, BlockPos, PlayerInventory, PlayerEntity, T> constructor) {
        return IForgeContainerType.create((windowId1, inv, data) -> {
            BlockPos pos = data.readBlockPos();
            World world = inv.player.getEntityWorld();
            return constructor.apply(windowId1, world, pos, inv, inv.player);
        });
    }

    /**
     * Creates a container type for a ContainerTile.
     *
     * @param constructor A method reference to the container's constructor.
     */
    public static <T extends Container> ContainerType<T> createContainerType(Function6<ContainerType<T>, Integer, World, BlockPos, PlayerInventory, PlayerEntity, T> constructor) {
        AtomicReference<ContainerType<T>> typeRef = new AtomicReference<>(null);
        ContainerType<T> type = IForgeContainerType.create((windowId1, inv, data) -> {
            BlockPos pos1 = data.readBlockPos();
            World world1 = inv.player.getEntityWorld();
            return constructor.apply(typeRef.get(), windowId1, world1, pos1, inv, inv.player);
        });
        typeRef.set(type);
        return type;
    }

    /**
     * Opens a TileContainer for a player.
     */
    public static void openContainer(ServerPlayerEntity player, ContainerType<? extends TileContainer<?>> container, ITextComponent title, BlockPos pos) {
        INamedContainerProvider containerProvider = new INamedContainerProvider() {
            @Nonnull
            @Override
            public ITextComponent getDisplayName() {
                return title;
            }

            @Override
            public Container createMenu(int windowId, @Nonnull PlayerInventory playerInventory, @Nonnull PlayerEntity player) {
                PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
                buffer.writeBlockPos(pos);
                return container.create(windowId, playerInventory, buffer);
            }
        };
        NetworkHooks.openGui(player, containerProvider, pos);
    }
}
