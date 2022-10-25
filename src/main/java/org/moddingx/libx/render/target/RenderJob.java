package org.moddingx.libx.render.target;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec2;

/**
 * A render job defines the logic on how to render a scene into an image. Can be used with {@link ImageHelper}.
 */
public interface RenderJob {

    /**
     * The image width in pixels.
     */
    int width();
    
    /**
     * The image height in pixels.
     */
    int height();

    /**
     * The projection matrix to use. By default creates an orthographic projection with a scale of 1.
     * (Everything from 0 to image width/height is projected onto the image).
     */
    default Matrix4f setupProjectionMatrix() {
        return Matrix4f.orthographic(this.width(), this.height(), 500, 6000);
    }

    /**
     * The modelView matrix to use. Do not confuse with the transformation matrix. By default this is
     * a translation matrix by 2000 units in negative z direction.
     * 
     * @see #setupTransformation(PoseStack)
     */
    default Matrix4f setupModelViewMatrix() {
        return Matrix4f.createTranslateMatrix(0, 0, -2000);
    }

    /**
     * Sets up initial transformation on the {@link PoseStack}. These transformations will be accounted
     * for when projecting points in {@link #renderOverlay(PoseStack, MultiBufferSource, Projector)}.
     * 
     * @see #setupModelViewMatrix()
     */
    default void setupTransformation(PoseStack poseStack) {
        //
    }

    /**
     * Renders the actual scene.
     */
    void render(PoseStack poseStack, MultiBufferSource buffer);

    /**
     * Gets whether this render job uses an overlay.
     * 
     * @see #renderOverlay(PoseStack, MultiBufferSource, Projector)
     */
    default boolean usesOverlay() {
        return false;
    }
    
    /**
     * Renders an overlay over the scene. This is useful if the scene used a non-orthographic projection matrix
     * (ie a 3d scene). This method is always called with an orthographic projection matrix to render a 2d overlay
     * over the scene. The viewport ranges from (0,0) to (width,height).
     * 
     * <b>In order for this to be called, {@link #usesOverlay()} must return {@code true}</b>
     * 
     * @param projector A point projector that can project points in the 3 dimensional space used in
     *                  {@link #render(PoseStack, MultiBufferSource)} to the two dimensional space after projection.
     * 
     * @see #usesOverlay()
     */
    default void renderOverlay(PoseStack poseStack, MultiBufferSource buffer, Projector projector) {
        
    }
    
    
    interface Projector {

        /**
         * Projects a three dimensional vector onto its 2d coordinates after applying transformation and projection matrix.
         */
        Vec2 projectPoint(Vector3f point);
    }
}
