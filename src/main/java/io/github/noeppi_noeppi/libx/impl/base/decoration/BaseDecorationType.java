package io.github.noeppi_noeppi.libx.impl.base.decoration;

import io.github.noeppi_noeppi.libx.base.decoration.DecoratedBlock;
import io.github.noeppi_noeppi.libx.base.decoration.DecorationContext;
import io.github.noeppi_noeppi.libx.base.decoration.DecorationType;
import io.github.noeppi_noeppi.libx.fi.Function3;
import io.github.noeppi_noeppi.libx.mod.ModX;

import java.util.function.BiFunction;
import java.util.function.Function;

public class BaseDecorationType<T> implements DecorationType<T> {

    private final String name;
    private final Function3<ModX, DecorationContext, DecoratedBlock, T> action;

    public BaseDecorationType(String name, Function<DecoratedBlock, T> action) {
        this(name, (mod, context, block) -> action.apply(block));
    }
    
    public BaseDecorationType(String name, BiFunction<DecorationContext, DecoratedBlock, T> action) {
        this(name, (mod, context, block) -> action.apply(context, block));
    }
    
    public BaseDecorationType(String name, Function3<ModX, DecorationContext, DecoratedBlock, T> action) {
        this.name = name;
        this.action = action;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public T registration(ModX mod, DecorationContext context, DecoratedBlock block) {
        return this.action.apply(mod, context, block);
    }
}
