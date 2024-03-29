package org.moddingx.libx.impl.config.wrapper;

import com.google.gson.JsonElement;
import net.minecraft.network.FriendlyByteBuf;
import org.moddingx.libx.config.mapper.ValueMapper;

public class TypesafeMapper extends JsonTypesafeMapper<Object> {
    
    private TypesafeMapper(ValueMapper<?, ?> mapper) {
        //noinspection unchecked
        super((ValueMapper<Object, ?>) mapper);
    }
    
    public static TypesafeMapper of(ValueMapper<?, ?> mapper) {
        if (mapper instanceof TypesafeMapper tm) {
            return tm;
        } else {
            return new TypesafeMapper(mapper);
        }
    }

    @Override
    public JsonElement toJson(Object value) {
        if (this.wrapped.type().isAssignableFrom(value.getClass())) {
            return this.wrapped.toJson(value);
        } else {
            throw new IllegalArgumentException("Type mismatch in config mapper toJSON: Expected " + this.wrapped.type() + ", got " + value.getClass());
        }
    }

    @Override
    public void toNetwork(Object value, FriendlyByteBuf buffer) {
        if (this.wrapped.type().isAssignableFrom(value.getClass())) {
            this.wrapped.toNetwork(value, buffer);
        } else {
            throw new IllegalArgumentException("Type mismatch in config mapper write: Expected " + this.wrapped.type() + ", got " + value.getClass());
        }
    }
}
