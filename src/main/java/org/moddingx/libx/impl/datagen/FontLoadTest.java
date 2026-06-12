package org.moddingx.libx.impl.datagen;

import net.minecraft.client.StringSplitter;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.impl.datagen.load.DatagenFontLoader;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class FontLoadTest implements DataProvider {
    
    public FontLoadTest(DatagenContext ctx) {
        ResourceManager rm = ctx.resourceManager(PackType.CLIENT_RESOURCES);
        // Skip font test if vanilla client assets are not available (e.g. server-only data run)
        if (rm.getResource(net.minecraft.resources.Identifier.withDefaultNamespace("textures/font/ascii.png")).isEmpty()) {
            return;
        }
        // Font metric loading is wonky, test that it works
        StringSplitter fontMetrics = DatagenFontLoader.getFontMetrics(rm);
        if (fontMetrics == DatagenFontLoader.MISSING) {
            throw new IllegalStateException("Datagen font loading failed.");
        }
        // Test that we actually did load meaningful font metrics
        if (fontMetrics.stringWidth("i") == fontMetrics.stringWidth("M")) {
            throw new IllegalStateException("Datagen font loading seems to have produced an invalid result.");
        }
    }

    @Nonnull
    @Override
    public String getName() {
        return "LibX font load test";
    }
    
    @Nonnull
    @Override
    public CompletableFuture<?> run(@Nonnull CachedOutput output) {
        return CompletableFuture.completedFuture(null);
    }
}
