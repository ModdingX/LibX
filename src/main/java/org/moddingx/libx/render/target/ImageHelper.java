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
     * Renders the given {@link RenderJob job} into an image. The future will be completed
     * when the next frame of the game is rendered.
     */
    public static CompletableFuture<NativeImage> render(RenderJob job) {
        CompletableFuture<NativeImage> future = new CompletableFuture<>();
        Minecraft.getInstance().progressTasks.add(() -> {
            try {
                future.complete(JobRenderer.renderJob(job));
            } catch (Exception e) {
                future.completeExceptionally(e);
            } finally {
                future.completeExceptionally(new Exception("Failed"));
            }
        });
        return future;
    }
}
