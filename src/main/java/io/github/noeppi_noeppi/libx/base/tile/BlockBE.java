package io.github.noeppi_noeppi.libx.base.tile;

import com.google.common.collect.ImmutableSet;
import io.github.noeppi_noeppi.libx.base.BlockBase;
import io.github.noeppi_noeppi.libx.mod.ModX;
import io.github.noeppi_noeppi.libx.mod.registration.ModXRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.Set;

/**
 * Base class for blocks with {@link BlockEntity block entities} for mods using {@link ModXRegistration}.
 * This will automatically set the creative tab if it's defined in the mod and register a block item and
 * a block entity type.
 * <p>
 * The constructor requires a {@link BlockEntity} class. The block entity class  <b>must</b> define a public
 * constructor with three arguments of types {@link BlockEntityType}, {@link BlockPos} and {@link BlockState}
 * for this to create a block entity type. This class will do the magic to wire the block entity to the block
 * and invoke the constructor.
 * <p>
 * The block entity class can implement {@link TickableBlock} and {@link GameEventBlock}. This class will then
 * generate a matching {@link BlockEntityTicker} and a {@link GameEventListener} if required.
 */
public class BlockBE<T extends BlockEntity> extends BlockBase implements EntityBlock {

    private final Class<T> beClass;
    private final Constructor<T> beConstructor;
    private final BlockEntityType<T> beType;

    public BlockBE(ModX mod, Class<T> beClass, Properties properties) {
        this(mod, beClass, properties, new Item.Properties());
    }

    public BlockBE(ModX mod, Class<T> beClass, Properties properties, Item.Properties itemProperties) {
        super(mod, properties, itemProperties);
        this.beClass = beClass;

        try {
            this.beConstructor = beClass.getConstructor(BlockEntityType.class, BlockPos.class, BlockState.class);
        } catch (ReflectiveOperationException e) {
            if (e.getCause() != null)
                e.getCause().printStackTrace();
            throw new RuntimeException("Could not get constructor for block entity " + beClass + ".", e);
        }
        //noinspection ConstantConditions
        this.beType = new BlockEntityType<>((pos, state) -> {
            try {
                return this.beConstructor.newInstance(this.getBlockEntityType(), pos, state);
            } catch (ReflectiveOperationException e) {
                if (e.getCause() != null)
                    e.getCause().printStackTrace();
                throw new RuntimeException("Could not create BlockEntity of type " + beClass + ".", e);
            }
        }, Set.of(this), null);
    }

    @Override
    public Set<Object> getAdditionalRegisters(ResourceLocation id) {
        return ImmutableSet.builder().addAll(super.getAdditionalRegisters(id)).add(this.beType).build();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return this.beType.create(pos, state);
    }

    @Nullable
    @Override
    public <X extends BlockEntity> BlockEntityTicker<X> getTicker(@Nonnull Level level, @Nonnull BlockState state, @Nonnull BlockEntityType<X> beType) {
        if (this.beType.isValid(state) && TickableBlock.class.isAssignableFrom(this.beClass)) {
            //noinspection Convert2Lambda
            return new BlockEntityTicker<>() {

                @Override
                public void tick(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull X blockEntity) {
                    if (blockEntity instanceof TickableBlock tickable) {
                        tickable.tick();
                    }
                }
            };
        }
        return null;
    }

    @Nullable
    @Override
    public <X extends BlockEntity> GameEventListener getListener(@Nonnull Level level, @Nonnull X blockEntity) {
        if (blockEntity instanceof GameEventBlock eventBlock) {
            PositionSource source = new BlockPositionSource(blockEntity.getBlockPos());
            return new GameEventListener() {
                
                @Nonnull
                @Override
                public PositionSource getListenerSource() {
                    return source;
                }

                @Override
                public int getListenerRadius() {
                    return eventBlock.gameEventRange();
                }

                @Override
                public boolean handleGameEvent(@Nonnull Level level, @Nonnull GameEvent event, @Nullable Entity cause, @Nonnull BlockPos pos) {
                    return eventBlock.notifyGameEvent(event, cause);
                }
            };
        } else {
            return null;
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
        if (!level.isClientSide && (!state.is(newState.getBlock()) ||  !newState.hasBlockEntity()) && this.shouldDropInventory(level, pos, state)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be != null) {
                be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(handler -> {
                    if (handler instanceof IItemHandlerModifiable modifiable) {
                        for (int i = 0; i < modifiable.getSlots(); i++) {
                            ItemStack stack = modifiable.getStackInSlot(i);
                            ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, stack.copy());
                            level.addFreshEntity(entity);
                            modifiable.setStackInSlot(i, ItemStack.EMPTY);
                        }
                    }
                });
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    public T getBlockEntity(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null || !this.beClass.isAssignableFrom(be.getClass())) {
            throw new IllegalStateException("Expected a block entity of type " + this.beClass + " at " + level + " " + pos + ", got" + be);
        }
        //noinspection unchecked
        return (T) be;
    }

    public BlockEntityType<T> getBlockEntityType() {
        return this.beType;
    }

    /**
     * Override this to prevent the inventory of the block entity to be dropped when the block is
     * broken. To automatically drop the inventory the block entity must provide an item handler
     * capability that is an instance of {@link IItemHandlerModifiable}
     */
    protected boolean shouldDropInventory(Level level, BlockPos pos, BlockState state) {
        return true;
    }
}
