package io.github.noeppi_noeppi.libx.impl.config.mappers.advanced;

import com.google.gson.JsonElement;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;

public class TextComponentValueMapper implements ValueMapper<IFormattableTextComponent, JsonElement> {

    public static final TextComponentValueMapper INSTANCE = new TextComponentValueMapper();

    private TextComponentValueMapper() {

    }

    @Override
    public Class<IFormattableTextComponent> type() {
        return IFormattableTextComponent.class;
    }

    @Override
    public Class<JsonElement> element() {
        return JsonElement.class;
    }

    @Override
    public IFormattableTextComponent fromJSON(JsonElement json) {
        return ITextComponent.Serializer.getComponentFromJson(json);
    }

    @Override
    public JsonElement toJSON(IFormattableTextComponent value) {
        return ITextComponent.Serializer.toJsonTree(value);
    }
}
