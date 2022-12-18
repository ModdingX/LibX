package org.moddingx.libx.impl.base.decoration;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.moddingx.libx.base.decoration.DecoratedBlock;
import org.moddingx.libx.base.decoration.DecorationContext;
import org.moddingx.libx.fi.Function3;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.registration.Registerable;
import org.moddingx.libx.registration.RegistrationContext;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class BlockDecorationType<T extends Block> extends BaseDecorationType<T> {

    private final Supplier<Item.Properties> properties;

    public BlockDecorationType(String name, @Nullable ResourceKey<? extends Registry<? super T>> registry, Function<DecoratedBlock, T> action) {
        super(name, registry, action);
        this.properties = Item.Properties::new;
    }

    public BlockDecorationType(String name, @Nullable ResourceKey<? extends Registry<? super T>> registry, BiFunction<DecorationContext, DecoratedBlock, T> action) {
        super(name, registry, action);
        this.properties = Item.Properties::new;
    }

    public BlockDecorationType(String name, @Nullable ResourceKey<? extends Registry<? super T>> registry, Function3<ModX, DecorationContext, DecoratedBlock, T> action) {
        super(name, registry, action);
        this.properties = Item.Properties::new;
    }
    
    public BlockDecorationType(String name, @Nullable ResourceKey<? extends Registry<? super T>> registry, Supplier<Item.Properties> properties, Function<DecoratedBlock, T> action) {
        super(name, registry, action);
        this.properties = properties;
    }

    public BlockDecorationType(String name, @Nullable ResourceKey<? extends Registry<? super T>> registry, Supplier<Item.Properties> properties, BiFunction<DecorationContext, DecoratedBlock, T> action) {
        super(name, registry, action);
        this.properties = properties;
    }

    public BlockDecorationType(String name, @Nullable ResourceKey<? extends Registry<? super T>> registry, Supplier<Item.Properties> properties, Function3<ModX, DecorationContext, DecoratedBlock, T> action) {
        super(name, registry, action);
        this.properties = properties;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void registerAdditional(ModX mod, DecorationContext context, DecoratedBlock block, T element, RegistrationContext registrationContext, Registerable.EntryCollector builder) {
        Item.Properties itemProperties = this.properties.get();
        if (mod.tab != null) itemProperties.tab(mod.tab);
        builder.register(Registries.ITEM, new BlockItem(element, itemProperties));
    }
}
