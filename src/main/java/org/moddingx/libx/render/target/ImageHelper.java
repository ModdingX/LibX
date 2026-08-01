package org.moddingx.libx.render.target;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import org.moddingx.libx.impl.render.JobRenderer;

import java.util.concurrent.CompletableFuture;

/**
 * Helper to render scenes into images in the context of minecraft.
 */
public class ImageHelper {

    /**
     * Renders the given {@link RenderJob job} into an image.
     */
    public static CompletableFuture<NativeImage> render(RenderJob job) {
        CompletableFuture<NativeImage> future = new CompletableFuture<>();
        Minecraft.getInstance().execute(() -> {
            try {
                JobRenderer.renderJob(job, future);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }
}
