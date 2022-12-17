package org.moddingx.libx.datagen.provider.patchouli;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.moddingx.libx.annotation.meta.Experimental;
import org.moddingx.libx.impl.datagen.FontLoader;
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

/**
 * A provider for patchouli categories and entries. This will not generate the {@code book.json} file.
 */
@Experimental
public abstract class PatchouliProviderBase implements DataProvider {
    
    protected final ModX mod;
    protected final PackOutput packOutput;
    protected final ExistingFileHelper fileHelper;
    private final BookProperties properties;
    
    private final List<CategoryBuilder> categories;
    private final Set<ResourceLocation> categoryIds;
    private final List<EntryBuilder> entries;
    
    public PatchouliProviderBase(ModX mod, PackOutput packOutput, ExistingFileHelper fileHelper, BookProperties properties) {
        this.mod = mod;
        this.packOutput = packOutput;
        this.fileHelper = fileHelper;
        this.properties = properties;
        FontLoader.getFontWidthProvider(fileHelper);
        
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
        CategoryBuilder builder = new CategoryBuilder(this.mod.resource(id));
        this.categories.add(builder);
        this.categoryIds.add(builder.id);
        return builder;
    }

    /**
     * Adds a new entry to this book. The entry will be added to the last added category.
     *
     * @see EntryBuilder
     */
    public EntryBuilder entry(String id) {
        if (this.categories.isEmpty()) throw new IllegalStateException("No categories defined");
        return this.entry(id, this.categories.get(this.categories.size() - 1).id);
    }

    /**
     * Adds a new entry to this book.
     *
     * @see EntryBuilder
     */
    public EntryBuilder entry(String id, String category) {
        return this.entry(id, this.mod.resource(category));
    }

    /**
     * Adds a new entry to this book.
     *
     * @see EntryBuilder
     */
    public EntryBuilder entry(String id, ResourceLocation category) {
        if (this.mod.modid.equals(category.getNamespace()) && !this.categoryIds.contains(category)) throw new IllegalArgumentException("Unknown category: " + category);
        EntryBuilder builder = new EntryBuilder(id, category);
        this.entries.add(builder);
        return builder;
    }

    @Nonnull
    @Override
    public String getName() {
        return this.mod.modid + " " + this.properties.bookName() + " patchouli book";
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

        CompletableFuture<?>[] futures = new CompletableFuture[this.categories.size() + this.entries.size() + (mgr != null ? 1 : 0)];
        int i = 0;
        for (int j = 0; j < this.categories.size(); j++) {
            CategoryBuilder category = this.categories.get(j);
            Path path = this.packOutput.getOutputFolder().resolve(this.properties.packTarget().getDirectory() + "/" + category.id.getNamespace() + "/patchouli_books/" + this.properties.bookName() + "/en_us/categories/" + category.id.getPath() + ".json");
            futures[i++] = DataProvider.saveStable(cache, category.build(translations, j), path);
        }
        for (EntryBuilder entry : this.entries) {
            Path path = this.packOutput.getOutputFolder().resolve(this.properties.packTarget().getDirectory() + "/" + entry.category.getNamespace() + "/patchouli_books/" + this.properties.bookName() + "/en_us/entries/" + entry.category.getPath() + "/" + entry.id + ".json");
            futures[i++] = DataProvider.saveStable(cache, entry.build(translations, this.fileHelper), path);
        }

        if (mgr != null) {
            Path langPath = this.packOutput.getOutputFolder().resolve(PackType.CLIENT_RESOURCES.getDirectory() + "/" + this.mod.modid + "_" + this.properties.bookName() + "/lang/en_us.json");
            futures[i] = DataProvider.saveStable(cache, mgr.build(), langPath);
        }
        return CompletableFuture.allOf(futures);
    }
}
