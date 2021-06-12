package io.github.noeppi_noeppi.libx.data.provider;

import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.block.Block;
import net.minecraft.data.*;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

// TODO add javadoc
public abstract class CommonTagsProviderBase implements IDataProvider {

    protected final ModX mod;
    protected final DataGenerator generator;
    protected final ExistingFileHelper fileHelper;

    private final BlockTagProviderBase blockTags;
    private final ItemTagProviderBase itemTags;
    private final FluidTagProviderBase fluidTags;

    private boolean isSetup = false;
    private final List<Pair<ITag.INamedTag<Fluid>, ITag.INamedTag<Block>>> fluidCopies = new ArrayList<>();

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

    public void defaultItemTags(Item item) {

    }

    public void defaultBlockTags(Block item) {

    }

    public void defaultFluidTags(Fluid item) {

    }

    public TagsProvider.Builder<Item> item(ITag.INamedTag<Item> tag) {
        return this.itemTags.getOrCreateBuilder(tag);
    }

    public TagsProvider.Builder<Block> block(ITag.INamedTag<Block> tag) {
        return this.blockTags.getOrCreateBuilder(tag);
    }

    public TagsProvider.Builder<Fluid> fluid(ITag.INamedTag<Fluid> tag) {
        return this.fluidTags.getOrCreateBuilder(tag);
    }

    public void copyBlock(ITag.INamedTag<Block> from, ITag.INamedTag<Item> to) {
        this.itemTags.copy(from, to);
    }

    public void copyFluid(ITag.INamedTag<Fluid> from, ITag.INamedTag<Block> to) {
        this.fluidCopies.add(Pair.of(from, to));
    }

    @Nonnull
    @Override
    public String getName() {
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
