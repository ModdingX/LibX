package org.moddingx.libx.impl.base.decoration;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
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
        Item.Properties itemProperties = this.properties.get();
        DecoratedBlockItem item = new DecoratedBlockItem(element, block, this.burnTimeModifier, itemProperties);
        builder.register(Registries.ITEM, item);
    }
    
    private static class DecoratedBlockItem extends BlockItem {

        private final DecoratedBlock parent;
        private final double burnTimeModifier;
        
        public DecoratedBlockItem(Block block, DecoratedBlock parent, double burnTimeModifier, Properties properties) {
            super(block, properties);
            this.parent = parent;
            this.burnTimeModifier = burnTimeModifier;
        }

        @Override
        public int getBurnTime(ItemStack stack, @Nullable RecipeType<?> recipeType) {
            if (this.burnTimeModifier == 0) return 0;
            int burnTime = this.parent.getBurnTime(stack, recipeType);
            if (burnTime < 0) return burnTime;
            return (int) Math.round(this.burnTimeModifier * burnTime);
        }
    }
}
