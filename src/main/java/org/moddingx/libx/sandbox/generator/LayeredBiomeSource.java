package org.moddingx.libx.sandbox.generator;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import org.moddingx.libx.impl.sandbox.layer.NoiseLayerSelector;
import org.moddingx.libx.sandbox.SandBox;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Stream;

/**
 * A {@link BiomeSource} that can generate multiple {@link BiomeLayer layers} using multiple {@link MultiNoiseBiomeSource noise biome sources}.
 */
public class LayeredBiomeSource extends BiomeSource {

    public static final Codec<LayeredBiomeSource> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryCodecs.homogeneousList(SandBox.BIOME_LAYER, BiomeLayer.DIRECT_CODEC).fieldOf("layers").forGetter((LayeredBiomeSource biomes) -> biomes.layers)
    ).apply(instance, LayeredBiomeSource::new));
    
    private final HolderSet<BiomeLayer> layers;
    
    private NoiseLayerSelector sel;
    private MultiNoiseBiomeSource[] sources;
    private Climate.ParameterPoint[] ranges;

    /**
     * Creates a new {@code LayeredBiomeSource}.
     * 
     * @param layers The layers to use.
     */
    public LayeredBiomeSource(HolderSet<BiomeLayer> layers) {
        this.layers = layers;
    }

    @Nonnull
    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return this.layers.stream().flatMap(layer -> layer.value().biomes().values().stream()).map(Pair::getSecond).distinct();
    }

    public void init(long seed) {
        List<BiomeLayer> layersInOrder = this.layers.stream().map(Holder::value).toList();
        this.sel = new NoiseLayerSelector(layersInOrder.stream().map(BiomeLayer::density).toList(), RandomSource.create(seed * 0xA574225077B4ECADL));
        this.ranges = layersInOrder.stream().map(BiomeLayer::range).toArray(Climate.ParameterPoint[]::new);
        this.sources = layersInOrder.stream().map(layer -> MultiNoiseBiomeSource.createFromList(layer.biomes())).toArray(MultiNoiseBiomeSource[]::new);
    }

    @Nonnull
    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    private int getNoiseLayer(int x, int y, int z, @Nonnull Climate.TargetPoint target) {
        if (this.sel == null) throw new IllegalStateException("Random layer selector not initialised.");
        if (this.sources.length == 1) return 0;
        int matchAmount = 0;
        int lastMatch = 0;
        boolean[] matches = new boolean[this.sources.length];
        for (int i = 0; i < this.sources.length; i++) {
            if (isInRange(this.ranges[i], target)) {
                matchAmount += 1;
                matches[i] = true;
                lastMatch = i;
            }
        }
        if (matchAmount == 1) {
            return lastMatch;
        }
        if (matchAmount == 0) {
            // Nothing matches, one layer has to provide a biome, match against all of them.
            for (int i = 0; i < this.sources.length; i++) {
                matches[i] = true;
            }
        }
        return this.sel.sample(x, y, z, matches);
    }

    @Nonnull
    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, @Nonnull Climate.Sampler sampler) {
        Climate.TargetPoint target = sampler.sample(x, y, z);
        return this.sources[this.getNoiseLayer(x, y, z, target)].getNoiseBiome(target);
    }
    
    @SuppressWarnings("RedundantIfStatement")
    private static boolean isInRange(Climate.ParameterPoint point, Climate.TargetPoint target) {
        if (point.temperature().distance(target.temperature()) != 0) return false;
        if (point.humidity().distance(target.humidity()) != 0) return false;
        if (point.continentalness().distance(target.continentalness()) != 0) return false;
        if (point.erosion().distance(target.erosion()) != 0) return false;
        if (point.depth().distance(target.depth()) != 0) return false;
        if (point.weirdness().distance(target.weirdness()) != 0) return false;
        return true;
    }
    
    @Override
    public void addDebugInfo(List<String> lines, BlockPos pos, Climate.Sampler sampler) {
        Climate.TargetPoint target = sampler.sample(QuartPos.fromBlock(pos.getX()), QuartPos.fromBlock(pos.getY()), QuartPos.fromBlock(pos.getZ()));
        float continentalness = Climate.unquantizeCoord(target.continentalness());
        float erosion = Climate.unquantizeCoord(target.erosion());
        float temperature = Climate.unquantizeCoord(target.temperature());
        float humidity = Climate.unquantizeCoord(target.humidity());
        float weirdness = Climate.unquantizeCoord(target.weirdness());
        double peaks = NoiseRouterData.peaksAndValleys(weirdness);
        OverworldBiomeBuilder builder = new OverworldBiomeBuilder();
        StringBuilder sb = new StringBuilder("Biome builder");
        sb.append(" PV: ").append(OverworldBiomeBuilder.getDebugStringForPeaksAndValleys(peaks));
        sb.append(" C: ").append(builder.getDebugStringForContinentalness(continentalness));
        sb.append(" E: ").append(builder.getDebugStringForErosion(erosion));
        sb.append(" T: ").append(builder.getDebugStringForTemperature(temperature));
        sb.append(" H: ").append(builder.getDebugStringForHumidity(humidity));
        lines.add(sb.toString());
        if (this.sel != null) {
            sb = new StringBuilder("Layer selector");
            sb.append(" L: ").append(this.getNoiseLayer(pos.getX(), pos.getY(), pos.getZ(), target));
            sb.append(" M: ").append(this.sources.length);
            lines.add(sb.toString());
        }
    }
}
