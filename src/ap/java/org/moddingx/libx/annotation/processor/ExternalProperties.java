package org.moddingx.libx.annotation.processor;

import org.moddingx.libx.annotation.processor.meta.ArtifactVersion;

import javax.annotation.Nullable;

public class ExternalProperties {

    @Nullable
    public static ArtifactVersion minecraftVersion(ProcessorEnv env) {
        if (!env.options().containsKey("mod.properties.mc_version")) return null;
        return ArtifactVersion.parse(env.options().get("mod.properties.mc_version"));
    }

    @Nullable
    public static ArtifactVersion modVersion(ProcessorEnv env) {
        if (!env.options().containsKey("mod.properties.mod_version")) return null;
        return ArtifactVersion.parse(env.options().get("mod.properties.mod_version"));
    }
    
    @Nullable
    public static Boolean release(ProcessorEnv env) {
        if (!env.options().containsKey("mod.properties.release")) return null;
        return Boolean.parseBoolean(env.options().get("mod.properties.release"));
    }
}
