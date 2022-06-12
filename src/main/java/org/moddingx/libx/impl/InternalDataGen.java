package org.moddingx.libx.impl;

import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.moddingx.libx.LibX;
import org.moddingx.libx.impl.tags.InternalTagProvider;

public class InternalDataGen {
    
    public static void gatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(new InternalTagProvider(LibX.getInstance(), event.getGenerator(), event.getExistingFileHelper()));
    }
}