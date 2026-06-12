package org.moddingx.libx.base;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.moddingx.libx.annotation.registration.PlainRegisterable;
import org.moddingx.libx.impl.base.fluid.DefaultBucketItem;
import org.moddingx.libx.impl.base.fluid.DefaultClientExtensions;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.registration.Registerable;
import org.moddingx.libx.registration.RegistrationContext;
import org.moddingx.libx.registration.util.ClientExtensionInfo;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * A {@link Registerable} that registers a {@link FluidType fluid type}, source and flowing {@link Fluid fluid},
 * a {@link LiquidBlock liquid block} and a {@link BucketItem bucket item}.
 */
@PlainRegisterable
public class FluidBase implements ItemLike, Registerable {
    
    protected final ModX mod;

    private final FluidType fluidType;
    private final FlowingFluid sourceFluid;
    private final FlowingFluid flowingFluid;
    private LiquidBlock liquidBlock;
    private BucketItem bucketItem;

    // Factories and raw properties kept to create block/item in registerAdditional where the id is known.
    private final BiFunction<? super FlowingFluid, ? super BlockBehaviour.Properties, ? extends LiquidBlock> liquidBlockFactory;
    private final BiFunction<? super Fluid, ? super Item.Properties, ? extends BucketItem> bucketItemFactory;
    private final BlockBehaviour.Properties pendingBlockProperties;
    private final Item.Properties pendingBucketItemProperties;

    public FluidBase(ModX mod, Builder fluidBuilder) {
        this.mod = mod;
        BaseFlowingFluid.Properties fluidProperties = fluidBuilder.fluidProperties.apply(new BaseFlowingFluid.Properties(this::getFluidType, this::getSourceFluid, this::getFlowingFluid));
        fluidProperties = fluidProperties.block(this::getLiquidBlock).bucket(this::getBucketItem);
        this.fluidType = fluidBuilder.fluidTypeFactory.apply(fluidBuilder.fluidTypeProperties);
        this.sourceFluid = fluidBuilder.sourceFluidFactory.apply(fluidProperties);
        this.flowingFluid = fluidBuilder.flowingFluidFactory.apply(fluidProperties);

        this.liquidBlockFactory = fluidBuilder.liquidBlockFactory;
        this.bucketItemFactory = fluidBuilder.bucketItemFactory;
        this.pendingBlockProperties = fluidBuilder.blockProperties;
        this.pendingBucketItemProperties = fluidBuilder.bucketItemProperties;
    }

    /**
     * Creates the {@link IClientFluidTypeExtensions client extensions} for this fluid. The default
     * implementation creates client extensions that have no special properties, use a still texture
     * located at {@code [namespace]:block/[path]} and a flowing texture located at
     * {@code [namespace]:block/[path]_flowing}.
     */
    protected ClientExtensionInfo.Fluid createClientExtensions(Identifier id) {
        Identifier stillTexture = Identifier.fromNamespaceAndPath(id.getNamespace(), "block/" + id.getPath());
        Identifier flowingTexture = Identifier.fromNamespaceAndPath(id.getNamespace(), "block/" + id.getPath() + "_flowing");
        return new ClientExtensionInfo.Fluid(new DefaultClientExtensions(stillTexture, flowingTexture));
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void registerAdditional(RegistrationContext ctx, EntryCollector builder) {
        if (this.liquidBlock == null) {
            this.liquidBlock = this.liquidBlockFactory.apply(this.sourceFluid,
                    this.pendingBlockProperties.setId(ResourceKey.create(Registries.BLOCK, ctx.id())));
        }
        if (this.bucketItem == null) {
            Identifier bucketLoc = Identifier.fromNamespaceAndPath(ctx.id().getNamespace(), ctx.id().getPath() + "_bucket");
            this.bucketItem = this.bucketItemFactory.apply(this.sourceFluid,
                    this.pendingBucketItemProperties.setId(ResourceKey.create(Registries.ITEM, bucketLoc)));
        }
        builder.register(NeoForgeRegistries.Keys.FLUID_TYPES, this.fluidType);
        builder.register(Registries.FLUID, this.sourceFluid);
        builder.registerNamed(Registries.FLUID, "flowing", this.flowingFluid);
        builder.register(Registries.BLOCK, this.liquidBlock);
        builder.registerNamed(Registries.ITEM, "bucket", this.bucketItem);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void registerClientAdditional(RegistrationContext ctx, EntryCollector builder) {
        builder.register(null, this.createClientExtensions(ctx.id()));
    }

    /**
     * Gets the {@link FluidType fluid type} for this {@link FluidBase}.
     */
    @Nonnull
    public final FluidType getFluidType() {
        return this.fluidType;
    }
    
    /**
     * Gets the {@link Fluid source fluid} for this {@link FluidBase}. This is the same as {@link #getSourceFluid()}.
     * One should prefer this method over {@link #getSourceFluid()} when needing a representative for the whole
     * {@link FluidType fluid type}, for example when creating {@link FluidStack fluid stacks}.
     */
    @Nonnull
    public final Fluid getFluid() {
        return this.getSourceFluid();
    }
    
    /**
     * Gets the {@link Fluid source fluid} for this {@link FluidBase}. This is the same as {@link #getFluid()}.
     * One should prefer this method over {@link #getFluid()} when a distinction between source and flowing fluids
     * is to be made.
     */
    @Nonnull
    public final Fluid getSourceFluid() {
        return this.sourceFluid;
    }

    /**
     * Gets the {@link Fluid flowing fluid} for this {@link FluidBase}.
     */
    @Nonnull
    public final Fluid getFlowingFluid() {
        return this.flowingFluid;
    }

    /**
     * Gets the {@link LiquidBlock liquid block} for this {@link FluidBase}.
     */
    @Nonnull
    public final LiquidBlock getLiquidBlock() {
        return this.liquidBlock;
    }
    
    /**
     * Gets the {@link BucketItem bucket item} for this {@link FluidBase}.
     */
    @Nonnull
    public final BucketItem getBucketItem() {
        return this.bucketItem;
    }

    /**
     * This is the same as {@link #getBucketItem()} and allows {@link FluidBase} to be used as an {@link ItemLike}.
     */
    @Nonnull
    @Override
    public final Item asItem() {
        return this.getBucketItem();
    }

    /**
     * Creates a new fluid builder for setting the basic fluid properties to be passed in the constructor.
     */
    public static Builder fluidBuilder() {
        return new Builder();
    }
    
    public static class Builder {

        private Function<? super FluidType.Properties, ? extends FluidType> fluidTypeFactory;
        private Function<? super BaseFlowingFluid.Properties, ? extends FlowingFluid> sourceFluidFactory;
        private Function<? super BaseFlowingFluid.Properties, ? extends FlowingFluid> flowingFluidFactory;
        private BiFunction<? super FlowingFluid, ? super BlockBehaviour.Properties, ? extends LiquidBlock> liquidBlockFactory;
        private BiFunction<? super Fluid, ? super Item.Properties, ? extends BucketItem> bucketItemFactory;
        
        private FluidType.Properties fluidTypeProperties;
        private BlockBehaviour.Properties blockProperties;
        private Item.Properties bucketItemProperties;
        private UnaryOperator<BaseFlowingFluid.Properties> fluidProperties;
        
        private Builder() {
            this.fluidTypeFactory = FluidType::new;
            this.sourceFluidFactory = BaseFlowingFluid.Source::new;
            this.flowingFluidFactory = BaseFlowingFluid.Flowing::new;
            this.liquidBlockFactory = LiquidBlock::new;
            this.bucketItemFactory = DefaultBucketItem::new;
            
            this.fluidTypeProperties = FluidType.Properties.create();
            this.blockProperties = BlockBehaviour.Properties.ofFullCopy(Blocks.WATER);
            this.bucketItemProperties = new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1);
            this.fluidProperties = UnaryOperator.identity();
        }

        /**
         * Provides a custom implementation for {@link FluidType}.
         */
        public FluidBase.Builder fluidType(Function<? super FluidType.Properties, ? extends FluidType> fluidTypeFactory) {
            this.fluidTypeFactory = fluidTypeFactory;
            return this;
        }

        /**
         * Provides a custom implementation for the {@link FlowingFluid source fluid}.
         */
        public FluidBase.Builder sourceFluid(Function<? super BaseFlowingFluid.Properties, ? extends FlowingFluid> sourceFluidFactory) {
            this.sourceFluidFactory = sourceFluidFactory;
            return this;
        }

        /**
         * Provides a custom implementation for the {@link FlowingFluid flowing fluid}.
         */
        public FluidBase.Builder flowingFluid(Function<? super BaseFlowingFluid.Properties, ? extends FlowingFluid> flowingFluidFactory) {
            this.flowingFluidFactory = flowingFluidFactory;
            return this;
        }


        /**
         * Provides a custom implementation for the {@link LiquidBlock liquid block}.
         */
        public FluidBase.Builder liquidBlock(BiFunction<? super FlowingFluid, ? super BlockBehaviour.Properties, ? extends LiquidBlock> liquidBlockFactory) {
            this.liquidBlockFactory = liquidBlockFactory;
            return this;
        }

        /**
         * Provides a custom implementation for the {@link BucketItem bucket item}.
         */
        public FluidBase.Builder bucketItem(BiFunction<? super Fluid, ? super Item.Properties, ? extends BucketItem> bucketItemFactory) {
            this.bucketItemFactory = bucketItemFactory;
            return this;
        }

        /**
         * Sets the {@link FluidType.Properties fluid type properties}.
         */
        public FluidBase.Builder properties(FluidType.Properties fluidTypeProperties) {
            this.fluidTypeProperties = fluidTypeProperties;
            return this;
        }
        
        /**
         * Runs an action on the current {@link FluidType.Properties fluid type properties}.
         */
        public FluidBase.Builder properties(UnaryOperator<FluidType.Properties> fluidTypePropertiesOp) {
            this.fluidTypeProperties = fluidTypePropertiesOp.apply(this.fluidTypeProperties);
            return this;
        }
        
        /**
         * Sets the {@link BlockBehaviour.Properties liquid block properties}.
         */
        public FluidBase.Builder blockProperties(BlockBehaviour.Properties blockProperties) {
            this.blockProperties = blockProperties;
            return this;
        }
        
        /**
         * Runs an action on the current {@link BlockBehaviour.Properties liquid block properties}.
         */
        public FluidBase.Builder blockProperties(UnaryOperator<BlockBehaviour.Properties> blockPropertiesOp) {
            this.blockProperties = blockPropertiesOp.apply(this.blockProperties);
            return this;
        }
        
        /**
         * Sets the {@link Item.Properties bucket item properties}.
         */
        public FluidBase.Builder bucketItemProperties(Item.Properties bucketItemProperties) {
            this.bucketItemProperties = bucketItemProperties;
            return this;
        }
        
        /**
         * Runs an action on the current {@link Item.Properties bucket item properties}.
         */
        public FluidBase.Builder bucketItemProperties(UnaryOperator<Item.Properties> bucketItemPropertiesOp) {
            this.bucketItemProperties = bucketItemPropertiesOp.apply(this.bucketItemProperties);
            return this;
        }

        /**
         * Runs an action on the current {@link BaseFlowingFluid.Properties fluid properties}.
         */
        public FluidBase.Builder fluidProperties(UnaryOperator<BaseFlowingFluid.Properties> fluidPropertiesOp) {
            this.fluidProperties = compose(this.fluidProperties, fluidPropertiesOp);
            return this;
        }
        
        private static <T> UnaryOperator<T> compose(UnaryOperator<T> a, UnaryOperator<T> b) {
            return t -> b.apply(a.apply(t));
        }
    }
}
