package io.github.noeppi_noeppi.libx.render;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.noeppi_noeppi.libx.data.provider.ItemModelProviderBase;
import io.github.noeppi_noeppi.libx.util.LazyValue;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * This class is meant to apply a TileEntityRenderer to items. Using it is really straightforward:
 * <p>
 * Add this to your item properties: {@code .setISTER(() -> ItemStackRenderer::get)}
 * <p>
 * Then in {@code registerClient} call {@link ItemStackRenderer#addRenderTile(TileEntityType, boolean)}
 * <p>
 * The required models will generate automatically if you're using {@link ItemModelProviderBase}.
 */
public class ItemStackRenderer extends BlockEntityWithoutLevelRenderer {

    private static final ItemStackRenderer INSTANCE = new ItemStackRenderer();

    private static final List<BlockEntityType<?>> types = Collections.synchronizedList(new LinkedList<>());
    private static final Map<BlockEntityType<?>, Pair<LazyValue<BlockEntity>, Boolean>> tiles = Collections.synchronizedMap(new HashMap<>());
    private static final Map<BlockEntityType<?>, CompoundTag> defaultTags = new HashMap<>();

    private ItemStackRenderer() {
        super();
    }

    /**
     * Registers a {@link TileEntityType} to be rendered with the ItemStackRenderer.
     *
     * @param teType             The Tile Entity Type.
     * @param readBlockEntityTag If this is set to true and an item has a {@code BlockEntityTag}, the tile
     *                           entities {@code read} method will get called before rendering.
     */
    public static <T extends BlockEntity> void addRenderTile(BlockEntityType<T> teType, boolean readBlockEntityTag) {
        types.add(teType);
        tiles.put(teType, Pair.of(new LazyValue<>(teType::create), readBlockEntityTag));
    }

    @Override
    public void renderByItem(ItemStack stack, @Nonnull ItemTransforms.TransformType type, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource buffer, int light, int overlay) {
        Block block = Block.byItem(stack.getItem());
        if (block != Blocks.AIR) {
            for (BlockEntityType<?> teType : types) {
                if (teType.isValid(block)) {
                    Pair<LazyValue<BlockEntity>, Boolean> pair = tiles.get(teType);
                    BlockState state = block.defaultBlockState();
                    BlockEntity tile = pair.getLeft().get();

                    BlockEntityRenderer<BlockEntity> renderer = BlockEntityRenderDispatcher.instance.getRenderer(tile);
                    if (renderer != null) {

                        if (pair.getRight()) {
                            if (!defaultTags.containsKey(teType)) {
                                setWorldPosStabe(tile, state);
                                defaultTags.put(teType, tile.save(new CompoundTag()));
                            }

                            CompoundTag nbt = stack.getTag();
                            setWorldPosStabe(tile, state);
                            tile.load(state, defaultTags.get(teType));
                            if (nbt != null && nbt.contains("BlockEntityTag", Constants.NBT.TAG_COMPOUND)) {
                                CompoundTag blockTag = nbt.getCompound("BlockEntityTag");
                                tile.load(state, blockTag);
                            }
                        }

                        if (Minecraft.getInstance().level != null) {
                            tile.setLevelAndPosition(Minecraft.getInstance().level, BlockPos.ZERO);
                        }
                        tile.blockState = state;

                        poseStack.pushPose();

                        if (state.getRenderShape() != RenderShape.ENTITYBLOCK_ANIMATED) {
                            //noinspection deprecation
                            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(block.defaultBlockState(), poseStack, buffer, light, overlay);
                        }
                        renderer.render(tile, Minecraft.getInstance().getFrameTime(), poseStack, buffer, light, overlay);

                        poseStack.popPose();

                        break;
                    }
                }
            }
        }
    }

    private static void setWorldPosStabe(BlockEntity tile, BlockState state) {
        if (Minecraft.getInstance().level != null) {
            tile.setLevelAndPosition(Minecraft.getInstance().level, BlockPos.ZERO);
        }
        tile.blockState = state;
    }

    /**
     * Gets the instance of the ItemStackRenderer.
     */
    public static ItemStackRenderer get() {
        return INSTANCE;
    }
}
