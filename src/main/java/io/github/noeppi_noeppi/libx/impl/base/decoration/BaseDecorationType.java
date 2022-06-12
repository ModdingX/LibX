package io.github.noeppi_noeppi.libx.impl.base.decoration;

import io.github.noeppi_noeppi.libx.base.decoration.DecoratedBlock;
import io.github.noeppi_noeppi.libx.base.decoration.DecorationContext;
import io.github.noeppi_noeppi.libx.base.decoration.DecorationType;
import io.github.noeppi_noeppi.libx.fi.Function3;
import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Function;

public class BaseDecorationType<T> implements DecorationType<T> {

    private final String name;
    
    @Nullable 
    private final ResourceKey<? extends Registry<? super T>> registry;
    private final Function3<ModX, DecorationContext, DecoratedBlock, T> action;

    public BaseDecorationType(String name, @Nullable ResourceKey<? extends Registry<? super T>> registry, Function<DecoratedBlock, T> action) {
        this(name, registry, (mod, context, block) -> action.apply(block));
    }
    
    public BaseDecorationType(String name, @Nullable ResourceKey<? extends Registry<? super T>> registry, BiFunction<DecorationContext, DecoratedBlock, T> action) {
        this(name, registry, (mod, context, block) -> action.apply(context, block));
    }
    
    public BaseDecorationType(String name, @Nullable ResourceKey<? extends Registry<? super T>> registry, Function3<ModX, DecorationContext, DecoratedBlock, T> action) {
        this.name = name;
        this.registry = registry;
        this.action = action;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public DecorationElement<? super T, T> element(ModX mod, DecorationContext context, DecoratedBlock block) {
        //noinspection unchecked
        return new DecorationElement<>((ResourceKey<? extends Registry<Object>>) this.registry, this.action.apply(mod, context, block));
    }
}
