package org.moddingx.libx.coremods.transformers;

import net.neoforged.fml.jarcontents.JarContents;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RegisterClassIdsTest {

    private static final String METADATA_PATH = "META-INF/libx_registration.json";

    @TempDir
    public Path tempDir;

    @Test
    public void loadsMetadataFromJar() throws IOException {
        Path jar = this.createJar("test.jar", metadata("test/JarClass", "field", "test:jar"));
        try (JarContents contents = JarContents.ofPath(jar)) {
            Map<String, List<RegisterClassIds.FieldEntry>> entries = RegisterClassIds.loadEntries(List.of(contents));
            assertEntry(entries, "test/JarClass", "field", "test:jar");
        }
    }

    @Test
    public void loadsMetadataFromDirectory() throws IOException {
        Path directory = this.tempDir.resolve("directory");
        this.writeMetadata(directory, metadata("test/DirectoryClass", "field", "test:directory"));
        try (JarContents contents = JarContents.ofPath(directory)) {
            Map<String, List<RegisterClassIds.FieldEntry>> entries = RegisterClassIds.loadEntries(List.of(contents));
            assertEntry(entries, "test/DirectoryClass", "field", "test:directory");
        }
    }

    @Test
    public void loadsMetadataFromCompositeRoots() throws IOException {
        Path classes = this.tempDir.resolve("classes");
        Path resources = this.tempDir.resolve("resources");
        Files.createDirectories(classes);
        this.writeMetadata(resources, metadata("test/CompositeClass", "field", "test:composite"));
        try (JarContents contents = JarContents.ofPaths(List.of(classes, resources))) {
            Map<String, List<RegisterClassIds.FieldEntry>> entries = RegisterClassIds.loadEntries(List.of(contents));
            assertEntry(entries, "test/CompositeClass", "field", "test:composite");
        }
    }

    @Test
    public void combinesMultipleModFiles() throws IOException {
        Path first = this.createJar("first.jar", metadata("test/FirstClass", "first", "test:first"));
        Path second = this.createJar("second.jar", metadata("test/SecondClass", "second", "test:second"));
        try (JarContents firstContents = JarContents.ofPath(first);
             JarContents secondContents = JarContents.ofPath(second)) {
            Map<String, List<RegisterClassIds.FieldEntry>> entries =
                    RegisterClassIds.loadEntries(List.of(firstContents, secondContents));
            assertEntry(entries, "test/FirstClass", "first", "test:first");
            assertEntry(entries, "test/SecondClass", "second", "test:second");
        }
    }

    @Test
    public void ignoresMissingMetadata() throws IOException {
        Path directory = this.tempDir.resolve("missing");
        Files.createDirectories(directory);
        try (JarContents contents = JarContents.ofPath(directory)) {
            assertTrue(RegisterClassIds.loadEntries(List.of(contents)).isEmpty());
        }
    }

    @Test
    public void continuesAfterFailingModFile() throws IOException {
        Path broken = this.tempDir.resolve("broken");
        Files.createDirectories(broken.resolve(METADATA_PATH));
        Path valid = this.createJar("valid.jar", metadata("test/ValidClass", "field", "test:valid"));
        try (JarContents brokenContents = JarContents.ofPath(broken);
             JarContents validContents = JarContents.ofPath(valid)) {
            Map<String, List<RegisterClassIds.FieldEntry>> entries =
                    RegisterClassIds.loadEntries(List.of(brokenContents, validContents));
            assertEntry(entries, "test/ValidClass", "field", "test:valid");
        }
    }

    @Test
    public void continuesAfterMalformedMetadata() throws IOException {
        Path broken = this.createJar("malformed.jar", "{\"not\":\"an array\"}");
        Path valid = this.createJar("valid-after-malformed.jar",
                metadata("test/ValidAfterMalformedClass", "field", "test:valid_after_malformed"));
        try (JarContents brokenContents = JarContents.ofPath(broken);
             JarContents validContents = JarContents.ofPath(valid)) {
            Map<String, List<RegisterClassIds.FieldEntry>> entries =
                    RegisterClassIds.loadEntries(List.of(brokenContents, validContents));
            assertEntry(entries, "test/ValidAfterMalformedClass", "field", "test:valid_after_malformed");
        }
    }

    private Path createJar(String name, String metadata) throws IOException {
        Path jar = this.tempDir.resolve(name);
        try (OutputStream output = Files.newOutputStream(jar);
             JarOutputStream jarOutput = new JarOutputStream(output)) {
            jarOutput.putNextEntry(new JarEntry(METADATA_PATH));
            jarOutput.write(metadata.getBytes(StandardCharsets.UTF_8));
            jarOutput.closeEntry();
        }
        return jar;
    }

    private void writeMetadata(Path root, String metadata) throws IOException {
        Path file = root.resolve(METADATA_PATH);
        Files.createDirectories(file.getParent());
        Files.writeString(file, metadata, StandardCharsets.UTF_8);
    }

    private static String metadata(String className, String field, String id) {
        return "[{\"class\":\"" + className + "\",\"field\":\"" + field + "\",\"id\":\"" + id + "\"}]";
    }

    private static void assertEntry(
            Map<String, List<RegisterClassIds.FieldEntry>> entries,
            String className,
            String field,
            String id
    ) {
        assertEquals(List.of(new RegisterClassIds.FieldEntry(field, id)), entries.get(className));
    }
}
