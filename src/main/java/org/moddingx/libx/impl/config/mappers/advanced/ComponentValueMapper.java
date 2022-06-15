package org.moddingx.libx.impl.config.mappers.advanced;

import com.google.gson.JsonElement;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.moddingx.libx.config.validator.ValidatorInfo;
import org.moddingx.libx.config.mapper.ValueMapper;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.impl.config.gui.screen.content.component.ComponentContent;

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

    @Override
    @OnlyIn(Dist.CLIENT)
    public ConfigEditor<Component> createEditor(ValidatorInfo<?> validator) {
        return ConfigEditor.custom(Component.empty(), ComponentContent::new);
    }
}
