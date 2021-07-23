package io.github.noeppi_noeppi.libx.impl.config.mappers.advanced;

import com.google.gson.JsonElement;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;

public class ComponentValueMapper implements ValueMapper<MutableComponent, JsonElement> {

    public static final ComponentValueMapper INSTANCE = new ComponentValueMapper();

    private ComponentValueMapper() {

    }

    @Override
    public Class<MutableComponent> type() {
        return MutableComponent.class;
    }

    @Override
    public Class<JsonElement> element() {
        return JsonElement.class;
    }

    @Override
    public MutableComponent fromJSON(JsonElement json) {
        return Component.Serializer.fromJson(json);
    }

    @Override
    public JsonElement toJSON(MutableComponent value) {
        return Component.Serializer.toJsonTree(value);
    }
}
