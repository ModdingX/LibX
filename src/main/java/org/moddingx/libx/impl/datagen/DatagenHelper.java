package org.moddingx.libx.impl.datagen;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.lang.reflect.Field;

public class DatagenHelper {
    
    public static ResourceManager resources(ExistingFileHelper fileHelper, PackType packType) {
        try {
            Field field = switch (packType) {
                case CLIENT_RESOURCES -> ExistingFileHelper.class.getDeclaredField("clientResources");
                case SERVER_DATA -> ExistingFileHelper.class.getDeclaredField("serverData");
            };
            field.setAccessible(true);
            return (ResourceManager) field.get(fileHelper);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't access resource manager for packtype " + packType + " on " + fileHelper);
        }
    }
}
