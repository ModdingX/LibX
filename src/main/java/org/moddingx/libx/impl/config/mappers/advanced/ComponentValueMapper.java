package org.moddingx.libx.impl.config.mappers.advanced;

import com.google.gson.JsonElement;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.mapper.ValueMapper;
import org.moddingx.libx.config.validator.ValidatorInfo;
import org.moddingx.libx.impl.config.gui.screen.content.component.ComponentContent;
import org.moddingx.libx.impl.registration.BuiltinRegistryHelper;

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
        return Component.Serializer.deserialize(json, BuiltinRegistryHelper.BUILTIN_REGISTRY_LOOKUP);
    }

    @Override
    public JsonElement toJson(Component value) {
        return Component.Serializer.serialize(value, BuiltinRegistryHelper.BUILTIN_REGISTRY_LOOKUP);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ConfigEditor<Component> createEditor(ValidatorInfo<?> validator) {
        return ConfigEditor.custom(Component.empty(), ComponentContent::new);
    }
}
