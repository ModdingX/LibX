package io.github.noeppi_noeppi.libx.impl;

import io.github.noeppi_noeppi.libx.LibX;
import io.github.noeppi_noeppi.libx.impl.tags.InternalTagProvider;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

public class InternalDataGen {
    
    public static void gatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(new InternalTagProvider(LibX.getInstance(), event.getGenerator(), event.getExistingFileHelper()));
    }
}
