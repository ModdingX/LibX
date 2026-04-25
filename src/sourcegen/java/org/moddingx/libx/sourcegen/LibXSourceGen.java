package org.moddingx.libx.sourcegen;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@Mod("libx")
public class LibXSourceGen {
    
    public LibXSourceGen(IEventBus modBus) {
        modBus.addListener(GatherDataEvent.Client.class, this::gatherClientData);
        modBus.addListener(GatherDataEvent.Server.class, this::gatherServerData);
    }

    private void gatherClientData(GatherDataEvent.Client event) {
        this.gatherData(event);
    }

    private void gatherServerData(GatherDataEvent.Server event) {
        this.gatherData(event);
    }

    private void gatherData(GatherDataEvent event) {
        RegistryKeyProvider.create(event, StructureTemplatePool.class, Registries.TEMPLATE_POOL, "TemplatePools");
    }
}
