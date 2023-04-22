package org.moddingx.libx.datagen.provider.tags;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.mod.ModX;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * A provider for tags of a type. If you want to use {@link BlockTags block}, {@link ItemTags item}
 * or {@link FluidTags fluid} tags, use {@link CommonTagsProviderBase}. You can set your tags in
 * {@link #setup() setup}. With {@link #defaultTags(Object)}, you can add default tags
 * that can be retrieved from the element.
 */
public abstract class TagProviderBase<T> extends IntrinsicHolderTagsProvider<T> {

    protected final ModX mod;
    private final Registry<T> registry;
    
    protected TagProviderBase(DatagenContext ctx, ResourceKey<? extends Registry<T>> registryKey) {
        this(ctx, registryKey, ctx.registries().registry(registryKey), CompletableFuture.completedFuture(ctx.registries().registryAccess()));
    }
    
    private TagProviderBase(DatagenContext ctx, ResourceKey<? extends Registry<T>> registryKey, Registry<T> registry, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(ctx.output(), registryKey, lookupProvider, (T value) -> ResourceKey.create(registry.key(), Objects.requireNonNull(registry.getKey(value), () -> "Value not registered: " + value)), ctx.mod().modid, ctx.fileHelper());
        this.mod = ctx.mod();
        this.registry = registry;
    }

    @Nonnull
    @Override
    public final String getName() {
        return this.mod.modid + " " + this.registryKey.location() + " tags";
    }

    @Override
    protected final void addTags(@Nonnull HolderLookup.Provider lookupProvider) {
        this.setup();
        
        this.registry.entrySet().stream()
                .filter(entry -> this.mod.modid.equals(entry.getKey().location().getNamespace()))
                .sorted(Comparator.comparing(entry -> entry.getKey().location()))
                .map(Map.Entry::getValue)
                .forEach(this::defaultTags);
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
