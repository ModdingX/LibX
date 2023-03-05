package org.moddingx.libx.base.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.registries.ForgeRegistries;
import org.moddingx.libx.base.BlockBase;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.mod.ModXRegistration;
import org.moddingx.libx.registration.RegistrationContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.lang.reflect.Constructor;
import java.util.Set;

/**
 * Base class for blocks with {@link BlockEntity block entities} for mods using {@link ModXRegistration}.
 * This will automatically set the creative tab if it's defined in the mod and register a block item and
 * a block entity type.
 *
 * The constructor requires a {@link BlockEntity} class. The block entity class must define a public
 * constructor with three arguments of types {@link BlockEntityType}, {@link BlockPos} and {@link BlockState}
 * for this to create a block entity type. This class will do the magic to wire the block entity to the block
 * and invoke the constructor.
 *
 * The block entity class can implement {@link TickingBlock} and {@link GameEventBlock}. This class will then
 * generate a matching {@link BlockEntityTicker} and a {@link GameEventListener} if required.
 */
public class BlockBE<T extends BlockEntity> extends BlockBase implements EntityBlock {

    private final Class<T> beClass;
    private final Constructor<T> beConstructor;
    private final BlockEntityType<T> beType;

    public BlockBE(ModX mod, Class<T> beClass, Properties properties) {
        this(mod, beClass, properties, new Item.Properties());
    }

    public BlockBE(ModX mod, Class<T> beClass, Properties properties, @Nullable Item.Properties itemProperties) {
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
    @OverridingMethodsMustInvokeSuper
    public void registerAdditional(RegistrationContext ctx, EntryCollector builder) {
        super.registerAdditional(ctx, builder);
        builder.register(Registries.BLOCK_ENTITY_TYPE, this.beType);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void initTracking(RegistrationContext ctx, TrackingCollector builder) throws ReflectiveOperationException {
        super.initTracking(ctx, builder);
        builder.track(ForgeRegistries.BLOCK_ENTITY_TYPES, BlockBE.class.getDeclaredField("beType"));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return this.beType.create(pos, state);
    }

    @Nullable
    @Override
    public <X extends BlockEntity> BlockEntityTicker<X> getTicker(@Nonnull Level level, @Nonnull BlockState state, @Nonnull BlockEntityType<X> beType) {
        if (this.beType.isValid(state) && TickingBlock.class.isAssignableFrom(this.beClass)) {
            //noinspection Convert2Lambda
            return new BlockEntityTicker<>() {

                @Override
                public void tick(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull X blockEntity) {
                    if (blockEntity instanceof TickingBlock ticking) {
                        ticking.tick();
                    }
                }
            };
        }
        return null;
    }

    @Nullable
    @Override
    public <X extends BlockEntity> GameEventListener getListener(@Nonnull ServerLevel level, @Nonnull X blockEntity) {
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
                public boolean handleGameEvent(@Nonnull ServerLevel level, @Nonnull GameEvent gameEvent, @Nonnull GameEvent.Context context, @Nonnull Vec3 pos) {
                    return eventBlock.notifyGameEvent(level, gameEvent, context, pos);
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
                be.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(handler -> {
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
