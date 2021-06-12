package io.github.noeppi_noeppi.libx.tmp;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.github.noeppi_noeppi.libx.mod.ModX;
import io.github.noeppi_noeppi.libx.mod.registration.Registerable;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.ForgeFlowingFluid;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * A {@link Registerable} that registers a {@link Fluid fluid}, a flowing fluid,
 * a {@link FlowingFluidBlock fluid block} and a {@link BucketItem bucket item}.
 */
public class FluidBase implements Registerable, IItemProvider {

    protected final ModX mod;

    private final Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Source> sourceFactory;
    private final Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Flowing> flowingFactory;
    private final UnaryOperator<FluidAttributes.Builder> attributes;

    private ForgeFlowingFluid.Source source;
    private ForgeFlowingFluid.Flowing flowing;
    private ForgeFlowingFluid.Properties properties;
    private final FlowingFluidBlock block;
    private final BucketItem bucket;

    /**
     * Creates a new Instance of FluidBase
     *
     * @see #FluidBase(ModX, Function, Function, UnaryOperator, AbstractBlock.Properties, Item.Properties)
     */
    public FluidBase(ModX mod) {
        this(mod, ForgeFlowingFluid.Source::new, ForgeFlowingFluid.Flowing::new, UnaryOperator.identity(), AbstractBlock.Properties.from(Blocks.WATER), defaultItemProps(mod));
    }

    /**
     * Creates a new Instance of FluidBase
     *
     * @see #FluidBase(ModX, Function, Function, UnaryOperator, AbstractBlock.Properties, Item.Properties)
     */
    public FluidBase(ModX mod, Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Source> sourceFactory, Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Flowing> flowingFactory) {
        this(mod, sourceFactory, flowingFactory, UnaryOperator.identity(), AbstractBlock.Properties.from(Blocks.WATER), defaultItemProps(mod));
    }

    /**
     * Creates a new Instance of FluidBase
     *
     * @see #FluidBase(ModX, Function, Function, UnaryOperator, AbstractBlock.Properties, Item.Properties)
     */
    public FluidBase(ModX mod, UnaryOperator<FluidAttributes.Builder> attributes) {
        this(mod, ForgeFlowingFluid.Source::new, ForgeFlowingFluid.Flowing::new, attributes, AbstractBlock.Properties.from(Blocks.WATER), defaultItemProps(mod));
    }

    /**
     * Creates a new Instance of FluidBase
     *
     * @see #FluidBase(ModX, Function, Function, UnaryOperator, AbstractBlock.Properties, Item.Properties)
     */
    public FluidBase(ModX mod, Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Source> sourceFactory, Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Flowing> flowingFactory, UnaryOperator<FluidAttributes.Builder> attributes) {
        this(mod, sourceFactory, flowingFactory, attributes, AbstractBlock.Properties.from(Blocks.WATER), defaultItemProps(mod));
    }

    /**
     * Creates a new Instance of FluidBase
     *
     * @see #FluidBase(ModX, Function, Function, UnaryOperator, AbstractBlock.Properties, Item.Properties)
     */
    public FluidBase(ModX mod, Item.Properties itemProperties) {
        this(mod, ForgeFlowingFluid.Source::new, ForgeFlowingFluid.Flowing::new, UnaryOperator.identity(), AbstractBlock.Properties.from(Blocks.WATER), itemProperties);
    }

    /**
     * Creates a new Instance of FluidBase
     *
     * @see #FluidBase(ModX, Function, Function, UnaryOperator, AbstractBlock.Properties, Item.Properties)
     */
    public FluidBase(ModX mod, Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Source> sourceFactory, Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Flowing> flowingFactory, Item.Properties itemProperties) {
        this(mod, sourceFactory, flowingFactory, UnaryOperator.identity(), AbstractBlock.Properties.from(Blocks.WATER), itemProperties);
    }

    /**
     * Creates a new Instance of FluidBase
     *
     * @see #FluidBase(ModX, Function, Function, UnaryOperator, AbstractBlock.Properties, Item.Properties)
     */
    public FluidBase(ModX mod, UnaryOperator<FluidAttributes.Builder> attributes, Item.Properties itemProperties) {
        this(mod, ForgeFlowingFluid.Source::new, ForgeFlowingFluid.Flowing::new, attributes, AbstractBlock.Properties.from(Blocks.WATER), itemProperties);
    }

    /**
     * Creates a new Instance of FluidBase
     *
     * @see #FluidBase(ModX, Function, Function, UnaryOperator, AbstractBlock.Properties, Item.Properties)
     */
    public FluidBase(ModX mod, Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Source> sourceFactory, Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Flowing> flowingFactory, UnaryOperator<FluidAttributes.Builder> attributes, Item.Properties itemProperties) {
        this(mod, sourceFactory, flowingFactory, attributes, AbstractBlock.Properties.from(Blocks.WATER), itemProperties);
    }

    /**
     * Creates a new Instance of FluidBase
     *
     * @see #FluidBase(ModX, Function, Function, UnaryOperator, AbstractBlock.Properties, Item.Properties)
     */
    public FluidBase(ModX mod, AbstractBlock.Properties blockProperties) {
        this(mod, ForgeFlowingFluid.Source::new, ForgeFlowingFluid.Flowing::new, UnaryOperator.identity(), blockProperties, defaultItemProps(mod));
    }

    /**
     * Creates a new Instance of FluidBase
     *
     * @see #FluidBase(ModX, Function, Function, UnaryOperator, AbstractBlock.Properties, Item.Properties)
     */
    public FluidBase(ModX mod, Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Source> sourceFactory, Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Flowing> flowingFactory, AbstractBlock.Properties blockProperties) {
        this(mod, sourceFactory, flowingFactory, UnaryOperator.identity(), blockProperties, defaultItemProps(mod));
    }

    /**
     * Creates a new Instance of FluidBase
     *
     * @see #FluidBase(ModX, Function, Function, UnaryOperator, AbstractBlock.Properties, Item.Properties)
     */
    public FluidBase(ModX mod, UnaryOperator<FluidAttributes.Builder> attributes, AbstractBlock.Properties blockProperties) {
        this(mod, ForgeFlowingFluid.Source::new, ForgeFlowingFluid.Flowing::new, attributes, blockProperties, defaultItemProps(mod));
    }

    /**
     * Creates a new Instance of FluidBase
     *
     * @see #FluidBase(ModX, Function, Function, UnaryOperator, AbstractBlock.Properties, Item.Properties)
     */
    public FluidBase(ModX mod, Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Source> sourceFactory, Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Flowing> flowingFactory, UnaryOperator<FluidAttributes.Builder> attributes, AbstractBlock.Properties blockProperties) {
        this(mod, sourceFactory, flowingFactory, attributes, blockProperties, defaultItemProps(mod));
    }

    /**
     * Creates a new Instance of FluidBase
     *
     * @see #FluidBase(ModX, Function, Function, UnaryOperator, AbstractBlock.Properties, Item.Properties)
     */
    public FluidBase(ModX mod, AbstractBlock.Properties blockProperties, Item.Properties itemProperties) {
        this(mod, ForgeFlowingFluid.Source::new, ForgeFlowingFluid.Flowing::new, UnaryOperator.identity(), blockProperties, itemProperties);
    }

    /**
     * Creates a new Instance of FluidBase
     *
     * @see #FluidBase(ModX, Function, Function, UnaryOperator, AbstractBlock.Properties, Item.Properties)
     */
    public FluidBase(ModX mod, Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Source> sourceFactory, Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Flowing> flowingFactory, AbstractBlock.Properties blockProperties, Item.Properties itemProperties) {
        this(mod, sourceFactory, flowingFactory, UnaryOperator.identity(), blockProperties, itemProperties);
    }

    /**
     * Creates a new Instance of FluidBase
     *
     * @see #FluidBase(ModX, Function, Function, UnaryOperator, AbstractBlock.Properties, Item.Properties)
     */
    public FluidBase(ModX mod, UnaryOperator<FluidAttributes.Builder> attributes, AbstractBlock.Properties blockProperties, Item.Properties itemProperties) {
        this(mod, ForgeFlowingFluid.Source::new, ForgeFlowingFluid.Flowing::new, attributes, blockProperties, itemProperties);
    }

    /**
     * Creates a new Instance of FluidBase
     *
     * @param sourceFactory   A factory to create a still fluid from the fluids properties. In most cases this will just be a constructor reference.
     * @param flowingFactory  A factory to create a flowing fluid from the fluids properties. In most cases this will just be a constructor reference.
     * @param attributes      A function to modify the attribute builder to alter the fluids attributes
     * @param blockProperties The properties for the fluids block
     * @param itemProperties  The properties for the bucket item
     */
    public FluidBase(ModX mod, Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Source> sourceFactory, Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Flowing> flowingFactory, UnaryOperator<FluidAttributes.Builder> attributes, AbstractBlock.Properties blockProperties, Item.Properties itemProperties) {
        this.mod = mod;
        this.sourceFactory = sourceFactory;
        this.flowingFactory = flowingFactory;
        this.attributes = attributes;
        this.block = new FlowingFluidBlock(this::getSource, AbstractBlock.Properties.from(Blocks.WATER));
        this.bucket = new BucketItem(this::getSource, new Item.Properties().maxStackSize(1).group(mod.tab)) {

            @Override
            public ItemStack getContainerItem(ItemStack stack) {
                return new ItemStack(Items.BUCKET);
            }

            @Nonnull
            @Override
            protected String getDefaultTranslationKey() {
                return "libx.tooltip.fluidbase.bucket";
            }

            @Nonnull
            @Override
            public ITextComponent getDisplayName(@Nonnull ItemStack stack) {
                return new TranslationTextComponent("libx.tooltip.fluidbase.bucket", FluidBase.this.getFluid().getAttributes().getDisplayName(new FluidStack(this.getFluid(), FluidAttributes.BUCKET_VOLUME)));
            }

            @Nonnull
            @Override
            public ITextComponent getName() {
                return new TranslationTextComponent("libx.tooltip.fluidbase.bucket", FluidBase.this.getFluid().getAttributes().getDisplayName(new FluidStack(this.getFluid(), FluidAttributes.BUCKET_VOLUME)));
            }
        };
    }

    @Nonnull
    public ForgeFlowingFluid.Source getFluid() {
        return this.getSource();
    }

    @Nonnull
    public ForgeFlowingFluid.Source getSource() {
        return Objects.requireNonNull(this.source, "FluidBase has not yet been registered.");
    }

    @Nonnull
    public ForgeFlowingFluid.Flowing getFlowing() {
        return Objects.requireNonNull(this.flowing, "FluidBase has not yet been registered.");
    }

    @Nonnull
    public FlowingFluidBlock getBlock() {
        return Objects.requireNonNull(this.block, "FluidBase has not yet been registered.");
    }

    @Nonnull
    public BucketItem getBucket() {
        return Objects.requireNonNull(this.bucket, "FluidBase has not yet been registered.");
    }

    @Nonnull
    public ForgeFlowingFluid.Properties getProperties() {
        return Objects.requireNonNull(this.properties, "FluidBase has not yet been registered.");
    }

    @Nonnull
    @Override
    public Item asItem() {
        return this.bucket;
    }

    @Override
    public Set<Object> getAdditionalRegisters(ResourceLocation id) {
        this.init(id);
        return ImmutableSet.of(this.source, this.block);
    }

    @Override
    public Map<String, Object> getNamedAdditionalRegisters(ResourceLocation id) {
        this.init(id);
        return ImmutableMap.of(
                "flowing", this.flowing,
                "bucket", this.bucket
        );
    }

    @Override
    public void registerClient(ResourceLocation id, Consumer<Runnable> defer) {
        Registerable.super.registerClient(id, defer);
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

    private static Item.Properties defaultItemProps(ModX mod) {
        if (mod.tab != null) {
            return new Item.Properties().group(mod.tab);
        } else {
            return new Item.Properties();
        }
    }
}
