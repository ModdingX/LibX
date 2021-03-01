package io.github.noeppi_noeppi.libx.impl.config;

import com.google.gson.JsonElement;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;

public class AdvancedValueMappers {
    
    public static final ValueMapper<Ingredient, JsonElement> INGREDIENT = new ValueMapper<Ingredient, JsonElement>() {

        @Override
        public Class<Ingredient> type() {
            return Ingredient.class;
        }

        @Override
        public Class<JsonElement> element() {
            return JsonElement.class;
        }

        @Override
        public Ingredient fromJSON(JsonElement json, Class<?> elementType) {
            return Ingredient.deserialize(json);
        }

        @Override
        public JsonElement toJSON(Ingredient value, Class<?> elementType) {
            return value.serialize();
        }

        @Override
        public Ingredient read(PacketBuffer buffer, Class<?> elementType) {
            return Ingredient.read(buffer);
        }

        @Override
        public void write(Ingredient value, PacketBuffer buffer, Class<?> elementType) {
            value.write(buffer);
        }
    };
    
    public static final ValueMapper<IFormattableTextComponent, JsonElement> TEXT_COMPONENT = new ValueMapper<IFormattableTextComponent, JsonElement>() {

        @Override
        public Class<IFormattableTextComponent> type() {
            return IFormattableTextComponent.class;
        }

        @Override
        public Class<JsonElement> element() {
            return JsonElement.class;
        }

        @Override
        public IFormattableTextComponent fromJSON(JsonElement json, Class<?> elementType) {
            return ITextComponent.Serializer.getComponentFromJson(json);
        }

        @Override
        public JsonElement toJSON(IFormattableTextComponent value, Class<?> elementType) {
            return ITextComponent.Serializer.toJsonTree(value);
        }
    };
}
