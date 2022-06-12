package io.github.noeppi_noeppi.libx.base.decoration;

import io.github.noeppi_noeppi.libx.annotation.meta.SuperChainRequired;
import io.github.noeppi_noeppi.libx.impl.base.decoration.DecorationTypes;
import io.github.noeppi_noeppi.libx.mod.ModX;
import io.github.noeppi_noeppi.libx.registration.Registerable;
import io.github.noeppi_noeppi.libx.registration.RegistrationContext;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.*;

import javax.annotation.Nullable;

/**
 * Something that is registered together with a {@link DecoratedBlock}.
 */
public interface DecorationType<T> {

    /**
     * The {@link DecoratedBlock} itself.
     */
    DecorationType<DecoratedBlock> BASE = DecorationTypes.BASE;
    
    DecorationType<? extends SlabBlock> SLAB = DecorationTypes.SLAB;
    DecorationType<? extends StairBlock> STAIR = DecorationTypes.STAIR;
    DecorationType<? extends WallBlock> WALL = DecorationTypes.WALL;
    DecorationType<? extends FenceBlock> FENCE = DecorationTypes.FENCE;
    DecorationType<? extends FenceGateBlock> FENCE_GATE = DecorationTypes.FENCE_GATE;
    DecorationType<? extends WoodButtonBlock> WOOD_BUTTON = DecorationTypes.WOOD_BUTTON;
    DecorationType<? extends StoneButtonBlock> STONE_BUTTON = DecorationTypes.STONE_BUTTON;
    DecorationType<? extends PressurePlateBlock> WOOD_PRESSURE_PLATE = DecorationTypes.WOOD_PRESSURE_PLATE;
    DecorationType<? extends PressurePlateBlock> STONE_PRESSURE_PLATE = DecorationTypes.STONE_PRESSURE_PLATE;
    DecorationType<? extends DoorBlock> DOOR = DecorationTypes.DOOR;
    DecorationType<? extends TrapDoorBlock> TRAPDOOR = DecorationTypes.TRAPDOOR;
    DecorationType<? extends SignAccess> SIGN = DecorationTypes.SIGN;

    /**
     * Gets the name for this decoration type. Must be unique within a {@link DecorationContext}.
     */
    String name();

    /**
     * Gets the element to register.
     */
    DecorationElement<? super T, T> element(ModX mod, DecorationContext context, DecoratedBlock block);

    @SuperChainRequired
    default void registerAdditional(ModX mod, DecorationContext context, DecoratedBlock block, T element, RegistrationContext registrationContext, Registerable.EntryCollector builder) {
        //
    }
    
    public static record DecorationElement<R, T extends R>(@Nullable ResourceKey<? extends Registry<R>> registry, T element) {
        
        public void registerTo(Registerable.EntryCollector builder) {
            builder.register(this.registry(), this.element());
        }
    }
}
