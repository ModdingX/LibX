package org.moddingx.libx.impl.datagen.resource;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.moddingx.libx.datagen.PackTarget;
import org.moddingx.libx.util.lazy.LazyValue;

import javax.annotation.Nullable;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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
    public PackTarget.Resource getResource(ExistingFileHelper fileHelper, ResourceLocation res) {
        for (Path basePath : this.paths) {
            Path path = basePath.resolve(res.getNamespace()).resolve(res.getPath());
            if (Files.isRegularFile(path)) {
                return new PathResource(path);
            }
        }
        if (this.prefix != null) {
            ResourceLocation resolved = new ResourceLocation(res.getNamespace(), this.prefix + "/" + res.getPath());
            for (ResourceLocator parent : this.parents) {
                PackTarget.Resource resource = parent.getResource(fileHelper, resolved);
                if (resource != null) return resource;
            }
        }
        for (ResourceLocator parent : this.parents) {
            PackTarget.Resource resource = parent.getResource(fileHelper, res);
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
        public PackTarget.Resource getResource(ExistingFileHelper fileHelper, ResourceLocation res) {
            if (!fileHelper.exists(res, this.type)) return null;
            try {
                return new VanillaResource(fileHelper.getResource(res, this.type));
            } catch (FileNotFoundException e) {
                return null;
            }
        }
    }
}
