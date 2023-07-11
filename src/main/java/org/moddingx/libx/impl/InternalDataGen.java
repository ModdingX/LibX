package org.moddingx.libx.impl;

import org.moddingx.libx.LibX;
import org.moddingx.libx.datagen.DatagenSystem;
import org.moddingx.libx.impl.datagen.FontLoadTest;
import org.moddingx.libx.impl.tags.InternalTagProvider;

public class InternalDataGen {
    
    public static void init() {
        DatagenSystem.create(LibX.getInstance(), system -> {
            system.addDataProvider(InternalTagProvider::new);
            system.addDataProvider(FontLoadTest::new);
        });
    }
}
