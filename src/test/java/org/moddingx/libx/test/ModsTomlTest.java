package org.moddingx.libx.test;

import com.moandjiezana.toml.Toml;
import org.junit.jupiter.api.Test;
import org.moddingx.libx.LibX;
import org.moddingx.libx.test.util.GradleProperties;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Tests values in mods.toml
public class ModsTomlTest {

    @Test
    public void testModsToml() throws Throwable {
        try (Reader in = new InputStreamReader(Objects.requireNonNull(LibX.class.getResourceAsStream("/META-INF/mods.toml"), "mods.toml file not found"))) {
            String version = GradleProperties.getProperty("forge_version").orElseThrow();
            String minecraft = version.substring(0, version.indexOf('-'));
            String forge = version.substring(version.indexOf('-') + 1);
            String loader = forge.substring(0, forge.indexOf('.'));

            Toml toml = new Toml().read(in);
            assertEquals("[" + loader + ",)", toml.getString("loaderVersion"), "Wrong loader version range");

            for (Toml dep : toml.getTables("dependencies.libx")) {
                if ("minecraft".equals(dep.getString("modId"))) {
                    assertEquals("[" + minecraft + ",)", dep.getString("versionRange"));
                }
                if ("forge".equals(dep.getString("modId"))) {
                    assertEquals("[" + forge + ",)", dep.getString("versionRange"));
                }
            }
        }
    }
}
