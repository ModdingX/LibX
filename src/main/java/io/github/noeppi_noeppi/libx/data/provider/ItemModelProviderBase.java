package io.github.noeppi_noeppi.libx.data.provider;

import io.github.noeppi_noeppi.libx.data.AlwaysExistentModelFile;
import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ItemModelProviderBase extends ItemModelProvider {

    private static final ResourceLocation GENERATED = new ResourceLocation("item/generated");
    private static final ResourceLocation HANDHELD = new ResourceLocation("item/handheld");

    protected final ModX mod;

    private final Set<Item> handheld = new HashSet<>();
    private final Set<Item> blacklist = new HashSet<>();

    public ItemModelProviderBase(ModX mod, DataGenerator generator, ExistingFileHelper fileHelper) {
        super(generator, mod.modid, fileHelper);
        this.mod = mod;
    }

    @Nonnull
    @Override
    public final String getName() {
        return mod.modid + " item models";
    }

    protected void handheld(Item item) {
        this.handheld.add(item);
    }

    protected void manualModel(Item item) {
        this.blacklist.add(item);
    }

    @Override
    protected void registerModels() {
        for (Map.Entry<ResourceLocation, Item> entry : ForgeRegistries.ITEMS.getEntries()) {
            ResourceLocation id = entry.getKey();
            Item item = entry.getValue();
            if (this.mod.modid.equals(id.getNamespace()) && !this.blacklist.contains(item)) {
                if (item instanceof BlockItem) {
                    this.getBuilder(id.getPath()).parent(new AlwaysExistentModelFile(new ResourceLocation(id.getNamespace(), "block/" + id.getPath())));
                } else if (this.handheld.contains(item)) {
                    this.withExistingParent(id.getPath(), HANDHELD).texture("layer0", new ResourceLocation(id.getNamespace(), "item/" + id.getPath()));
                } else {
                    this.withExistingParent(id.getPath(), GENERATED).texture("layer0", new ResourceLocation(id.getNamespace(), "item/" + id.getPath()));
                }
            }
        }
    }
}
