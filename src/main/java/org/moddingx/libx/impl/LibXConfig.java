package org.moddingx.libx.impl;

import org.moddingx.libx.annotation.config.RegisterConfig;
import org.moddingx.libx.config.Config;

@RegisterConfig
public class LibXConfig {

    @Config("If disabled, LibX doesn't override \"Mod List\" section in crash reports to include SHA1 and Murmur2 hashes")
    public static boolean appendHashesInCrashes = true;
}
