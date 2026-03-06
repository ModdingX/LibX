package org.moddingx.libx.annotation.processor;

public class Classes {

    public static final String MODX = "org.moddingx.libx.mod.ModX";
    public static final String MODX_REGISTRATION = "org.moddingx.libx.mod.ModXRegistration";
    public static final String MOD = "net.neoforged.fml.common.Mod";
    public static final String FOR_MOD = "org.moddingx.libx.annotation.ForMod";
    public static final String DIST = "net.neoforged.api.distmarker.Dist";
    public static final String ONLY_IN = "net.neoforged.api.distmarker.OnlyIn";
    public static final String ONLY_INS = "net.neoforged.api.distmarker.OnlyIns";

    public static final String ITEM = "net.minecraft.world.item.Item";
    public static final String BLOCK = "net.minecraft.world.level.block.Block";
    public static final String FLUID = "net.minecraft.world.level.material.Fluid";
    public static final String ENTITY_TYPE = "net.minecraft.world.entity.EntityType";

    public static final String PROCESSOR_INTERFACE = "org.moddingx.libx.annotation.impl.ProcessorInterface";
    public static final String LAZY_MAP_BUILDER = "org.moddingx.libx.annotation.impl.LazyMapBuilder";
    public static final String REGISTRATION_PROPERTIES_HELPER = "org.moddingx.libx.annotation.impl.RegistrationPropertiesHelper";

    public static final String CONFIG = "org.moddingx.libx.config.Config";
    public static final String CONFIG_MANAGER = "org.moddingx.libx.config.ConfigManager";
    public static final String VALUE_MAPPER = "org.moddingx.libx.config.mapper.ValueMapper";
    public static final String GENERIC_VALUE_MAPPER = "org.moddingx.libx.config.mapper.GenericValueMapper";
    public static final String VALUE_MAPPER_FACTORY = "org.moddingx.libx.config.mapper.MapperFactory";

    public static final String REGISTERABLE = "org.moddingx.libx.registration.Registerable";
    public static final String REGISTRY = "net.minecraft.core.Registry";
    public static final String REGISTRIES = "net.minecraft.core.registries.Registries";
    public static final String RESOURCE_KEY = "net.minecraft.resources.ResourceKey";
    public static final String NEOFORGE_KEYS = "net.neoforged.neoforge.registries.NeoForgeRegistries$Keys";
    
    public static final String BAKED_MODEL = "net.minecraft.client.resources.model.BakedModel";
    public static final String MODEL_REGISTRY_EVENT = "net.neoforged.neoforge.client.event.ModelEvent$RegisterAdditional";
    public static final String MODEL_BAKE_EVENT = "net.neoforged.neoforge.client.event.ModelEvent$BakingCompleted";
    public static final String CODEC = "com.mojang.serialization.Codec";
    public static final String MAP_CODEC = "com.mojang.serialization.MapCodec";
    public static final String RECORD_CODEC_BUILDER = "com.mojang.serialization.codecs.RecordCodecBuilder";

    public static final String OVERRIDING_METHODS_SUPER = "javax.annotation.OverridingMethodsMustInvokeSuper";
    public static final String JETBRAINS_NOTNULL = "org.jetbrains.annotations.NotNull";
    public static final String JETBRAINS_NULLABLE = "org.jetbrains.annotations.Nullable";

    public static String sourceName(String cls) {
        return cls.replace('$', '.');
    }
}
