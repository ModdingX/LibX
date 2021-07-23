package io.github.noeppi_noeppi.libx.impl.libxcore;

import io.github.noeppi_noeppi.libx.impl.datapack.LibXDatapackFinder;
import net.minecraft.server.packs.repository.PackRepository;

public class CorePackRepository {

    /**
     * Patched into {@link ResourcePackList#ResourcePackList(ResourcePackInfo.IFactory, IPackFinder...)}
     * before any {@code return} passing the {@code this} reference.
     */
    public static void initRepository(PackRepository repository) {
        repository.addPackFinder(LibXDatapackFinder.INSTANCE);
    }
}
