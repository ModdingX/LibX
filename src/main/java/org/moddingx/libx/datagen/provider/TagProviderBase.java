package org.moddingx.libx.datagen.provider;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.IForgeRegistry;
import org.moddingx.libx.mod.ModX;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Map;

/**
 * A provider for tags of a type. If you want to use {@link BlockTags block}, {@link ItemTags item}
 * or {@link FluidTags fluid} tags, use {@link CommonTagsProviderBase}. You can set your tags in
 * {@link #setup() setup}. With {@link #defaultTags(Object)}, you can add default tags
 * that can be retrieved from the element.
 */
public abstract class TagProviderBase<T> extends TagsProvider<T> {

    protected final ModX mod;
    
    @Nullable
    protected final IForgeRegistry<T> forgeRegistry;

    
    protected TagProviderBase(ModX mod, DataGenerator generator, @Nonnull Registry<T> registry, @Nullable ExistingFileHelper fileHelper) {
        super(generator, registry, mod.modid, fileHelper);
        this.mod = mod;
        this.forgeRegistry = null;
    }
    
    protected TagProviderBase(ModX mod, DataGenerator generator, @Nonnull IForgeRegistry<T> registry, @Nullable ExistingFileHelper fileHelper) {
        super(generator, wrapForge(registry), mod.modid, fileHelper);
        this.mod = mod;
        this.forgeRegistry = registry;
    }

    @Override
    protected final void addTags() {
        this.setup();

        if (this.forgeRegistry != null) {
            this.forgeRegistry.getEntries().stream()
                    .filter(entry -> this.mod.modid.equals(entry.getKey().location().getNamespace()))
                    .sorted(Map.Entry.comparingByKey(Comparator.comparing(ResourceKey::location)))
                    .map(Map.Entry::getValue)
                    .forEach(this::defaultTags);
        } else {
            this.registry.entrySet().stream()
                    .filter(entry -> this.mod.modid.equals(entry.getKey().location().getNamespace()))
                    .sorted(Map.Entry.comparingByKey(Comparator.comparing(ResourceKey::location)))
                    .map(Map.Entry::getValue)
                    .forEach(this::defaultTags);
        }
    }

    @Nonnull
    @Override
    public final String getName() {
        return this.mod.modid + " " + (this.forgeRegistry == null ? this.registry.key().location() : this.forgeRegistry.getRegistryName()) + " tags";
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

    private static <T> Registry<T> wrapForge(IForgeRegistry<T> forgeRegistry) {
        if (forgeRegistry.tags() == null) {
            throw new IllegalArgumentException("Registry has no tag support: " + forgeRegistry.getRegistryName());
        } else if (forgeRegistry.getDefaultKey() == null) {
            return GameData.getWrapper(forgeRegistry.getRegistryKey(), Lifecycle.experimental());
        } else {
            return GameData.getWrapper(forgeRegistry.getRegistryKey(), Lifecycle.experimental(), "default");
        }
    }
}
