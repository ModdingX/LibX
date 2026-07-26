package org.moddingx.libx.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
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

    private static final Identifier BLOCK_ATLAS = Identifier.withDefaultNamespace("textures/atlas/blocks.png");
    private static final RenderType RENDER_TYPE_BREAK = RenderTypes.crumbling(BLOCK_ATLAS);
    private static final RandomSource random = RandomSource.create();
    private static final List<BlockStateModelPart> partCache = new ArrayList<>();
    private static final QuadInstance quadInstance = new QuadInstance();

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
            Identifier tex = ModelBakery.DESTROY_STAGES.get((breakProgress - 1) % ModelBakery.DESTROY_STAGES.size());
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
            BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
            VertexConsumer vertex = Minecraft.getInstance().renderBuffers().crumblingBufferSource().getBuffer(RENDER_TYPE_BREAK);
            random.setSeed(positionRandom);
            partCache.clear();
            model.collectParts(BlockAndTintGetter.EMPTY, BlockPos.ZERO, state, random, partCache);
            for (BlockStateModelPart part : partCache) {
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
        quadInstance.setColor(-1);
        quadInstance.setLightCoords(light);
        quadInstance.setOverlayCoords(overlay);
        for (BakedQuad quad : list) {
            if (dirs.test(quad.direction())) {
                vertex.putBakedQuad(pose, modifyBlockQuad(quad, sprite), quadInstance);
            }
        }
    }

    private static BakedQuad modifyBlockQuad(BakedQuad quad, TextureAtlasSprite newSprite) {
        BakedQuad result = BlockOverlayQuadCache.get(quad, newSprite);
        if (result == null) {
            BakedQuad.MaterialInfo material = quad.materialInfo();
            TextureAtlasSprite oldSprite = material.sprite();
            long[] newUVs = new long[4];
            for (int i = 0; i < 4; i++) {
                long packed = quad.packedUV(i);
                float u = UVPair.unpackU(packed);
                float v = UVPair.unpackV(packed);
                float localU = (u - oldSprite.getU0()) / (oldSprite.getU1() - oldSprite.getU0());
                float localV = (v - oldSprite.getV0()) / (oldSprite.getV1() - oldSprite.getV0());
                float newU = newSprite.getU0() + localU * (newSprite.getU1() - newSprite.getU0());
                float newV = newSprite.getV0() + localV * (newSprite.getV1() - newSprite.getV0());
                newUVs[i] = UVPair.pack(newU, newV);
            }
            BakedQuad.MaterialInfo newMaterial = new BakedQuad.MaterialInfo(
                    newSprite, material.layer(), material.itemRenderType(), material.tintIndex(),
                    material.shade(), material.lightEmission(), material.ambientOcclusion()
            );
            result = new BakedQuad(
                    quad.position0(), quad.position1(), quad.position2(), quad.position3(),
                    newUVs[0], newUVs[1], newUVs[2], newUVs[3],
                    quad.direction(), newMaterial,
                    quad.bakedNormals(), quad.bakedColors()
            );
            BlockOverlayQuadCache.put(quad, result);
        }
        return result;
    }
}
