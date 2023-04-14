package org.moddingx.libx.impl.datapack;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.forgespi.language.IModFileInfo;
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
    private final Set<ResourceLocation> enabledPacks = new HashSet<>();
    
    private DynamicPackLocator(PackType type) {
        this.type = type;
    }

    public static void locatePacks(AddPackFindersEvent event) {
        if (PackType.CLIENT_RESOURCES.equals(event.getPackType())) event.addRepositorySource(RESOURCE_PACKS);
        if (PackType.SERVER_DATA.equals(event.getPackType())) event.addRepositorySource(DATA_PACKS);
    }
    
    public synchronized void enablePack(ResourceLocation id) {
        if (!Objects.equals(id.getNamespace(), ModLoadingContext.get().getActiveNamespace())) {
            LibX.logger.error("Wrong modid for dynamic pack, expected " + ModLoadingContext.get().getActiveNamespace() + " got " + id.getNamespace());
        }
        this.enabledPacks.add(id);
    }
    
    public synchronized boolean isEnabled(ResourceLocation id) {
        return this.enabledPacks.contains(id);
    }
    
    @Override
    public void loadPacks(@Nonnull Consumer<Pack> packs) {
        for (ResourceLocation id : this.enabledPacks) {
            String packId = LibXPack.PACK_CONFIG.get(this.type).prefix() + "/" + id.getNamespace() + ":" + id.getPath();
            IModFileInfo fileInfo = ModList.get().getModFileById(id.getNamespace());
            if (fileInfo == null || fileInfo.getFile() == null) {
                LibX.logger.warn("Can't create dynamic pack " + id + ": Invalid mod file: " + fileInfo);
            } else {
                LazyValue<LibXPack> resources = new LazyValue<>(() -> new LibXPack(fileInfo.getFile(), this.type, id.getPath()));
                Pack pack = Pack.readMetaAndCreate(packId, Component.literal(packId), false,
                        anotherId -> resources.get(), this.type, Pack.Position.BOTTOM,
                        LibXPack.PACK_CONFIG.get(this.type).source()
                );
                if (pack != null) {
                    packs.accept(pack);
                }
            }
        }
    }
}
