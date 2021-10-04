package io.github.noeppi_noeppi.libx.data.provider;

import io.github.noeppi_noeppi.libx.base.decorative.ChildBlock;
import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * A base class for block state and model providers. An extending class should call the
 * {@link #manualState(Block) manualState} and {@link #manualModel(Block) manualModel} methods in
 * {@link #setup() setup}.
 * Another thing you can do is override {@link #defaultState(ResourceLocation, Block, ModelFile) defaultState}
 * and {@link #defaultModel(ResourceLocation, Block) defaultModel} to adjust the state and model depending
 * on the block.
 */
public abstract class BlockStateProviderBase extends BlockStateProvider {

    public static final ResourceLocation LEAVES_PARENT = new ResourceLocation("minecraft", "block/leaves");

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
                    this.defaultState(id, block, this.models().getExistingFile(new ResourceLocation(id.getNamespace(), "block/" + id.getPath())));
                } else if (customModel.containsKey(block)) {
                    this.defaultState(id, block, customModel.get(block));
                } else {
                    this.defaultState(id, block, this.defaultModel(id, block));
                }
            }
        }
    }

    protected abstract void setup();

    /**
     * Creates a block state for the given block using the given model. The default implementation checks
     * whether the block has the properties {@link BlockStateProperties#HORIZONTAL_FACING} or
     * {@link BlockStateProperties#FACING} and creates block states matching those.
     */
    protected void defaultState(ResourceLocation id, Block block, ModelFile model) {
        if (block instanceof ChildBlock child && child.getParent() != null) {
            id = child.getParent().getRegistryName();
        }

        if (block instanceof SlabBlock) {
            this.slabBlock((SlabBlock) block, id, id);
        } else if (block instanceof StairBlock) {
            this.stairsBlock((StairBlock) block, id);
        } else if (block instanceof WallBlock) {
            this.wallBlock((WallBlock) block, id);
        } else if (block instanceof FenceBlock) {
            this.fenceBlock((FenceBlock) block, id);
        } else if (block instanceof FenceGateBlock) {
            this.fenceGateBlock((FenceGateBlock) block, id);
        } else if (block.getStateDefinition().getProperties().contains(BlockStateProperties.HORIZONTAL_FACING)) {
            VariantBlockStateBuilder builder = this.getVariantBuilder(block);
            for (Direction direction : BlockStateProperties.HORIZONTAL_FACING.getPossibleValues()) {
                builder.partialState().with(BlockStateProperties.HORIZONTAL_FACING, direction)
                        .addModels(new ConfiguredModel(model, 0, (int) direction.getOpposite().toYRot(), false));
            }
        } else if (block.getStateDefinition().getProperties().contains(BlockStateProperties.FACING)) {
            VariantBlockStateBuilder builder = this.getVariantBuilder(block);
            for (Direction direction : BlockStateProperties.FACING.getPossibleValues()) {
                builder.partialState().with(BlockStateProperties.FACING, direction)
                        .addModels(new ConfiguredModel(model, direction == Direction.DOWN ? 180 : direction.getAxis().isHorizontal() ? 90 : 0, direction.getAxis().isVertical() ? 0 : (int) direction.getOpposite().toYRot(), false));
            }
        } else {
            this.simpleBlock(block, model);
        }
    }

    /**
     * Creates a model for the given block. The default implementation creates special models for blocks
     * of type {@link LiquidBlock} and {@link LeavesBlock}.
     */
    protected ModelFile defaultModel(ResourceLocation id, Block block) {
        if (block instanceof SlabBlock || block instanceof StairBlock || block instanceof WallBlock || block instanceof FenceBlock || block instanceof FenceGateBlock) {
            return null;
        }

        if (block.getStateDefinition().getPossibleStates().stream().allMatch(state -> state.getRenderShape() != RenderShape.MODEL)) {
            if (block instanceof LiquidBlock fluidBlock) {
                return this.models().getBuilder(id.getPath()).texture("particle", fluidBlock.getFluid().getAttributes().getStillTexture());
            } else {
                return this.models().getBuilder(id.getPath()); // We don't need a model for that block.
            }
        } else if (block instanceof LeavesBlock) {
            return this.models().withExistingParent(Objects.requireNonNull(block.getRegistryName()).getPath(), LEAVES_PARENT).texture("all", this.blockTexture(block));
        } else {
            return this.cubeAll(block);
        }
    }
}
