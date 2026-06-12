package org.moddingx.libx.impl.datagen.resource;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.moddingx.libx.datagen.PackTarget;
import org.moddingx.libx.util.lazy.LazyValue;

import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ResourceLocator {

    private static final LazyValue<ResourceLocator> CLIENT_RESOURCES = new LazyValue<>(() -> new Root(PackType.CLIENT_RESOURCES));
    private static final LazyValue<ResourceLocator> SERVER_DATA = new LazyValue<>(() -> new Root(PackType.SERVER_DATA));
    
    public static ResourceLocator root(PackType type) {
        return switch (type) {
            case CLIENT_RESOURCES -> CLIENT_RESOURCES.get();
            case SERVER_DATA -> SERVER_DATA.get();
        };
    }
    
    private final List<Path> paths;
    @Nullable private final String prefix;
    private final List<ResourceLocator> parents;

    public ResourceLocator(List<Path> paths, @Nullable String prefix, List<ResourceLocator> parents) {
        this.paths = List.copyOf(paths);
        if (prefix != null) while (prefix.endsWith("/")) prefix = prefix.substring(1);
        this.prefix = prefix;
        this.parents = List.copyOf(parents);
    }
    
    @Nullable
    public PackTarget.Resource getResource(Map<PackType, ResourceManager> resourceManagerMap, Identifier res) {
        for (Path basePath : this.paths) {
            Path path = basePath.resolve(res.getNamespace()).resolve(res.getPath());
            if (Files.isRegularFile(path)) {
                return new PathResource(path);
            }
        }
        if (this.prefix != null) {
            Identifier resolved = Identifier.fromNamespaceAndPath(res.getNamespace(), this.prefix + "/" + res.getPath());
            for (ResourceLocator parent : this.parents) {
                PackTarget.Resource resource = parent.getResource(resourceManagerMap, resolved);
                if (resource != null) return resource;
            }
        }
        for (ResourceLocator parent : this.parents) {
            PackTarget.Resource resource = parent.getResource(resourceManagerMap, res);
            if (resource != null) return resource;
        }
        return null;
    }
    
    private static class Root extends ResourceLocator {

        private final PackType type;

        public Root(PackType type) {
            super(List.of(), null, List.of());
            this.type = type;
        }

        @Nullable
        @Override
        public PackTarget.Resource getResource(Map<PackType, ResourceManager> resourceManagerMap, Identifier res) {
            Optional<Resource> resource = resourceManagerMap.get(this.type).getResource(res);

            return resource.map(VanillaResource::new).orElse(null);
        }
    }
}
