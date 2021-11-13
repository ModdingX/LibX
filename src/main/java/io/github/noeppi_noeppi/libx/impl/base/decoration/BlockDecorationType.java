package io.github.noeppi_noeppi.libx.impl.base.decoration;

import io.github.noeppi_noeppi.libx.base.decoration.DecoratedBlock;
import io.github.noeppi_noeppi.libx.base.decoration.DecorationContext;
import io.github.noeppi_noeppi.libx.fi.Function3;
import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class BlockDecorationType<T extends Block> extends BaseDecorationType<T> {

    private final Supplier<Item.Properties> properties;

    public BlockDecorationType(String name, Function<DecoratedBlock, T> action) {
        super(name, action);
        this.properties = Item.Properties::new;
    }

    public BlockDecorationType(String name, BiFunction<DecorationContext, DecoratedBlock, T> action) {
        super(name, action);
        this.properties = Item.Properties::new;
    }

    public BlockDecorationType(String name, Function3<ModX, DecorationContext, DecoratedBlock, T> action) {
        super(name, action);
        this.properties = Item.Properties::new;
    }
    
    public BlockDecorationType(String name, Supplier<Item.Properties> properties, Function<DecoratedBlock, T> action) {
        super(name, action);
        this.properties = properties;
    }

    public BlockDecorationType(String name, Supplier<Item.Properties> properties, BiFunction<DecorationContext, DecoratedBlock, T> action) {
        super(name, action);
        this.properties = properties;
    }

    public BlockDecorationType(String name, Supplier<Item.Properties> properties, Function3<ModX, DecorationContext, DecoratedBlock, T> action) {
        super(name, action);
        this.properties = properties;
    }

    @Override
    public Set<Object> additionalRegistration(ModX mod, DecorationContext context, DecoratedBlock block, T element) {
        Item.Properties itemProperties = this.properties.get();
        if (mod.tab != null) itemProperties.tab(mod.tab);
        return Set.of(new BlockItem(element, itemProperties));
    }
}
