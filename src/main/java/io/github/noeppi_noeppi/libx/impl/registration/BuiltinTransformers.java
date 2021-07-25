package io.github.noeppi_noeppi.libx.impl.registration;

import io.github.noeppi_noeppi.libx.mod.registration.RegistryTransformer;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DataSerializerEntry;

import javax.annotation.Nullable;

public class BuiltinTransformers {
    
    public static final RegistryTransformer DATA_SERIALIZER = new RegistryTransformer() {
        
        @Nullable
        @Override
        public Object getAdditional(ResourceLocation id, Object object) {
            if (object instanceof EntityDataSerializer<?> serializer) {
                return new DataSerializerEntry(serializer);
            } else {
                return null;
            }
        }
    };
}
