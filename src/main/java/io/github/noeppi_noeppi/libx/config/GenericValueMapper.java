package io.github.noeppi_noeppi.libx.config;

import com.google.gson.JsonElement;
import io.github.noeppi_noeppi.libx.impl.config.ConfigImpl;
import net.minecraft.network.PacketBuffer;

public interface GenericValueMapper<T, E extends JsonElement, C> extends CommonValueMapper<T, E> {

    int getGenericElementPosition();
    
    T fromJSON(E json, ValueMapper<C, JsonElement> mapper);
    
    E toJSON(T value, ValueMapper<C, JsonElement> mapper);
    
    default T read(PacketBuffer buffer, ValueMapper<C, JsonElement> mapper) {
        return this.fromJSON(ConfigImpl.INTERNAL.fromJson(buffer.readString(0x40000), this.element()), mapper);
    }

    /**
     * Writes a value to a {@code PacketBuffer}. The default implementation calls
     * {@code toJSON} and writes the resulting JSON as a string.
     */
    default void write(T value, PacketBuffer buffer, ValueMapper<C, JsonElement> mapper) {
        buffer.writeString(ConfigImpl.INTERNAL.toJson(this.toJSON(value, mapper)), 0x40000);
    }
}
