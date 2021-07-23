package io.github.noeppi_noeppi.libx.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.Direction;

import java.util.List;
import java.util.Objects;
import java.util.Random;

import net.minecraft.client.renderer.entity.ItemRenderer;

/**
 * Helper to deal with item rendering.
 */
public class RenderHelperItem {

    /**
     * Renders an {@link ItemStack item} tinted in the given color.
     */
    public static void renderItemTinted(ItemStack stack, ItemTransforms.TransformType transformType, int light, int overlay, PoseStack poseStack, MultiBufferSource buffer, float r, float g, float b, float alpha) {
        if (!stack.isEmpty()) {
            boolean isGui = transformType == ItemTransforms.TransformType.GUI;
            boolean isFixed = isGui || transformType == ItemTransforms.TransformType.GROUND || transformType == ItemTransforms.TransformType.FIXED;

            BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, null, null);
            model = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(poseStack, model, transformType, false);

            poseStack.pushPose();
            poseStack.translate(-0.5D, -0.5D, -0.5D);

            if (alpha < 1) {
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            }

            if (!model.isCustomRenderer() && (stack.getItem() != Items.TRIDENT || isFixed)) {
                RenderType type = ItemBlockRenderTypes.getRenderType(stack, true);
                if (isGui && Objects.equals(type, Sheets.translucentCullBlockSheet())) {
                    type = Sheets.translucentCullBlockSheet();
                }
                if (alpha < 1) {
                    if (Objects.equals(type, RenderType.solid())) {
                        type = RenderType.translucentNoCrumbling();
                    } else if (Objects.equals(type, RenderType.cutout())) {
                        type = RenderType.cutoutMipped();
                    } else if (Objects.equals(type, Sheets.solidBlockSheet())) {
                        type = Sheets.translucentCullBlockSheet();
                    } else if (Objects.equals(type, Sheets.cutoutBlockSheet())) {
                        type = Sheets.translucentCullBlockSheet();
                    }
                }

                VertexConsumer ivertexconsumer = ItemRenderer.getFoilBuffer(buffer, type, true, stack.hasFoil());
                renderTintedModel(model, stack, light, overlay, poseStack, ivertexconsumer, r, g, b, alpha);
            } else {
                //noinspection deprecation
                GlStateManager._color4f(r, g, b, alpha);
                stack.getItem().getItemStackTileEntityRenderer().renderByItem(stack, transformType, poseStack, buffer, light, overlay);
                //noinspection deprecation
                GlStateManager._color4f(1, 1, 1, 1);
            }

            if (alpha < 1) {
                RenderSystem.disableBlend();
            }

            poseStack.popPose();
        }
    }

    private static void renderTintedModel(BakedModel model, ItemStack stack, int light, int overlay, PoseStack poseStack, VertexConsumer buffer, float r, float g, float b, float alpha) {
        Random random = new Random();

        for (Direction direction : Direction.values()) {
            random.setSeed(42);
            //noinspection deprecation
            renderTintedQuads(poseStack, buffer, model.getQuads(null, direction, random), stack, light, overlay, r, g, b, alpha);
        }

        random.setSeed(42);
        //noinspection deprecation
        renderTintedQuads(poseStack, buffer, model.getQuads(null, null, random), stack, light, overlay, r, g, b, alpha);
    }

    private static void renderTintedQuads(PoseStack poseStack, VertexConsumer buffer, List<BakedQuad> quads, ItemStack stack, int light, int overlay, float r, float g, float b, float alpha) {
        PoseStack.Pose pose = poseStack.last();

        for (BakedQuad bakedquad : quads) {
            if (bakedquad.isTinted()) {
                int mixColor = Minecraft.getInstance().getItemRenderer().itemColors.getColor(stack, bakedquad.getTintIndex());
                float ir = (float)(mixColor >> 16 & 255) / 255f;
                float ig = (float)(mixColor >> 8 & 255) / 255f;
                float ib = (float)(mixColor & 255) / 255f;
                buffer.addVertexData(pose, bakedquad, r * ir, g * ig, b * ib, alpha, light, overlay, true);
            } else {
                buffer.addVertexData(pose, bakedquad, r, g, b, alpha, light, overlay, true);
            }
        }
    }

    /**
     * Renders an {@link ItemStack item} into a gui. This allows to set the size of the item and whether the
     * amount should be included.
     */
    public static void renderItemGui(PoseStack poseStack, MultiBufferSource buffer, ItemStack stack, int x, int y, int size, boolean includeAmount) {
        renderItemGui(poseStack, buffer, stack, x, y, size, includeAmount, 1, 1, 1, 1);
    }

    /**
     * Renders an {@link ItemStack item} into a gui. This allows to set the size of the item and whether the
     * amount should be included.
     */
    public static void renderItemGui(PoseStack poseStack, MultiBufferSource buffer, ItemStack stack, int x, int y, int size, boolean includeAmount, float r, float g, float b, float alpha) {
        if (!stack.isEmpty()) {
            BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, null, Minecraft.getInstance().player);

            poseStack.pushPose();
            Minecraft.getInstance().getTextureManager().bind(InventoryMenu.BLOCK_ATLAS);
            //noinspection ConstantConditions
            Minecraft.getInstance().getTextureManager().getTexture(InventoryMenu.BLOCK_ATLAS).setFilter(false, false);

            //noinspection deprecation
            RenderSystem.enableAlphaTest();
            RenderSystem.defaultAlphaFunc();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            //noinspection deprecation
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

            poseStack.translate(x, y, 50);
            poseStack.scale(size / 16f, size / 16f, 1);
            poseStack.translate(8.0F, 8.0F, 0.0F);
            poseStack.scale(1.0F, -1.0F, 1.0F);
            poseStack.scale(16.0F, 16.0F, 16.0F);

            if (!model.usesBlockLight()) {
                com.mojang.blaze3d.platform.Lighting.setupForFlatItems();
            }

            renderItemTinted(stack, ItemTransforms.TransformType.GUI, 0xf000f0, OverlayTexture.NO_OVERLAY, poseStack, buffer, r, g, b, alpha);
            ((MultiBufferSource.BufferSource) buffer).endBatch();

            RenderSystem.enableDepthTest();

            if (!model.usesBlockLight()) {
                com.mojang.blaze3d.platform.Lighting.setupFor3DItems();
            }

            //noinspection deprecation
            RenderSystem.disableAlphaTest();
            //noinspection deprecation
            RenderSystem.disableRescaleNormal();

            poseStack.popPose();

            if (includeAmount && stack.getCount() > 1) {
                poseStack.pushPose();
                poseStack.translate(x, y, 90);

                Font fr = Minecraft.getInstance().font;
                String text = Integer.toString(stack.getCount());
                fr.drawInBatch(text, (float) (17 - fr.width(text)), 9, 16777215, true, poseStack.last().pose(), buffer, false, 0, 15728880);

                poseStack.popPose();
            }
        }
    }
}
