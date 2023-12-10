package org.moddingx.libx.datagen.provider.patchouli;

import com.google.common.collect.Streams;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.PackTarget;
import org.moddingx.libx.impl.datagen.load.DatagenFontLoader;
import org.moddingx.libx.impl.datagen.patchouli.translate.TranslationManager;
import org.moddingx.libx.mod.ModX;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * A provider for patchouli categories and entries. This will not generate the {@code book.json} file.
 */
public abstract class PatchouliProviderBase implements DataProvider {
    
    protected final ModX mod;
    protected final PackTarget packTarget;
    protected final ExistingFileHelper fileHelper;
    private final BookProperties properties;
    private final String bookNamespace;
    
    private final List<CategoryBuilder> categories;
    private final Set<String> categoryIds;
    private final List<EntryBuilder> entries;
    
    public PatchouliProviderBase(DatagenContext ctx, BookProperties properties) {
        this.mod = ctx.mod();
        this.packTarget = ctx.target();
        this.fileHelper = ctx.fileHelper();
        this.properties = properties;
        this.bookNamespace = properties.namespace() != null ? properties.namespace() : this.mod.modid;
        
        // Preload font information now as we won't have an ExistingFileHelper available later
        // See PageJson#splitText
        DatagenFontLoader.getFontMetrics(this.fileHelper);
        
        this.categories = new ArrayList<>();
        this.categoryIds = new HashSet<>();
        this.entries = new ArrayList<>();
    }

    /**
     * Creates the categories and entries for the patchouli book.
     */
    protected abstract void setup();

    /**
     * Adds a new category to this book.
     * 
     * @see CategoryBuilder
     */
    public CategoryBuilder category(String id) {
        CategoryBuilder builder = new CategoryBuilder(this.mod, new ResourceLocation(this.bookNamespace, id));
        this.categories.add(builder);
        this.categoryIds.add(id);
        return builder;
    }

    /**
     * Adds a new entry to this book. The entry will be added to the last added category.
     *
     * @see EntryBuilder
     */
    public EntryBuilder entry(String id) {
        if (this.categories.isEmpty()) throw new IllegalStateException("No categories defined");
        return this.entry(id, this.categories.get(this.categories.size() - 1).id.getPath());
    }

    /**
     * Adds a new entry to this book.
     *
     * @see EntryBuilder
     */
    public EntryBuilder entry(String id, String category) {
        return this.entry(id, category, false);
    }

    /**
     * Adds a new foreign entry to this book. A foreign entry is an entry for a category not added by this provider.
     * Useful when extending books.
     *
     * @see EntryBuilder
     */
    public EntryBuilder foreignEntry(String id, String category) {
        return this.entry(id, category, true);
    }
    
    private EntryBuilder entry(String id, String category, boolean foreignEntry) {
        if (foreignEntry) {
            if (this.categoryIds.contains(category)) throw new IllegalArgumentException("Foreign entry in known category: " + category);
        } else {
            if (!this.categoryIds.contains(category)) throw new IllegalArgumentException("Unknown category: " + category);
        }
        EntryBuilder builder = new EntryBuilder(this.mod, id, new ResourceLocation(this.bookNamespace, category));
        this.entries.add(builder);
        return builder;
    }

    @Nonnull
    @Override
    public String getName() {
        return this.mod.modid + " " + (this.properties.namespace() == null ? "" : this.properties.namespace() + ":") + this.properties.bookName() + " patchouli book";
    }
    
    @Nonnull
    @Override
    public CompletableFuture<?> run(@Nonnull CachedOutput cache) {
        this.setup();
        
        TranslationManager mgr;
        BiFunction<String, List<String>, String> translations;
        if (this.properties.translate()) {
            mgr = new TranslationManager(this.properties.bookName());
            translations = mgr::add;
        } else {
            mgr = null;
            translations = (str, path) -> str;
        }

        return CompletableFuture.allOf(Streams.concat(
                Streams.mapWithIndex(this.categories.stream(), (category, idx) -> {
                    Path path = this.packTarget.path(PackType.CLIENT_RESOURCES).resolve(category.id.getNamespace() + "/patchouli_books/" + this.properties.bookName() + "/en_us/categories/" + category.id.getPath() + ".json");
                    return DataProvider.saveStable(cache, category.build(translations, (int) idx), path);
                }),
                this.entries.stream().map(entry -> {
                    Path path = this.packTarget.path(PackType.CLIENT_RESOURCES).resolve(entry.category.getNamespace() + "/patchouli_books/" + this.properties.bookName() + "/en_us/entries/" + entry.category.getPath() + "/" + entry.id + ".json");
                    return DataProvider.saveStable(cache, entry.build(translations, this.fileHelper), path);
                }),
                Stream.ofNullable(mgr).map(theMgr -> {
                    Path langPath = this.packTarget.path(PackType.CLIENT_RESOURCES).resolve(this.mod.modid + "_" + this.properties.bookName() + "/lang/en_us.json");
                    return DataProvider.saveStable(cache, theMgr.build(), langPath);
                })
        ).toArray(CompletableFuture[]::new));
    }
}
