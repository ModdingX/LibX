package org.moddingx.libx.coremods;

import net.neoforged.neoforgespi.transformation.ClassProcessorProvider;
import org.moddingx.libx.coremods.transformers.*;

public class LibXCoreMod implements ClassProcessorProvider {

    @Override
    public void createProcessors(Context context, Collector collector) {
        collector.add(new HolderSerialize());
        collector.add(new Interact());
        collector.add(new LevelLoad());
        collector.add(new RegisterClassIds());
        collector.add(new RegistryLoad());
        collector.add(new TagLoad());
    }
}
