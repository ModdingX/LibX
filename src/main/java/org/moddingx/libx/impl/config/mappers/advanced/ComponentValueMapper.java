package org.moddingx.libx.impl.config.mappers.advanced;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.RegistryOps;
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
        return ComponentSerialization.CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, BuiltinRegistryHelper.BUILTIN_REGISTRY_LOOKUP), json).getOrThrow();
    }

    @Override
    public JsonElement toJson(Component value) {
        return ComponentSerialization.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, BuiltinRegistryHelper.BUILTIN_REGISTRY_LOOKUP), value).getOrThrow();
    }

    @Override
    public ConfigEditor<Component> createEditor(ValidatorInfo<?> validator) {
        return ConfigEditor.custom(Component.empty(), ComponentContent::new);
    }
}
