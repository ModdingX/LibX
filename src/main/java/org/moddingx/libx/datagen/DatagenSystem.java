package org.moddingx.libx.datagen;

import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.Nullable;
import org.moddingx.libx.LibX;
import org.moddingx.libx.impl.ModInternal;
import org.moddingx.libx.impl.datagen.InternalDataProvider;
import org.moddingx.libx.impl.datagen.load.DatagenRegistryLoader;
import org.moddingx.libx.impl.datagen.registries.DatagenRegistrySet;
import org.moddingx.libx.impl.datapack.LibXPack;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.sandbox.SandBox;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class DatagenSystem {
    
    private static final Set<ResourceKey<? extends Registry<?>>> EXTENSION_REGISTRIES = new HashSet<>();

    /**
     * Marks a registry as an extension registry. An extension registry is a registry, where the id of the elements
     * reference the id inside another registry (for example LibX {@link SandBox#BIOME_SURFACE biome surface}).
     * 
     * Extension registries are frozen later during datagen to allow them access to frozen non-extension registries
     * while being populated.
     * 
     * @see DatagenStage
     */
    public static void registerExtensionRegistry(ResourceKey<? extends Registry<?>> registryKey) {
        String activeMod = ModLoadingContext.get().getActiveNamespace();
        if (!Objects.equals(LibX.getInstance().modid, activeMod) && !Objects.equals(registryKey.location().getNamespace(), activeMod)) {
            // LibX is the exception: It has to make vanilla and forge registries extension registries
            LibX.logger.warn("Registry " + registryKey.location() + " marked as extension registry by foreign mod: " + activeMod);
        }
        EXTENSION_REGISTRIES.add(registryKey);
    }

    /**
     * Gets a set of all extension.registries.
     * 
     * @see #registerExtensionRegistry(ResourceKey)
     */
    public static Set<ResourceKey<? extends Registry<?>>> extensionRegistries() {
        return Collections.unmodifiableSet(EXTENSION_REGISTRIES);
    }

    /**
     * Registers a datagen system for the given mod and executes the provided configuration action when running
     * datagen. Calling this method multiple times for the same mod will only create a single datagen system but
     * invoke all configuration in order.
     */
    public static void create(ModX mod, Consumer<DatagenSystem> configure) {
        if (ModInternal.get(mod).addDatagenConfiguration(configure)) {
            ModInternal.get(mod).modEventBus().addListener(EventPriority.NORMAL, false, GatherDataEvent.class, event -> {
                DatagenSystem system = new DatagenSystem(mod, event);
                ModInternal.get(mod).configureDatagenSystem(system);
                system.hookIntoGenerator();
            });
        }
    }
    
    private final ModX mod;
    private final DataGenerator generator;
    private final ExistingFileHelper fileHelper;
    private final DatagenRegistrySet rootRegistries;
    private final PackTarget mainTarget;
    
    @Nullable
    private Path resourceRoot;
    private boolean locked;

    private final List<InternalDataProvider.Entry<RegistryProvider>> registryProviders;
    private final List<InternalDataProvider.Entry<RegistryProvider>> extensionProviders;
    private final List<InternalDataProvider.Entry<DataProvider>> dataProviders;
    
    private DatagenSystem(ModX mod, GatherDataEvent event) {
        this.mod = mod;
        this.generator = event.getGenerator();
        this.fileHelper = event.getExistingFileHelper();
        this.rootRegistries = new DatagenRegistrySet(DatagenRegistryLoader.loadRegistries(this.fileHelper));
        this.mainTarget = new PackTarget("main", this, new DatagenRegistrySet(List.of(this.rootRegistries)), Map.of(
                PackType.CLIENT_RESOURCES, this.generator.getPackOutput().getOutputFolder(PackOutput.Target.RESOURCE_PACK),
                PackType.SERVER_DATA, this.generator.getPackOutput().getOutputFolder(PackOutput.Target.DATA_PACK)
        ), null, null, null);
        
        this.resourceRoot = null;
        this.locked = false;
        
        this.registryProviders = new ArrayList<>();
        this.extensionProviders = new ArrayList<>();
        this.dataProviders = new ArrayList<>();
    }
    
    private void checkNotLocked() {
        if (this.locked) {
            throw new IllegalStateException("Datagen system has already been configured.");
        }
    }
    
    public ModX mod() {
        return this.mod;
    }
    
    public ExistingFileHelper fileHelper() {
        return this.fileHelper;
    }

    /**
     * The main pack target.
     */
    public PackTarget mainTarget() {
        return this.mainTarget;
    }

    /**
     * The main output path. Use {@link PackTarget#path(PackType)} instead of this whenever possible.
     */
    public Path mainOutput() {
        return this.generator.getPackOutput().getOutputFolder();
    }

    /**
     * Sets the resource root relative to the {@link FMLPaths#GAMEDIR game dir}. This is used to allow resource lookup
     * in {@link #dynamic(String, PackType, PackTarget...) dynamic} pack targets.
     */
    public void setResourceRoot(String root) {
        this.checkNotLocked();
        this.resourceRoot = FMLPaths.GAMEDIR.get().resolve(root);
    }
    
    /**
     * Creates a pack target for a vanilla-style nested datapack inside the first given parent.
     * Client resources can't be output on this target.
     */
    public PackTarget nestedDatapack(ResourceLocation id, PackTarget... parents) {
        return this.makePackTarget(id.toString(), parents)
                .unsupported(PackType.CLIENT_RESOURCES)
                .resolveOutput(PackType.SERVER_DATA, id.getNamespace(), "datapacks", id.getPath(), "data")
                .build();
    }
    
    /**
     * Creates a pack target for a LibX dynamic pack. For resource packs, server data output defaults to the
     * first parent, for data packs, client resource output defaults to the first parent.
     */
    public PackTarget dynamic(String id, PackType type, PackTarget... parents) {
        String prefix = LibXPack.PACK_CONFIG.get(type).prefix();
        PackTargetBuilder builder =  this.makePackTarget(prefix + "[" + id + "]", parents)
                .setOutput(type, this.mainTarget.path(type).resolve(prefix).resolve(id));
        if (this.resourceRoot != null) {
            builder.resources(type, this.resourceRoot.resolve(prefix).resolve(id));
        }
        return builder.build();
    }

    /**
     * Joins two pack targets, using one for client resource output and the other one for server data output.
     */
    public PackTarget join(PackTarget resources, PackTarget data) {
        PackTargetBuilder builder = this.makePackTarget("[" + resources.name() + "|" + data.name() + "]", resources, data);
        if (resources.outputMap().containsKey(PackType.CLIENT_RESOURCES)) {
            builder.setOutput(PackType.CLIENT_RESOURCES, resources.outputMap().get(PackType.CLIENT_RESOURCES));
        } else {
            builder.unsupported(PackType.CLIENT_RESOURCES);
        }
        if (data.outputMap().containsKey(PackType.SERVER_DATA)) {
            builder.setOutput(PackType.SERVER_DATA, data.outputMap().get(PackType.SERVER_DATA));
        } else {
            builder.unsupported(PackType.SERVER_DATA);
        }
        return builder.build();
    }

    /**
     * Creates a new builder for a {@link PackTarget}. The registries for this pack target will contain all elements
     * from the parents registries. The output locations default to the output locations of the first parent.
     * If no parents are given, {@link #mainTarget()} is assumed.
     */
    public PackTargetBuilder makePackTarget(String name, PackTarget... parents) {
        this.checkNotLocked();
        PackTarget mainParent;
        List<PackTarget> allParents;
        List<DatagenRegistrySet> parentRegistries;
        if (parents.length == 0) {
            mainParent = this.mainTarget;
            allParents = List.of(this.mainTarget);
            parentRegistries = List.of((DatagenRegistrySet) this.mainTarget.registries());
        } else {
            mainParent = parents[0];
            allParents = List.of(parents);
            parentRegistries = Arrays.stream(parents).map(parent -> (DatagenRegistrySet) parent.registries()).toList();
        }
        DatagenRegistrySet registries = new DatagenRegistrySet(parentRegistries);
        return new PackTargetBuilder(name, registries, mainParent.outputMap(), allParents);
    }

    /**
     * Adds a provider to run in the {@link DatagenStage#REGISTRY_SETUP registry setup stage}.
     */
    public void addRegistryProvider(Function<DatagenContext, RegistryProvider> provider) {
        this.addRegistryProvider(this.mainTarget(), provider);
    }

    /**
     * Adds a provider to run in the {@link DatagenStage#REGISTRY_SETUP registry setup stage}.
     */
    public void addRegistryProvider(PackTarget target, Function<DatagenContext, RegistryProvider> provider) {
        this.checkNotLocked();
        this.registryProviders.add(new InternalDataProvider.Entry<>(target, provider));
    }
    
    /**
     * Adds a provider to run in the {@link DatagenStage#EXTENSION_SETUP extension setup stage}.
     */
    public void addExtensionProvider(Function<DatagenContext, RegistryProvider> provider) {
        this.addExtensionProvider(this.mainTarget(), provider);
    }
    
    /**
     * Adds a provider to run in the {@link DatagenStage#EXTENSION_SETUP extension setup stage}.
     */
    public void addExtensionProvider(PackTarget target, Function<DatagenContext, RegistryProvider> provider) {
        this.checkNotLocked();
        this.extensionProviders.add(new InternalDataProvider.Entry<>(target, provider));
    }

    /**
     * Adds a provider to run in the {@link DatagenStage#DATAGEN datagen stage}.
     */
    public void addDataProvider(Function<DatagenContext, DataProvider> provider) {
        this.addDataProvider(this.mainTarget(), provider);
    }
    
    /**
     * Adds a provider to run in the {@link DatagenStage#DATAGEN datagen stage}.
     */
    public void addDataProvider(PackTarget target, Function<DatagenContext, DataProvider> provider) {
        this.checkNotLocked();
        this.dataProviders.add(new InternalDataProvider.Entry<>(target, provider));
    }
    
    private void hookIntoGenerator() {
        this.locked = true;
        this.generator.addProvider(true, new InternalDataProvider(this, this.rootRegistries, this.registryProviders, this.extensionProviders, this.dataProviders));
    }
    
    public class PackTargetBuilder {
        
        private final String name;
        private final DatagenRegistrySet registries;
        private final Map<PackType, Path> outputMap;
        private final List<PackTarget> parents;
        private final Map<PackType, String> prefixMap;
        private final Map<PackType, List<Path>> resourcePathMap;

        private PackTargetBuilder(String name, DatagenRegistrySet registries, Map<PackType, Path> outputMap, List<PackTarget> parents) {
            this.name = name;
            this.registries = registries;
            this.outputMap = new HashMap<>(outputMap);
            this.parents = parents;
            this.prefixMap = new HashMap<>();
            this.resourcePathMap = new HashMap<>();
        }

        /**
         * Sets the output path for the given {@link PackType}.
         */
        public PackTargetBuilder setOutput(PackType type, Path path) {
            this.outputMap.put(type, path);
            this.prefixMap.remove(type);
            return this;
        }
        
        /**
         * Changes the output path for the given {@link PackType} by {@link Path#resolve(String) resolving} the given
         * sub-path to the current output path. This also adds a resource prefix for resource lookup through the
         * {@link PackTarget#find(PackType, ResourceLocation)} method.
         */
        public PackTargetBuilder resolveOutput(PackType type, String... subPath) {
            if (!this.outputMap.containsKey(type)) {
                throw new IllegalStateException("Can't resolve output path on pack target that does not support " + type);
            }
            Path path = this.outputMap.get(type);
            for (String pathPart : subPath) path = path.resolve(pathPart);
            this.outputMap.put(type, path);
            this.prefixMap.put(type, String.join("/", subPath));
            return this;
        }

        /**
         * Indicates, that the {@link PackTarget} can't output the given {@link PackType}.
         */
        public PackTargetBuilder unsupported(PackType type) {
            this.outputMap.remove(type);
            return this;
        }

        /**
         * Adds a path for resource lookup through {@link PackTarget#find(PackType, ResourceLocation)}.
         */
        public PackTargetBuilder resources(PackType type, Path path) {
            this.resourcePathMap.computeIfAbsent(type, k -> new ArrayList<>()).add(path);
            return this;
        }
        
        /**
         * Builds the resulting {@link PackTarget}.
         */
        public PackTarget build() {
            return new PackTarget(this.name, DatagenSystem.this, this.registries, this.outputMap, this.prefixMap, this.resourcePathMap, this.parents);
        }
    }
}
