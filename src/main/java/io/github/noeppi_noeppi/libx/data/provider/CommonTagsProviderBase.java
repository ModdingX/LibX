package io.github.noeppi_noeppi.libx.data.provider;

import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
        public void defaultTags(Block block) {
            CommonTagsProviderBase.this.defaultBlockTags(block);
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
        public void defaultTags(Item item) {
            CommonTagsProviderBase.this.defaultItemTags(item);
        }
    }

    private class FluidTagProviderBase extends TagProviderBase<Fluid> {

        protected FluidTagProviderBase(ModX mod, DataGenerator generator, IForgeRegistry<Fluid> registry, @Nullable ExistingFileHelper fileHelper) {
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
        public void defaultTags(Fluid fluid) {
            CommonTagsProviderBase.this.defaultFluidTags(fluid);
        }
    }
}
