package org.moddingx.libx.coremods;

import cpw.mods.modlauncher.api.ITransformer;
import net.neoforged.neoforgespi.coremod.ICoreMod;
import org.moddingx.libx.coremods.transformers.HolderSerialize;
import org.moddingx.libx.coremods.transformers.Interact;
import org.moddingx.libx.coremods.transformers.LevelLoad;
import org.moddingx.libx.coremods.transformers.RegistryLoad;

import java.util.List;

public class LibXCoreMod implements ICoreMod {

    @Override
    public Iterable<? extends ITransformer<?>> getTransformers() {
        return List.of(
                new HolderSerialize(),
                new Interact(),
                new LevelLoad(),
                new RegistryLoad()
        );
    }
}
