package org.moddingx.libx.impl.render;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.WeakHashMap;

public class BlockOverlayQuadCache {

    private static final WeakHashMap<TextureAtlasSprite, WeakHashMap<BakedQuad, BakedQuad>> quadCache = new WeakHashMap<>();

    @Nullable
    public static BakedQuad get(BakedQuad source, TextureAtlasSprite sprite) {
        WeakHashMap<BakedQuad, BakedQuad> quads = quadCache.computeIfAbsent(sprite, k -> new WeakHashMap<>());
        return quads.get(source);
    }
    
    public static void put(BakedQuad source, BakedQuad transformed) {
        WeakHashMap<BakedQuad, BakedQuad> quads = quadCache.computeIfAbsent(transformed.getSprite(), k -> new WeakHashMap<>());
        quads.put(source, transformed);
    }
    
    public static void resourcesReload(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new SimplePreparableReloadListener<Void>() {
            
            @Nonnull
            @Override
            protected Void prepare(@Nonnull ResourceManager rm, @Nonnull ProfilerFiller filler) {
                return null;
            }

            @Override
            protected void apply(@Nonnull Void obj, @Nonnull ResourceManager rm, @Nonnull ProfilerFiller filler) {
                quadCache.clear();
            }
        });
    }
}
