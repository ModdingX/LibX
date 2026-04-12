package org.moddingx.libx.datagen.provider.model;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.model.generators.*;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.impl.base.decoration.blocks.*;
import org.moddingx.libx.impl.datagen.model.TypedBlockModelProvider;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.util.lazy.LazyValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A base class for block state and model providers. An extending class should call the
 * {@link #manualState(Block) manualState} and {@link #manualModel(Block) manualModel} methods in
 * {@link #setup() setup}.
 * Another thing you can do is override {@link #defaultState(ResourceLocation, Block, Supplier) defaultState}
 * and {@link #defaultModel(ResourceLocation, Block) defaultModel} to adjust the state and model depending
 * on the block.
 */
public abstract class BlockStateProviderBase extends BlockStateProvider {

    public static final ResourceLocation LEAVES_PARENT = ResourceLocation.fromNamespaceAndPath("minecraft", "block/leaves");
    public static final ResourceLocation BUTTON_PARENT = ResourceLocation.fromNamespaceAndPath("minecraft", "block/button");
    public static final ResourceLocation PRESSED_BUTTON_PARENT = ResourceLocation.fromNamespaceAndPath("minecraft", "block/button_pressed");
    public static final ResourceLocation PRESSURE_PLATE_PARENT = ResourceLocation.fromNamespaceAndPath("minecraft", "block/pressure_plate_up");
    public static final ResourceLocation PRESSED_PRESSURE_PLATE_PARENT = ResourceLocation.fromNamespaceAndPath("minecraft", "block/pressure_plate_down");

    protected final ModX mod;
    protected final PackOutput packOutput;
    protected final ExistingFileHelper fileHelper;

    private final Set<Block> manualState = new HashSet<>();
    private final Set<Block> existingModel = new HashSet<>();
    private final Map<Block, ModelFile> customModel = new HashMap<>();
    
    @Nullable
    private ResourceLocation currentRenderTypes = null;
    private final Map<ResourceLocation, TypedBlockModelProvider> typedModelProviders = new HashMap<>();

    public BlockStateProviderBase(DatagenContext ctx) {
        super(ctx.output(), ctx.mod().modid, ctx.fileHelper());
        this.mod = ctx.mod();
        this.packOutput = ctx.output();
        this.fileHelper = ctx.fileHelper();
    }

    @Nonnull
    @Override
    public final String getName() {
        return this.mod.modid + " block states and models";
    }

    /**
     * The provider will not process this block.
     */
    protected void manualState(Block b) {
        this.manualState.add(b);
    }

    /**
     * The provider will add a block state for a custom manual model
     */
    protected void manualModel(Block b) {
        this.existingModel.add(b);
    }

    /**
     * The provider will add a block state with the given model
     */
    protected void manualModel(Block b, ModelFile model) {
        this.customModel.put(b, model);
    }
    
    @Nonnull
    @Override
    public BlockModelProvider models() {
        return this.models(this.currentRenderTypes);
    }

    /**
     * Gets a {@link BlockModelProvider} which assigns the given {@link RenderTypeGroup} to each created builder.
     * 
     * @param renderTypes The default {@link RenderTypeGroup} to use, or {@code null} for no render types (ie the default {@link BlockModelProvider})
     */
    protected BlockModelProvider models(@Nullable ResourceLocation renderTypes) {
        if (renderTypes == null) {
            return super.models();
        } else {
            return this.typedModelProviders.computeIfAbsent(renderTypes, k -> new TypedBlockModelProvider(this.packOutput, this.mod.modid, this.fileHelper, renderTypes));
        }
    }

    /**
     * Makes {@link #models()} behave as if it {@link #models(ResourceLocation)} was called with the given argument.
     */
    protected void setRenderType(@Nullable ResourceLocation renderTypes) {
        this.currentRenderTypes = renderTypes;
    }

    @Override
    protected final void registerStatesAndModels() {
        this.setup();

        for (ResourceLocation id : BuiltInRegistries.BLOCK.keySet().stream().sorted().toList()) {
            Block block = BuiltInRegistries.BLOCK.getValue(id);
            if (this.mod.modid.equals(id.getNamespace()) && !this.manualState.contains(block)) {
                if (this.existingModel.contains(block)) {
                    this.defaultState(id, block, () -> this.models().getExistingFile(ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "block/" + id.getPath())));
                } else if (this.customModel.containsKey(block)) {
                    this.defaultState(id, block, () -> this.customModel.get(block));
                } else {
                    LazyValue<ModelFile> defaultModel = new LazyValue<>(() -> this.defaultModel(id, block));
                    this.defaultState(id, block, defaultModel::get);
                }
            }
        }
    }

    @Nonnull
    @Override
    public CompletableFuture<?> run(@Nonnull CachedOutput cache) {
        CompletableFuture<?> mainFuture = super.run(cache);
        return CompletableFuture.allOf(Stream.concat(Stream.of(mainFuture), this.typedModelProviders.values().stream()
                .map(provider -> provider.generateAll(cache))
        ).toArray(CompletableFuture[]::new));
    }

    protected abstract void setup();
    
    /**
     * Creates a block state for the given block using the given model. The default implementation checks
     * whether the block has the properties {@link BlockStateProperties#HORIZONTAL_FACING} or
     * {@link BlockStateProperties#FACING} and creates block states matching those.
     * If you don't use the model, don't call the supplier, so the default model is not generated.
     */
    protected void defaultState(ResourceLocation id, Block block, Supplier<ModelFile> model) {
        if (block instanceof DecoratedWoodBlock decorated) {
            ResourceLocation textureSide;
            ResourceLocation textureTop;
            if (decorated.log == null) {
                textureSide = textureId(id);
                textureTop = textureId(id, "top");
            } else if (decorated.parent.has(decorated.log)) {
                ResourceLocation logId = Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(decorated.parent.get(decorated.log)));
                textureSide = textureId(logId);
                textureTop = textureId(logId);
            } else {
                textureSide = textureId(id);
                textureTop = textureId(id);
            }
            this.axisBlock(decorated, textureSide, textureTop);
        } else if (block instanceof DecoratedSlabBlock decorated) {
            this.slabBlock(decorated, textureId(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(decorated.parent))), textureId(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(decorated.parent))));
        } else if (block instanceof DecoratedStairBlock decorated) {
            this.stairsBlock(decorated, textureId(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(decorated.parent))));
        } else if (block instanceof DecoratedWallBlock decorated) {
            this.wallBlock(decorated, textureId(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(decorated.parent))));
        } else if (block instanceof DecoratedFenceBlock decorated) {
            this.fenceBlock(decorated, textureId(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(decorated.parent))));
        } else if (block instanceof DecoratedFenceGateBlock decorated) {
            this.fenceGateBlock(decorated, textureId(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(decorated.parent))));
        } else if (block instanceof DecoratedButton decorated) {
            this.buttonBlock(decorated, textureId(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(decorated.parent))));
        } else if (block instanceof DecoratedPressurePlate decorated) {
            this.pressurePlateBlock(decorated, textureId(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(decorated.parent))));
        } else if (block instanceof DecoratedDoorBlock decorated) {
            this.doorBlockWithRenderType(decorated, textureId(id, "bottom"), textureId(id, "top"), RenderTypes.CUTOUT);
        } else if (block instanceof DecoratedTrapdoorBlock decorated) {
            this.trapdoorBlockWithRenderType(decorated, textureId(id), true, RenderTypes.CUTOUT);
        } else if (block instanceof DecoratedSign.Standing decorated) {
            this.getVariantBuilder(block).partialState().addModels(new ConfiguredModel(this.models().getBuilder(id.getPath()).texture("particle", textureId(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(decorated.parent))))));
        } else if (block instanceof DecoratedSign.Wall decorated) {
            this.getVariantBuilder(block).partialState().addModels(new ConfiguredModel(this.models().getBuilder(id.getPath()).texture("particle", textureId(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(decorated.parent))))));
        } else if (block instanceof DecoratedHangingSign.Ceiling decorated) {
            this.getVariantBuilder(block).partialState().addModels(new ConfiguredModel(this.models().getBuilder(id.getPath()).texture("particle", textureId(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(decorated.parent))))));
        } else if (block instanceof DecoratedHangingSign.Wall decorated) {
            this.getVariantBuilder(block).partialState().addModels(new ConfiguredModel(this.models().getBuilder(id.getPath()).texture("particle", textureId(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(decorated.parent))))));
        } else if (block.getStateDefinition().getProperties().contains(BlockStateProperties.HORIZONTAL_FACING)) {
            VariantBlockStateBuilder builder = this.getVariantBuilder(block);
            for (Direction direction : BlockStateProperties.HORIZONTAL_FACING.getPossibleValues()) {
                builder.partialState().with(BlockStateProperties.HORIZONTAL_FACING, direction)
                        .addModels(new ConfiguredModel(model.get(), 0, (int) direction.getOpposite().toYRot(), false));
            }
        } else if (block.getStateDefinition().getProperties().contains(BlockStateProperties.FACING)) {
            VariantBlockStateBuilder builder = this.getVariantBuilder(block);
            for (Direction direction : BlockStateProperties.FACING.getPossibleValues()) {
                builder.partialState().with(BlockStateProperties.FACING, direction)
                        .addModels(new ConfiguredModel(model.get(), direction == Direction.DOWN ? 180 : direction.getAxis().isHorizontal() ? 90 : 0, direction.getAxis().isVertical() ? 0 : (int) direction.getOpposite().toYRot(), false));
            }
        } else {
            this.simpleBlock(block, model.get());
        }
    }

    /**
     * Creates a model for the given block. The default implementation creates special models for blocks
     * of type {@link LiquidBlock} and {@link LeavesBlock}.
     */
    protected ModelFile defaultModel(ResourceLocation id, Block block) {
        if (block.getStateDefinition().getPossibleStates().stream().allMatch(state -> state.getRenderShape() != RenderShape.MODEL)) {
            if (block instanceof LiquidBlock liquidBlock) {
                Optional<ResourceLocation> tex = Optional.ofNullable(this.fluidTextureId(liquidBlock.fluid.getSource().getFluidType()));
                return tex.map(resourceLocation -> this.models().getBuilder(id.getPath()).texture("particle", resourceLocation))
                        .orElseGet(() -> this.models().getBuilder(id.getPath()));
            } else {
                return this.models().getBuilder(id.getPath()); // We don't need a model for that block.
            }
        } else if (block instanceof LeavesBlock) {
            return this.models().withExistingParent(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(block)).getPath(), LEAVES_PARENT)
                    .texture("all", this.blockTexture(block))
                    .renderType(RenderTypes.CUTOUT_MIPPED);
        } else {
            return this.cubeAll(block);
        }
    }

    /**
     * Creates a block state and models for a button.
     */
    public void buttonBlock(Block block, ResourceLocation texture) {
        ResourceLocation blockId = Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(block));

        ModelFile model = this.models().withExistingParent(blockId.getPath(), BUTTON_PARENT)
                .texture("texture", texture);

        ModelFile pressedModel = this.models().withExistingParent(blockId.getPath() + "_pressed", PRESSED_BUTTON_PARENT)
                .texture("texture", texture);

        VariantBlockStateBuilder builder = this.getVariantBuilder(block);
        for (AttachFace face : BlockStateProperties.ATTACH_FACE.getPossibleValues()) {
            for (Direction direction : BlockStateProperties.HORIZONTAL_FACING.getPossibleValues()) {
                builder.partialState()
                        .with(BlockStateProperties.ATTACH_FACE, face)
                        .with(BlockStateProperties.HORIZONTAL_FACING, direction)
                        .with(BlockStateProperties.POWERED, false)
                        .addModels(new ConfiguredModel(
                                model, face.ordinal() * 90,
                                (int) direction.getOpposite().toYRot(), false
                        ));
                builder.partialState()
                        .with(BlockStateProperties.ATTACH_FACE, face)
                        .with(BlockStateProperties.HORIZONTAL_FACING, direction)
                        .with(BlockStateProperties.POWERED, true)
                        .addModels(new ConfiguredModel(
                                pressedModel, face.ordinal() * 90,
                                (int) direction.getOpposite().toYRot(), false
                        ));
            }
        }
    }
    
    /**
     * Creates a block state and models for a pressure plate.
     */
    public void pressurePlateBlock(Block block, ResourceLocation texture) {
        ResourceLocation blockId = Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(block));

        ModelFile model = this.models().withExistingParent(blockId.getPath(), PRESSURE_PLATE_PARENT)
                .texture("texture", texture);

        ModelFile pressedModel = this.models().withExistingParent(blockId.getPath() + "_down", PRESSED_PRESSURE_PLATE_PARENT)
                .texture("texture", texture);
        
        VariantBlockStateBuilder builder = this.getVariantBuilder(block);
        builder.partialState().with(BlockStateProperties.POWERED, false).addModels(new ConfiguredModel(model));
        builder.partialState().with(BlockStateProperties.POWERED, true).addModels(new ConfiguredModel(pressedModel));
    }
    
    private static ResourceLocation textureId(ResourceLocation blockId) {
        Objects.requireNonNull(blockId);
        return ResourceLocation.fromNamespaceAndPath(blockId.getNamespace(), "block/" + blockId.getPath());
    }
    
    private static ResourceLocation textureId(ResourceLocation blockId, String suffix) {
        Objects.requireNonNull(blockId);
        return ResourceLocation.fromNamespaceAndPath(blockId.getNamespace(), "block/" + blockId.getPath() + "_" + suffix);
    }

    /**
     * Retrieves the texture for a given {@link FluidType}. This is used as particle texture in the default model.
     * The default implementation returns {@code namespace:block/path} if it exists, otherwise throws an exception.
     * Returning {@code null} causes no particle texure to be set.
     */
    @Nullable
    protected ResourceLocation fluidTextureId(FluidType fluidType) {
        ResourceLocation id = NeoForgeRegistries.FLUID_TYPES.getKey(fluidType);
        if (id == null) throw new IllegalStateException("fluid type not registered: " + fluidType);
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "block/" + id.getPath());
        if (!this.fileHelper.exists(texture, ModelProvider.TEXTURE)) {
            throw new IllegalStateException("Could not find a texture for fluid " + id + ". You can provide one at " + texture + " or override fluidTextureId to use a different texture location.");
        }
        return texture;
    }

    /**
     * Provides keys for builtin {@link RenderTypeGroup render types} to use.
     */
    public static class RenderTypes {
        
        private RenderTypes() {
            
        }
        
        public static final ResourceLocation SOLID = ResourceLocation.fromNamespaceAndPath("minecraft", "solid");
        public static final ResourceLocation CUTOUT = ResourceLocation.fromNamespaceAndPath("minecraft", "cutout");
        public static final ResourceLocation CUTOUT_MIPPED = ResourceLocation.fromNamespaceAndPath("minecraft", "cutout_mipped");
        public static final ResourceLocation CUTOUT_MIPPED_ALL = ResourceLocation.fromNamespaceAndPath("minecraft", "cutout_mipped_all");
        public static final ResourceLocation TRANSLUCENT = ResourceLocation.fromNamespaceAndPath("minecraft", "translucent");
        public static final ResourceLocation TRIPWIRE = ResourceLocation.fromNamespaceAndPath("minecraft", "tripwire");
    }
}
