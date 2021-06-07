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

@SuppressWarnings("ALL")
public abstract class CommonTagsProviderBase implements IDataProvider {

    protected final ModX mod;
    protected final DataGenerator generator;
    protected final ExistingFileHelper fileHelper;

    private final BlockTagProviderBase blockTags;
    private final ItemTagProviderBase itemTags;
    private final FluidTagProviderBase fluidTags;

    private boolean isSetup = false;

    public CommonTagsProviderBase(ModX mod, DataGenerator generator, ExistingFileHelper fileHelper) {
        this.mod = mod;
        this.generator = generator;
        this.fileHelper = fileHelper;
        this.blockTags = new BlockTagProviderBase(mod, generator, ForgeRegistries.BLOCKS, fileHelper);
        this.itemTags = new ItemTagProviderBase(mod, generator, ForgeRegistries.ITEMS, fileHelper);
        this.fluidTags = new FluidTagProviderBase(mod, generator, ForgeRegistries.FLUIDS, fileHelper);
        generator.addProvider(this.blockTags);
        generator.addProvider(this.itemTags);
        generator.addProvider(this.fluidTags);
    }

    public abstract void setup();

    public void defaultItemTags(Item item) {

    }

    public void defaultBlockTags(Block item) {

    }

    public void defaultFluidTags(Fluid item) {

    }

    @Nonnull
    @Override
    public String getName() {
        return this.mod.modid + " common tags";
    }

    public void act(@Nonnull DirectoryCache cache) {
        // We don't do anything here, everything is done by the three child providers
    }

    private class BlockTagProviderBase extends TagProviderBase<Block> {

        protected BlockTagProviderBase(ModX mod, DataGenerator generator, IForgeRegistry<Block> registry, @Nullable ExistingFileHelper fileHelper) {
            super(mod, generator, registry, fileHelper);
        }

        @Override
        protected void setup() {
            if (!CommonTagsProviderBase.this.isSetup) {
                CommonTagsProviderBase.this.isSetup = true;
                CommonTagsProviderBase.this.setup();
            }
        }

        @Override
        public void act(DirectoryCache cache) {
            this.tagCache = new HashMap<>(this.tagToBuilder);
            super.act(cache);
        }

        @Nonnull
        public TagsProvider.Builder<Block> getOrCreateBuilder(@Nonnull ITag.INamedTag<Block> tag) {
            return super.getOrCreateBuilder(tag);
        }
    }

    private class ItemTagProviderBase extends TagProviderBase<Item> {

        protected ItemTagProviderBase(ModX mod, DataGenerator generator, IForgeRegistry<Item> registry, @Nullable ExistingFileHelper fileHelper) {
            super(mod, generator, registry, fileHelper);
        }

        @Override
        protected void setup() {
            if (!CommonTagsProviderBase.this.isSetup) {
                CommonTagsProviderBase.this.isSetup = true;
                CommonTagsProviderBase.this.setup();
            }
        }

        @Override
        public void act(DirectoryCache cache) {
            this.tagCache = new HashMap<>(this.tagToBuilder);
            super.act(cache);
        }

        @Nonnull
        public TagsProvider.Builder<Item> getOrCreateBuilder(@Nonnull ITag.INamedTag<Item> tag) {
            return super.getOrCreateBuilder(tag);
        }
    }

    private class FluidTagProviderBase extends TagProviderBase<Fluid> {

        protected FluidTagProviderBase(ModX mod, DataGenerator generator, IForgeRegistry<Fluid> registry, @Nullable ExistingFileHelper fileHelper) {
            super(mod, generator, registry, fileHelper);
        }

        @Override
        protected void registerTags() {
            if (!CommonTagsProviderBase.this.isSetup) {
                CommonTagsProviderBase.this.isSetup = true;
                CommonTagsProviderBase.this.setup();
            }
        }

        @Override
        public void act(DirectoryCache cache) {
            this.tagCache = new HashMap<>(this.tagToBuilder);
            super.act(cache);
        }

        @Nonnull
        public TagsProvider.Builder<Fluid> getOrCreateBuilder(@Nonnull ITag.INamedTag<Fluid> tag) {
            return super.getOrCreateBuilder(tag);
        }
    }
}
