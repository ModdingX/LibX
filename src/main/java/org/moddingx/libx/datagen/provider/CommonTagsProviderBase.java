package org.moddingx.libx.datagen.provider;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;
import org.moddingx.libx.impl.datagen.DecorationTags;
import org.moddingx.libx.impl.tags.InternalTagProvider;
import org.moddingx.libx.impl.tags.InternalTags;
import org.moddingx.libx.mod.ModX;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final List<Pair<TagKey<Fluid>, TagKey<Block>>> fluidCopies = new ArrayList<>();
    
    private boolean hasLibXInternalTags = false;

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
        generator.addProvider(true, this.blockTags);
        generator.addProvider(true, this.itemTags);
        generator.addProvider(true, this.fluidTags);
    }

    public abstract void setup();

    private void doSetup() {
        if (this.getClass() == InternalTagProvider.class) this.initInternalTags();
        this.setup();
        ForgeRegistries.BLOCKS.getEntries().stream()
                .filter(entry -> this.mod.modid.equals(entry.getKey().location().getNamespace()))
                .map(Map.Entry::getValue)
                .forEach(block -> {
                    DecorationTags.addTags(block, this, this::initInternalTags);
                    this.defaultBlockTags(block);
                });
        ForgeRegistries.ITEMS.getEntries().stream()
                .filter(entry -> this.mod.modid.equals(entry.getKey().location().getNamespace()))
                .map(Map.Entry::getValue)
                .forEach(this::defaultItemTags);
        ForgeRegistries.FLUIDS.getEntries().stream()
                .filter(entry -> this.mod.modid.equals(entry.getKey().location().getNamespace()))
                .map(Map.Entry::getValue)
                .forEach(this::defaultFluidTags);
    }

    /**
     * Adds default {@link ItemTags item tags} to an {@link Item}
     */
    public void defaultItemTags(Item item) {

    }

    /**
     * Adds default {@link BlockTags item tags} to a {@link Block}
     */
    public void defaultBlockTags(Block block) {

    }

    /**
     * Adds default {@link FluidTags item tags} to a {@link Fluid}
     */
    public void defaultFluidTags(Fluid fluid) {

    }

    /**
     * Gets a {@link TagsProvider.TagAppender tag appender} for an {@link Item}
     */
    public TagsProvider.TagAppender<Item> item(TagKey<Item> tag) {
        return this.itemTags.tag(tag);
    }

    /**
     * Gets a {@link TagsProvider.TagAppender tag appender} for a {@link Block}
     */
    public TagsProvider.TagAppender<Block> block(TagKey<Block> tag) {
        return this.blockTags.tag(tag);
    }

    /**
     * Gets a {@link TagsProvider.TagAppender tag appender} for a {@link Fluid}
     */
    public TagsProvider.TagAppender<Fluid> fluid(TagKey<Fluid> tag) {
        return this.fluidTags.tag(tag);
    }

    /**
     * Copies all entries from a block tag to an item tag.
     */
    public void copyBlock(TagKey<Block> from, TagKey<Item> to) {
        this.itemCopies.add(() -> this.itemTags.copy(from, to));
    }

    /**
     * Copies all entries from a fluid tag to a block tag.
     */
    public void copyFluid(TagKey<Fluid> from, TagKey<Block> to) {
        this.fluidCopies.add(Pair.of(from, to));
    }

    @Nonnull
    @Override
    public final String getName() {
        return this.mod.modid + " common tags";
    }

    @Override
    public void run(@Nonnull CachedOutput cache) {
        // We don't do anything here, everything is done by the three child providers
    }
    
    private void initInternalTags() {
        if (!this.hasLibXInternalTags) {
            this.hasLibXInternalTags = true;
            for (Map.Entry<TagKey<Item>, TagKey<Item>> entry : InternalTags.Items.getTags().entrySet()) {
                this.item(entry.getValue());
            }
            for (Map.Entry<TagKey<Block>, TagKey<Block>> entry : InternalTags.Blocks.getTags().entrySet()) {
                this.block(entry.getValue());
            }
            for (Map.Entry<TagKey<Block>, TagKey<Item>> entry : InternalTags.Items.getCopies().entrySet()) {
                this.copyBlock(entry.getKey(), entry.getValue());
            }
        }
    }

    private class BlockTagProviderBase extends BlockTagsProvider {

        private Map<ResourceLocation, TagBuilder> tagCache;

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
            for (Pair<TagKey<Fluid>, TagKey<Block>> copy : CommonTagsProviderBase.this.fluidCopies) {
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
        public void run(@Nonnull CachedOutput cache) {
            this.tagCache = new HashMap<>(this.builders);
            super.run(cache);
        }

        @Override
        @Nonnull
        public TagsProvider.TagAppender<Block> tag(@Nonnull TagKey<Block> tag) {
            return super.tag(tag);
        }

        @Nonnull
        @Override
        public String getName() {
            return CommonTagsProviderBase.this.mod.modid + " common block tags";
        }
    }

    private class ItemTagProviderBase extends ItemTagsProvider {

        private Map<ResourceLocation, TagBuilder> tagCache;

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
        public void run(@Nonnull CachedOutput cache) {
            this.tagCache = new HashMap<>(this.builders);
            super.run(cache);
        }

        @Override
        @Nonnull
        public TagsProvider.TagAppender<Item> tag(@Nonnull TagKey<Item> tag) {
            return super.tag(tag);
        }

        @Override
        public void copy(@Nonnull TagKey<Block> blockTag, @Nonnull TagKey<Item> itemTag) {
            super.copy(blockTag, itemTag);
        }

        @Nonnull
        @Override
        public String getName() {
            return CommonTagsProviderBase.this.mod.modid + " common item tags";
        }
    }

    private class FluidTagProviderBase extends FluidTagsProvider {

        private Map<ResourceLocation, TagBuilder> tagCache;

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
        public void run(@Nonnull CachedOutput cache) {
            this.tagCache = new HashMap<>(this.builders);
            super.run(cache);
        }

        @Override
        @Nonnull
        public TagsProvider.TagAppender<Fluid> tag(@Nonnull TagKey<Fluid> tag) {
            return super.tag(tag);
        }

        public List<ResourceLocation> getTagInfo(TagKey<Fluid> tag) {
            TagsProvider.TagAppender<Fluid> builder = this.tag(tag);
            return builder.getInternalBuilder().entries.stream()
                    .filter(p -> !p.tag)
                    .map(TagEntry::getId)
                    .toList();
        }

        @Nonnull
        @Override
        public String getName() {
            return CommonTagsProviderBase.this.mod.modid + " common fluid tags";
        }
    }
}
