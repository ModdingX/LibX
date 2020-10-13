package io.github.noeppi_noeppi.libx.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;

import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Helper to deal with item rendering.
 */
public class RenderHelperItem {

    /**
     * Renders an item tinted in the given color.
     */
    public static void renderItemTinted(ItemStack stack, ItemCameraTransforms.TransformType transformType, int light, int overlay, MatrixStack matrixStack, IRenderTypeBuffer buffer, float r, float g, float b) {
        if (!stack.isEmpty()) {
            boolean isGui = transformType == ItemCameraTransforms.TransformType.GUI;
            boolean isFixed = isGui || transformType == ItemCameraTransforms.TransformType.GROUND || transformType == ItemCameraTransforms.TransformType.FIXED;

            IBakedModel model = Minecraft.getInstance().getItemRenderer().getItemModelWithOverrides(stack, null, null);
            model = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(matrixStack, model, transformType, false);

            matrixStack.push();
            matrixStack.translate(-0.5D, -0.5D, -0.5D);

            if (!model.isBuiltInRenderer() && (stack.getItem() != Items.TRIDENT || isFixed)) {
                RenderType type = RenderTypeLookup.func_239219_a_(stack, true);
                if (isGui && Objects.equals(type, Atlases.getTranslucentCullBlockType())) {
                    type = Atlases.getTranslucentCullBlockType();
                }

                IVertexBuilder ivertexbuilder = ItemRenderer.getBuffer(buffer, type, true, stack.hasEffect());
                renderTintedModel(model, light, overlay, matrixStack, ivertexbuilder, r, g, b);
            } else {
                //noinspection deprecation
                GlStateManager.color4f(r, g, b, 1);
                stack.getItem().getItemStackTileEntityRenderer().func_239207_a_(stack, transformType, matrixStack, buffer, light, overlay);
                //noinspection deprecation
                GlStateManager.color4f(1, 1, 1, 1);
            }

            matrixStack.pop();
        }
    }

    private static void renderTintedModel(IBakedModel model, int light, int overlay, MatrixStack matrixStack, IVertexBuilder buffer, float r, float g, float b) {
        Random random = new Random();

        for (Direction direction : Direction.values()) {
            random.setSeed(42);
            //noinspection deprecation
            renderTintedQuads(matrixStack, buffer, model.getQuads(null, direction, random), light, overlay, r, g, b);
        }

        random.setSeed(42);
        //noinspection deprecation
        renderTintedQuads(matrixStack, buffer, model.getQuads(null, null, random), light, overlay, r, g, b);
    }

    private static void renderTintedQuads(MatrixStack matrixStack, IVertexBuilder buffer, List<BakedQuad> quads, int light, int overlay, float r, float g, float b) {
        MatrixStack.Entry entry = matrixStack.getLast();

        for (BakedQuad bakedquad : quads) {
            buffer.addVertexData(entry, bakedquad, r, g, b, light, overlay, true);
        }
    }
}
