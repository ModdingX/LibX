package io.github.noeppi_noeppi.libx.base;

import com.google.common.collect.ImmutableSet;
import io.github.noeppi_noeppi.libx.mod.ModX;
import io.github.noeppi_noeppi.libx.mod.registration.ModXRegistration;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.Set;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

/**
 * Base class for blocks with {@link TileEntity tile entities} for mods using {@link ModXRegistration}.
 * This will automatically set the creative tab if it's defined in the mod and register a block item and
 * a tile entity type.
 * <p>
 * The constructor requires a {@link TileEntity} class. The tile entity class  <b>must</b> define a public
 * constructor with one argument of type {@link TileEntityType} for this to create a tile entity type.
 * This class will do the magic to wire the tile entity to the block and invoke the constructor.
 */
public class BlockBE<T extends BlockEntity> extends BlockBase {

    private final Class<T> beClass;
    private final Constructor<T> beConstructor;
    private final BlockEntityType<T> beType;

    public BlockBE(ModX mod, Class<T> beClass, Properties properties) {
        this(mod, beClass, properties, new Item.Properties());
    }

    public BlockBE(ModX mod, Class<T> beClass, Properties properties, Item.Properties ibemProperties) {
        super(mod, properties, ibemProperties);
        this.beClass = beClass;

        try {
            this.beConstructor = beClass.getConstructor(BlockEntityType.class);
        } catch (ReflectiveOperationException e) {
            if (e.getCause() != null)
                e.getCause().printStackTrace();
            throw new RuntimeException("Could not get constructor for tile entity " + beClass + ".", e);
        }
        //noinspection ConstantConditions
        this.beType = new BlockEntityType<>(() -> {
            try {
                return this.beConstructor.newInstance(this.getTileType());
            } catch (ReflectiveOperationException e) {
                if (e.getCause() != null)
                    e.getCause().printStackTrace();
                throw new RuntimeException("Could not create TileEntity of type " + beClass + ".", e);
            }
        }, ImmutableSet.of(this), null);
    }

    @Override
    public Set<Object> getAdditionalRegisters(ResourceLocation id) {
        return ImmutableSet.builder().addAll(super.getAdditionalRegisters(id)).add(this.beType).build();
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public T createTileEntity(BlockState state, BlockGetter level) {
        return this.beType.create();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
        if (!level.isClientSide && (!state.is(newState.getBlock()) || !newState.hasTileEntity()) && this.shouldDropInventory(level, pos, state)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be != null) {
                be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(handler -> {
                    if (handler instanceof IItemHandlerModifiable) {
                        for (int i = 0; i < handler.getSlots(); i++) {
                            ItemStack stack = handler.getStackInSlot(i);
                            ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, stack.copy());
                            level.addFreshEntity(entity);
                            ((IItemHandlerModifiable) handler).setStackInSlot(i, ItemStack.EMPTY);
                        }
                    }
                });
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    public T getTile(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null || !this.beClass.isAssignableFrom(be.getClass())) {
            throw new IllegalStateException("Expected a tile entity of type " + this.beClass + " at " + level + " " + pos + ", got" + be);
        }
        //noinspection unchecked
        return (T) be;
    }

    public BlockEntityType<T> getTileType() {
        return this.beType;
    }

    /**
     * Override this to prevent the inventory of the tile entity to be dropped when the block is
     * broken. To automatically drop the inventory the tile entity must provide an item handler
     * capability that is an instance of {@link IItemHandlerModifiable}
     */
    protected boolean shouldDropInventory(Level level, BlockPos pos, BlockState state) {
        return true;
    }
}
