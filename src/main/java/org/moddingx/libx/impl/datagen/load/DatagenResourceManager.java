package org.moddingx.libx.impl.datagen.load;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class DatagenResourceManager implements ResourceManager {

    private final ResourceManager parent;

    private DatagenResourceManager(ResourceManager parent) {
        this.parent = parent;
    }

    public static ResourceManager markRegistryBootstrap(ResourceManager parent) {
        return parent instanceof DatagenResourceManager ? parent : new DatagenResourceManager(parent);
    }

    public static boolean isRegistryBootstrap(ResourceManager manager) {
        return manager instanceof DatagenResourceManager;
    }

    @Nonnull
    @Override
    public Set<String> getNamespaces() {
        return this.parent.getNamespaces();
    }

    @Nonnull
    @Override
    public Optional<Resource> getResource(@Nonnull Identifier location) {
        return this.parent.getResource(location);
    }

    @Nonnull
    @Override
    public List<Resource> getResourceStack(@Nonnull Identifier location) {
        return this.parent.getResourceStack(location);
    }

    @Nonnull
    @Override
    public Map<Identifier, Resource> listResources(@Nonnull String directory, @Nonnull Predicate<Identifier> filter) {
        return this.parent.listResources(directory, filter);
    }

    @Nonnull
    @Override
    public Map<Identifier, List<Resource>> listResourceStacks(@Nonnull String directory, @Nonnull Predicate<Identifier> filter) {
        return this.parent.listResourceStacks(directory, filter);
    }

    @Nonnull
    @Override
    public Stream<PackResources> listPacks() {
        return this.parent.listPacks();
    }
}
