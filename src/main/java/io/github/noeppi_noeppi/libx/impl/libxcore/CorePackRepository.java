package io.github.noeppi_noeppi.libx.impl.libxcore;

import io.github.noeppi_noeppi.libx.impl.datapack.LibXDatapackFinder;
import net.minecraft.resources.IPackFinder;
import net.minecraft.resources.ResourcePackInfo;
import net.minecraft.resources.ResourcePackList;

public class CorePackRepository {

    /**
     * Patched into {@link ResourcePackList#ResourcePackList(ResourcePackInfo.IFactory, IPackFinder...)}
     * before any {@code return} passing the {@code this} reference.
     */
    public static void initRepository(ResourcePackList list) {
        list.addPackFinder(LibXDatapackFinder.INSTANCE);
    }
}
