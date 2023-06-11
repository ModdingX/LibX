package org.moddingx.libx.base.decoration;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.*;
import org.moddingx.libx.annotation.meta.SuperChainRequired;
import org.moddingx.libx.datagen.provider.model.BlockStateProviderBase;
import org.moddingx.libx.datagen.provider.recipe.DefaultExtension;
import org.moddingx.libx.datagen.provider.tags.CommonTagsProviderBase;
import org.moddingx.libx.impl.base.decoration.DecorationTypes;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.registration.Registerable;
import org.moddingx.libx.registration.RegistrationContext;

import javax.annotation.Nullable;

/**
 * Something that is registered together with a {@link DecoratedBlock}.
 * 
 * All builtin decoration types provide datagen through {@link BlockStateProviderBase}, {@link CommonTagsProviderBase}
 * and a special the recipe extension: {@link DefaultExtension}.
 */
public interface DecorationType<T> {

    /**
     * The {@link DecoratedBlock} itself.
     */
    DecorationType<DecoratedBlock> BASE = DecorationTypes.BASE;
    
    DecorationType<? extends SlabBlock> SLAB = DecorationTypes.SLAB;
    DecorationType<? extends StairBlock> STAIRS = DecorationTypes.STAIRS;
    DecorationType<? extends WallBlock> WALL = DecorationTypes.WALL;
    DecorationType<? extends FenceBlock> FENCE = DecorationTypes.FENCE;
    DecorationType<? extends FenceGateBlock> FENCE_GATE = DecorationTypes.FENCE_GATE;
    DecorationType<? extends ButtonBlock> WOOD_BUTTON = DecorationTypes.WOOD_BUTTON;
    DecorationType<? extends ButtonBlock> STONE_BUTTON = DecorationTypes.STONE_BUTTON;
    DecorationType<? extends PressurePlateBlock> WOOD_PRESSURE_PLATE = DecorationTypes.WOOD_PRESSURE_PLATE;
    DecorationType<? extends PressurePlateBlock> STONE_PRESSURE_PLATE = DecorationTypes.STONE_PRESSURE_PLATE;
    DecorationType<? extends DoorBlock> DOOR = DecorationTypes.DOOR;
    DecorationType<? extends TrapDoorBlock> TRAPDOOR = DecorationTypes.TRAPDOOR;
    DecorationType<? extends SignAccess> SIGN = DecorationTypes.SIGN;
    DecorationType<? extends HangingSignAccess> HANGING_SIGN = DecorationTypes.HANGING_SIGN;

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
    
    record DecorationElement<R, T extends R>(@Nullable ResourceKey<? extends Registry<R>> registry, T element) {
        
        public void registerTo(Registerable.EntryCollector builder) {
            builder.register(this.registry(), this.element());
        }
    }
}
