package org.moddingx.libx.impl.datapack;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.RepositorySource;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.IModInfo;
import org.moddingx.libx.LibX;
import org.moddingx.libx.util.lazy.LazyValue;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public class DynamicPackLocator implements RepositorySource {
    
    public static final DynamicPackLocator RESOURCE_PACKS = new DynamicPackLocator(PackType.CLIENT_RESOURCES);
    public static final DynamicPackLocator DATA_PACKS = new DynamicPackLocator(PackType.SERVER_DATA);
    
    private final PackType type;
    private final Set<Identifier> enabledPacks = new HashSet<>();
    
    private DynamicPackLocator(PackType type) {
        this.type = type;
    }

    public static void locatePacks(AddPackFindersEvent event) {
        if (PackType.CLIENT_RESOURCES.equals(event.getPackType())) event.addRepositorySource(RESOURCE_PACKS);
        if (PackType.SERVER_DATA.equals(event.getPackType())) event.addRepositorySource(DATA_PACKS);
    }
    
    public synchronized void enablePack(Identifier id) {
        if (!Objects.equals(id.getNamespace(), ModLoadingContext.get().getActiveNamespace())) {
            LibX.logger.error("Wrong modid for dynamic pack, expected {} got {}", ModLoadingContext.get().getActiveNamespace(), id.getNamespace());
        }
        this.enabledPacks.add(id);
    }
    
    public synchronized boolean isEnabled(Identifier id) {
        return this.enabledPacks.contains(id);
    }
    
    @Override
    public void loadPacks(@Nonnull Consumer<Pack> packs) {
        for (Identifier id : this.enabledPacks) {
            IModInfo modInfo = ModList.get().getModContainerById(id.getNamespace()).map(ModContainer::getModInfo).orElse(null);
            IModFileInfo modFileInfo = modInfo == null ? null : modInfo.getOwningFile();
            if (modInfo == null || modFileInfo == null || modFileInfo.getFile() == null) {
                LibX.logger.error("Can't create dynamic pack {}: Invalid mod file: {} ({})", id, id.getNamespace(), modFileInfo);
            } else {
                PackLocationInfo location = LibXPack.generateLocationInfo(modInfo, this.type, id.getPath());
                LazyValue<LibXPack> resources = new LazyValue<>(() -> new LibXPack(location, this.type, modInfo, modFileInfo.getFile(), id.getPath()));
                Pack pack = Pack.readMetaAndCreate(location, new SimpleResourceSupplier(resources), this.type, LibXPack.PACK_CONFIG.get(this.type).selection());
                if (pack != null) {
                    packs.accept(pack);
                }
            }
        }
    }
    
    private record SimpleResourceSupplier(LazyValue<? extends PackResources> resources) implements Pack.ResourcesSupplier {
        
        @Nonnull
        @Override
        public PackResources openPrimary(@Nonnull PackLocationInfo location) {
            return this.resources().get();
        }

        @Nonnull
        @Override
        public PackResources openFull(@Nonnull PackLocationInfo location, @Nonnull Pack.Metadata metadata) {
            return this.resources().get();
        }
    }
}
