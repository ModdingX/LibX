package org.moddingx.libx.data.provider;

import net.minecraft.data.DataGenerator;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeRegistryTagsProvider;
import net.minecraftforge.registries.IForgeRegistry;
import org.moddingx.libx.mod.ModX;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * A provider for tags of a type. If you want to use {@link BlockTags block}, {@link ItemTags item}
 * or {@link FluidTags fluid} tags, use {@link CommonTagsProviderBase}. You can set your tags in
 * {@link #setup() setup}. With {@link #defaultTags(Object)}, you can add default tags
 * that can be retrieved from the element.
 */
// TODO still needed?
public abstract class TagProviderBase<T> extends ForgeRegistryTagsProvider<T> {

    protected final ModX mod;
    protected final IForgeRegistry<T> registry;

    /**
     * Creates a new tag provider base
     */
    protected TagProviderBase(ModX mod, DataGenerator generator, IForgeRegistry<T> registry, @Nullable ExistingFileHelper fileHelper) {
        super(generator, registry, mod.modid, fileHelper);
        this.mod = mod;
        this.registry = registry;
    }

    @Override
    protected final void addTags() {
        this.setup();

        this.registry.getEntries().stream()
                .filter(entry -> this.mod.modid.equals(entry.getKey().location().getNamespace()))
                .map(Map.Entry::getValue)
                .forEach(this::defaultTags);
    }

    @Nonnull
    @Override
    public final String getName() {
        return this.mod.modid + " " + this.registry.getRegistryName() + " tags";
    }

    /**
     * A method to add your custom tags.
     */
    protected abstract void setup();

    /**
     * Called for every element from your mod. You should add tags here,
     * that can be inferred from the element itself.
     */
    public void defaultTags(T element) {

    }
}
