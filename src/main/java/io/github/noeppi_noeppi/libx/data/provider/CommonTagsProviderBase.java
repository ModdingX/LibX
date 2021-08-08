package io.github.noeppi_noeppi.libx.data.provider;

import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
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
public abstract class CommonTagsProviderBase implements DataProvider {

    protected final ModX mod;
    protected final DataGenerator generator;
    protected final ExistingFileHelper fileHelper;

    private final BlockTagProviderBase blockTags;
    private final ItemTagProviderBase itemTags;
    private final FluidTagProviderBase fluidTags;

    private boolean isSetup = false;
    // Copies must happen at last so we store them
    private final List<Runnable> itemCopies = new ArrayList<>();
    private final List<Pair<Tag.Named<Fluid>, Tag.Named<Block>>> fluidCopies = new ArrayList<>();

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
     * Gets a {@link TagsProvider.TagAppender tag appender} for an {@link Item}
     */
    public TagsProvider.TagAppender<Item> item(Tag.Named<Item> tag) {
        return this.itemTags.tag(tag);
    }

    /**
     * Gets a {@link TagsProvider.TagAppender tag appender} for a {@link Block}
     */
    public TagsProvider.TagAppender<Block> block(Tag.Named<Block> tag) {
        return this.blockTags.tag(tag);
    }

    /**
     * Gets a {@link TagsProvider.TagAppender tag appender} for a {@link Fluid}
     */
    public TagsProvider.TagAppender<Fluid> fluid(Tag.Named<Fluid> tag) {
        return this.fluidTags.tag(tag);
    }

    /**
     * Copies all entries from a block tag to an item tag.
     */
    public void copyBlock(Tag.Named<Block> from, Tag.Named<Item> to) {
        this.itemCopies.add(() -> this.itemTags.copy(from, to));
    }

    /**
     * Copies all entries from a fluid tag to a block tag.
     */
    public void copyFluid(Tag.Named<Fluid> from, Tag.Named<Block> to) {
        this.fluidCopies.add(Pair.of(from, to));
    }

    @Nonnull
    @Override
    public final String getName() {
        return this.mod.modid + " common tags";
    }

    @Override
    public void run(@Nonnull HashCache cache) {
        // We don't do anything here, everything is done by the three child providers
    }

    private class BlockTagProviderBase extends BlockTagsProvider {

        private Map<ResourceLocation, Tag.Builder> tagCache;

        protected BlockTagProviderBase(DataGenerator generator, String modid, ExistingFileHelper fileHelper) {
            super(generator, modid, fileHelper);
        }

        @Override
        protected void addTags() {
            if (!CommonTagsProviderBase.this.isSetup) {
                CommonTagsProviderBase.this.isSetup = true;
                CommonTagsProviderBase.this.doSetup();
            } else if (this.tagCache != null) {
                this.builders.putAll(this.tagCache);
            }
            // Add fluid copies
            for (Pair<Tag.Named<Fluid>, Tag.Named<Block>> copy : CommonTagsProviderBase.this.fluidCopies) {
                TagsProvider.TagAppender<Block> builder = this.tag(copy.getRight());
                for (ResourceLocation entry : CommonTagsProviderBase.this.fluidTags.getTagInfo(copy.getLeft())) {
                    Fluid fluid = ForgeRegistries.FLUIDS.getValue(entry);
                    if (fluid != null) {
                        builder.add(fluid.defaultFluidState().createLegacyBlock().getBlock());
                    }
                }
            }
        }

        @Override
        public void run(@Nonnull HashCache cache) {
            this.tagCache = new HashMap<>(this.builders);
            super.run(cache);
        }

        @Override
        @Nonnull
        public TagsProvider.TagAppender<Block> tag(@Nonnull Tag.Named<Block> tag) {
            return super.tag(tag);
        }

        @Nonnull
        @Override
        public String getName() {
            return CommonTagsProviderBase.this.mod.modid + " common block tags";
        }
    }

    private class ItemTagProviderBase extends ItemTagsProvider {

        private Map<ResourceLocation, Tag.Builder> tagCache;

        protected ItemTagProviderBase(DataGenerator generator, String modid, ExistingFileHelper fileHelper, BlockTagProviderBase blockTags) {
            super(generator, blockTags, modid, fileHelper);
        }

        @Override
        protected void addTags() {
            if (!CommonTagsProviderBase.this.isSetup) {
                CommonTagsProviderBase.this.isSetup = true;
                CommonTagsProviderBase.this.doSetup();
            } else if (this.tagCache != null) {
                this.builders.putAll(this.tagCache);
            }
            for (Runnable copy : CommonTagsProviderBase.this.itemCopies) {
                copy.run();
            }
        }

        @Override
        public void run(@Nonnull HashCache cache) {
            this.tagCache = new HashMap<>(this.builders);
            super.run(cache);
        }

        @Override
        @Nonnull
        public TagsProvider.TagAppender<Item> tag(@Nonnull Tag.Named<Item> tag) {
            return super.tag(tag);
        }

        @Override
        public void copy(@Nonnull Tag.Named<Block> blockTag, @Nonnull Tag.Named<Item> itemTag) {
            super.copy(blockTag, itemTag);
        }

        @Nonnull
        @Override
        public String getName() {
            return CommonTagsProviderBase.this.mod.modid + " common item tags";
        }
    }

    private class FluidTagProviderBase extends FluidTagsProvider {

        private Map<ResourceLocation, Tag.Builder> tagCache;

        protected FluidTagProviderBase(DataGenerator generator, String modid, ExistingFileHelper fileHelper) {
            super(generator, modid, fileHelper);
        }

        @Override
        protected void addTags() {
            if (!CommonTagsProviderBase.this.isSetup) {
                CommonTagsProviderBase.this.isSetup = true;
                CommonTagsProviderBase.this.doSetup();
            } else if (this.tagCache != null) {
                this.builders.putAll(this.tagCache);
            }
        }

        @Override
        public void run(@Nonnull HashCache cache) {
            this.tagCache = new HashMap<>(this.builders);
            super.run(cache);
        }

        @Override
        @Nonnull
        public TagsProvider.TagAppender<Fluid> tag(@Nonnull Tag.Named<Fluid> tag) {
            return super.tag(tag);
        }

        public List<ResourceLocation> getTagInfo(Tag.Named<Fluid> tag) {
            TagsProvider.TagAppender<Fluid> builder = this.tag(tag);
            return builder.getInternalBuilder().getEntries()
                    .filter(p -> p.getEntry() instanceof Tag.ElementEntry)
                    .map(p -> new ResourceLocation(((Tag.ElementEntry) p.getEntry()).toString()))
                    .collect(Collectors.toList());
        }

        @Nonnull
        @Override
        public String getName() {
            return CommonTagsProviderBase.this.mod.modid + " common fluid tags";
        }
    }
}
