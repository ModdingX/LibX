package org.moddingx.libx.datagen.provider.tags;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider.IntrinsicTagAppender;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.impl.datagen.tags.DecorationTags;
import org.moddingx.libx.impl.tags.InternalTagProvider;
import org.moddingx.libx.impl.tags.InternalTags;
import org.moddingx.libx.mod.ModX;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * A provider for {@link BlockTags block}, {@link ItemTags item} and {@link FluidTags fluid} tags.
 * You can set your tags in {@link #setup() setup}. With {@link #defaultItemTags(Item) defaultItemTags},
 * {@link #defaultBlockTags(Block) defaultBlockTags} and {@link #defaultFluidTags(Fluid) defaultFluidTags},
 * you can add default tags that can be retrieved from the element.
 */
public abstract class CommonTagsProviderBase implements DataProvider {

    protected final ModX mod;

    private final BlockTagProviderBase blockTags;
    private final ItemTagProviderBase itemTags;
    private final FluidTagProviderBase fluidTags;

    private boolean isSetup = false;
    // Copies must happen last, so we store them
    private final List<Runnable> itemCopies = new ArrayList<>();
    private final List<Pair<TagKey<Fluid>, TagKey<Block>>> fluidCopies = new ArrayList<>();

    private boolean hasLibXInternalTags = false;

    /**
     * Creates a new CommonTagsProviderBase
     */
    public CommonTagsProviderBase(DatagenContext ctx) {
        this.mod = ctx.mod();
        CompletableFuture<HolderLookup.Provider> lookupProvider = CompletableFuture.completedFuture(ctx.registries().registryAccess());
        this.blockTags = new BlockTagProviderBase(ctx.output(), lookupProvider, ctx.mod().modid, ctx.fileHelper());
        this.itemTags = new ItemTagProviderBase(ctx.output(), lookupProvider, ctx.mod().modid, ctx.fileHelper(), this.blockTags);
        this.fluidTags = new FluidTagProviderBase(ctx.output(), lookupProvider, ctx.mod().modid, ctx.fileHelper());
        ctx.addAdditionalProvider(c -> this.blockTags);
        ctx.addAdditionalProvider(c -> this.itemTags);
        ctx.addAdditionalProvider(c -> this.fluidTags);
    }

    public abstract void setup();

    private void doSetup() {
        if (this.getClass() == InternalTagProvider.class) this.initInternalTags();
        this.setup();
        ForgeRegistries.BLOCKS.getEntries().stream()
                .filter(entry -> this.mod.modid.equals(entry.getKey().location().getNamespace()))
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(ResourceKey::location)))
                .map(Map.Entry::getValue)
                .forEach(block -> {
                    DecorationTags.addTags(block, this, this::initInternalTags);
                    this.defaultBlockTags(block);
                });
        ForgeRegistries.ITEMS.getEntries().stream()
                .filter(entry -> this.mod.modid.equals(entry.getKey().location().getNamespace()))
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(ResourceKey::location)))
                .map(Map.Entry::getValue)
                .forEach(this::defaultItemTags);
        ForgeRegistries.FLUIDS.getEntries().stream()
                .filter(entry -> this.mod.modid.equals(entry.getKey().location().getNamespace()))
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(ResourceKey::location)))
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
    public IntrinsicTagAppender<Item> item(TagKey<Item> tag) {
        return this.itemTags.tag(tag);
    }

    /**
     * Gets a {@link TagsProvider.TagAppender tag appender} for a {@link Block}
     */
    public IntrinsicTagAppender<Block> block(TagKey<Block> tag) {
        return this.blockTags.tag(tag);
    }

    /**
     * Gets a {@link TagsProvider.TagAppender tag appender} for a {@link Fluid}
     */
    public IntrinsicTagAppender<Fluid> fluid(TagKey<Fluid> tag) {
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

    @Nonnull
    @Override
    public CompletableFuture<?> run(@Nonnull CachedOutput cache) {
        // We don't do anything here, everything is done by the three child providers
        return CompletableFuture.completedFuture(null);
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

        protected BlockTagProviderBase(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider, String modid, ExistingFileHelper fileHelper) {
            super(packOutput, lookupProvider, modid, fileHelper);
        }

        @Override
        protected void addTags(@Nonnull HolderLookup.Provider lookupProvider) {
            if (!CommonTagsProviderBase.this.isSetup) {
                CommonTagsProviderBase.this.isSetup = true;
                CommonTagsProviderBase.this.doSetup();
            } else if (this.tagCache != null) {
                this.builders.putAll(this.tagCache);
            }
            // Add fluid copies
            for (Pair<TagKey<Fluid>, TagKey<Block>> copy : CommonTagsProviderBase.this.fluidCopies) {
                IntrinsicTagAppender<Block> builder = this.tag(copy.getRight());
                for (ResourceLocation entry : CommonTagsProviderBase.this.fluidTags.getTagInfo(copy.getLeft())) {
                    Fluid fluid = ForgeRegistries.FLUIDS.getValue(entry);
                    if (fluid != null) {
                        builder.add(fluid.defaultFluidState().createLegacyBlock().getBlock());
                    }
                }
            }
        }

        @Nonnull
        @Override
        public CompletableFuture<?> run(@Nonnull CachedOutput cache) {
            this.tagCache = new HashMap<>(this.builders);
            return super.run(cache);
        }

        @Override
        @Nonnull
        public IntrinsicTagAppender<Block> tag(@Nonnull TagKey<Block> tag) {
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

        protected ItemTagProviderBase(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider, String modid, ExistingFileHelper fileHelper, BlockTagProviderBase blockTags) {
            super(packOutput, lookupProvider, blockTags.contentsGetter(), modid, fileHelper);
        }

        @Override
        protected void addTags(@Nonnull HolderLookup.Provider lookupProvider) {
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

        @Nonnull
        @Override
        public CompletableFuture<?> run(@Nonnull CachedOutput cache) {
            this.tagCache = new HashMap<>(this.builders);
            return super.run(cache);
        }

        @Override
        @Nonnull
        public IntrinsicTagAppender<Item> tag(@Nonnull TagKey<Item> tag) {
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

        protected FluidTagProviderBase(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider, String modid, ExistingFileHelper fileHelper) {
            super(packOutput, lookupProvider, modid, fileHelper);
        }

        @Override
        protected void addTags(@Nonnull HolderLookup.Provider lookupProvider) {
            if (!CommonTagsProviderBase.this.isSetup) {
                CommonTagsProviderBase.this.isSetup = true;
                CommonTagsProviderBase.this.doSetup();
            } else if (this.tagCache != null) {
                this.builders.putAll(this.tagCache);
            }
        }

        @Nonnull
        @Override
        public CompletableFuture<?> run(@Nonnull CachedOutput cache) {
            this.tagCache = new HashMap<>(this.builders);
            return super.run(cache);
        }

        @Override
        @Nonnull
        public IntrinsicTagAppender<Fluid> tag(@Nonnull TagKey<Fluid> tag) {
            return super.tag(tag);
        }

        public List<ResourceLocation> getTagInfo(TagKey<Fluid> tag) {
            IntrinsicTagAppender<Fluid> builder = this.tag(tag);
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
