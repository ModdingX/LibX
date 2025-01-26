package org.moddingx.libx.datagen.provider.model;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.loaders.DynamicFluidContainerModelBuilder;
import org.moddingx.libx.LibX;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.impl.base.decoration.blocks.*;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.render.ItemStackRenderer;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A base class for item model provider. An extending class should call the
 * {@link #handheld(Item) handheld} and {@link #manualModel(Item) manualModel} methods
 * in {@link #setup() setup}.
 */
public abstract class ItemModelProviderBase extends ItemModelProvider {

    public static final ResourceLocation GENERATED = ResourceLocation.fromNamespaceAndPath("minecraft", "item/generated");
    public static final ResourceLocation HANDHELD = ResourceLocation.fromNamespaceAndPath("minecraft", "item/handheld");
    public static final ResourceLocation DRIPPING_BUCKET = ResourceLocation.fromNamespaceAndPath("neoforge", "bucket_drip");
    public static final ResourceLocation SPECIAL_BLOCK_PARENT = LibX.getInstance().resource("item/base/special_block");
    public static final ResourceLocation SPAWN_EGG_PARENT = ResourceLocation.fromNamespaceAndPath("minecraft", "item/template_spawn_egg");
    public static final ResourceLocation FENCE_PARENT = ResourceLocation.fromNamespaceAndPath("minecraft", "block/fence_inventory");
    public static final ResourceLocation BUTTON_PARENT = ResourceLocation.fromNamespaceAndPath("minecraft", "block/button_inventory");
    public static final ResourceLocation WALL_PARENT = ResourceLocation.fromNamespaceAndPath("minecraft", "block/wall_inventory");

    protected final ModX mod;

    private final Set<Item> handheld = new HashSet<>();
    private final Set<Item> ignored = new HashSet<>();
    private final Set<Block> specialBlocks = new HashSet<>();

    public ItemModelProviderBase(DatagenContext ctx) {
        super(ctx.output(), ctx.mod().modid, ctx.fileHelper());
        this.mod = ctx.mod();
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

    /**
     * The {@link BlockItem} of the provided {@link Block} uses a custom {@link BlockEntityWithoutLevelRenderer} such as {@link ItemStackRenderer}.
     */
    protected void specialBlock(Block block) {
        this.specialBlocks.add(block);
    }

    @Override
    protected void registerModels() {
        this.setup();

        for (ResourceLocation id : BuiltInRegistries.ITEM.keySet().stream().sorted().toList()) {
            Item item = BuiltInRegistries.ITEM.get(id);
            if (this.mod.modid.equals(id.getNamespace()) && !this.ignored.contains(item)) {
                if (item instanceof BlockItem blockItem) {
                    this.defaultBlock(id, blockItem);
                } else if (this.handheld.contains(item)) {
                    this.withExistingParent(id.getPath(), HANDHELD).texture("layer0", ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "item/" + id.getPath()));
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
                    .customLoader(DynamicFluidContainerModelBuilder::begin)
                    .fluid(bucketItem.content);
        } else {
            this.withExistingParent(id.getPath(), GENERATED).texture("layer0", ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "item/" + id.getPath()));
        }
    }

    protected void defaultBlock(ResourceLocation id, BlockItem item) {
        if (this.specialBlocks.contains(item.getBlock())) {
            this.getBuilder(id.getPath()).parent(new ModelFile.UncheckedModelFile(SPECIAL_BLOCK_PARENT));
        } else if (item.getBlock() instanceof DecoratedFenceBlock decorated) {
            ResourceLocation parentId = Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(decorated.parent));
            ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(parentId.getNamespace(), "block/" + parentId.getPath());
            this.getBuilder(id.getPath()).parent(new ModelFile.UncheckedModelFile(FENCE_PARENT)).texture("texture", texture);
        } else if (item.getBlock() instanceof DecoratedButton decorated) {
            ResourceLocation parentId = Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(decorated.parent));
            ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(parentId.getNamespace(), "block/" + parentId.getPath());
            this.getBuilder(id.getPath()).parent(new ModelFile.UncheckedModelFile(BUTTON_PARENT)).texture("texture", texture);
        } else if (item.getBlock() instanceof DecoratedWallBlock decorated) {
            ResourceLocation parentId = Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(decorated.parent));
            ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(parentId.getNamespace(), "block/" + parentId.getPath());
            this.getBuilder(id.getPath()).parent(new ModelFile.UncheckedModelFile(WALL_PARENT)).texture("wall", texture);
        } else if (item.getBlock() instanceof DecoratedTrapdoorBlock) {
            this.getBuilder(id.getPath()).parent(new ModelFile.UncheckedModelFile(ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "block/" + id.getPath() + "_bottom")));
        } else if (item.getBlock() instanceof DecoratedDoorBlock || item.getBlock() instanceof DecoratedSign.Standing || item.getBlock() instanceof DecoratedSign.Wall || item.getBlock() instanceof DecoratedHangingSign.Ceiling || item.getBlock() instanceof DecoratedHangingSign.Wall) {
            this.withExistingParent(id.getPath(), GENERATED).texture("layer0", ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "item/" + id.getPath()));
        } else {
            this.getBuilder(id.getPath()).parent(new ModelFile.UncheckedModelFile(ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "block/" + id.getPath())));
        }
    }
}
