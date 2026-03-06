package org.moddingx.libx.impl.base.decoration;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import javax.annotation.Nullable;

/**
 * Thread-local that carries the {@link ResourceKey} for the block currently being constructed
 * inside {@link org.moddingx.libx.base.decoration.DecorationContext#register}.  Variant block
 * constructors call {@link #applyId(BlockBehaviour.Properties)} to set the id on their copied
 * {@code Properties} object before passing it to the super-constructor.
 */
public final class DecorationBlockIdContext {

    private static final ThreadLocal<ResourceKey<Block>> CURRENT = new ThreadLocal<>();

    private DecorationBlockIdContext() {}

    @Nullable
    public static ResourceKey<Block> get() {
        return CURRENT.get();
    }

    public static void set(ResourceKey<Block> key) {
        CURRENT.set(key);
    }

    public static void clear() {
        CURRENT.remove();
    }

    /**
     * If a variant key is currently set, calls {@link BlockBehaviour.Properties#setId} on
     * {@code props} and returns it; otherwise returns {@code props} unchanged.
     */
    public static BlockBehaviour.Properties applyId(BlockBehaviour.Properties props) {
        ResourceKey<Block> key = CURRENT.get();
        if (key != null) {
            props.setId(key);
        }

        return props;
    }
}
