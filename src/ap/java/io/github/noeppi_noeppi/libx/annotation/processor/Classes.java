package io.github.noeppi_noeppi.libx.annotation.processor;

import java.util.Set;

public class Classes {

    public static final String MODX = "io.github.noeppi_noeppi.libx.mod.ModX";
    public static final String MODX_REGISTRATION = "io.github.noeppi_noeppi.libx.mod.ModX";
    public static final String MOD = "net.minecraftforge.fml.common.Mod";
    public static final String FOR_MOD = "io.github.noeppi_noeppi.libx.annotation.ForMod";

    public static final String PROCESSOR_INTERFACE = "io.github.noeppi_noeppi.libx.annotation.impl.ProcessorInterface";
    public static final String LAZY_MAP_BUILDER = "io.github.noeppi_noeppi.libx.annotation.impl.LazyMapBuilder";

    public static final String CONFIG = "io.github.noeppi_noeppi.libx.config.Config";
    public static final String CONFIG_MANAGER = "io.github.noeppi_noeppi.libx.config.ConfigManager";
    public static final String VALUE_MAPPER = "io.github.noeppi_noeppi.libx.config.ValueMapper";
    public static final String GENERIC_VALUE_MAPPER = "io.github.noeppi_noeppi.libx.config.GenericValueMapper";

    public static final String BAKED_MODEL = "net.minecraft.client.resources.model.BakedModel";
    public static final String REGISTRY = "net.minecraft.core.Registry";
    public static final String CODEC = "com.mojang.serialization.Codec";
    public static final String MAP_CODEC = "com.mojang.serialization.MapCodec";
    public static final String RECORD_CODEC_BUILDER = "com.mojang.serialization.codecs.RecordCodecBuilder";
    public static final String DATA_PROVIDER = "net.minecraft.data.DataProvider";
    public static final String DATA_GENERATOR = "net.minecraft.data.DataGenerator";
    public static final String DATA_FILE_HELPER = "net.minecraftforge.common.data.ExistingFileHelper";

    // When something is added here, also add it to ProcessorInterface.getCodecDefaultRegistryKey
    public static final Set<String> ALLOWED_REGISTRY_CODEC_TYPES = Set.of(
            "net.minecraft.world.level.dimension.DimensionType",
            "net.minecraft.world.level.biome.Biome",
            "net.minecraft.world.level.levelgen.surfacebuilders.ConfiguredSurfaceBuilder",
            "net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver",
            "net.minecraft.world.level.levelgen.feature.ConfiguredFeature",
            "net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature",
            "net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList",
            "net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool",
            "net.minecraft.world.level.levelgen.NoiseGeneratorSettings"
    );
}
