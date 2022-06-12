package org.moddingx.libx.data.provider;

import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.client.RenderProperties;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.loaders.DynamicBucketModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.moddingx.libx.LibX;
import org.moddingx.libx.data.AlwaysExistentModelFile;
import org.moddingx.libx.impl.RendererOnDataGenException;
import org.moddingx.libx.impl.base.decoration.blocks.*;
import org.moddingx.libx.mod.ModX;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A base class for item model provider. An extending class should call the
 * {@link #handheld(Item) handheld} and {@link #manualModel(Item) manualModel} methods
 * in {@link #setup() setup}.
 */
public abstract class ItemModelProviderBase extends ItemModelProvider {

    public static final ResourceLocation GENERATED = new ResourceLocation("item/generated");
    public static final ResourceLocation HANDHELD = new ResourceLocation("item/handheld");
    public static final ResourceLocation DRIPPING_BUCKET = new ResourceLocation("forge", "bucket_drip");
    public static final ResourceLocation SPECIAL_BLOCK_PARENT = LibX.getInstance().resource("item/base/special_block");
    public static final ResourceLocation SPAWN_EGG_PARENT = new ResourceLocation("minecraft", "item/template_spawn_egg");
    public static final ResourceLocation FENCE_PARENT = new ResourceLocation("minecraft", "block/fence_inventory");
    public static final ResourceLocation BUTTON_PARENT = new ResourceLocation("minecraft", "block/button_inventory");
    public static final ResourceLocation WALL_PARENT = new ResourceLocation("minecraft", "block/wall_inventory");

    protected final ModX mod;

    private final Set<Item> handheld = new HashSet<>();
    private final Set<Item> ignored = new HashSet<>();

    public ItemModelProviderBase(ModX mod, DataGenerator generator, ExistingFileHelper fileHelper) {
        super(generator, mod.modid, fileHelper);
        this.mod = mod;
    }

    @Nonnull
    @Override
    public final String getName() {
        return this.mod.modid + " item models";
    }

    /**
     * This item will get a handheld model.
     */
    protected void handheld(Item item) {
        this.handheld.add(item);
    }

    /**
     * This item will not be processed by the generator.
     */
    protected void manualModel(Item item) {
        this.ignored.add(item);
    }

    @Override
    protected void registerModels() {
        this.setup();

        for (ResourceLocation id : ForgeRegistries.ITEMS.getKeys()) {
            Item item = ForgeRegistries.ITEMS.getValue(id);
            if (item != null && this.mod.modid.equals(id.getNamespace()) && !this.ignored.contains(item)) {
                if (item instanceof BlockItem blockItem) {
                    this.defaultBlock(id, blockItem);
                } else if (this.handheld.contains(item)) {
                    this.withExistingParent(id.getPath(), HANDHELD).texture("layer0", new ResourceLocation(id.getNamespace(), "item/" + id.getPath()));
                } else {
                    this.defaultItem(id, item);
                }
            }
        }
    }

    protected abstract void setup();

    protected void defaultItem(ResourceLocation id, Item item) {
        if (item instanceof SpawnEggItem) {
            this.withExistingParent(id.getPath(), SPAWN_EGG_PARENT);
        } else if (item instanceof BucketItem bucketItem) {
            this.withExistingParent(id.getPath(), DRIPPING_BUCKET)
                    .texture("base", this.modLoc("item/" + id.getPath()))
                    .customLoader(DynamicBucketModelBuilder::begin)
                    .fluid(bucketItem.getFluid());
        } else {
            this.withExistingParent(id.getPath(), GENERATED).texture("layer0", new ResourceLocation(id.getNamespace(), "item/" + id.getPath()));
        }
    }

    protected void defaultBlock(ResourceLocation id, BlockItem item) {
        if (isItemStackRenderer(item)) {
            this.getBuilder(id.getPath()).parent(new AlwaysExistentModelFile(SPECIAL_BLOCK_PARENT));
        } else if (item.getBlock() instanceof DecoratedFenceBlock decorated) {
            ResourceLocation parentId = Objects.requireNonNull(decorated.parent.getRegistryName());
            ResourceLocation texture = new ResourceLocation(parentId.getNamespace(), "block/" + parentId.getPath());
            this.getBuilder(id.getPath()).parent(new AlwaysExistentModelFile(FENCE_PARENT)).texture("texture", texture);
        } else if (item.getBlock() instanceof DecoratedWoodButton decorated) {
            ResourceLocation parentId = Objects.requireNonNull(decorated.parent.getRegistryName());
            ResourceLocation texture = new ResourceLocation(parentId.getNamespace(), "block/" + parentId.getPath());
            this.getBuilder(id.getPath()).parent(new AlwaysExistentModelFile(BUTTON_PARENT)).texture("texture", texture);
        } else if (item.getBlock() instanceof DecoratedStoneButton decorated) {
            ResourceLocation parentId = Objects.requireNonNull(decorated.parent.getRegistryName());
            ResourceLocation texture = new ResourceLocation(parentId.getNamespace(), "block/" + parentId.getPath());
            this.getBuilder(id.getPath()).parent(new AlwaysExistentModelFile(BUTTON_PARENT)).texture("texture", texture);
        } else if (item.getBlock() instanceof DecoratedWallBlock decorated) {
            ResourceLocation parentId = Objects.requireNonNull(decorated.parent.getRegistryName());
            ResourceLocation texture = new ResourceLocation(parentId.getNamespace(), "block/" + parentId.getPath());
            this.getBuilder(id.getPath()).parent(new AlwaysExistentModelFile(WALL_PARENT)).texture("wall", texture);
        } else if (item.getBlock() instanceof DecoratedTrapdoorBlock) {
            this.getBuilder(id.getPath()).parent(new AlwaysExistentModelFile(new ResourceLocation(id.getNamespace(), "block/" + id.getPath() + "_bottom")));
        } else if (item.getBlock() instanceof DecoratedDoorBlock || item.getBlock() instanceof DecoratedSign.Standing || item.getBlock() instanceof DecoratedSign.Wall) {
            this.withExistingParent(id.getPath(), GENERATED).texture("layer0", new ResourceLocation(id.getNamespace(), "item/" + id.getPath()));
        } else {
            this.getBuilder(id.getPath()).parent(new AlwaysExistentModelFile(new ResourceLocation(id.getNamespace(), "block/" + id.getPath())));
        }
    }

    private static boolean isItemStackRenderer(Item item) {
        try {
            IItemRenderProperties properties = RenderProperties.get(item);
            if (properties != IItemRenderProperties.DUMMY) {
                properties.getItemStackRenderer();
            } else {
                // Forge no longer calls this during datagen
                // so we need to do it manually
                AtomicReference<IItemRenderProperties> ref = new AtomicReference<>(null);
                item.initializeClient(ref::set);
                properties = ref.get();
                if (properties != null) properties.getItemStackRenderer();
            }
        } catch (RendererOnDataGenException e) {
            return true;
        } catch (Exception | NoClassDefFoundError e) {
            return false;
        }
        return false;
    }
}
