package io.github.noeppi_noeppi.libx.impl.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ConfigState {
    
    private final ConfigImpl config;
    private final Map<ConfigKey, Object> values;

    public ConfigState(ConfigImpl config, ImmutableMap<ConfigKey, Object> values) {
        this.config = config;
        this.values = values;
    }
    
    public Object getValue(ConfigKey key) {
        if (!this.values.containsKey(key)) {
            throw new IllegalStateException("Can't get value from config state: Key is invalid.");
        }
        return this.values.get(key);
    }
    
    public void apply() {
        try {
            for (Map.Entry<ConfigKey, Object> entry : this.values.entrySet()) {
                ConfigKey key = entry.getKey();
                Object value = entry.getValue();
                key.field.setAccessible(true);
                key.field.set(null, value);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to insert value into field.", e);
        }
    }
    
    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.values.size());
        for (Map.Entry<ConfigKey, Object> entry : this.values.entrySet()) {
            ConfigKey key = entry.getKey();
            Object value = entry.getValue();
            buffer.writeUtf(key.field.getDeclaringClass().getName(), 0x7fff);
            buffer.writeUtf(key.field.getName(), 0x7fff);
            //noinspection unchecked
            ((ValueMapper<Object, ?>) key.mapper).toNetwork(value, buffer);
        }
    }

    public void writeToFile(@Nullable Path path, @Nullable Set<ConfigKey> keys) throws IOException {
        if (path == null) path = this.config.path;
        if (!Files.isDirectory(path.getParent())) {
            Files.createDirectories(path.getParent());
        }
        Writer writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        writer.write("{\n" + this.applyIndent(this.writeObject(keys == null ? this.values.keySet() : keys, this.config.groups, 0)) + "\n}\n");
        writer.close();
    }
    
    public String writeObject(@Nonnull Set<ConfigKey> keys, Set<ConfigGroup> groups, int pathStrip) {
        List<ConfigKey> simpleKeysSorted = keys.stream()
                .filter(key -> key.path.size() == pathStrip + 1)
                .sorted(ConfigKey.BY_PATH).collect(Collectors.toList());
        
        Map<String, Set<ConfigKey>> subGroups = keys.stream()
                .filter(key -> key.path.size() > pathStrip + 1)
                .collect(Collectors.groupingBy(key -> key.path.get(pathStrip), Collectors.toSet()));
        
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (ConfigKey key : simpleKeysSorted) {
            if (first) {
                first = false;
            } else {
                builder.append(",\n\n");
            }
            key.comment.forEach(line -> builder.append("// ").append(line.replace('\n', ' ')).append("\n"));
            builder.append("\"").append(quote(key.path.get(key.path.size() - 1))).append("\": ");
            Object value = this.values.get(key);
            //noinspection unchecked
            JsonElement json = ((ValueMapper<Object, ?>) key.mapper).toJson(value);
            builder.append(this.specialString(json));
        }
        
        List<String> subGroupKeys = subGroups.keySet().stream().sorted().collect(Collectors.toList());
        for (String group : subGroupKeys) {
            ConfigGroup cg = groups.stream()
                    .filter(g -> g.path.size() == pathStrip + 1)
                    .filter(g -> g.path.get(pathStrip).equals(group))
                    .findFirst().orElse(null);
            Set<ConfigGroup> subGroupInstances = groups.stream()
                    .filter(g -> g.path.size() > pathStrip + 1)
                    .filter(g -> g.path.get(pathStrip).equals(group))
                    .collect(Collectors.toSet());
            if (first) {
                first = false;
            } else {
                builder.append(",\n\n");
            }
            if (cg != null) {
                cg.comment.forEach(line -> builder.append("// ").append(line.replace('\n', ' ')).append("\n"));
            }
            builder.append("\"").append(quote(group)).append("\": {\n\n");
            builder.append(this.applyIndent(this.writeObject(subGroups.get(group), subGroupInstances, pathStrip + 1)));
            builder.append("\n}");
        }
        return builder.toString();
    }
    
    private String specialString(JsonElement json) {
        if (json.isJsonObject() && json.getAsJsonObject().size() == 0) {
            return "{}";
        }
        if (json.isJsonArray() && json.getAsJsonArray().size() == 0) {
            return "[]";
        }
        if (json.isJsonArray() && json.getAsJsonArray().size() <= 5) {
            //noinspection UnstableApiUsage
            List<JsonElement> list = Streams.stream(json.getAsJsonArray()).collect(Collectors.toList());
            if (list.stream().allMatch(this::isSimple)) {
                return "[ " + list.stream().map(ConfigImpl.GSON::toJson).collect(Collectors.joining(", ")) + " ]";
            }
        }
        if (json.isJsonObject()) {
            String content = json.getAsJsonObject().entrySet().stream()
                    .map(e -> ConfigImpl.GSON.toJson(new JsonPrimitive(e.getKey())) + ": " + this.specialString(e.getValue()))
                    .collect(Collectors.joining(",\n")).trim();
            return "{\n" + this.applyIndent(content) + "\n}";
        }
        if (json.isJsonArray()) {
            //noinspection UnstableApiUsage
            String content = Streams.stream(json.getAsJsonArray())
                    .map(this::specialString)
                    .collect(Collectors.joining(",\n")).trim();
            return "[\n" + this.applyIndent(content) + "\n]";
        }
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber()) {
            Number number = json.getAsJsonPrimitive().getAsNumber();
            if (number instanceof Float f) {
                if (Math.abs(f) >= 1e-5 && Math.abs(f) < 1e9) {
                    // We can't use BigDecimal.valueOf here as the float would be cast to
                    // a double which might change the value.
                    // e.g. 5.0e-5f gets 4.999999873689376e-5
                    return new BigDecimal(Float.toString(f)).stripTrailingZeros().toPlainString();
                }
            } else if (number instanceof Double d) {
                if (Math.abs(d) >= 1e-5 && Math.abs(d) < 1e9) {
                    return BigDecimal.valueOf(d).stripTrailingZeros().toPlainString();
                }
            } 
        }
        return ConfigImpl.GSON.toJson(json);
    }
    
    private boolean isSimple(JsonElement json) {
        if (json.isJsonNull()) {
            return true;
        } else if (json.isJsonPrimitive()) {
            return !json.getAsJsonPrimitive().isString() || json.getAsJsonPrimitive().getAsString().length() <= 10;
        } else {
            return false;
        }
    }
    
    private String applyIndent(String str) {
        return "  " + str.replace("\n", "\n" + "  ");
    }

    private static String quote(String str) {
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\t", "\\\t")
                .replace("\b", "\\\b")
                .replace("\n", "\\\n")
                .replace("\r", "\\\r")
                .replace("\f", "\\\f");
    }
}
