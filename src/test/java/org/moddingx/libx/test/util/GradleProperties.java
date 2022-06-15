package org.moddingx.libx.test.util;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

public class GradleProperties {

    @Nullable
    private static Map<String, String> map = null;

    public static Optional<String> getProperty(String key) {
        if (map == null) {
            try (Reader reader = Files.newBufferedReader(Paths.get("gradle.properties"))) {
                Properties properties = new Properties();
                properties.load(reader);
                map = properties.entrySet().stream()
                        .map(entry -> Map.entry(entry.getKey().toString(), entry.getValue().toString()))
                        .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return Optional.ofNullable(map.get(key));
    }
}
