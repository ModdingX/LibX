package org.moddingx.libx.datagen.provider.model;

import com.mojang.math.Quadrant;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.impl.base.decoration.blocks.*;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.util.lazy.LazyValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A base class for block state and model providers. An extending class should call the
 * {@link #manualState(Block)} and {@link #manualModel(Block)} methods in {@link #setup()}.
 */
public abstract class BlockStateProviderBase extends ModelProvider {

    public static final Identifier LEAVES_PARENT = Identifier.fromNamespaceAndPath("minecraft", "block/leaves");
    public static final Identifier BUTTON_PARENT = Identifier.fromNamespaceAndPath("minecraft", "block/button");
    public static final Identifier PRESSED_BUTTON_PARENT = Identifier.fromNamespaceAndPath("minecraft", "block/button_pressed");
    public static final Identifier PRESSURE_PLATE_PARENT = Identifier.fromNamespaceAndPath("minecraft", "block/pressure_plate_up");
    public static final Identifier PRESSED_PRESSURE_PLATE_PARENT = Identifier.fromNamespaceAndPath("minecraft", "block/pressure_plate_down");

    protected final ModX mod;
    private final ResourceManager clientResources;

    private final Set<Block> manualState = new HashSet<>();
    private final Set<Block> existingModel = new HashSet<>();
    private final Map<Block, Identifier> customModel = new HashMap<>();

    private boolean forceTranslucent = false;

    @Nullable
    private BlockModelGenerators blockModels = null;

    public BlockStateProviderBase(DatagenContext ctx) {
        super(ctx.output(), ctx.mod().modid);
        this.mod = ctx.mod();
        this.clientResources = ctx.resourceManager(PackType.CLIENT_RESOURCES);
    }

    @Nonnull
    @Override
    public final String getName() {
        return this.mod.modid + " block states and models";
    }

    @Nonnull
    @Override
    protected final Stream<? extends Holder<Block>> getKnownBlocks() {
        return BuiltInRegistries.BLOCK.listElements()
                .filter(holder -> this.mod.modid.equals(holder.getKey().identifier().getNamespace()))
                .filter(holder -> !this.manualState.contains(holder.value()));
    }

    @Nonnull
    @Override
    protected final Stream<? extends Holder<Item>> getKnownItems() {
        return Stream.empty();
    }

    /**
     * The provider will not process this block.
     */
    protected void manualState(Block block) {
        this.manualState.add(block);
    }

    /**
     * The provider will add a block state for a custom manual model.
     */
    protected void manualModel(Block block) {
        this.existingModel.add(block);
    }

    /**
     * The provider will add a block state with the given model location.
     */
    protected void manualModel(Block block, Identifier model) {
        this.customModel.put(block, model);
    }

    /**
     * Sets whether the {@link Material materials} generated from here on are marked as translucent.
     * Like the render type it replaces, this stays in effect until it is changed again, so it is
     * typically set in {@link #setup()} around the blocks that need it.
     *
     * Translucency is a property of the material in this version, not a render type on the model.
     * A block whose texture has partially transparent pixels needs this, or those pixels are drawn
     * as fully opaque or fully cut out.
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
    protected final void registerModels(@Nonnull BlockModelGenerators blockModels, @Nonnull ItemModelGenerators itemModels) {
        this.blockModels = blockModels;
        this.setup();

        for (Identifier id : BuiltInRegistries.BLOCK.keySet().stream().sorted().toList()) {
            Block block = BuiltInRegistries.BLOCK.getValue(id);
            if (this.mod.modid.equals(id.getNamespace()) && !this.manualState.contains(block)) {
                if (this.existingModel.contains(block)) {
                    this.defaultState(id, block, () -> blockModelId(id));
                } else if (this.customModel.containsKey(block)) {
                    this.defaultState(id, block, () -> this.customModel.get(block));
                } else {
                    LazyValue<Identifier> model = new LazyValue<>(() -> this.defaultModel(id, block));
                    this.defaultState(id, block, model::get);
                }
            }
        }
    }

    protected abstract void setup();

    /**
     * Creates a block state for the given block using the given model.
     */
    protected void defaultState(Identifier id, Block block, Supplier<Identifier> model) {
        BlockModelGenerators generators = this.models();

        if (block instanceof DecoratedWoodBlock decorated) {
            Identifier textureSide;
            Identifier textureTop;
            if (decorated.log == null) {
                textureSide = textureId(id);
                textureTop = textureId(id, "top");
            } else if (decorated.parent.has(decorated.log)) {
                Identifier logId = Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(decorated.parent.get(decorated.log)));
                textureSide = textureId(logId);
                textureTop = textureId(logId);
            } else {
                textureSide = textureId(id);
                textureTop = textureId(id);
            }
            Identifier axisModel = this.createBlockModel(
                    blockModelId(id),
                    ModelTemplates.CUBE_COLUMN,
                    TextureMapping.column(this.material(textureSide), this.material(textureTop))
            );
            generators.blockStateOutput.accept(BlockModelGenerators.createAxisAlignedPillarBlock(block, variant(axisModel)));
        } else if (block instanceof DecoratedSlabBlock decorated) {
            Identifier texture = textureId(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(decorated.parent)));
            Material material = this.material(texture);
            TextureMapping mapping = new TextureMapping().put(TextureSlot.BOTTOM, material).put(TextureSlot.TOP, material).put(TextureSlot.SIDE, material);
            Identifier bottom = this.createBlockModel(blockModelId(id), ModelTemplates.SLAB_BOTTOM, mapping);
            Identifier top = this.createBlockModel(blockModelId(id, "_top"), ModelTemplates.SLAB_TOP, mapping);
            Identifier doubled = blockModelId(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(decorated.parent)));
            generators.blockStateOutput.accept(BlockModelGenerators.createSlab(block, variant(bottom), variant(top), variant(doubled)));
        } else if (block instanceof DecoratedStairBlock decorated) {
            Identifier texture = textureId(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(decorated.parent)));
            Material material = this.material(texture);
            TextureMapping mapping = new TextureMapping().put(TextureSlot.BOTTOM, material).put(TextureSlot.TOP, material).put(TextureSlot.SIDE, material);
            Identifier inner = this.createBlockModel(blockModelId(id, "_inner"), ModelTemplates.STAIRS_INNER, mapping);
            Identifier straight = this.createBlockModel(blockModelId(id), ModelTemplates.STAIRS_STRAIGHT, mapping);
            Identifier outer = this.createBlockModel(blockModelId(id, "_outer"), ModelTemplates.STAIRS_OUTER, mapping);
            generators.blockStateOutput.accept(BlockModelGenerators.createStairs(block, variant(inner), variant(straight), variant(outer)));
        } else if (block instanceof DecoratedWallBlock decorated) {
            Identifier texture = textureId(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(decorated.parent)));
            this.wallBlock(block, texture);
        } else if (block instanceof DecoratedFenceBlock decorated) {
            Identifier texture = textureId(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(decorated.parent)));
            this.fenceBlock(block, texture);
        } else if (block instanceof DecoratedFenceGateBlock decorated) {
            Identifier texture = textureId(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(decorated.parent)));
            this.fenceGateBlock(block, texture);
        } else if (block instanceof DecoratedButton decorated) {
            this.buttonBlock(block, textureId(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(decorated.parent))));
        } else if (block instanceof DecoratedPressurePlate decorated) {
            this.pressurePlateBlock(block, textureId(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(decorated.parent))));
        } else if (block instanceof DecoratedDoorBlock) {
            this.doorBlock(block, textureId(id, "bottom"), textureId(id, "top"));
        } else if (block instanceof DecoratedTrapdoorBlock) {
            this.trapdoorBlock(block, textureId(id), true);
        } else if (block instanceof DecoratedSign.Standing decorated) {
            Identifier particle = textureId(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(decorated.parent)));
            Identifier signModel = this.createBlockModel(blockModelId(id), ModelTemplates.PARTICLE_ONLY, TextureMapping.particle(this.material(particle)));
            generators.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, variant(signModel)));
        } else if (block instanceof DecoratedSign.Wall decorated) {
            Identifier particle = textureId(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(decorated.parent)));
            Identifier signModel = this.createBlockModel(blockModelId(id), ModelTemplates.PARTICLE_ONLY, TextureMapping.particle(this.material(particle)));
            generators.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, variant(signModel)));
        } else if (block instanceof DecoratedHangingSign.Ceiling decorated) {
            Identifier particle = textureId(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(decorated.parent)));
            Identifier signModel = this.createBlockModel(blockModelId(id), ModelTemplates.PARTICLE_ONLY, TextureMapping.particle(this.material(particle)));
            generators.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, variant(signModel)));
        } else if (block instanceof DecoratedHangingSign.Wall decorated) {
            Identifier particle = textureId(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(decorated.parent)));
            Identifier signModel = this.createBlockModel(blockModelId(id), ModelTemplates.PARTICLE_ONLY, TextureMapping.particle(this.material(particle)));
            generators.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, variant(signModel)));
        } else if (block.getStateDefinition().getProperties().contains(BlockStateProperties.HORIZONTAL_FACING)) {
            generators.blockStateOutput.accept(
                    MultiVariantGenerator.dispatch(block, variant(model.get()))
                            .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING)
            );
        } else if (block.getStateDefinition().getProperties().contains(BlockStateProperties.FACING)) {
            generators.blockStateOutput.accept(
                    MultiVariantGenerator.dispatch(block, variant(model.get()))
                            .with(PropertyDispatch.modify(BlockStateProperties.FACING)
                                    .select(Direction.DOWN, rotation(180, 0))
                                    .select(Direction.UP, rotation(0, 0))
                                    .select(Direction.NORTH, rotation(90, 0))
                                    .select(Direction.SOUTH, rotation(90, 180))
                                    .select(Direction.WEST, rotation(90, 270))
                                    .select(Direction.EAST, rotation(90, 90))
                            )
            );
        } else {
            generators.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, variant(model.get())));
        }
    }

    /**
     * Creates a model for the given block.
     */
    protected Identifier defaultModel(Identifier id, Block block) {
        if (block.getStateDefinition().getPossibleStates().stream().allMatch(state -> state.getRenderShape() != RenderShape.MODEL)) {
            if (block instanceof LiquidBlock liquidBlock) {
                Optional<Identifier> tex = Optional.ofNullable(this.fluidTextureId(liquidBlock.fluid.getSource().getFluidType()));
                TextureMapping mapping = tex.map(texture -> TextureMapping.particle(this.material(texture))).orElseGet(() -> TextureMapping.particle(this.material(textureId(id))));
                return this.createBlockModel(blockModelId(id), ModelTemplates.PARTICLE_ONLY, mapping);
            }
            return this.createBlockModel(blockModelId(id), ModelTemplates.PARTICLE_ONLY, TextureMapping.particle(this.material(textureId(id))));
        } else {
            return this.createBlockModel(blockModelId(id), ModelTemplates.CUBE_ALL, TextureMapping.cube(this.material(textureId(id))));
        }
    }

    /**
     * Creates a block state and models for a button.
     */
    public void buttonBlock(Block block, Identifier texture) {
        TextureMapping mapping = TextureMapping.singleSlot(TextureSlot.TEXTURE, this.material(texture));
        Identifier model = this.createBlockModel(blockModelId(block), ModelTemplates.BUTTON, mapping);
        Identifier pressed = this.createBlockModel(blockModelId(block, "_pressed"), ModelTemplates.BUTTON_PRESSED, mapping);
        this.models().blockStateOutput.accept(BlockModelGenerators.createButton(block, variant(model), variant(pressed)));
    }

    /**
     * Creates a block state and models for a pressure plate.
     */
    public void pressurePlateBlock(Block block, Identifier texture) {
        TextureMapping mapping = TextureMapping.singleSlot(TextureSlot.TEXTURE, this.material(texture));
        Identifier model = this.createBlockModel(blockModelId(block), ModelTemplates.PRESSURE_PLATE_UP, mapping);
        Identifier pressed = this.createBlockModel(blockModelId(block, "_down"), ModelTemplates.PRESSURE_PLATE_DOWN, mapping);
        this.models().blockStateOutput.accept(BlockModelGenerators.createPressurePlate(block, variant(model), variant(pressed)));
    }

    public void wallBlock(Block block, Identifier texture) {
        TextureMapping mapping = TextureMapping.singleSlot(TextureSlot.WALL, this.material(texture));
        Identifier post = this.createBlockModel(blockModelId(block, "_post"), ModelTemplates.WALL_POST, mapping);
        Identifier low = this.createBlockModel(blockModelId(block, "_side"), ModelTemplates.WALL_LOW_SIDE, mapping);
        Identifier tall = this.createBlockModel(blockModelId(block, "_side_tall"), ModelTemplates.WALL_TALL_SIDE, mapping);
        this.models().blockStateOutput.accept(BlockModelGenerators.createWall(block, variant(post), variant(low), variant(tall)));
    }

    public void fenceBlock(Block block, Identifier texture) {
        TextureMapping mapping = TextureMapping.singleSlot(TextureSlot.TEXTURE, this.material(texture));
        Identifier post = this.createBlockModel(blockModelId(block, "_post"), ModelTemplates.FENCE_POST, mapping);
        Identifier side = this.createBlockModel(blockModelId(block, "_side"), ModelTemplates.FENCE_SIDE, mapping);
        this.models().blockStateOutput.accept(BlockModelGenerators.createFence(block, variant(post), variant(side)));
    }

    public void fenceGateBlock(Block block, Identifier texture) {
        TextureMapping mapping = TextureMapping.singleSlot(TextureSlot.TEXTURE, this.material(texture));
        Identifier open = this.createBlockModel(blockModelId(block, "_open"), ModelTemplates.FENCE_GATE_OPEN, mapping);
        Identifier closed = this.createBlockModel(blockModelId(block), ModelTemplates.FENCE_GATE_CLOSED, mapping);
        Identifier wallOpen = this.createBlockModel(blockModelId(block, "_wall_open"), ModelTemplates.FENCE_GATE_WALL_OPEN, mapping);
        Identifier wallClosed = this.createBlockModel(blockModelId(block, "_wall"), ModelTemplates.FENCE_GATE_WALL_CLOSED, mapping);
        this.models().blockStateOutput.accept(BlockModelGenerators.createFenceGate(block, variant(open), variant(closed), variant(wallOpen), variant(wallClosed), true));
    }

    public void doorBlock(Block block, Identifier bottom, Identifier top) {
        TextureMapping mapping = TextureMapping.door(this.material(top), this.material(bottom));
        Identifier bl = this.createBlockModel(blockModelId(block, "_bottom_left"), ModelTemplates.DOOR_BOTTOM_LEFT, mapping);
        Identifier blo = this.createBlockModel(blockModelId(block, "_bottom_left_open"), ModelTemplates.DOOR_BOTTOM_LEFT_OPEN, mapping);
        Identifier br = this.createBlockModel(blockModelId(block, "_bottom_right"), ModelTemplates.DOOR_BOTTOM_RIGHT, mapping);
        Identifier bro = this.createBlockModel(blockModelId(block, "_bottom_right_open"), ModelTemplates.DOOR_BOTTOM_RIGHT_OPEN, mapping);
        Identifier tl = this.createBlockModel(blockModelId(block, "_top_left"), ModelTemplates.DOOR_TOP_LEFT, mapping);
        Identifier tlo = this.createBlockModel(blockModelId(block, "_top_left_open"), ModelTemplates.DOOR_TOP_LEFT_OPEN, mapping);
        Identifier tr = this.createBlockModel(blockModelId(block, "_top_right"), ModelTemplates.DOOR_TOP_RIGHT, mapping);
        Identifier tro = this.createBlockModel(blockModelId(block, "_top_right_open"), ModelTemplates.DOOR_TOP_RIGHT_OPEN, mapping);
        this.models().blockStateOutput.accept(BlockModelGenerators.createDoor(block, variant(bl), variant(blo), variant(br), variant(bro), variant(tl), variant(tlo), variant(tr), variant(tro)));
    }

    public void trapdoorBlock(Block block, Identifier texture, boolean orientable) {
        TextureMapping mapping = TextureMapping.singleSlot(TextureSlot.TEXTURE, this.material(texture));
        Identifier top;
        Identifier bottom;
        Identifier open;
        if (orientable) {
            top = this.createBlockModel(blockModelId(block, "_top"), ModelTemplates.ORIENTABLE_TRAPDOOR_TOP, mapping);
            bottom = this.createBlockModel(blockModelId(block, "_bottom"), ModelTemplates.ORIENTABLE_TRAPDOOR_BOTTOM, mapping);
            open = this.createBlockModel(blockModelId(block, "_open"), ModelTemplates.ORIENTABLE_TRAPDOOR_OPEN, mapping);
            this.models().blockStateOutput.accept(BlockModelGenerators.createOrientableTrapdoor(block, variant(top), variant(bottom), variant(open)));
        } else {
            top = this.createBlockModel(blockModelId(block, "_top"), ModelTemplates.TRAPDOOR_TOP, mapping);
            bottom = this.createBlockModel(blockModelId(block, "_bottom"), ModelTemplates.TRAPDOOR_BOTTOM, mapping);
            open = this.createBlockModel(blockModelId(block, "_open"), ModelTemplates.TRAPDOOR_OPEN, mapping);
            this.models().blockStateOutput.accept(BlockModelGenerators.createTrapdoor(block, variant(top), variant(bottom), variant(open)));
        }
    }

    protected BlockModelGenerators models() {
        if (this.blockModels == null) throw new IllegalStateException("Not in registerModels call.");
        return this.blockModels;
    }

    protected Identifier createBlockModel(Identifier modelId, ModelTemplate template, TextureMapping mapping) {
        return template.create(modelId, mapping, this.models().modelOutput);
    }

    private static MultiVariant variant(Identifier model) {
        return BlockModelGenerators.plainVariant(model);
    }

    private static VariantMutator rotation(int xRot, int yRot) {
        Quadrant x = toRotation(xRot);
        Quadrant y = toRotation(yRot);
        return variant -> {
            if (x != Quadrant.R0) variant = variant.with(VariantMutator.X_ROT.withValue(x));
            if (y != Quadrant.R0) variant = variant.with(VariantMutator.Y_ROT.withValue(y));
            return variant;
        };
    }

    private static Quadrant toRotation(int rot) {
        return switch(Math.floorMod(rot, 360)) {
            case 0 -> Quadrant.R0;
            case 90 -> Quadrant.R90;
            case 180 -> Quadrant.R180;
            case 270 -> Quadrant.R270;
            default -> throw new IllegalArgumentException("Invalid rotation: " + rot);
        };
    }

    protected static Identifier blockModelId(Identifier blockId) {
        return Identifier.fromNamespaceAndPath(blockId.getNamespace(), "block/" + blockId.getPath());
    }

    protected static Identifier blockModelId(Identifier blockId, String suffix) {
        return Identifier.fromNamespaceAndPath(blockId.getNamespace(), "block/" + blockId.getPath() + suffix);
    }

    protected static Identifier blockModelId(Block block) {
        return blockModelId(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(block)));
    }

    protected static Identifier blockModelId(Block block, String suffix) {
        return blockModelId(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(block)), suffix);
    }

    protected static Identifier textureId(Identifier blockId) {
        return Identifier.fromNamespaceAndPath(blockId.getNamespace(), "block/" + blockId.getPath());
    }

    protected static Identifier textureId(Identifier blockId, String suffix) {
        return Identifier.fromNamespaceAndPath(blockId.getNamespace(), "block/" + blockId.getPath() + "_" + suffix);
    }

    /**
     * Retrieves the texture for a given {@link FluidType}.
     */
    @Nullable
    protected Identifier fluidTextureId(FluidType fluidType) {
        Identifier id = NeoForgeRegistries.FLUID_TYPES.getKey(fluidType);
        if (id == null) throw new IllegalStateException("fluid type not registered: " + fluidType);
        Identifier texture = Identifier.fromNamespaceAndPath(id.getNamespace(), "block/" + id.getPath());
        Identifier textureFile = Identifier.fromNamespaceAndPath(texture.getNamespace(), "textures/" + texture.getPath() + ".png");
        if (this.clientResources.getResource(textureFile).isEmpty()) {
            throw new IllegalStateException("Could not find a texture for fluid " + id + ". You can provide one at " + texture + " or override fluidTextureId.");
        }
        return texture;
    }

}
