package org.moddingx.libx.sandbox.generator;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import org.moddingx.libx.impl.sandbox.NoiseLayerSelector;
import org.moddingx.libx.sandbox.SandBox;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Stream;

/**
 * A {@link BiomeSource} that can generate multiple {@link BiomeLayer layers} using multiple {@link MultiNoiseBiomeSource noise biome sources}.
 */
public class LayeredBiomeSource extends BiomeSource {

    public static final Codec<LayeredBiomeSource> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("horizontal_scale").forGetter(biomes -> biomes.horizontalScale),
            Codec.DOUBLE.fieldOf("vertical_scale").forGetter(biomes -> biomes.verticalScale),
            RegistryCodecs.homogeneousList(SandBox.BIOME_LAYER, BiomeLayer.DIRECT_CODEC).fieldOf("layers").forGetter((LayeredBiomeSource biomes) -> biomes.layers)
    ).apply(instance, LayeredBiomeSource::new));
    
    private final double horizontalScale;
    private final double verticalScale;
    private final HolderSet<BiomeLayer> layers;
    
    private double[] weights;
    private NoiseLayerSelector sel;
    private MultiNoiseBiomeSource[] sources;
    private Climate.ParameterPoint[] ranges;

    /**
     * Creates a new {@code LayeredBiomeSource}.
     * @param horizontalScale The horizontal scale factor for the noise that determines the layer at a position.
     * @param verticalScale The vertical scale factor for the noise that determines the layer at a position.
     * @param layers The layers to use.
     */
    public LayeredBiomeSource(double horizontalScale, double verticalScale, HolderSet<BiomeLayer> layers) {
        this.horizontalScale = horizontalScale;
        this.verticalScale = verticalScale;
        this.layers = layers;
    }

    @Nonnull
    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return this.layers.stream().flatMap(layer -> layer.value().biomes().values().stream()).map(Pair::getSecond).distinct();
    }

    public void init(long seed) {
        List<BiomeLayer> layersInOrder = this.layers.stream().map(Holder::value).toList();
        this.weights = layersInOrder.stream().mapToDouble(BiomeLayer::weight).toArray();
        this.sel = new NoiseLayerSelector(this.horizontalScale, this.verticalScale, this.weights, RandomSource.create(seed * 0xA574225077B4ECADL));
        this.ranges = layersInOrder.stream().map(BiomeLayer::range).toArray(Climate.ParameterPoint[]::new);
        this.sources = layersInOrder.stream().map(layer -> MultiNoiseBiomeSource.createFromList(layer.biomes())).toArray(MultiNoiseBiomeSource[]::new);
    }

    @Nonnull
    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Nonnull
    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, @Nonnull Climate.Sampler sampler) {
        if (this.sel == null) throw new IllegalStateException("Random layer selector not initialised.");
        if (this.weights.length == 1) return this.sources[0].getNoiseBiome(x, y, z, sampler);
        Climate.TargetPoint target = sampler.sample(x, y, z);
        int matchAmount = 0;
        int lastMatch = 0;
        boolean[] matches = new boolean[this.weights.length];
        for (int i = 0; i < this.weights.length; i++) {
            if (isInRange(this.ranges[i], target)) {
                matchAmount += 1;
                matches[i] = true;
                lastMatch = i;
            }
        }
        if (matchAmount == 1) {
            return this.sources[lastMatch].getNoiseBiome(target);
        }
        if (matchAmount == 0) {
            // Nothing matches, one layer has to provide a biome, match against all of them.
            for (int i = 0; i < this.weights.length; i++) {
                matches[i] = true;
            }
        }
        int targetIdx = this.sel.sample(x, y, z, matches);
        return this.sources[targetIdx].getNoiseBiome(target);
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
        //noinspection StringBufferReplaceableByString
        StringBuilder sb = new StringBuilder("Biome builder");
        sb.append(" PV: ").append(OverworldBiomeBuilder.getDebugStringForPeaksAndValleys(peaks));
        sb.append(" C: ").append(builder.getDebugStringForContinentalness(continentalness));
        sb.append(" E: ").append(builder.getDebugStringForErosion(erosion));
        sb.append(" T: ").append(builder.getDebugStringForTemperature(temperature));
        sb.append(" H: ").append(builder.getDebugStringForHumidity(humidity));
        lines.add(sb.toString());
    }
}
