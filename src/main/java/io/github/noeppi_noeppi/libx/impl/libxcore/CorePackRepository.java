package io.github.noeppi_noeppi.libx.impl.libxcore;

import io.github.noeppi_noeppi.libx.impl.datapack.LibXDatapackFinder;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;

public class CorePackRepository {

    /**
     * Patched into {@link PackRepository#PackRepository(Pack.PackConstructor, RepositorySource...)}
     * before any {@code return} passing the {@code this} reference.
     */
    public static void initRepository(PackRepository repository) {
        repository.addPackFinder(LibXDatapackFinder.INSTANCE);
    }
}
