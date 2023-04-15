package org.moddingx.libx.impl.sandbox;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import org.moddingx.libx.codec.CodecHelper;
import org.moddingx.libx.datapack.DatapackHelper;
import org.moddingx.libx.sandbox.generator.BiomeLayer;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

// Make biome layers for overworld and nether.
// These only use vanilla biomes, so we can work with the regular lookup provider.
public class InternalBiomeLayerProvider implements DataProvider {

    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;

    public InternalBiomeLayerProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        this.output = output;
        this.lookupProvider = lookupProvider;
    }

    @Nonnull
    @Override
    public final String getName() {
        return "libx biome layers";
    }

    @Nonnull
    @Override
    public CompletableFuture<?> run(@Nonnull CachedOutput cache) {
        return this.lookupProvider.thenCompose(provider -> CompletableFuture.allOf(
                this.write(cache, provider, BiomeLayer.OVERWORLD, MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD),
                this.write(cache, provider, BiomeLayer.NETHER, MultiNoiseBiomeSourceParameterList.Preset.NETHER)
        ));
    }
    
    private CompletableFuture<?> write(CachedOutput cache, HolderLookup.Provider provider, ResourceKey<BiomeLayer> key, MultiNoiseBiomeSourceParameterList.Preset preset) {
        HolderLookup<Biome> biomes = provider.lookupOrThrow(Registries.BIOME);
        BiomeLayer layer = new BiomeLayer(preset.provider().apply(biomes::getOrThrow));
        return saveMinified(cache,
                CodecHelper.JSON.write(BiomeLayer.DIRECT_CODEC, layer, provider),
                this.output.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(DatapackHelper.registryPath(key))
        );
    }

   @SuppressWarnings("UnstableApiUsage")
   private static CompletableFuture<?> saveMinified(CachedOutput output, JsonElement json, Path path) {
      return CompletableFuture.runAsync(() -> {
         try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            @SuppressWarnings("deprecation")
            HashingOutputStream hashing = new HashingOutputStream(Hashing.sha1(), bytes);
            try (JsonWriter writer = new JsonWriter(new OutputStreamWriter(hashing, StandardCharsets.UTF_8))) {
               writer.setIndent("");
               GsonHelper.writeValue(writer, json, KEY_COMPARATOR);
            }
            output.writeIfNeeded(path, bytes.toByteArray(), hashing.hash());
         } catch (IOException e) {
            LOGGER.error("Failed to save file to {}", path, e);
         }
      }, Util.backgroundExecutor());
   }
}
