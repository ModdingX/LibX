package io.github.noeppi_noeppi.libx.impl.datapack;

import io.github.noeppi_noeppi.libx.LibX;
import io.github.noeppi_noeppi.libx.datapack.DynamicDatapacks;
import net.minecraft.resources.IPackFinder;
import net.minecraft.resources.IPackNameDecorator;
import net.minecraft.resources.ResourcePackInfo;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class LibXDatapackFinder implements IPackFinder {

    public static final LibXDatapackFinder INSTANCE = new LibXDatapackFinder();
    
    
    private LibXDatapackFinder() {
        
    }
    
    @Override
    public void findPacks(@Nonnull Consumer<ResourcePackInfo> consumer, @Nonnull ResourcePackInfo.IFactory factory) {
        for (ResourceLocation pack : DynamicDatapacks.getEnabledPacks()) {
            String name = LibXDatapack.PREFIX + "/" + pack.getNamespace() + ":" + pack.getPath();
            ModFileInfo fileInfo = ModList.get().getModFileById(pack.getNamespace());
            if (fileInfo == null || fileInfo.getFile() == null) {
                LibX.logger.warn("Can't create dynamic datapack " + pack + ": Invalid mod file: " + fileInfo);
            } else {
                ResourcePackInfo info = ResourcePackInfo.createResourcePack(name, false,
                        () -> new LibXDatapack(fileInfo.getFile(), pack.getPath()), factory,
                        ResourcePackInfo.Priority.BOTTOM, IPackNameDecorator.PLAIN);
                if (info != null) {
                    consumer.accept(info);
                }
            }
        }
    }
}
