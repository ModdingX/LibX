package org.moddingx.libx.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.EmptyBlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.moddingx.libx.impl.render.BlockOverlayQuadCache;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Helper to deal with {@link Block} rendering
 */
public class RenderHelperBlock {

    private static final ResourceLocation BLOCK_ATLAS = ResourceLocation.withDefaultNamespace("textures/atlas/blocks.png");
    private static final RenderType RENDER_TYPE_BREAK = RenderType.crumbling(BLOCK_ATLAS);
    private static final RandomSource random = RandomSource.create();
    private static final List<BlockModelPart> partCache = new ArrayList<>();
    private static final int BLOCK_VERTEX_STRIDE = DefaultVertexFormat.BLOCK.getVertexSize() / 4;
    private static final int BLOCK_UV_OFFSET = DefaultVertexFormat.BLOCK.contains(VertexFormatElement.UV0) ? DefaultVertexFormat.BLOCK.getOffset(VertexFormatElement.UV0) / 4 : -1;

    /**
     * Renders the break effect for a {@link BlockState}.
     *
     * @param breakProgress How much the block already broke. 0 means no break. This should not be lower than 0 and not be greater than 10.
     */
    public static void renderBlockBreak(BlockState state, PoseStack poseStack, int light, int overlay, int breakProgress) {
        renderBlockBreak(state, poseStack, light, overlay, breakProgress, state.getSeed(BlockPos.ZERO));
    }

    /**
     * Renders the break effect for a {@link BlockState}.
     *
     * @param breakProgress  How much the block already broke. 0 means no break. This should not be lower than 0 and not be greater than 10.
     * @param positionRandom The long value to randomize the position. This can be obtained via {@code BlockState#getPositionRandom}.
     */
    public static void renderBlockBreak(BlockState state, PoseStack poseStack, int light, int overlay, int breakProgress, long positionRandom) {
        if (breakProgress > 0) {
            ResourceLocation tex = ModelBakery.DESTROY_STAGES.get((breakProgress - 1) % ModelBakery.DESTROY_STAGES.size());
            TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).getSprite(tex);
            renderBlockOverlaySprite(state, poseStack, light, overlay, sprite, positionRandom);
        }
    }

    /**
     * Renders a block overlay on top of a {@link BlockState} with the same method as the crumbling is rendered. However you cen specify
     * your own TextureAtlasSprite here to be used. (It must be from the block atlas)
     */
    public static void renderBlockOverlaySprite(BlockState state, PoseStack poseStack, int light, int overlay, TextureAtlasSprite sprite) {
        renderBlockOverlaySprite(state, poseStack, light, overlay, sprite, state.getSeed(BlockPos.ZERO));
    }

    /**
     * Renders a block overlay on top of a {@link BlockState} with the same method as the crumbling is rendered. However you cen specify
     * your own TextureAtlasSprite here to be used. (It must be from the block atlas)
     */
    public static void renderBlockOverlaySprite(BlockState state, PoseStack poseStack, int light, int overlay, TextureAtlasSprite sprite, long positionRandom) {
        renderBlockOverlaySprite(state, poseStack, light, overlay, sprite, positionRandom, dir -> true);
    }

    /**
     * Renders a block overlay on top of a {@link BlockState} with the same method as the crumbling is rendered. However you cen specify
     * your own TextureAtlasSprite here to be used. (It must be from the block atlas)
     */
    public static void renderBlockOverlaySprite(BlockState state, PoseStack poseStack, int light, int overlay, TextureAtlasSprite sprite, Predicate<Direction> dirs) {
        renderBlockOverlaySprite(state, poseStack, light, overlay, sprite, state.getSeed(BlockPos.ZERO), dirs);
    }

    /**
     * Renders a block overlay on top of a {@link BlockState} with the same method as the crumbling is rendered. However you cen specify
     * your own TextureAtlasSprite here to be used. (It must be from the block atlas)
     */
    public static void renderBlockOverlaySprite(BlockState state, PoseStack poseStack, int light, int overlay, TextureAtlasSprite sprite, long positionRandom, Predicate<Direction> dirs) {
        if (state.getRenderShape() == RenderShape.MODEL) {
            BlockStateModel model = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(state);
            VertexConsumer vertex = Minecraft.getInstance().renderBuffers().crumblingBufferSource().getBuffer(RENDER_TYPE_BREAK);
            random.setSeed(positionRandom);
            partCache.clear();
            model.collectParts(EmptyBlockAndTintGetter.INSTANCE, BlockPos.ZERO, state, random, partCache);
            for (BlockModelPart part : partCache) {
                for (Direction direction : Direction.values()) {
                    List<BakedQuad> list = part.getQuads(direction);
                    if (!list.isEmpty()) {
                        renderBlockOverlayQuad(poseStack.last(), vertex, list, light, overlay, sprite, dirs);
                    }
                }
                List<BakedQuad> list = part.getQuads(null);
                if (!list.isEmpty()) {
                    renderBlockOverlayQuad(poseStack.last(), vertex, list, light, overlay, sprite, dirs);
                }
            }
        }
    }

    /**
     * Invokes {@link MultiBufferSource.BufferSource#endBatch(RenderType)} on the crumbling buffer with the {@link RenderType}
     * used for custom overlays and crumbling.
     */
    public static void endOverlayBatch() {
        Minecraft.getInstance().renderBuffers().crumblingBufferSource().endBatch(RENDER_TYPE_BREAK);
    }

    private static void renderBlockOverlayQuad(PoseStack.Pose pose, VertexConsumer vertex, List<BakedQuad> list, int light, int overlay, TextureAtlasSprite sprite, Predicate<Direction> dirs) {
        for (BakedQuad quad : list) {
            if (dirs.test(quad.direction())) {
                vertex.putBulkData(pose, modifyBlockQuad(quad, sprite), 1, 1, 1, 1, light, overlay);
            }
        }
    }

    private static BakedQuad modifyBlockQuad(BakedQuad quad, TextureAtlasSprite newSprite) {
        BakedQuad result = BlockOverlayQuadCache.get(quad, newSprite);
        if (result == null) {
            int[] data = quad.vertices();
            int[] newData = new int[data.length];
            System.arraycopy(data, 0, newData, 0, data.length);
            if (BLOCK_UV_OFFSET != -1) {
                TextureAtlasSprite oldSprite = quad.sprite();
                for (int off = 0; off + BLOCK_UV_OFFSET + 1 < newData.length; off += BLOCK_VERTEX_STRIDE) {
                    newData[off + BLOCK_UV_OFFSET] = Float.floatToRawIntBits(((Float.intBitsToFloat(data[off + BLOCK_UV_OFFSET]) - oldSprite.getU0()) * newSprite.contents().width()) / oldSprite.contents().width() + newSprite.getU0());
                    newData[off + BLOCK_UV_OFFSET + 1] = Float.floatToRawIntBits(((Float.intBitsToFloat(data[off + BLOCK_UV_OFFSET + 1]) - oldSprite.getV0()) * newSprite.contents().height() / oldSprite.contents().height()) + newSprite.getV0());
                }
            }
            result = new BakedQuad(newData, quad.tintIndex(), quad.direction(), newSprite, quad.shade(), quad.lightEmission(), quad.hasAmbientOcclusion());
            BlockOverlayQuadCache.put(quad, result);
        }
        return result;
    }
}
