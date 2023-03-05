package org.moddingx.libx.base;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
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
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.moddingx.libx.impl.base.fluid.DefaultClientExtensions;
import org.moddingx.libx.impl.base.fluid.FluidTypeBase;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.registration.Registerable;
import org.moddingx.libx.registration.RegistrationContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A {@link Registerable} that registers a {@link Fluid fluid}, a flowing fluid,
 * a {@link LiquidBlock liquid block} and a {@link BucketItem bucket item}.
 */
public class FluidBase implements Registerable, ItemLike {

    protected final ModX mod;

    private final Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Source> sourceFactory;
    private final Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Flowing> flowingFactory;
    private final FluidType.Properties properties;

    @Nullable
    private final Supplier<Supplier<IClientFluidTypeExtensions>> clientExtensions;

    private boolean initialised;

    private ForgeFlowingFluid.Source source;
    private ForgeFlowingFluid.Flowing flowing;
    private FluidType type;
    private ForgeFlowingFluid.Properties fluidProperties;
    private final LiquidBlock block;
    private final BucketItem bucket;

    private FluidBase(ModX mod, Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Source> sourceFactory, Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Flowing> flowingFactory, FluidType.Properties properties, @Nullable Supplier<Supplier<IClientFluidTypeExtensions>> clientExtensions, BlockBehaviour.Properties blockProperties, Item.Properties itemProperties) {
        this.mod = mod;
        this.sourceFactory = sourceFactory;
        this.flowingFactory = flowingFactory;
        this.properties = properties;
        this.clientExtensions = clientExtensions;

        this.initialised = false;

        this.block = new LiquidBlock(this::getSource, blockProperties);
        this.bucket = new BucketItem(this::getSource, itemProperties.stacksTo(1)) {

            @Override
            public ItemStack getCraftingRemainingItem(ItemStack stack) {
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
                return Component.translatable("libx.tooltip.fluidbase.bucket", FluidBase.this.getFluid().getFluidType().getDescription(new FluidStack(this.getFluid(), FluidType.BUCKET_VOLUME)));
            }

            @Nonnull
            @Override
            public Component getDescription() {
                return Component.translatable("libx.tooltip.fluidbase.bucket", FluidBase.this.getFluid().getFluidType().getDescription(new FluidStack(this.getFluid(), FluidType.BUCKET_VOLUME)));
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
     * Gets the fluid type.
     */
    @Nonnull
    public FluidType getType() {
        return Objects.requireNonNull(this.type, "FluidBase has not yet been registered.");
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
        return Objects.requireNonNull(this.fluidProperties, "FluidBase has not yet been registered.");
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
        builder.register(Registries.FLUID, this.source);
        builder.registerNamed(Registries.FLUID, "flowing", this.flowing);
        builder.register(Registries.BLOCK, this.block);
        builder.registerNamed(Registries.ITEM, "bucket", this.bucket);
        builder.registerNamed(ForgeRegistries.Keys.FLUID_TYPES, "type", this.type);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void initTracking(RegistrationContext ctx, TrackingCollector builder) throws ReflectiveOperationException {
        this.init(ctx.id());
        builder.track(ForgeRegistries.FLUIDS, FluidBase.class.getDeclaredField("source"));
        builder.trackNamed(ForgeRegistries.FLUIDS, "flowing", FluidBase.class.getDeclaredField("flowing"));
        builder.track(ForgeRegistries.BLOCKS, FluidBase.class.getDeclaredField("block"));
        builder.trackNamed(ForgeRegistries.ITEMS, "bucket", FluidBase.class.getDeclaredField("bucket"));
        builder.trackNamed(ForgeRegistries.ITEMS, "type", FluidBase.class.getDeclaredField("type"));
    }

    private synchronized void init(ResourceLocation id) {
        if (!this.initialised) {
            this.initialised = true;
            this.properties.descriptionId("fluid." + id.getNamespace() + "." + id.getPath());
            this.type = new FluidTypeBase(this.properties, this.clientExtensions != null ? this.clientExtensions : () -> () -> new DefaultClientExtensions(
                    new ResourceLocation(id.getNamespace(), "block/" + id.getPath())
            ));

            this.fluidProperties = new ForgeFlowingFluid.Properties(this::getType, this::getSource, this::getFlowing)
                    .block(this::getBlock)
                    .bucket(this::getBucket);

            this.source = this.sourceFactory.apply(this.fluidProperties);
            this.flowing = this.flowingFactory.apply(this.fluidProperties);
        }
    }

    public static Builder builder(ModX mod) {
        return new Builder(mod);
    }

    public static class Builder {

        private final ModX mod;

        private Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Source> sourceFactory;
        private Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Flowing> flowingFactory;

        @Nullable
        private Supplier<Supplier<IClientFluidTypeExtensions>> clientExtensions;
        private FluidType.Properties properties;
        private BlockBehaviour.Properties blockProperties;
        private Item.Properties itemProperties;

        private Builder(ModX mod) {
            this.mod = mod;
            this.sourceFactory = ForgeFlowingFluid.Source::new;
            this.flowingFactory = ForgeFlowingFluid.Flowing::new;
            this.clientExtensions = null;
            this.properties = FluidType.Properties.create();
            this.blockProperties = BlockBehaviour.Properties.copy(Blocks.WATER);
                this.itemProperties = new Item.Properties();
        }

        public Builder sourceFactory(Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Source> sourceFactory) {
            this.sourceFactory = sourceFactory;
            return this;
        }

        public Builder flowingFactory(Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid.Flowing> flowingFactory) {
            this.flowingFactory = flowingFactory;
            return this;
        }

        public Builder clientExtensions(Supplier<Supplier<IClientFluidTypeExtensions>> clientExtensions) {
            this.clientExtensions = clientExtensions;
            return this;
        }

        public Builder properties(FluidType.Properties properties) {
            this.properties = properties;
            return this;
        }

        public Builder properties(Consumer<FluidType.Properties> action) {
            action.accept(this.properties);
            return this;
        }

        public Builder blockProperties(BlockBehaviour.Properties blockProperties) {
            this.blockProperties = blockProperties;
            return this;
        }

        public Builder blockProperties(Consumer<BlockBehaviour.Properties> action) {
            action.accept(this.blockProperties);
            return this;
        }

        public Builder itemProperties(Item.Properties itemProperties) {
            this.itemProperties = itemProperties;
            return this;
        }

        public Builder itemProperties(Consumer<Item.Properties> action) {
            action.accept(this.itemProperties);
            return this;
        }

        public FluidBase build() {
            return new FluidBase(
                    this.mod, this.sourceFactory, this.flowingFactory, this.properties,
                    this.clientExtensions, this.blockProperties, this.itemProperties
            );
        }
    }
}
