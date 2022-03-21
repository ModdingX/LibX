package io.github.noeppi_noeppi.libx.test.util;

import net.minecraftforge.srgutils.IMappingFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class Mappings {
    
    private static IMappingFile mappings;
    
    public static String remapField(Class<?> cls, String srg) {
        loadMappings();
        return getMappedClass(cls).map(c -> c.remapField(srg)).orElse(srg);
    }
    
    public static String remapMethod(Class<?> cls, String srg, String descriptor) {
        loadMappings();
        return getMappedClass(cls).map(c -> c.remapMethod(srg, descriptor)).orElse(srg);
    }
    
    private static Optional<IMappingFile.IClass> getMappedClass(Class<?> cls) {
        IMappingFile.IClass mapped = mappings.getClass(cls.getName().replace('.', '/'));
        return Optional.ofNullable(mapped);
    }
    
    private static void loadMappings() {
        if (mappings == null) {
            Path mappingsPath = Paths.get("build", "createSrgToMcp", "output.srg").toAbsolutePath().normalize();
            if (!Files.exists(mappingsPath)) throw new IllegalStateException("Mappings have not been created by ForgeGradle: " + mappingsPath);
            try (InputStream in = Files.newInputStream(mappingsPath)) {
                mappings = IMappingFile.load(in);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to load mappings from " + mappingsPath, e);
            }
        }
    }
}
