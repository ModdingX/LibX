package org.moddingx.libx.sourcegen;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("libx_sourcegen")
public class LibXSourceGen {
    
    public LibXSourceGen() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::gatherData);
    }
    
    private void gatherData(GatherDataEvent event) {
        RegistryKeyProvider.create(event, StructureTemplatePool.class, Registries.TEMPLATE_POOL, "TemplatePools");
    }
}
