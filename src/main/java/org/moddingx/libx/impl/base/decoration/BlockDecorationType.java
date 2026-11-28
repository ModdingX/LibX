package org.moddingx.libx.impl.base.decoration;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.moddingx.libx.base.decoration.DecoratedBlock;
import org.moddingx.libx.base.decoration.DecorationContext;
import org.moddingx.libx.fi.Function3;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.registration.Registerable;
import org.moddingx.libx.registration.RegistrationContext;
import org.moddingx.libx.util.game.CookingFuels;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class BlockDecorationType<T extends Block> extends BaseDecorationType<T> {

    private final double burnTimeModifier;
    private final Supplier<Item.Properties> properties;

    public BlockDecorationType(String name, @Nullable ResourceKey<? extends Registry<? super T>> registry, double burnTimeModifier, Function<DecoratedBlock, T> action) {
        super(name, registry, action);
        this.properties = Item.Properties::new;
        this.burnTimeModifier = burnTimeModifier;
    }

    public BlockDecorationType(String name, @Nullable ResourceKey<? extends Registry<? super T>> registry, double burnTimeModifier, BiFunction<DecorationContext, DecoratedBlock, T> action) {
        super(name, registry, action);
        this.properties = Item.Properties::new;
        this.burnTimeModifier = burnTimeModifier;
    }

    public BlockDecorationType(String name, @Nullable ResourceKey<? extends Registry<? super T>> registry, double burnTimeModifier, Function3<ModX, DecorationContext, DecoratedBlock, T> action) {
        super(name, registry, action);
        this.properties = Item.Properties::new;
        this.burnTimeModifier = burnTimeModifier;
    }

    public BlockDecorationType(String name, @Nullable ResourceKey<? extends Registry<? super T>> registry, Supplier<Item.Properties> properties, double burnTimeModifier, Function<DecoratedBlock, T> action) {
        super(name, registry, action);
        this.properties = properties;
        this.burnTimeModifier = burnTimeModifier;
    }

    public BlockDecorationType(String name, @Nullable ResourceKey<? extends Registry<? super T>> registry, Supplier<Item.Properties> properties, double burnTimeModifier, BiFunction<DecorationContext, DecoratedBlock, T> action) {
        super(name, registry, action);
        this.properties = properties;
        this.burnTimeModifier = burnTimeModifier;
    }

    public BlockDecorationType(String name, @Nullable ResourceKey<? extends Registry<? super T>> registry, Supplier<Item.Properties> properties, double burnTimeModifier, Function3<ModX, DecorationContext, DecoratedBlock, T> action) {
        super(name, registry, action);
        this.properties = properties;
        this.burnTimeModifier = burnTimeModifier;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void registerAdditional(ModX mod, DecorationContext context, DecoratedBlock block, T element, RegistrationContext ctx, Registerable.EntryCollector builder) {
        Item.Properties itemProperties = this.properties.get().setId(ResourceKey.create(Registries.ITEM, ctx.id())).useBlockDescriptionPrefix();
        int burnTime = (int) Math.round(block.burnTime() * this.burnTimeModifier);
        if (burnTime > 0) {
            CookingFuels.cookingFuel(itemProperties, mod, burnTime);
        }

        DecoratedBlockItem item = new DecoratedBlockItem(element, block, itemProperties);
        builder.register(Registries.ITEM, item);
    }

    private static class DecoratedBlockItem extends BlockItem {

        private final DecoratedBlock parent;

        public DecoratedBlockItem(Block block, DecoratedBlock parent, Properties properties) {
            super(block, properties);
            this.parent = parent;
        }

        @Override
        public boolean isEnabled(@Nonnull FeatureFlagSet enabledFeatures) {
            return this.parent.isEnabled(enabledFeatures);
        }
    }
}
