package org.moddingx.libx.datagen.provider.sandbox;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.Musics;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TimelineTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.attribute.*;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.timeline.Timeline;
import net.minecraft.world.timeline.Timelines;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.DatagenStage;
import org.moddingx.libx.datagen.provider.RegistryProviderBase;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * SandBox provider for {@link DimensionType dimension types}.
 *
 * This provider must run in the {@link DatagenStage#REGISTRY_SETUP registry setup} stage.
 */
public abstract class DimensionTypeProviderBase extends RegistryProviderBase {

    public static final EnvironmentAttributeMap OVERWORLD_ATTRIBUTES = EnvironmentAttributeMap.builder()
            .set(EnvironmentAttributes.FOG_COLOR, 0XFFC0D8FF)
            .set(EnvironmentAttributes.SKY_COLOR, OverworldBiomes.calculateSkyColor(0.8F))
            .set(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, 0XFF0A0A0A)
            .set(EnvironmentAttributes.CLOUD_COLOR, ARGB.white(0.8F))
            .set(EnvironmentAttributes.CLOUD_HEIGHT, 192.33F)
            .set(EnvironmentAttributes.BACKGROUND_MUSIC, BackgroundMusic.OVERWORLD)
            .set(EnvironmentAttributes.BED_RULE, BedRule.CAN_SLEEP_WHEN_DARK)
            .set(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, false)
            .set(EnvironmentAttributes.NETHER_PORTAL_SPAWNS_PIGLINS, true)
            .set(EnvironmentAttributes.AMBIENT_SOUNDS, AmbientSounds.LEGACY_CAVE_SETTINGS)
            .build();

    public static final EnvironmentAttributeMap NETHER_ATTRIBUTES = EnvironmentAttributeMap.builder()
            .set(EnvironmentAttributes.FOG_START_DISTANCE, 10.0F)
            .set(EnvironmentAttributes.FOG_END_DISTANCE, 96.0F)
            .set(EnvironmentAttributes.SKY_LIGHT_COLOR, Timelines.NIGHT_SKY_LIGHT_COLOR)
            .set(EnvironmentAttributes.SKY_LIGHT_LEVEL, 4.0F)
            .set(EnvironmentAttributes.SKY_LIGHT_FACTOR, 0.0F)
            .set(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, 0XFF302821)
            .set(EnvironmentAttributes.DEFAULT_DRIPSTONE_PARTICLE, ParticleTypes.DRIPPING_DRIPSTONE_LAVA)
            .set(EnvironmentAttributes.BED_RULE, BedRule.EXPLODES)
            .set(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, true)
            .set(EnvironmentAttributes.WATER_EVAPORATES, true)
            .set(EnvironmentAttributes.FAST_LAVA, true)
            .set(EnvironmentAttributes.PIGLINS_ZOMBIFY, false)
            .set(EnvironmentAttributes.CAN_START_RAID, false)
            .set(EnvironmentAttributes.SNOW_GOLEM_MELTS, true)
            .build();

    public static final EnvironmentAttributeMap THE_END_ATTRIBUTES = EnvironmentAttributeMap.builder()
            .set(EnvironmentAttributes.FOG_COLOR, 0XFF181318)
            .set(EnvironmentAttributes.SKY_LIGHT_COLOR, 0XFFAC60CD)
            .set(EnvironmentAttributes.SKY_COLOR, 0XFF000000)
            .set(EnvironmentAttributes.SKY_LIGHT_FACTOR, 0.0F)
            .set(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, 0XFF3F473F)
            .set(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(Musics.END))
            .set(EnvironmentAttributes.AMBIENT_SOUNDS, AmbientSounds.LEGACY_CAVE_SETTINGS)
            .set(EnvironmentAttributes.BED_RULE, BedRule.EXPLODES)
            .set(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, false)
            .build();

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
        private boolean hasEnderDragonFight;
        private double coordinateScale;
        private int minY;
        private int height;
        private int logicalHeight;
        private TagKey<Block> infiniburn;
        private float ambientLight;
        private IntProvider monsterSpawnLightTest;
        private int monsterSpawnBlockLightLimit;
        private DimensionType.Skybox skybox;
        private CardinalLighting.Type cardinalLightType;
        private EnvironmentAttributeMap attributes;
        @Nullable
        private TagKey<Timeline> timelinesTag;
        @Nullable
        private ResourceKey<WorldClock> defaultClock;

        private DimensionTypeBuilder() {
            this.hasFixedTime = false;
            this.hasSkyLight = true;
            this.hasCeiling = false;
            this.hasEnderDragonFight = false;
            this.coordinateScale = 1;
            this.minY = -64;
            this.height = 384;
            this.logicalHeight = 384;
            this.infiniburn = BlockTags.INFINIBURN_OVERWORLD;
            this.ambientLight = 0;
            this.monsterSpawnLightTest = UniformInt.of(0, 7);
            this.monsterSpawnBlockLightLimit = 0;
            this.skybox = DimensionType.Skybox.OVERWORLD;
            this.cardinalLightType = CardinalLighting.Type.DEFAULT;
            this.attributes = DimensionTypeProviderBase.OVERWORLD_ATTRIBUTES;
            this.timelinesTag = TimelineTags.IN_OVERWORLD;
            this.defaultClock = WorldClocks.OVERWORLD;
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

        /**
         * Marks this dimension type as being able to host an ender dragon fight.
         */
        public DimensionTypeBuilder enderDragonFight() {
            this.hasEnderDragonFight = true;
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
         * Sets the cardinal light type. Use {@link CardinalLighting.Type#NETHER} for nether-like
         * dimensions.
         */
        public DimensionTypeBuilder cardinalLight(CardinalLighting.Type cardinalLightType) {
            this.cardinalLightType = cardinalLightType;
            return this;
        }

        /**
         * Sets the default {@link WorldClock} for this dimension type. Defaults to
         * {@link WorldClocks#OVERWORLD}, matching the other overworld-like defaults of this builder.
         *
         * Pass {@code null} for a clockless dimension, as the nether is in vanilla. A dimension
         * without a clock has no time at all: {@code /time} fails for it and sleeping does not
         * advance time. End-like dimensions use {@link WorldClocks#THE_END}.
         */
        public DimensionTypeBuilder defaultClock(@Nullable ResourceKey<WorldClock> defaultClock) {
            this.defaultClock = defaultClock;
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
                this.hasEnderDragonFight,
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
                timelines,
                Optional.ofNullable(this.defaultClock)
                        .<Holder<WorldClock>>map(DimensionTypeProviderBase.this::holder)
            );
            return DimensionTypeProviderBase.this.registries.writableRegistry(Registries.DIMENSION_TYPE).createIntrusiveHolder(type);
        }
    }
}
