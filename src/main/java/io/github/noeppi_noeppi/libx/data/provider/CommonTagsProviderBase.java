package io.github.noeppi_noeppi.libx.data.provider;

import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.block.Block;
import net.minecraft.data.*;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A provider for {@link BlockTags block}, {@link ItemTags item} and {@link FluidTags fluid} tags.
 * You can set your tags in {@link #setup() setup}. With {@link #defaultItemTags(Item) defaultItemTags},
 * {@link #defaultBlockTags(Block) defaultBlockTags} and {@link #defaultFluidTags(Fluid) defaultFluidTags},
 * you can add default tags that can be retrieved from the element.
 */
public abstract class CommonTagsProviderBase implements IDataProvider {

    protected final ModX mod;
    protected final DataGenerator generator;
    protected final ExistingFileHelper fileHelper;

    private final BlockTagProviderBase blockTags;
    private final ItemTagProviderBase itemTags;
    private final FluidTagProviderBase fluidTags;

    private boolean isSetup = false;
    private final List<Pair<ITag.INamedTag<Fluid>, ITag.INamedTag<Block>>> fluidCopies = new ArrayList<>();

    /**
     * Creates a new CommonTagsProviderBase
     */
    public CommonTagsProviderBase(ModX mod, DataGenerator generator, ExistingFileHelper fileHelper) {
        this.mod = mod;
        this.generator = generator;
        this.fileHelper = fileHelper;
        this.blockTags = new BlockTagProviderBase(generator, mod.modid, fileHelper);
        this.itemTags = new ItemTagProviderBase(generator, mod.modid, fileHelper, this.blockTags);
        this.fluidTags = new FluidTagProviderBase(generator, mod.modid, fileHelper);
        generator.addProvider(this.blockTags);
        generator.addProvider(this.itemTags);
        generator.addProvider(this.fluidTags);
    }

    public abstract void setup();

    private void doSetup() {
        this.setup();
        ForgeRegistries.BLOCKS.getValues().stream()
                .filter(i -> CommonTagsProviderBase.this.mod.modid.equals(Objects.requireNonNull(i.getRegistryName()).getNamespace()))
                .forEach(CommonTagsProviderBase.this::defaultBlockTags);
        ForgeRegistries.ITEMS.getValues().stream()
                .filter(i -> CommonTagsProviderBase.this.mod.modid.equals(Objects.requireNonNull(i.getRegistryName()).getNamespace()))
                .forEach(CommonTagsProviderBase.this::defaultItemTags);
        ForgeRegistries.FLUIDS.getValues().stream()
                .filter(i -> CommonTagsProviderBase.this.mod.modid.equals(Objects.requireNonNull(i.getRegistryName()).getNamespace()))
                .forEach(CommonTagsProviderBase.this::defaultFluidTags);
    }

    /**
     * Adds default {@link ItemTags item tags} to an {@link Item}
     */
    public void defaultItemTags(Item item) {

    }

    /**
     * Adds default {@link BlockTags item tags} to a {@link Block}
     */
    public void defaultBlockTags(Block item) {

    }

    /**
     * Adds default {@link FluidTags item tags} to a {@link Fluid}
     */
    public void defaultFluidTags(Fluid item) {

    }

    /**
     * Gets a {@link TagsProvider.Builder tag builder} for an {@link Item}
     */
    public TagsProvider.Builder<Item> item(ITag.INamedTag<Item> tag) {
        return this.itemTags.getOrCreateBuilder(tag);
    }

    /**
     * Gets a {@link TagsProvider.Builder tag builder} for a {@link Block}
     */
    public TagsProvider.Builder<Block> block(ITag.INamedTag<Block> tag) {
        return this.blockTags.getOrCreateBuilder(tag);
    }

    /**
     * Gets a {@link TagsProvider.Builder tag builder} for a {@link Fluid}
     */
    public TagsProvider.Builder<Fluid> fluid(ITag.INamedTag<Fluid> tag) {
        return this.fluidTags.getOrCreateBuilder(tag);
    }

    /**
     * Copies all entries from a block tag to an item tag.
     */
    public void copyBlock(ITag.INamedTag<Block> from, ITag.INamedTag<Item> to) {
        this.itemTags.copy(from, to);
    }

    /**
     * Copies all entries from a fluid tag to a block tag.
     */
    public void copyFluid(ITag.INamedTag<Fluid> from, ITag.INamedTag<Block> to) {
        this.fluidCopies.add(Pair.of(from, to));
    }

    @Nonnull
    @Override
    public final String getName() {
        return this.mod.modid + " common tags";
    }

    @Override
    public void act(@Nonnull DirectoryCache cache) {
        // We don't do anything here, everything is done by the three child providers
    }

    private class BlockTagProviderBase extends BlockTagsProvider {

        private Map<ResourceLocation, ITag.Builder> tagCache;

        protected BlockTagProviderBase(DataGenerator generator, String modid, ExistingFileHelper fileHelper) {
            super(generator, modid, fileHelper);
        }

        @Override
        protected void registerTags() {
            if (!CommonTagsProviderBase.this.isSetup) {
                CommonTagsProviderBase.this.isSetup = true;
                CommonTagsProviderBase.this.doSetup();
            } else if (this.tagCache != null) {
                this.tagToBuilder.putAll(this.tagCache);
            }
            // Add fluid copies
            for (Pair<ITag.INamedTag<Fluid>, ITag.INamedTag<Block>> copy : CommonTagsProviderBase.this.fluidCopies) {
                TagsProvider.Builder<Block> builder = this.getOrCreateBuilder(copy.getRight());
                for (ResourceLocation entry : CommonTagsProviderBase.this.fluidTags.getTagInfo(copy.getLeft())) {
                    Fluid fluid = ForgeRegistries.FLUIDS.getValue(entry);
                    if (fluid != null) {
                        builder.add(fluid.getDefaultState().getBlockState().getBlock());
                    }
                }
            }
        }

        @Override
        public void act(@Nonnull DirectoryCache cache) {
            this.tagCache = new HashMap<>(this.tagToBuilder);
            super.act(cache);
        }

        @Override
        @Nonnull
        public TagsProvider.Builder<Block> getOrCreateBuilder(@Nonnull ITag.INamedTag<Block> tag) {
            return super.getOrCreateBuilder(tag);
        }

        @Nonnull
        @Override
        public String getName() {
            return CommonTagsProviderBase.this.mod.modid + " common block tags";
        }
    }

    private class ItemTagProviderBase extends ItemTagsProvider {

        private Map<ResourceLocation, ITag.Builder> tagCache;

        protected ItemTagProviderBase(DataGenerator generator, String modid, ExistingFileHelper fileHelper, BlockTagProviderBase blockTags) {
            super(generator, blockTags, modid, fileHelper);
        }

        @Override
        protected void registerTags() {
            if (!CommonTagsProviderBase.this.isSetup) {
                CommonTagsProviderBase.this.isSetup = true;
                CommonTagsProviderBase.this.doSetup();
            } else if (this.tagCache != null) {
                this.tagToBuilder.putAll(this.tagCache);
            }
        }

        @Override
        public void act(@Nonnull DirectoryCache cache) {
            this.tagCache = new HashMap<>(this.tagToBuilder);
            super.act(cache);
        }

        @Override
        @Nonnull
        public TagsProvider.Builder<Item> getOrCreateBuilder(@Nonnull ITag.INamedTag<Item> tag) {
            return super.getOrCreateBuilder(tag);
        }

        @Override
        public void copy(@Nonnull ITag.INamedTag<Block> blockTag, @Nonnull ITag.INamedTag<Item> itemTag) {
            super.copy(blockTag, itemTag);
        }

        @Nonnull
        @Override
        public String getName() {
            return CommonTagsProviderBase.this.mod.modid + " common item tags";
        }
    }

    private class FluidTagProviderBase extends FluidTagsProvider {

        private Map<ResourceLocation, ITag.Builder> tagCache;

        protected FluidTagProviderBase(DataGenerator generator, String modid, ExistingFileHelper fileHelper) {
            super(generator, modid, fileHelper);
        }

        @Override
        protected void registerTags() {
            if (!CommonTagsProviderBase.this.isSetup) {
                CommonTagsProviderBase.this.isSetup = true;
                CommonTagsProviderBase.this.doSetup();
            } else if (this.tagCache != null) {
                this.tagToBuilder.putAll(this.tagCache);
            }
        }

        @Override
        public void act(@Nonnull DirectoryCache cache) {
            this.tagCache = new HashMap<>(this.tagToBuilder);
            super.act(cache);
        }

        @Override
        @Nonnull
        public TagsProvider.Builder<Fluid> getOrCreateBuilder(@Nonnull ITag.INamedTag<Fluid> tag) {
            return super.getOrCreateBuilder(tag);
        }

        public List<ResourceLocation> getTagInfo(ITag.INamedTag<Fluid> tag) {
            TagsProvider.Builder<Fluid> builder = this.getOrCreateBuilder(tag);
            return builder.getInternalBuilder().getProxyStream()
                    .filter(p -> p.getEntry() instanceof ITag.ItemEntry)
                    .map(p -> new ResourceLocation(((ITag.ItemEntry) p.getEntry()).toString()))
                    .collect(Collectors.toList());
        }

        @Nonnull
        @Override
        public String getName() {
            return CommonTagsProviderBase.this.mod.modid + " common fluid tags";
        }
    }
}
