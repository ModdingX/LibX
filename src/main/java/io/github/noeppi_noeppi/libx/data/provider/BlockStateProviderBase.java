package io.github.noeppi_noeppi.libx.data.provider;

import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.LeavesBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * A base class for block state and model provider. When overriding this you should call the {@code manualState}
 * and {@code manualModel} methods in {@code setup}. Unlike the other provider this has an extra method
 * because custom models would not generate if not done there. Another thing you can do is override {@code defaultState} and
 * {@code defaultModel} to adjust the state and model depending on the block.
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
     * Creates a block state for the given block using the given model. The default implementation checks whether
     * the block has the properties {@code BlockStateProperties.HORIZONTAL_FACING} or
     * {@code BlockStateProperties.FACING} and creates block states matching those.
     */
    protected void defaultState(ResourceLocation id, Block block, ModelFile model) {
        if (block.getStateContainer().getProperties().contains(BlockStateProperties.HORIZONTAL_FACING)) {
            VariantBlockStateBuilder builder = this.getVariantBuilder(block);
            for (Direction direction : BlockStateProperties.HORIZONTAL_FACING.getAllowedValues()) {
                builder.partialState().with(BlockStateProperties.HORIZONTAL_FACING, direction)
                        .addModels(new ConfiguredModel(model, direction.getHorizontalIndex() == -1 ? direction.getOpposite().getAxisDirection().getOffset() * 90 : 0, (int) direction.getOpposite().getHorizontalAngle(), false));
            }
        } else if (block.getStateContainer().getProperties().contains(BlockStateProperties.FACING)) {
            VariantBlockStateBuilder builder = this.getVariantBuilder(block);
            for (Direction direction : BlockStateProperties.FACING.getAllowedValues()) {
                builder.partialState().with(BlockStateProperties.FACING, direction)
                        .addModels(new ConfiguredModel(model, direction.getHorizontalIndex() == -1 ? direction.getOpposite().getAxisDirection().getOffset() * 90 : 0, (int) direction.getOpposite().getHorizontalAngle(), false));
            }
        } else {
            this.simpleBlock(block, model);
        }
    }

    /**
     * Creates a model for the given block. The default implementation always creates cube_all models.
     */
    protected ModelFile defaultModel(ResourceLocation id, Block block) {
        if (block.getStateContainer().getValidStates().stream().allMatch(state -> state.getRenderType() != BlockRenderType.MODEL)) {
            return this.models().getBuilder(id.getPath()); // We don't need a model for that block.
        } else if (block instanceof LeavesBlock) {
            return this.models().withExistingParent(Objects.requireNonNull(block.getRegistryName()).getPath(), LEAVES_PARENT)
                    .texture("all", this.blockTexture(block));
        } else {
            return this.cubeAll(block);
        }
    }
}
