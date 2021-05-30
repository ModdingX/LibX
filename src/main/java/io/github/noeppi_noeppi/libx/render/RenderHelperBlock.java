package io.github.noeppi_noeppi.libx.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

/**
 * Helper to deal with block rendering
 */
public class RenderHelperBlock {

    private static final RenderType RENDER_TYPE_BREAK = RenderType.getCrumbling(PlayerContainer.LOCATION_BLOCKS_TEXTURE);
    private static final Random random = new Random();

    /**
     * Renders the break effect for a block state.
     *
     * @param breakProgress How much the block already broke. 0 means no break. This should not be lower than 0 and not be greater than 10.
     */
    public static void renderBlockBreak(BlockState state, MatrixStack matrixStack, IRenderTypeBuffer buffer, int light, int overlay, int breakProgress) {
        renderBlockBreak(state, matrixStack, buffer, light, overlay, breakProgress, state.getPositionRandom(BlockPos.ZERO));
    }

    /**
     * Renders the break effect for a block state.
     *
     * @param breakProgress  How much the block already broke. 0 means no break. This should not be lower than 0 and not be greater than 10.
     * @param positionRandom The long value to randomize the position. This can be obtained via {@code BlockState#getPositionRandom}.
     */
    public static void renderBlockBreak(BlockState state, MatrixStack matrixStack, IRenderTypeBuffer buffer, int light, int overlay, int breakProgress, long positionRandom) {
        if (breakProgress > 0) {
            ResourceLocation tex = ModelBakery.DESTROY_STAGES.get((breakProgress - 1) % ModelBakery.DESTROY_STAGES.size());
            TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(tex);
            renderBlockOverlaySprite(state, matrixStack, buffer, light, overlay, sprite, positionRandom);
        }
    }

    /**
     * Renders a block overlay on top of a state with the same method as the crumbling is rendered. However you cen specify
     * your own TextureAtlasSprite here to be used. (It must be from PlayerContainer.LOCATION_BLOCKS_TEXTURE)
     */
    public static void renderBlockOverlaySprite(BlockState state, MatrixStack matrixStack, IRenderTypeBuffer buffer, int light, int overlay, TextureAtlasSprite sprite) {
        renderBlockOverlaySprite(state, matrixStack, buffer, light, overlay, sprite, state.getPositionRandom(BlockPos.ZERO));
    }

    /**
     * Renders a block overlay on top of a state with the same method as the crumbling is rendered. However you cen specify
     * your own TextureAtlasSprite here to be used. (It must be from PlayerContainer.LOCATION_BLOCKS_TEXTURE)
     */
    public static void renderBlockOverlaySprite(BlockState state, MatrixStack matrixStack, IRenderTypeBuffer buffer, int light, int overlay, TextureAtlasSprite sprite, long positionRandom) {
        renderBlockOverlaySprite(state, matrixStack, buffer, light, overlay, sprite, positionRandom, dir -> true);
    }

    /**
     * Renders a block overlay on top of a state with the same method as the crumbling is rendered. However you cen specify
     * your own TextureAtlasSprite here to be used. (It must be from PlayerContainer.LOCATION_BLOCKS_TEXTURE)
     */
    public static void renderBlockOverlaySprite(BlockState state, MatrixStack matrixStack, IRenderTypeBuffer buffer, int light, int overlay, TextureAtlasSprite sprite, Predicate<Direction> dirs) {
        renderBlockOverlaySprite(state, matrixStack, buffer, light, overlay, sprite, state.getPositionRandom(BlockPos.ZERO), dirs);
    }

    /**
     * Renders a block overlay on top of a state with the same method as the crumbling is rendered. However you cen specify
     * your own TextureAtlasSprite here to be used. (It must be from PlayerContainer.LOCATION_BLOCKS_TEXTURE)
     */
    public static void renderBlockOverlaySprite(BlockState state, MatrixStack matrixStack, IRenderTypeBuffer buffer, int light, int overlay, TextureAtlasSprite sprite, long positionRandom, Predicate<Direction> dirs) {
        if (state.getRenderType() == BlockRenderType.MODEL) {
            IBakedModel model = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(state);

            IVertexBuilder vertex = Minecraft.getInstance().getRenderTypeBuffers().getCrumblingBufferSource().getBuffer(RENDER_TYPE_BREAK);

            for (Direction direction : Direction.values()) {
                random.setSeed(positionRandom);
                List<BakedQuad> list = model.getQuads(state, direction, random, EmptyModelData.INSTANCE);
                if (!list.isEmpty()) {
                    renderBlockBreakQuad(matrixStack.getLast(), vertex, list, light, overlay, sprite, dirs);
                }
            }

            random.setSeed(positionRandom);
            List<BakedQuad> list = model.getQuads(state, null, random, EmptyModelData.INSTANCE);
            if (!list.isEmpty()) {
                renderBlockBreakQuad(matrixStack.getLast(), vertex, list, light, overlay, sprite, dirs);
            }
        }
    }

    private static void renderBlockBreakQuad(MatrixStack.Entry matrix, IVertexBuilder vertex, List<BakedQuad> list, int light, int overlay, TextureAtlasSprite sprite, Predicate<Direction> dirs) {
        for (BakedQuad quad : list) {
            if (dirs.test(quad.getFace())) {
                BakedQuad modifiedQuad = new BakedQuad(modifyBlockBreakQuadData(quad.getVertexData(), quad.getSprite(), sprite), quad.getTintIndex(), quad.getFace(), sprite, quad.applyDiffuseLighting());
                vertex.addQuad(matrix, modifiedQuad, 1, 1, 1, light, overlay);
            }
        }
    }

    private static int[] modifyBlockBreakQuadData(int[] data, TextureAtlasSprite oldSprite, TextureAtlasSprite newSprite) {
        // Only works for DefaultVertexFormats.BLOCK, might need to be fixed in the future
        int[] newData = new int[data.length];
        System.arraycopy(data, 0, newData, 0, data.length);
        for (int off = 0; off + 7 < newData.length; off += DefaultVertexFormats.BLOCK.getIntegerSize()) {
            newData[off + 4] = Float.floatToRawIntBits(((Float.intBitsToFloat(data[off + 4]) - oldSprite.getMinU()) * newSprite.getWidth() / oldSprite.getWidth()) + newSprite.getMinU());
            newData[off + 5] = Float.floatToRawIntBits(((Float.intBitsToFloat(data[off + 5]) - oldSprite.getMinV()) * newSprite.getHeight() / oldSprite.getHeight()) + newSprite.getMinV());
        }
        return newData;
    }
}
