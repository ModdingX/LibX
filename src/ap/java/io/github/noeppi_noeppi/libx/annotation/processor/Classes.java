package io.github.noeppi_noeppi.libx.annotation.processor;

import java.util.Set;

public class Classes {

    public static final String MODX = "io.github.noeppi_noeppi.libx.mod.ModX";
    public static final String MODX_REGISTRATION = "io.github.noeppi_noeppi.libx.mod.registration.ModXRegistration";
    public static final String MOD = "net.minecraftforge.fml.common.Mod";
    public static final String FOR_MOD = "io.github.noeppi_noeppi.libx.annotation.ForMod";
    public static final String DIST = "net.minecraftforge.api.distmarker.Dist";
    public static final String ONLY_IN = "net.minecraftforge.api.distmarker.OnlyIn";
    public static final String DIST_EXECUTOR = "net.minecraftforge.fml.DistExecutor";

    public static final String PROCESSOR_INTERFACE = "io.github.noeppi_noeppi.libx.annotation.impl.ProcessorInterface";
    public static final String LAZY_MAP_BUILDER = "io.github.noeppi_noeppi.libx.annotation.impl.LazyMapBuilder";

    public static final String CONFIG = "io.github.noeppi_noeppi.libx.config.Config";
    public static final String CONFIG_MANAGER = "io.github.noeppi_noeppi.libx.config.ConfigManager";
    public static final String VALUE_MAPPER = "io.github.noeppi_noeppi.libx.config.ValueMapper";
    public static final String GENERIC_VALUE_MAPPER = "io.github.noeppi_noeppi.libx.config.GenericValueMapper";

    public static final String BAKED_MODEL = "net.minecraft.client.resources.model.BakedModel";
    public static final String MODEL_REGISTRY_EVENT = "net.minecraftforge.client.event.ModelRegistryEvent";
    public static final String MODEL_BAKE_EVENT = "net.minecraftforge.client.event.ModelBakeEvent";
    public static final String MODEL_BAKERY = "net.minecraftforge.client.model.ForgeModelBakery";
    public static final String CODEC = "com.mojang.serialization.Codec";
    public static final String MAP_CODEC = "com.mojang.serialization.MapCodec";
    public static final String RECORD_CODEC_BUILDER = "com.mojang.serialization.codecs.RecordCodecBuilder";
    public static final String GATHER_DATA_EVENT = "net.minecraftforge.forge.event.lifecycle.GatherDataEvent";
    public static final String DATA_PROVIDER = "net.minecraft.data.DataProvider";
    public static final String DATA_GENERATOR = "net.minecraft.data.DataGenerator";
    public static final String DATA_FILE_HELPER = "net.minecraftforge.common.data.ExistingFileHelper";
    
    public static String sourceName(String cls) {
        return cls.replace('$', '.');
    }
}
