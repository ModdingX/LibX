package io.github.noeppi_noeppi.libx.impl.registration;

import io.github.noeppi_noeppi.libx.mod.registration.RegistryTransformer;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.DataSerializerEntry;

import javax.annotation.Nullable;

public class BuiltinTransformers {
    
    public static final RegistryTransformer DATA_SERIALIZER = new RegistryTransformer() {
        
        @Nullable
        @Override
        public Object getAdditional(ResourceLocation id, Object object) {
            if (object instanceof IDataSerializer<?>) {
                return new DataSerializerEntry((IDataSerializer<?>) object);
            } else {
                return null;
            }
        }
    };
}
