package org.moddingx.libx.datagen.provider;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.moddingx.libx.mod.ModX;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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

    
    protected TagProviderBase(ModX mod, PackOutput packOutput, @Nonnull ResourceKey<? extends Registry<T>> registryKey, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper fileHelper) {
        super(packOutput, registryKey, lookupProvider, mod.modid, fileHelper);
        this.mod = mod;
        this.forgeRegistry = RegistryManager.ACTIVE.getRegistry(registryKey);
    }

    @Override
    protected final void addTags(@Nonnull HolderLookup.Provider lookupProvider) {
        this.setup();

        if (this.forgeRegistry != null) {
            this.forgeRegistry.getEntries().stream()
                    .filter(entry -> this.mod.modid.equals(entry.getKey().location().getNamespace()))
                    .sorted(Map.Entry.comparingByKey(Comparator.comparing(ResourceKey::location)))
                    .map(Map.Entry::getValue)
                    .forEach(this::defaultTags);
        } else {
            lookupProvider.lookupOrThrow(this.registryKey).listElements()
                    .filter(entry -> this.mod.modid.equals(entry.key().location().getNamespace()))
                    .sorted(Comparator.comparing(entry -> entry.key().location()))
                    .map(Holder::get)
                    .forEach(this::defaultTags);
        }
    }

    @Nonnull
    @Override
    public final String getName() {
        return this.mod.modid + " " + (this.forgeRegistry == null ? this.registryKey.location() : this.forgeRegistry.getRegistryName()) + " tags";
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
