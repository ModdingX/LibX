package org.moddingx.libx.datagen.provider.sandbox;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.DatagenStage;
import org.moddingx.libx.datagen.provider.RegistryProviderBase;

import java.util.OptionalLong;

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
    
    public class DimensionTypeBuilder {

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private OptionalLong fixedTime;
        private boolean hasSkyLight;
        private boolean hasCeiling;
        private boolean ultraWarm;
        private boolean natural;
        private double coordinateScale;
        private boolean bedWorks;
        private boolean respawnAnchorWorks;
        private int minY;
        private int height;
        private int logicalHeight;
        private TagKey<Block> infiniburn;
        private ResourceLocation effectsLocation;
        private float ambientLight;
        private boolean piglinSafe;
        private boolean hasRaids;
        private IntProvider monsterSpawnLightTest;
        private int monsterSpawnBlockLightLimit;
        
        private DimensionTypeBuilder() {
            this.fixedTime = OptionalLong.empty();
            this.hasSkyLight = true;
            this.hasCeiling = false;
            this.ultraWarm = false;
            this.natural = true;
            this.coordinateScale = 1;
            this.bedWorks = true;
            this.respawnAnchorWorks = false;
            this.minY = -64;
            this.height = 384;
            this.logicalHeight = 384;
            this.infiniburn = BlockTags.INFINIBURN_OVERWORLD;
            this.effectsLocation = BuiltinDimensionTypes.OVERWORLD_EFFECTS;
            this.ambientLight = 0;
            this.piglinSafe = false;
            this.hasRaids = true;
            this.monsterSpawnLightTest = UniformInt.of(0, 7);
            this.monsterSpawnBlockLightLimit = 0;
        }

        public DimensionTypeBuilder fixedTime(long time) {
            this.fixedTime = OptionalLong.of(time);
            return this;
        }

        public DimensionTypeBuilder sky(boolean skyLight, boolean ceiling) {
            this.hasSkyLight = skyLight;
            this.hasCeiling = ceiling;
            return this;
        }
        

        public DimensionTypeBuilder ultraWarm() {
            this.ultraWarm = true;
            return this;
        }

        public DimensionTypeBuilder nonNatural() {
            this.natural = false;
            return this;
        }

        public DimensionTypeBuilder coordinateScale(double coordinateScale) {
            this.coordinateScale = coordinateScale;
            return this;
        }

        public DimensionTypeBuilder respawnDevices(boolean bed, boolean respawnAnchor) {
            this.bedWorks = bed;
            this.respawnAnchorWorks = respawnAnchor;
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

        public DimensionTypeBuilder effects(String path) {
            return this.effects(DimensionTypeProviderBase.this.mod.resource(path));
        }
        
        public DimensionTypeBuilder effects(String namespace, String path) {
            return this.effects(new ResourceLocation(namespace, path));
        }
            
        public DimensionTypeBuilder effects(ResourceLocation id) {
            this.effectsLocation = id;
            return this;
        }

        public DimensionTypeBuilder ambientLight(float ambientLight) {
            this.ambientLight = ambientLight;
            return this;
        }

        public DimensionTypeBuilder piglinSafe() {
            this.piglinSafe = true;
            return this;
        }

        public DimensionTypeBuilder disableRaids() {
            this.hasRaids = false;
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
         * Builds the {@link DimensionType}.
         *
         * This method returns an {@link Holder.Reference.Type#INTRUSIVE intrusive holder} that must be properly
         * added the registry. {@link RegistryProviderBase} does this automatically if the result is stored in a
         * {@code public}, non-{@code static} field inside the provider.
         */
        public Holder<DimensionType> build() {
            DimensionType type = new DimensionType(
                    this.fixedTime, this.hasSkyLight, this.hasCeiling, this.ultraWarm, this.natural, this.coordinateScale, this.bedWorks,
                    this.respawnAnchorWorks, this.minY, this.height, this.logicalHeight, this.infiniburn, this.effectsLocation, this.ambientLight,
                    new DimensionType.MonsterSettings(this.piglinSafe, this.hasRaids, this.monsterSpawnLightTest, this.monsterSpawnBlockLightLimit)
            );
            return DimensionTypeProviderBase.this.registries.writableRegistry(Registries.DIMENSION_TYPE).createIntrusiveHolder(type);
        }
    }
}
