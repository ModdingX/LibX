package org.moddingx.libx.datagen.provider.sandbox;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.timeline.Timeline;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.DatagenStage;
import org.moddingx.libx.datagen.provider.RegistryProviderBase;

import javax.annotation.Nullable;
import java.util.List;

/**
 * SandBox provider for {@link DimensionType dimension types}.
 *
 * This provider must run in the {@link DatagenStage#REGISTRY_SETUP registry setup} stage.
 */
public abstract class DimensionTypeProviderBase extends RegistryProviderBase {

    protected DimensionTypeProviderBase(DatagenContext ctx) {
        super(ctx, DatagenStage.REGISTRY_SETUP);
    }

    @Override
    public final String getName() {
        return this.mod.modid + " dimension types";
    }

    public DimensionTypeBuilder dimension() {
        return new DimensionTypeBuilder();
    }

    /**
     * Creates a new builder for dimension environment attributes.
     */
    public EnvironmentAttributeMap.Builder attributes() {
        return EnvironmentAttributeMap.builder();
    }

    public class DimensionTypeBuilder {

        private boolean hasFixedTime;
        private boolean hasSkyLight;
        private boolean hasCeiling;
        private double coordinateScale;
        private int minY;
        private int height;
        private int logicalHeight;
        private TagKey<Block> infiniburn;
        private float ambientLight;
        private IntProvider monsterSpawnLightTest;
        private int monsterSpawnBlockLightLimit;
        private DimensionType.Skybox skybox;
        private DimensionType.CardinalLightType cardinalLightType;
        private EnvironmentAttributeMap attributes;
        @Nullable
        private TagKey<Timeline> timelinesTag;

        private DimensionTypeBuilder() {
            this.hasFixedTime = false;
            this.hasSkyLight = true;
            this.hasCeiling = false;
            this.coordinateScale = 1;
            this.minY = -64;
            this.height = 384;
            this.logicalHeight = 384;
            this.infiniburn = BlockTags.INFINIBURN_OVERWORLD;
            this.ambientLight = 0;
            this.monsterSpawnLightTest = UniformInt.of(0, 7);
            this.monsterSpawnBlockLightLimit = 0;
            this.skybox = DimensionType.Skybox.OVERWORLD;
            this.cardinalLightType = DimensionType.CardinalLightType.DEFAULT;
            this.attributes = EnvironmentAttributeMap.EMPTY;
            this.timelinesTag = null;
        }

        /**
         * Marks this dimension type as having a fixed time (sun/moon don't move).
         */
        public DimensionTypeBuilder fixedTime() {
            this.hasFixedTime = true;
            return this;
        }

        public DimensionTypeBuilder sky(boolean skyLight, boolean ceiling) {
            this.hasSkyLight = skyLight;
            this.hasCeiling = ceiling;
            return this;
        }

        public DimensionTypeBuilder coordinateScale(double coordinateScale) {
            this.coordinateScale = coordinateScale;
            return this;
        }

        public DimensionTypeBuilder height(int minY, int maxY) {
            return this.height(minY, maxY, maxY - minY);
        }

        public DimensionTypeBuilder height(int minY, int maxY, int logicalHeight) {
            this.minY = minY;
            this.height = maxY - minY;
            this.logicalHeight = Math.min(logicalHeight, this.height);
            return this;
        }

        public DimensionTypeBuilder infiniteBurn(TagKey<Block> key) {
            this.infiniburn = key;
            return this;
        }

        public DimensionTypeBuilder ambientLight(float ambientLight) {
            this.ambientLight = ambientLight;
            return this;
        }

        public DimensionTypeBuilder monsterSpawnRule(int maxSkyLight, int maxBlockLight) {
            return this.monsterSpawnRule(maxSkyLight == 0 ? ConstantInt.of(0) : UniformInt.of(0, maxSkyLight), maxBlockLight);
        }

        public DimensionTypeBuilder monsterSpawnRule(IntProvider skyLight, int maxBlockLight) {
            this.monsterSpawnLightTest = skyLight;
            this.monsterSpawnBlockLightLimit = maxBlockLight;
            return this;
        }

        /**
         * Sets the skybox type for this dimension. Use {@link DimensionType.Skybox#NONE} for nether-like
         * dimensions, {@link DimensionType.Skybox#END} for end-like, and {@link DimensionType.Skybox#OVERWORLD}
         * (the default) for overworld-like dimensions.
         */
        public DimensionTypeBuilder skybox(DimensionType.Skybox skybox) {
            this.skybox = skybox;
            return this;
        }

        /**
         * Sets the cardinal light type. Use {@link DimensionType.CardinalLightType#NETHER} for nether-like
         * dimensions.
         */
        public DimensionTypeBuilder cardinalLight(DimensionType.CardinalLightType cardinalLightType) {
            this.cardinalLightType = cardinalLightType;
            return this;
        }

        /**
         * Sets the environment attributes for this dimension type.
         */
        public DimensionTypeBuilder attributes(EnvironmentAttributeMap.Builder builder) {
            this.attributes = builder.build();
            return this;
        }

        /**
         * Sets the timelines tag for this dimension (e.g. {@code TimelineTags.IN_OVERWORLD}).
         */
        public DimensionTypeBuilder timelines(TagKey<Timeline> tag) {
            this.timelinesTag = tag;
            return this;
        }

        /**
         * Builds the {@link DimensionType}.
         *
         * This method returns an {@link Holder.Reference.Type#INTRUSIVE intrusive holder} that must be properly
         * added the registry. {@link RegistryProviderBase} does this automatically if the result is stored in a
         * {@code public}, non-{@code static} field inside the provider.
         */
        public Holder<DimensionType> build() {
            HolderSet<Timeline> timelines;
            if (this.timelinesTag != null) {
                timelines = DimensionTypeProviderBase.this.set(this.timelinesTag);
            } else {
                timelines = HolderSet.direct(List.of());
            }
            DimensionType type = new DimensionType(
                this.hasFixedTime,
                this.hasSkyLight,
                this.hasCeiling,
                this.coordinateScale,
                this.minY,
                this.height,
                this.logicalHeight,
                this.infiniburn,
                this.ambientLight,
                new DimensionType.MonsterSettings(this.monsterSpawnLightTest, this.monsterSpawnBlockLightLimit),
                this.skybox,
                this.cardinalLightType,
                this.attributes,
                timelines
            );
            return DimensionTypeProviderBase.this.registries.writableRegistry(Registries.DIMENSION_TYPE).createIntrusiveHolder(type);
        }
    }
}
