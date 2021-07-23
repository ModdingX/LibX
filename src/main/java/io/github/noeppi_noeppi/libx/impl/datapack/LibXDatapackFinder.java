package io.github.noeppi_noeppi.libx.impl.datapack;

import io.github.noeppi_noeppi.libx.LibX;
import io.github.noeppi_noeppi.libx.datapack.DynamicDatapacks;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class LibXDatapackFinder implements RepositorySource {

    public static final LibXDatapackFinder INSTANCE = new LibXDatapackFinder();
    
    
    private LibXDatapackFinder() {
        
    }
    
    @Override
    public void loadPacks(@Nonnull Consumer<Pack> infoConsumer, @Nonnull Pack.PackConstructor infoFactory) {
        for (ResourceLocation pack : DynamicDatapacks.getEnabledPacks()) {
            String name = LibXDatapack.PREFIX + "/" + pack.getNamespace() + ":" + pack.getPath();
            ModFileInfo fileInfo = ModList.get().getModFileById(pack.getNamespace());
            if (fileInfo == null || fileInfo.getFile() == null) {
                LibX.logger.warn("Can't create dynamic datapack " + pack + ": Invalid mod file: " + fileInfo);
            } else {
                Pack info = Pack.create(name, false,
                        () -> new LibXDatapack(fileInfo.getFile(), pack.getPath()), infoFactory,
                        Pack.Position.BOTTOM, PackSource.DEFAULT);
                if (info != null) {
                    infoConsumer.accept(info);
                }
            }
        }
    }
}
