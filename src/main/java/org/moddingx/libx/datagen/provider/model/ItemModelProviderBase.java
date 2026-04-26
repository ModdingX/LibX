package org.moddingx.libx.datagen.provider.model;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.item.DynamicFluidContainerModel;
import org.moddingx.libx.LibX;
import org.moddingx.libx.base.SpawnEggItemBase;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.impl.base.decoration.blocks.*;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.render.ItemStackRenderer;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A base class for item model provider. An extending class should call the
 * {@link #handheld(Item)} and {@link #manualModel(Item)} methods in {@link #setup()}.
 */
public abstract class ItemModelProviderBase extends ModelProvider {

    public static final ResourceLocation GENERATED = ResourceLocation.fromNamespaceAndPath("minecraft", "item/generated");
    public static final ResourceLocation HANDHELD = ResourceLocation.fromNamespaceAndPath("minecraft", "item/handheld");
    public static final ResourceLocation SPECIAL_BLOCK_PARENT = LibX.getInstance().resource("item/base/special_block");
    public static final ResourceLocation SPAWN_EGG_PARENT = LibX.getInstance().resource("item/base/spawn_egg");
    public static final ResourceLocation FENCE_PARENT = ResourceLocation.fromNamespaceAndPath("minecraft", "block/fence_inventory");
    public static final ResourceLocation BUTTON_PARENT = ResourceLocation.fromNamespaceAndPath("minecraft", "block/button_inventory");
    public static final ResourceLocation WALL_PARENT = ResourceLocation.fromNamespaceAndPath("minecraft", "block/wall_inventory");

    protected final ModX mod;

    private final Set<Item> handheld = new HashSet<>();
    private final Set<Item> ignored = new HashSet<>();
    private final Set<Block> specialBlocks = new HashSet<>();

    public ItemModelProviderBase(DatagenContext ctx) {
        super(ctx.output(), ctx.mod().modid);
        this.mod = ctx.mod();
    }

    @Nonnull
    @Override
    public final String getName() {
        return this.mod.modid + " item models";
    }

    @Nonnull
    @Override
    protected final Stream<? extends Holder<Block>> getKnownBlocks() {
        return Stream.empty();
    }

    @Nonnull
    @Override
    protected final Stream<? extends Holder<Item>> getKnownItems() {
        return BuiltInRegistries.ITEM.listElements()
                .filter(holder -> this.mod.modid.equals(holder.getKey().location().getNamespace()))
                .filter(holder -> !this.ignored.contains(holder.value()));
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
     * The {@link BlockItem} of the provided {@link Block} uses a special model renderer such as {@link ItemStackRenderer}.
     */
    protected void specialBlock(Block block) {
        this.specialBlocks.add(block);
    }

    @Override
    protected void registerModels(@Nonnull BlockModelGenerators blockModels, @Nonnull ItemModelGenerators itemModels) {
        this.setup();

        for (ResourceLocation id : BuiltInRegistries.ITEM.keySet().stream().sorted().toList()) {
            Item item = BuiltInRegistries.ITEM.getValue(id);
            if (this.mod.modid.equals(id.getNamespace()) && !this.ignored.contains(item)) {
                if (item instanceof BlockItem blockItem) {
                    this.defaultBlock(id, blockItem, itemModels);
                } else if (this.handheld.contains(item)) {
                    itemModels.generateFlatItem(item, ModelTemplates.FLAT_HANDHELD_ITEM);
                } else {
                    this.defaultItem(item, itemModels);
                }
            }
        }
    }

    protected abstract void setup();

    protected void defaultItem(Item item, ItemModelGenerators itemModels) {
        if (item instanceof SpawnEggItemBase spawnEggItem) {
            itemModels.itemModelOutput.accept(
                    item,
                    ItemModelUtils.tintedModel(
                            SPAWN_EGG_PARENT,
                            ItemModelUtils.constantTint(spawnEggItem.getPrimaryColor()),
                            ItemModelUtils.constantTint(spawnEggItem.getSecondaryColor())
                    )
            );
        } else if (item instanceof BucketItem bucketItem) {
            itemModels.itemModelOutput.accept(
                    item,
                    new DynamicFluidContainerModel.Unbaked(
                            new DynamicFluidContainerModel.Textures(
                                    Optional.of(ResourceLocation.withDefaultNamespace("item/bucket")),
                                    Optional.of(ResourceLocation.withDefaultNamespace("item/bucket")),
                                    Optional.of(ResourceLocation.fromNamespaceAndPath("neoforge", "item/mask/bucket_fluid")),
                                    Optional.of(ResourceLocation.fromNamespaceAndPath("neoforge", "item/mask/bucket_fluid_cover"))
                            ),
                            bucketItem.content, false, true, true
                    )
            );
        } else {
            itemModels.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
        }
    }

    protected void defaultBlock(ResourceLocation id, BlockItem item, ItemModelGenerators itemModels) {
        if (this.specialBlocks.contains(item.getBlock())) {
            itemModels.itemModelOutput.accept(item, ItemModelUtils.specialModel(SPECIAL_BLOCK_PARENT, new ItemStackRenderer.Unbaked()));
        } else if (item.getBlock() instanceof DecoratedFenceBlock decorated) {
            ResourceLocation parentId = Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(decorated.parent));
            ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(parentId.getNamespace(), "block/" + parentId.getPath());
            ResourceLocation model = this.createItemModel(id, FENCE_PARENT, TextureMapping.singleSlot(TextureSlot.TEXTURE, texture), TextureSlot.TEXTURE, itemModels);
            itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(model));
        } else if (item.getBlock() instanceof DecoratedButton decorated) {
            ResourceLocation parentId = Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(decorated.parent));
            ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(parentId.getNamespace(), "block/" + parentId.getPath());
            ResourceLocation model = this.createItemModel(id, BUTTON_PARENT, TextureMapping.singleSlot(TextureSlot.TEXTURE, texture), TextureSlot.TEXTURE, itemModels);
            itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(model));
        } else if (item.getBlock() instanceof DecoratedWallBlock decorated) {
            ResourceLocation parentId = Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(decorated.parent));
            ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(parentId.getNamespace(), "block/" + parentId.getPath());
            ResourceLocation model = this.createItemModel(id, WALL_PARENT, TextureMapping.singleSlot(TextureSlot.WALL, texture), TextureSlot.WALL, itemModels);
            itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(model));
        } else if (item.getBlock() instanceof DecoratedTrapdoorBlock) {
            itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "block/" + id.getPath() + "_bottom")));
        } else if (item.getBlock() instanceof DecoratedDoorBlock
                || item.getBlock() instanceof DecoratedSign.Standing
                || item.getBlock() instanceof DecoratedSign.Wall
                || item.getBlock() instanceof DecoratedHangingSign.Ceiling
                || item.getBlock() instanceof DecoratedHangingSign.Wall) {
            itemModels.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
        } else {
            itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "block/" + id.getPath())));
        }
    }

    private ResourceLocation createItemModel(ResourceLocation id, ResourceLocation parent, TextureMapping texture, TextureSlot requiredSlot, ItemModelGenerators itemModels) {
        ModelTemplate template = new ModelTemplate(Optional.of(parent), Optional.empty(), requiredSlot);
        ResourceLocation model = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "item/" + id.getPath());
        return template.create(model, texture, itemModels.modelOutput);
    }
}
