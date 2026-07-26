package org.moddingx.libx.datagen.provider.model;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
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

    public static final Identifier GENERATED = Identifier.fromNamespaceAndPath("minecraft", "item/generated");
    public static final Identifier HANDHELD = Identifier.fromNamespaceAndPath("minecraft", "item/handheld");
    public static final Identifier SPECIAL_BLOCK_PARENT = LibX.getInstance().id("item/base/special_block");
    public static final Identifier SPAWN_EGG_PARENT = LibX.getInstance().id("item/base/spawn_egg");
    public static final Identifier FENCE_PARENT = Identifier.fromNamespaceAndPath("minecraft", "block/fence_inventory");
    public static final Identifier BUTTON_PARENT = Identifier.fromNamespaceAndPath("minecraft", "block/button_inventory");
    public static final Identifier WALL_PARENT = Identifier.fromNamespaceAndPath("minecraft", "block/wall_inventory");

    protected final ModX mod;

    private final Set<Item> handheld = new HashSet<>();
    private final Set<Item> ignored = new HashSet<>();
    private final Set<Block> specialBlocks = new HashSet<>();

    private boolean forceTranslucent = false;

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
                .filter(holder -> this.mod.modid.equals(holder.getKey().identifier().getNamespace()))
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

    /**
     * Sets whether the {@link Material materials} generated from here on are marked as translucent.
     * Like the render type it replaces, this stays in effect until it is changed again, so it is
     * typically set in {@link #setup()} around the items that need it.
     */
    protected void setTranslucent(boolean translucent) {
        this.forceTranslucent = translucent;
    }

    /**
     * Creates a {@link Material} for the given texture. Every material this provider generates goes
     * through here, so overriding this is the way to customize them beyond
     * {@link #setTranslucent(boolean)}.
     */
    protected Material material(Identifier texture) {
        return new Material(texture, this.forceTranslucent);
    }

    @Override
    protected void registerModels(@Nonnull BlockModelGenerators blockModels, @Nonnull ItemModelGenerators itemModels) {
        this.setup();

        for (Identifier id : BuiltInRegistries.ITEM.keySet().stream().sorted().toList()) {
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
                                    Optional.of(this.material(Identifier.withDefaultNamespace("item/bucket"))),
                                    Optional.of(this.material(Identifier.withDefaultNamespace("item/bucket"))),
                                    Optional.of(this.material(Identifier.fromNamespaceAndPath("neoforge", "item/mask/bucket_fluid"))),
                                    Optional.empty()
                            ),
                            bucketItem.content, false, true, true
                    )
            );
        } else {
            itemModels.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
        }
    }

    protected void defaultBlock(Identifier id, BlockItem item, ItemModelGenerators itemModels) {
        if (this.specialBlocks.contains(item.getBlock())) {
            itemModels.itemModelOutput.accept(item, ItemModelUtils.specialModel(SPECIAL_BLOCK_PARENT, new ItemStackRenderer.Unbaked()));
        } else if (item.getBlock() instanceof DecoratedFenceBlock decorated) {
            Identifier parentId = Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(decorated.parent));
            Identifier texture = Identifier.fromNamespaceAndPath(parentId.getNamespace(), "block/" + parentId.getPath());
            Identifier model = this.createItemModel(id, FENCE_PARENT, TextureMapping.singleSlot(TextureSlot.TEXTURE, this.material(texture)), TextureSlot.TEXTURE, itemModels);
            itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(model));
        } else if (item.getBlock() instanceof DecoratedButton decorated) {
            Identifier parentId = Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(decorated.parent));
            Identifier texture = Identifier.fromNamespaceAndPath(parentId.getNamespace(), "block/" + parentId.getPath());
            Identifier model = this.createItemModel(id, BUTTON_PARENT, TextureMapping.singleSlot(TextureSlot.TEXTURE, this.material(texture)), TextureSlot.TEXTURE, itemModels);
            itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(model));
        } else if (item.getBlock() instanceof DecoratedWallBlock decorated) {
            Identifier parentId = Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(decorated.parent));
            Identifier texture = Identifier.fromNamespaceAndPath(parentId.getNamespace(), "block/" + parentId.getPath());
            Identifier model = this.createItemModel(id, WALL_PARENT, TextureMapping.singleSlot(TextureSlot.WALL, this.material(texture)), TextureSlot.WALL, itemModels);
            itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(model));
        } else if (item.getBlock() instanceof DecoratedTrapdoorBlock) {
            itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(Identifier.fromNamespaceAndPath(id.getNamespace(), "block/" + id.getPath() + "_bottom")));
        } else if (item.getBlock() instanceof DecoratedDoorBlock
                || item.getBlock() instanceof DecoratedSign.Standing
                || item.getBlock() instanceof DecoratedSign.Wall
                || item.getBlock() instanceof DecoratedHangingSign.Ceiling
                || item.getBlock() instanceof DecoratedHangingSign.Wall) {
            itemModels.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
        } else {
            itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(Identifier.fromNamespaceAndPath(id.getNamespace(), "block/" + id.getPath())));
        }
    }

    private Identifier createItemModel(Identifier id, Identifier parent, TextureMapping texture, TextureSlot requiredSlot, ItemModelGenerators itemModels) {
        ModelTemplate template = new ModelTemplate(Optional.of(parent), Optional.empty(), requiredSlot);
        Identifier model = Identifier.fromNamespaceAndPath(id.getNamespace(), "item/" + id.getPath());
        return template.create(model, texture, itemModels.modelOutput);
    }
}
