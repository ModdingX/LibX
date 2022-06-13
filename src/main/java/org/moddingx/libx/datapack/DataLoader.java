package org.moddingx.libx.datapack;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Utilities to load data from a {@link ResourceManager}
 */
public class DataLoader {

    private static final Gson GSON = Util.make(() -> {
        GsonBuilder builder = new GsonBuilder();
        builder.disableHtmlEscaping();
        builder.setLenient();
        builder.setPrettyPrinting();
        return builder.create();
    });

    /**
     * Loads json data from a base path. For example if the base path is {@code a/b} and there are the json files
     * {@code a/b/c.json} and {@code a/b/d/e.json}, the resulting ids will be {@code modid:c} and {@code modid:d/e}.
     * 
     * @param factory A factory to create the resulting objects.
     */
    public static <T> Map<ResourceLocation, T> loadJson(ResourceManager rm, String basePath, ResourceFactory<JsonElement, T> factory) throws IOException {
        return collectJson(locate(rm, basePath, "json", true), factory);
    }

    /**
     * Collects data from the given {@link Resource resources} by a given factory.
     */
    public static <T> Map<ResourceLocation, T> collect(List<ResourceEntry> resources, ResourceFactory<Resource, T> factory) throws IOException {
        // Don't use ImmutableMap.Builder because it would fail on duplicate keys
        Map<ResourceLocation, T> map = new HashMap<>();
        for (ResourceEntry entry : resources) {
            map.put(entry.id(), factory.apply(entry.id(), entry.resource()));
        }
        return ImmutableMap.copyOf(map);
    }
    
    /**
     * Collects data from the given {@link Resource resources} by a given factory. The contents of the
     * resource are mapped to a {@link String} first.
     */
    public static <T> Map<ResourceLocation, T> collectText(List<ResourceEntry> resources, ResourceFactory<String, T> factory) throws IOException {
        return collect(resources, (id, resource) -> {
            try (Reader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                return factory.apply(id, IOUtils.toString(reader));
            }
        });
    }

    /**
     * Collects data from the given {@link Resource resources} by a given factory. The contents of the
     * resource are mapped to a {@link JsonElement} first.
     */
    public static <T> Map<ResourceLocation, T> collectJson(List<ResourceEntry> resources, ResourceFactory<JsonElement, T> factory) throws IOException {
        return collect(resources, (id, resource) -> {
            try (Reader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                return factory.apply(id, GSON.fromJson(reader, JsonElement.class));
            }
        });
    }
    
    /**
     * Joins data from the given {@link Resource resources} by a given factory into a stream.
     */
    public static <T> Stream<T> join(List<ResourceEntry> resources, ResourceFactory<Resource, T> factory) throws IOException {
        Stream.Builder<T> stream = Stream.builder();
        for (ResourceEntry entry : resources) {
            stream.add(factory.apply(entry.id(), entry.resource()));
        }
        return stream.build();
    }

    /**
     * Joins data from the given {@link Resource resources} by a given factory into a stream. The contents of the
     * resource are mapped to a {@link String} first.
     */
    public static <T> Stream<T> joinText(List<ResourceEntry> resources, ResourceFactory<String, T> factory) throws IOException {
        return join(resources, (id, resource) -> {
            try (Reader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                return factory.apply(id, IOUtils.toString(reader));
            }
        });
    }

    /**
     * Joins data from the given {@link Resource resources} by a given factory into a stream. The contents of the
     * resource are mapped to a {@link JsonElement} first.
     */
    public static <T> Stream<T> joinJson(List<ResourceEntry> resources, ResourceFactory<JsonElement, T> factory) throws IOException {
        return join(resources, (id, resource) -> {
            try (Reader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                return factory.apply(id, GSON.fromJson(reader, JsonElement.class));
            }
        });
    }

    /**
     * Locates resources in different namespaces.
     * 
     * @param rm The resource manager to use.
     * @param fullPath The full path of the resource
     * @param idPath The string that will be used as path name for the id in the resulting resource.
     */
    public static List<ResourceEntry> locate(ResourceManager rm, String fullPath, String idPath) {
        Set<String> namespaces = rm.getNamespaces();
        ImmutableList.Builder<ResourceEntry> list = ImmutableList.builder();
        for (String namespace : namespaces) {
            ResourceLocation location = new ResourceLocation(namespace, fullPath);
            rm.getResource(location).ifPresent(res -> list.add(new ResourceEntry(new ResourceLocation(namespace, idPath), res)));
        }
        return list.build();
    }

    /**
     * Locates resources in a specific folder.
     * 
     * @param rm The resource manager to use.
     * @param basePath The base path to scan.
     * @param suffix The suffix of the files to load. A file must wnd in a point followed by this
     *               if {@code suffix} is non-null.
     * @param recursive Whether to scan sub-directories as well.
     * @return A list of resources. Their ids will have {@code basePath} and {@code suffix} stripped.
     */
    public static List<ResourceEntry> locate(ResourceManager rm, String basePath, @Nullable String suffix, boolean recursive) {
        Map<ResourceLocation, Resource> resources = rm.listResources(basePath, file -> suffix == null || file.getPath().endsWith("." + suffix));
        ImmutableList.Builder<ResourceEntry> list = ImmutableList.builder();
        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation id = entry.getKey();
            if (!id.getPath().startsWith(basePath + "/")) continue;
            if (suffix != null && !id.getPath().endsWith("." + suffix)) continue;
            String realPath = id.getPath().substring(basePath.length() + 1, id.getPath().length() - (suffix == null ? 0 : (suffix.length() + 1)));
            if (realPath.isEmpty() || (!recursive && realPath.contains("/"))) continue;
            list.add(new ResourceEntry(new ResourceLocation(id.getNamespace(), realPath), entry.getValue()));
        }
        return list.build();
    }

    /**
     * A factory to map a resource to something else.
     */
    public interface ResourceFactory<T, R> {
        
        R apply(ResourceLocation id, T value) throws IOException;
    }
}
