package org.moddingx.libx.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.moddingx.libx.impl.render.BlockOverlayQuadCache;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

/**
 * Helper to deal with {@link Block} rendering
 */
public class RenderHelperBlock {

    private static final RenderType RENDER_TYPE_BREAK = RenderType.crumbling(InventoryMenu.BLOCK_ATLAS);
    private static final Random random = new Random();

    /**
     * Renders the break effect for a {@link BlockState}.
     *
     * @param breakProgress How much the block already broke. 0 means no break. This should not be lower than 0 and not be greater than 10.
     */
    public static void renderBlockBreak(BlockState state, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, int breakProgress) {
        renderBlockBreak(state, poseStack, buffer, light, overlay, breakProgress, state.getSeed(BlockPos.ZERO));
    }

    /**
     * Renders the break effect for a {@link BlockState}.
     *
     * @param breakProgress  How much the block already broke. 0 means no break. This should not be lower than 0 and not be greater than 10.
     * @param positionRandom The long value to randomize the position. This can be obtained via {@code BlockState#getPositionRandom}.
     */
    public static void renderBlockBreak(BlockState state, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, int breakProgress, long positionRandom) {
        if (breakProgress > 0) {
            ResourceLocation tex = ModelBakery.DESTROY_STAGES.get((breakProgress - 1) % ModelBakery.DESTROY_STAGES.size());
            TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(tex);
            renderBlockOverlaySprite(state, poseStack, buffer, light, overlay, sprite, positionRandom);
        }
    }

    /**
     * Renders a block overlay on top of a {@link BlockState} with the same method as the crumbling is rendered. However you cen specify
     * your own TextureAtlasSprite here to be used. (It must be from {@link InventoryMenu#BLOCK_ATLAS})
     */
    public static void renderBlockOverlaySprite(BlockState state, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, TextureAtlasSprite sprite) {
        renderBlockOverlaySprite(state, poseStack, buffer, light, overlay, sprite, state.getSeed(BlockPos.ZERO));
    }

    /**
     * Renders a block overlay on top of a {@link BlockState} with the same method as the crumbling is rendered. However you cen specify
     * your own TextureAtlasSprite here to be used. (It must be from {@link InventoryMenu#BLOCK_ATLAS})
     */
    public static void renderBlockOverlaySprite(BlockState state, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, TextureAtlasSprite sprite, long positionRandom) {
        renderBlockOverlaySprite(state, poseStack, buffer, light, overlay, sprite, positionRandom, dir -> true);
    }

    /**
     * Renders a block overlay on top of a {@link BlockState} with the same method as the crumbling is rendered. However you cen specify
     * your own TextureAtlasSprite here to be used. (It must be from {@link InventoryMenu#BLOCK_ATLAS})
     */
    public static void renderBlockOverlaySprite(BlockState state, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, TextureAtlasSprite sprite, Predicate<Direction> dirs) {
        renderBlockOverlaySprite(state, poseStack, buffer, light, overlay, sprite, state.getSeed(BlockPos.ZERO), dirs);
    }

    /**
     * Renders a block overlay on top of a {@link BlockState} with the same method as the crumbling is rendered. However you cen specify
     * your own TextureAtlasSprite here to be used. (It must be from {@link InventoryMenu#BLOCK_ATLAS})
     */
    public static void renderBlockOverlaySprite(BlockState state, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, TextureAtlasSprite sprite, long positionRandom, Predicate<Direction> dirs) {
        if (state.getRenderShape() == RenderShape.MODEL) {
            BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(state);
            VertexConsumer vertex = Minecraft.getInstance().renderBuffers().crumblingBufferSource().getBuffer(RENDER_TYPE_BREAK);
            for (Direction direction : Direction.values()) {
                random.setSeed(positionRandom);
                List<BakedQuad> list = model.getQuads(state, direction, random, EmptyModelData.INSTANCE);
                if (!list.isEmpty()) {
                    renderBlockOverlayQuad(DefaultVertexFormat.BLOCK, poseStack.last(), vertex, list, light, overlay, sprite, dirs);
                }
            }

            random.setSeed(positionRandom);
            List<BakedQuad> list = model.getQuads(state, null, random, EmptyModelData.INSTANCE);
            if (!list.isEmpty()) {
                renderBlockOverlayQuad(DefaultVertexFormat.BLOCK, poseStack.last(), vertex, list, light, overlay, sprite, dirs);
            }
        }
    }

    private static void renderBlockOverlayQuad(@SuppressWarnings("SameParameterValue") VertexFormat format, PoseStack.Pose pose, VertexConsumer vertex, List<BakedQuad> list, int light, int overlay, TextureAtlasSprite sprite, Predicate<Direction> dirs) {
        for (BakedQuad quad : list) {
            if (dirs.test(quad.getDirection())) {
                vertex.putBulkData(pose, modifyBlockQuad(format, quad, sprite), 1, 1, 1, light, overlay);
            }
        }
    }

    private static BakedQuad modifyBlockQuad(VertexFormat format, BakedQuad quad, TextureAtlasSprite newSprite) {
        BakedQuad result = BlockOverlayQuadCache.get(quad, newSprite);
        if (result == null) {
            int[] data = quad.getVertices();
            int[] newData = new int[data.length];
            System.arraycopy(data, 0, newData, 0, data.length);
            int uvIdx = format.getElements().indexOf(DefaultVertexFormat.ELEMENT_UV);
            if (uvIdx != -1) {
                // Byte offset / 4 = integer offset
                // Will break if UV is not on full ints but that is not the case in regular minecraft
                TextureAtlasSprite oldSprite = quad.getSprite();
                int uvOff = format.getOffset(uvIdx) / 4;
                int intSize = format.getIntegerSize();
                for (int off = 0; off + uvOff + 1 < newData.length; off += intSize) {
                    newData[off + uvOff] = Float.floatToRawIntBits(((Float.intBitsToFloat(data[off + uvOff]) - oldSprite.getU0()) * newSprite.getWidth() / oldSprite.getWidth()) + newSprite.getU0());
                    newData[off + uvOff + 1] = Float.floatToRawIntBits(((Float.intBitsToFloat(data[off + uvOff + 1]) - oldSprite.getV0()) * newSprite.getHeight() / oldSprite.getHeight()) + newSprite.getV0());
                }
            }
            result = new BakedQuad(newData, quad.getTintIndex(), quad.getDirection(), newSprite, quad.isShade());
            BlockOverlayQuadCache.put(quad, result);
        }
        return result;
    }
}
