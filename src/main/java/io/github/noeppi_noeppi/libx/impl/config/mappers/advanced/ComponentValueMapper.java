package io.github.noeppi_noeppi.libx.impl.config.mappers.advanced;

import com.google.gson.JsonElement;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import net.minecraft.network.chat.Component;

public class ComponentValueMapper implements ValueMapper<Component, JsonElement> {

    public static final ComponentValueMapper INSTANCE = new ComponentValueMapper();

    private ComponentValueMapper() {

    }

    @Override
    public Class<Component> type() {
        return Component.class;
    }

    @Override
    public Class<JsonElement> element() {
        return JsonElement.class;
    }

    @Override
    public Component fromJson(JsonElement json) {
        return Component.Serializer.fromJson(json);
    }

    @Override
    public JsonElement toJson(Component value) {
        return Component.Serializer.toJsonTree(value);
    }
}
