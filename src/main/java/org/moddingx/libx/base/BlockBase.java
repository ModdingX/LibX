package org.moddingx.libx.base;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.registries.ForgeRegistries;
import org.moddingx.libx.creativetab.CreativeTabItemProvider;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.mod.ModXRegistration;
import org.moddingx.libx.registration.Registerable;
import org.moddingx.libx.registration.RegistrationContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.function.Consumer;
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

    /**
     * Called from the item for this block from {@link Item#initializeClient(Consumer)}.
     * Can be used to set client extensions for the block item.
     */
    public void initializeItemClient(@Nonnull Consumer<IClientItemExtensions> consumer) {

    }
    
    public int getBurnTime(ItemStack stack, @Nullable RecipeType<?> recipeType) {
        return -1;
    }
    
    @Override
    public Stream<ItemStack> makeCreativeTabStacks() {
        return Stream.of(new ItemStack(this));
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void registerAdditional(RegistrationContext ctx, EntryCollector builder) {
        if (this.hasItem) {
            builder.register(Registries.ITEM, this.item);
        }
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void initTracking(RegistrationContext ctx, TrackingCollector builder) throws ReflectiveOperationException {
        if (this.hasItem) {
            builder.track(ForgeRegistries.ITEMS, BlockBase.class.getDeclaredField("item"));
        }
    }
    
    private class BaseBlockItem extends BlockItem implements CreativeTabItemProvider {

        public BaseBlockItem(Block block, Properties itemProperties) {
            super(block, itemProperties);
        }

        @Override
        public void initializeClient(@Nonnull Consumer<IClientItemExtensions> consumer) {
            BlockBase.this.initializeItemClient(consumer);
        }
        
        @Override
        public int getBurnTime(ItemStack stack, @Nullable RecipeType<?> recipeType) {
            return BlockBase.this.getBurnTime(stack, recipeType);
        }

        @Override
        public Stream<ItemStack> makeCreativeTabStacks() {
            return BlockBase.this.makeCreativeTabStacks();
        }
    }
}
