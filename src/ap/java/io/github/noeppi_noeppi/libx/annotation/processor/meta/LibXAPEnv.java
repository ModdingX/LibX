package io.github.noeppi_noeppi.libx.annotation.processor.meta;

import io.github.noeppi_noeppi.libx.annotation.processor.ProcessorEnv;

public class LibXAPEnv {

    public static ArtifactVersion minecraftVersion(ProcessorEnv env) {
        if (!env.options().containsKey("mod.properties.mc_version")) return null;
        return ArtifactVersion.parse(env.options().get("mod.properties.mc_version"));
    }

    public static ArtifactVersion modVersion(ProcessorEnv env) {
        if (!env.options().containsKey("mod.properties.mod_version")) return null;
        return ArtifactVersion.parse(env.options().get("mod.properties.mod_version"));
    }
    
    public static Boolean release(ProcessorEnv env) {
        if (!env.options().containsKey("mod.properties.release")) return null;
        return Boolean.parseBoolean(env.options().get("mod.properties.release"));
    }
}
