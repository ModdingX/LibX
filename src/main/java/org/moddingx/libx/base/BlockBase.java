package org.moddingx.libx.base;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import org.moddingx.libx.creativetab.CreativeTabItemProvider;
import org.moddingx.libx.creativetab.CreativeTabX;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.mod.ModXRegistration;
import org.moddingx.libx.registration.Registerable;
import org.moddingx.libx.registration.RegistrationContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.stream.Stream;

/**
 * Base class for {@link Block blocks} for mods using {@link ModXRegistration}. This will automatically set the
 * creative tab if it's defined in the mod and register a {@link BlockItem block item}.
 */
public class BlockBase extends Block implements Registerable, CreativeTabItemProvider {

    protected final ModX mod;
    
    private final boolean hasItem;
    @Nullable private final Item item;
    
    /**
     * Creates a new instance of BlockBase.
     */
    public BlockBase(ModX mod, Properties properties) {
        this(mod, properties, new Item.Properties());
    }

    /**
     * Creates a new instance of BlockBase.
     * 
     * @param itemProperties Properties for the {@link Item} of the block or {@code null} if no item should
     *                       be created.
     */
    public BlockBase(ModX mod, Properties properties, @Nullable Item.Properties itemProperties) {
        super(properties);
        this.mod = mod;
        if (itemProperties == null) {
            this.hasItem = false;
            this.item = null;
        } else {
            this.hasItem = true;
            this.item = new BaseBlockItem(this, itemProperties);
        }
    }
    
    public int getBurnTime(ItemStack stack, @Nullable RecipeType<?> recipeType) {
        return 0;
    }
    
    @Override
    public Stream<ItemStack> makeCreativeTabStacks(CreativeTabX.TabContext ctx) {
        return Stream.of(new ItemStack(this));
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void registerAdditional(RegistrationContext ctx, EntryCollector builder) {
        if (this.hasItem) {
            builder.register(Registries.ITEM, this.item);
        }
    }
    
    private class BaseBlockItem extends BlockItem implements CreativeTabItemProvider {

        public BaseBlockItem(Block block, Properties itemProperties) {
            super(block, itemProperties);
        }
        
        @Override
        public int getBurnTime(@Nonnull ItemStack stack, @Nullable RecipeType<?> recipeType) {
            return BlockBase.this.getBurnTime(stack, recipeType);
        }

        @Override
        public Stream<ItemStack> makeCreativeTabStacks(CreativeTabX.TabContext ctx) {
            return BlockBase.this.makeCreativeTabStacks(ctx);
        }

        @Override
        public boolean isEnabled(@Nonnull FeatureFlagSet enabledFeatures) {
            return this.getBlock().isEnabled(enabledFeatures);
        }
    }
}
