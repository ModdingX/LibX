package org.moddingx.libx.impl.datapack;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModFileInfo;
import org.moddingx.libx.LibX;
import org.moddingx.libx.datapack.DynamicDatapacks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class DynamicDatapackLocator implements RepositorySource {
    
    private static final DynamicDatapackLocator INSTANCE = new DynamicDatapackLocator();
    private static final Map<ResourceLocation, DynamicDatapacks.PackFactory> enabledPacks = new HashMap<>();
    
    private DynamicDatapackLocator() {
        
    }

    public static void locatePacks(AddPackFindersEvent event) {
        event.addRepositorySource(INSTANCE);
    }
    
    public static synchronized void enablePack(ResourceLocation id, @Nullable DynamicDatapacks.PackFactory pack) {
        enabledPacks.put(id, pack == null ? LibXDatapack::new : pack);
    }
    
    public static synchronized boolean isEnabled(ResourceLocation id) {
        return enabledPacks.containsKey(id);
    }
    
    @Override
    public void loadPacks(@Nonnull Consumer<Pack> packs, @Nonnull Pack.PackConstructor factory) {
        for (Map.Entry<ResourceLocation, DynamicDatapacks.PackFactory> entry : enabledPacks.entrySet()) {
            ResourceLocation id = entry.getKey();
            DynamicDatapacks.PackFactory packFactory = entry.getValue();
            String name = LibXDatapack.PREFIX + "/" + id.getNamespace() + ":" + id.getPath();
            IModFileInfo fileInfo = ModList.get().getModFileById(id.getNamespace());
            if (fileInfo == null || fileInfo.getFile() == null) {
                LibX.logger.warn("Can't create dynamic datapack " + id + ": Invalid mod file: " + fileInfo);
            } else {
                Pack pack = Pack.create(name, false,
                        () -> packFactory.create(fileInfo.getFile(), id.getPath()), factory,
                        Pack.Position.BOTTOM, PackSource.DEFAULT
                );
                if (pack != null) {
                    packs.accept(pack);
                }
            }
        }
    }
}
