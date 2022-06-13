package org.moddingx.libx.impl.datapack;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModFileInfo;
import org.moddingx.libx.LibX;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class DynamicDatapackLocator implements RepositorySource {
    
    private static final DynamicDatapackLocator INSTANCE = new DynamicDatapackLocator();
    private static final Set<ResourceLocation> enabledPacks = new HashSet<>();
    
    private DynamicDatapackLocator() {
        
    }

    public static void locatePacks(AddPackFindersEvent event) {
        event.addRepositorySource(INSTANCE);
    }
    
    public static synchronized void enablePack(ResourceLocation id) {
        enabledPacks.add(id);
    }
    
    public static synchronized boolean isEnabled(ResourceLocation id) {
        return enabledPacks.contains(id);
    }
    
    @Override
    public void loadPacks(@Nonnull Consumer<Pack> packs, @Nonnull Pack.PackConstructor factory) {
        for (ResourceLocation id : enabledPacks) {
            String name = LibXDatapack.PREFIX + "/" + id.getNamespace() + ":" + id.getPath();
            IModFileInfo fileInfo = ModList.get().getModFileById(id.getNamespace());
            if (fileInfo == null || fileInfo.getFile() == null) {
                LibX.logger.warn("Can't create dynamic datapack " + id + ": Invalid mod file: " + fileInfo);
            } else {
                Pack pack = Pack.create(name, false,
                        () -> new LibXDatapack(fileInfo.getFile(), id.getPath()), factory,
                        Pack.Position.BOTTOM, PackSource.DEFAULT
                );
                if (pack != null) {
                    packs.accept(pack);
                }
            }
        }
    }
}
