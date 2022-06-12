package org.moddingx.libx.base;

import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.registration.Registerable;
import org.moddingx.libx.registration.RegistrationContext;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * A {@link Registerable} that registers a {@link Fluid fluid}, a flowing fluid,
 * a {@link LiquidBlock liquid block} and a {@link BucketItem bucket item}.
 */
public class FluidBase implements Registerable, ItemLike {

    protected final ModX mod;

    private final Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Source> sourceFactory;
    private final Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Flowing> flowingFactory;
    private final UnaryOperator<FluidAttributes.Builder> attributes;

    private ForgeFlowingFluid.Source source;
    private ForgeFlowingFluid.Flowing flowing;
    private ForgeFlowingFluid.Properties properties;
    private final LiquidBlock block;
    private final BucketItem bucket;

    /**
     * Creates a new instance of FluidBase.
     *
     * @see #FluidBase(ModX, Function, Function, UnaryOperator, BlockBehaviour.Properties, Item.Properties)
     */
    public FluidBase(ModX mod) {
        this(mod, ForgeFlowingFluid.Source::new, ForgeFlowingFluid.Flowing::new, UnaryOperator.identity(), BlockBehaviour.Properties.copy(Blocks.WATER), defaultItemProperties(mod));
    }

    /**
     * Creates a new instance of FluidBase.
     *
     * @see #FluidBase(ModX, Function, Function, UnaryOperator, BlockBehaviour.Properties, Item.Properties)
     */
    public FluidBase(ModX mod, Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Source> sourceFactory, Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Flowing> flowingFactory) {
        this(mod, sourceFactory, flowingFactory, UnaryOperator.identity(), BlockBehaviour.Properties.copy(Blocks.WATER), defaultItemProperties(mod));
    }

    /**
     * Creates a new instance of FluidBase.
     *
     * @see #FluidBase(ModX, Function, Function, UnaryOperator, BlockBehaviour.Properties, Item.Properties)
     */
    public FluidBase(ModX mod, UnaryOperator<FluidAttributes.Builder> attributes) {
        this(mod, ForgeFlowingFluid.Source::new, ForgeFlowingFluid.Flowing::new, attributes, BlockBehaviour.Properties.copy(Blocks.WATER), defaultItemProperties(mod));
    }

    /**
     * Creates a new instance of FluidBase.
     *
     * @see #FluidBase(ModX, Function, Function, UnaryOperator, BlockBehaviour.Properties, Item.Properties)
     */
    public FluidBase(ModX mod, Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Source> sourceFactory, Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Flowing> flowingFactory, UnaryOperator<FluidAttributes.Builder> attributes) {
        this(mod, sourceFactory, flowingFactory, attributes, BlockBehaviour.Properties.copy(Blocks.WATER), defaultItemProperties(mod));
    }

    /**
     * Creates a new instance of FluidBase.
     *
     * @see #FluidBase(ModX, Function, Function, UnaryOperator, BlockBehaviour.Properties, Item.Properties)
     */
    public FluidBase(ModX mod, Item.Properties itemProperties) {
        this(mod, ForgeFlowingFluid.Source::new, ForgeFlowingFluid.Flowing::new, UnaryOperator.identity(), BlockBehaviour.Properties.copy(Blocks.WATER), itemProperties);
    }

    /**
     * Creates a new instance of FluidBase.
     *
     * @see #FluidBase(ModX, Function, Function, UnaryOperator, BlockBehaviour.Properties, Item.Properties)
     */
    public FluidBase(ModX mod, Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Source> sourceFactory, Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Flowing> flowingFactory, Item.Properties itemProperties) {
        this(mod, sourceFactory, flowingFactory, UnaryOperator.identity(), BlockBehaviour.Properties.copy(Blocks.WATER), itemProperties);
    }

    /**
     * Creates a new instance of FluidBase.
     *
     * @see #FluidBase(ModX, Function, Function, UnaryOperator, BlockBehaviour.Properties, Item.Properties)
     */
    public FluidBase(ModX mod, UnaryOperator<FluidAttributes.Builder> attributes, Item.Properties itemProperties) {
        this(mod, ForgeFlowingFluid.Source::new, ForgeFlowingFluid.Flowing::new, attributes, BlockBehaviour.Properties.copy(Blocks.WATER), itemProperties);
    }

    /**
     * Creates a new instance of FluidBase.
     *
     * @see #FluidBase(ModX, Function, Function, UnaryOperator, BlockBehaviour.Properties, Item.Properties)
     */
    public FluidBase(ModX mod, Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Source> sourceFactory, Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Flowing> flowingFactory, UnaryOperator<FluidAttributes.Builder> attributes, Item.Properties itemProperties) {
        this(mod, sourceFactory, flowingFactory, attributes, BlockBehaviour.Properties.copy(Blocks.WATER), itemProperties);
    }

    /**
     * Creates a new instance of FluidBase.
     *
     * @see #FluidBase(ModX, Function, Function, UnaryOperator, BlockBehaviour.Properties, Item.Properties)
     */
    public FluidBase(ModX mod, BlockBehaviour.Properties blockProperties) {
        this(mod, ForgeFlowingFluid.Source::new, ForgeFlowingFluid.Flowing::new, UnaryOperator.identity(), blockProperties, defaultItemProperties(mod));
    }

    /**
     * Creates a new instance of FluidBase.
     *
     * @see #FluidBase(ModX, Function, Function, UnaryOperator, BlockBehaviour.Properties, Item.Properties)
     */
    public FluidBase(ModX mod, Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Source> sourceFactory, Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Flowing> flowingFactory, BlockBehaviour.Properties blockProperties) {
        this(mod, sourceFactory, flowingFactory, UnaryOperator.identity(), blockProperties, defaultItemProperties(mod));
    }

    /**
     * Creates a new instance of FluidBase.
     *
     * @see #FluidBase(ModX, Function, Function, UnaryOperator, BlockBehaviour.Properties, Item.Properties)
     */
    public FluidBase(ModX mod, UnaryOperator<FluidAttributes.Builder> attributes, BlockBehaviour.Properties blockProperties) {
        this(mod, ForgeFlowingFluid.Source::new, ForgeFlowingFluid.Flowing::new, attributes, blockProperties, defaultItemProperties(mod));
    }

    /**
     * Creates a new instance of FluidBase.
     *
     * @see #FluidBase(ModX, Function, Function, UnaryOperator, BlockBehaviour.Properties, Item.Properties)
     */
    public FluidBase(ModX mod, Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Source> sourceFactory, Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Flowing> flowingFactory, UnaryOperator<FluidAttributes.Builder> attributes, BlockBehaviour.Properties blockProperties) {
        this(mod, sourceFactory, flowingFactory, attributes, blockProperties, defaultItemProperties(mod));
    }

    /**
     * Creates a new instance of FluidBase.
     *
     * @see #FluidBase(ModX, Function, Function, UnaryOperator, BlockBehaviour.Properties, Item.Properties)
     */
    public FluidBase(ModX mod, BlockBehaviour.Properties blockProperties, Item.Properties itemProperties) {
        this(mod, ForgeFlowingFluid.Source::new, ForgeFlowingFluid.Flowing::new, UnaryOperator.identity(), blockProperties, itemProperties);
    }

    /**
     * Creates a new instance of FluidBase.
     *
     * @see #FluidBase(ModX, Function, Function, UnaryOperator, BlockBehaviour.Properties, Item.Properties)
     */
    public FluidBase(ModX mod, Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Source> sourceFactory, Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Flowing> flowingFactory, BlockBehaviour.Properties blockProperties, Item.Properties itemProperties) {
        this(mod, sourceFactory, flowingFactory, UnaryOperator.identity(), blockProperties, itemProperties);
    }

    /**
     * Creates a new instance of FluidBase.
     *
     * @see #FluidBase(ModX, Function, Function, UnaryOperator, BlockBehaviour.Properties, Item.Properties)
     */
    public FluidBase(ModX mod, UnaryOperator<FluidAttributes.Builder> attributes, BlockBehaviour.Properties blockProperties, Item.Properties itemProperties) {
        this(mod, ForgeFlowingFluid.Source::new, ForgeFlowingFluid.Flowing::new, attributes, blockProperties, itemProperties);
    }

    /**
     * Creates a new instance of FluidBase.
     *
     * @param sourceFactory   A factory to create a still fluid from the fluids properties. In most cases this will just be a constructor reference.
     * @param flowingFactory  A factory to create a flowing fluid from the fluids properties. In most cases this will just be a constructor reference.
     * @param attributes      A function to modify the attribute builder to alter the fluids attributes.
     * @param blockProperties The properties for the fluids block.
     * @param itemProperties  The properties for the bucket item.
     */
    public FluidBase(ModX mod, Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Source> sourceFactory, Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Flowing> flowingFactory, UnaryOperator<FluidAttributes.Builder> attributes, BlockBehaviour.Properties blockProperties, Item.Properties itemProperties) {
        this.mod = mod;
        this.sourceFactory = sourceFactory;
        this.flowingFactory = flowingFactory;
        this.attributes = attributes;
        this.block = new LiquidBlock(this::getSource, blockProperties);
        this.bucket = new BucketItem(this::getSource, itemProperties.stacksTo(1)) {

            @Override
            public ItemStack getContainerItem(ItemStack stack) {
                return new ItemStack(Items.BUCKET);
            }

            @Nonnull
            @Override
            protected String getOrCreateDescriptionId() {
                return "libx.tooltip.fluidbase.bucket";
            }

            @Nonnull
            @Override
            public Component getName(@Nonnull ItemStack stack) {
                return new TranslatableComponent("libx.tooltip.fluidbase.bucket", FluidBase.this.getFluid().getAttributes().getDisplayName(new FluidStack(this.getFluid(), FluidAttributes.BUCKET_VOLUME)));
            }

            @Nonnull
            @Override
            public Component getDescription() {
                return new TranslatableComponent("libx.tooltip.fluidbase.bucket", FluidBase.this.getFluid().getAttributes().getDisplayName(new FluidStack(this.getFluid(), FluidAttributes.BUCKET_VOLUME)));
            }
        };
    }

    /**
     * Gets the fluid. This should be used in recipes or {@link IFluidHandler fluid handlers}.
     * 
     * @see #getSource()
     */
    @Nonnull
    public Fluid getFluid() {
        return this.getSource();
    }

    /**
     * Gets the source fluid. In most cases you should use {@link #getFluid()}.
     * 
     * @see #getFluid()
     */
    @Nonnull
    public ForgeFlowingFluid.Source getSource() {
        return Objects.requireNonNull(this.source, "FluidBase has not yet been registered.");
    }

    /**
     * Gets the flowing fluid.
     * 
     * @see #getSource()
     */
    @Nonnull
    public ForgeFlowingFluid.Flowing getFlowing() {
        return Objects.requireNonNull(this.flowing, "FluidBase has not yet been registered.");
    }

    /**
     * Gets the fluid block for this fluid.
     */
    @Nonnull
    public LiquidBlock getBlock() {
        return Objects.requireNonNull(this.block, "FluidBase has not yet been registered.");
    }

    /**
     * Gets the bucket item for this fluid.
     */
    @Nonnull
    public BucketItem getBucket() {
        return Objects.requireNonNull(this.bucket, "FluidBase has not yet been registered.");
    }

    /**
     * Gets the properties for this fluid.
     */
    @Nonnull
    public ForgeFlowingFluid.Properties getProperties() {
        return Objects.requireNonNull(this.properties, "FluidBase has not yet been registered.");
    }

    /**
     * Same as {@link #getBucket()}
     */
    @Nonnull
    @Override
    public Item asItem() {
        return this.getBucket();
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void registerAdditional(RegistrationContext ctx, EntryCollector builder) {
        this.init(ctx.id());
        builder.register(Registry.FLUID_REGISTRY, this.source);
        builder.registerNamed(Registry.FLUID_REGISTRY, "flowing", this.flowing);
        builder.register(Registry.BLOCK_REGISTRY, this.block);
        builder.registerNamed(Registry.ITEM_REGISTRY, "bucket", this.bucket);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void initTracking(RegistrationContext ctx, TrackingCollector builder) throws ReflectiveOperationException {
        this.init(ctx.id());
        builder.track(ForgeRegistries.FLUIDS, FluidBase.class.getDeclaredField("source"));
        builder.trackNamed(ForgeRegistries.FLUIDS, "flowing", FluidBase.class.getDeclaredField("flowing"));
        builder.track(ForgeRegistries.BLOCKS, FluidBase.class.getDeclaredField("block"));
        builder.trackNamed(ForgeRegistries.ITEMS, "bucket", FluidBase.class.getDeclaredField("bucket"));
    }

    private void init(ResourceLocation id) {
        if (this.properties == null) {
            FluidAttributes.Builder baseAttributes = FluidAttributes.builder(
                    new ResourceLocation(id.getNamespace(), "block/" + id.getPath()),
                    new ResourceLocation(id.getNamespace(), "block/" + id.getPath())
            );
            baseAttributes.translationKey("fluid." + id.getNamespace() + "." + id.getPath());
            this.properties = new ForgeFlowingFluid.Properties(this::getSource, this::getFlowing, this.attributes.apply(baseAttributes))
                    .block(this::getBlock)
                    .bucket(this::getBucket);
            this.source = this.sourceFactory.apply(this.properties);
            this.flowing = this.flowingFactory.apply(this.properties);
        }
    }

    private static Item.Properties defaultItemProperties(ModX mod) {
        if (mod.tab != null) {
            return new Item.Properties().tab(mod.tab);
        } else {
            return new Item.Properties();
        }
    }
}
