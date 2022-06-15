package org.moddingx.libx.datagen.provider;

import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.IFluidTypeRenderProperties;
import net.minecraftforge.client.RenderProperties;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.moddingx.libx.LibX;
import org.moddingx.libx.impl.base.decoration.blocks.*;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.util.LazyValue;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * A base class for block state and model providers. An extending class should call the
 * {@link #manualState(Block) manualState} and {@link #manualModel(Block) manualModel} methods in
 * {@link #setup() setup}.
 * Another thing you can do is override {@link #defaultState(ResourceLocation, Block, Supplier) defaultState}
 * and {@link #defaultModel(ResourceLocation, Block) defaultModel} to adjust the state and model depending
 * on the block.
 */
public abstract class BlockStateProviderBase extends BlockStateProvider {

    public static final ResourceLocation LEAVES_PARENT = new ResourceLocation("minecraft", "block/leaves");
    public static final ResourceLocation BUTTON_PARENT = new ResourceLocation("minecraft", "block/button");
    public static final ResourceLocation PRESSED_BUTTON_PARENT = new ResourceLocation("minecraft", "block/button_pressed");
    public static final ResourceLocation PRESSURE_PLATE_PARENT = new ResourceLocation("minecraft", "block/pressure_plate_up");
    public static final ResourceLocation PRESSED_PRESSURE_PLATE_PARENT = new ResourceLocation("minecraft", "block/pressure_plate_down");

    protected final ModX mod;

    private static final Set<Block> manualState = new HashSet<>();
    private static final Set<Block> existingModel = new HashSet<>();
    private static final Map<Block, ModelFile> customModel = new HashMap<>();

    public BlockStateProviderBase(ModX mod, DataGenerator generator, ExistingFileHelper fileHelper) {
        super(generator, mod.modid, fileHelper);
        this.mod = mod;
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
        manualState.add(b);
    }

    /**
     * The provider will add a block state for a custom manual model
     */
    protected void manualModel(Block b) {
        existingModel.add(b);
    }

    /**
     * The provider will add a block state with the given model
     */
    protected void manualModel(Block b, ModelFile model) {
        customModel.put(b, model);
    }

    @Override
    protected final void registerStatesAndModels() {
        this.setup();

        for (ResourceLocation id : ForgeRegistries.BLOCKS.getKeys()) {
            Block block = ForgeRegistries.BLOCKS.getValue(id);
            if (block != null && this.mod.modid.equals(id.getNamespace()) && !manualState.contains(block)) {
                if (existingModel.contains(block)) {
                    this.defaultState(id, block, () -> this.models().getExistingFile(new ResourceLocation(id.getNamespace(), "block/" + id.getPath())));
                } else if (customModel.containsKey(block)) {
                    this.defaultState(id, block, () -> customModel.get(block));
                } else {
                    LazyValue<ModelFile> defaultModel = new LazyValue<>(() -> this.defaultModel(id, block));
                    this.defaultState(id, block, defaultModel::get);
                }
            }
        }
    }

    protected abstract void setup();
    
    /**
     * Creates a block state for the given block using the given model. The default implementation checks
     * whether the block has the properties {@link BlockStateProperties#HORIZONTAL_FACING} or
     * {@link BlockStateProperties#FACING} and creates block states matching those.
     * If you don't use the model, don't call the supplier, so the default model is not generated.
     */
    protected void defaultState(ResourceLocation id, Block block, Supplier<ModelFile> model) {
        if (block instanceof DecoratedSlabBlock decorated) {
            this.slabBlock(decorated, textureId(Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(decorated.parent))), textureId(Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(decorated.parent))));
        } else if (block instanceof DecoratedStairBlock decorated) {
            this.stairsBlock(decorated, textureId(Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(decorated.parent))));
        } else if (block instanceof DecoratedWallBlock decorated) {
            this.wallBlock(decorated, textureId(Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(decorated.parent))));
        } else if (block instanceof DecoratedFenceBlock decorated) {
            this.fenceBlock(decorated, textureId(Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(decorated.parent))));
        } else if (block instanceof DecoratedFenceGateBlock decorated) {
            this.fenceGateBlock(decorated, textureId(Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(decorated.parent))));
        } else if (block instanceof DecoratedWoodButton decorated) {
            this.buttonBlock(decorated, textureId(Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(decorated.parent))));
        } else if (block instanceof DecoratedStoneButton decorated) {
            this.buttonBlock(decorated, textureId(Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(decorated.parent))));
        } else if (block instanceof DecoratedPressurePlate decorated) {
            this.pressurePlateBlock(decorated, textureId(Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(decorated.parent))));
        } else if (block instanceof DecoratedDoorBlock decorated) {
            this.doorBlock(decorated, textureId(id, "bottom"), textureId(id, "top"));
        } else if (block instanceof DecoratedTrapdoorBlock decorated) {
            this.trapdoorBlock(decorated, textureId(id), true);
        } else if (block instanceof DecoratedSign.Standing decorated) {
            this.getVariantBuilder(block).partialState().addModels(new ConfiguredModel(this.models().getBuilder(id.getPath()).texture("particle", textureId(Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(decorated.parent))))));
        } else if (block instanceof DecoratedSign.Wall decorated) {
            this.getVariantBuilder(block).partialState().addModels(new ConfiguredModel(this.models().getBuilder(id.getPath()).texture("particle", textureId(Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(decorated.parent))))));
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
                Optional<ResourceLocation> tex = fluidTextureId(liquidBlock.getFluid());
                if (tex.isPresent()) {
                    return this.models().getBuilder(id.getPath()).texture("particle", tex.get());
                } else {
                    return this.models().getBuilder(id.getPath());
                }
            } else {
                return this.models().getBuilder(id.getPath()); // We don't need a model for that block.
            }
        } else if (block instanceof LeavesBlock) {
            return this.models().withExistingParent(Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block)).getPath(), LEAVES_PARENT).texture("all", this.blockTexture(block));
        } else {
            return this.cubeAll(block);
        }
    }

    /**
     * Creates a block state and models for a button.
     */
    public void buttonBlock(Block block, ResourceLocation texture) {
        ResourceLocation blockId = Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block));

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
        ResourceLocation blockId = Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block));

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
        return new ResourceLocation(blockId.getNamespace(), "block/" + blockId.getPath());
    }
    
    private static ResourceLocation textureId(ResourceLocation blockId, String suffix) {
        Objects.requireNonNull(blockId);
        return new ResourceLocation(blockId.getNamespace(), "block/" + blockId.getPath() + "_" + suffix);
    }

    private static Optional<ResourceLocation> fluidTextureId(Fluid fluid) {
        try {
            IFluidTypeRenderProperties properties = RenderProperties.get(fluid);
            if (properties != IFluidTypeRenderProperties.DUMMY) {
                return Optional.ofNullable(properties.getStillTexture());
            } else {
                // Forge no longer calls this during datagen
                // so we need to do it manually
                AtomicReference<IFluidTypeRenderProperties> ref = new AtomicReference<>(null);
                fluid.getFluidType().initializeClient(ref::set);
                properties = ref.get();
                if (properties != null) {
                    return Optional.ofNullable(properties.getStillTexture());
                } else {
                    return Optional.empty();
                }
            }
        } catch (Exception | NoClassDefFoundError e) {
            LibX.logger.warn("Failed to load fluid render properties", e);
            return Optional.empty();
        }
    }
}
