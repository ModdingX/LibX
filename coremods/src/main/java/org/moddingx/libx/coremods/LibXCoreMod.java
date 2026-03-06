package org.moddingx.libx.coremods;

import cpw.mods.modlauncher.api.ITransformer;
import net.neoforged.neoforgespi.coremod.ICoreMod;
import org.moddingx.libx.coremods.transformers.*;

import java.util.List;

public class LibXCoreMod implements ICoreMod {

    @Override
    public Iterable<? extends ITransformer<?>> getTransformers() {
        return List.of(
                new HolderSerialize(),
                new Interact(),
                new LevelLoad(),
                new RegisterClassIds(),
                new RegistryLoad()
        );
    }
}
