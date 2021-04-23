package io.github.noeppi_noeppi.libx.impl.config;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import io.github.noeppi_noeppi.libx.util.ResourceList;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

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
    
    public static final ValueMapper<ResourceLocation, JsonPrimitive> RESOURCE = new ValueMapper<ResourceLocation, JsonPrimitive>() {

        @Override
        public Class<ResourceLocation> type() {
            return ResourceLocation.class;
        }

        @Override
        public Class<JsonPrimitive> element() {
            return JsonPrimitive.class;
        }

        @Override
        public ResourceLocation fromJSON(JsonPrimitive json, Class<?> elementType) {
            return new ResourceLocation(json.getAsString());
        }

        @Override
        public JsonPrimitive toJSON(ResourceLocation value, Class<?> elementType) {
            return new JsonPrimitive(value.toString());
        }

        @Override
        public ResourceLocation read(PacketBuffer buffer, Class<?> elementType) {
            return buffer.readResourceLocation();
        }

        @Override
        public void write(ResourceLocation value, PacketBuffer buffer, Class<?> elementType) {
            buffer.writeResourceLocation(value);
        }
    };
    
    public static final ValueMapper<ResourceList, JsonObject> RESOURCE_LIST = new ValueMapper<ResourceList, JsonObject>() {

        private final List<String> COMMENT = ImmutableList.of(
                "This is a resource list. In the `whitelist` field you can specify whether all entries will be accepted by",
                "default or rejected.",
                "`elements` is an array of rules. Each resource location that is matched against this list, will traverse these",
                "rules from top to bottom. The first rule that matches a resource location determines its result.",
                "Rules are resource locations, where asterisks (*) can be added to match any number of characters.",
                "However an asterisk can not match a colon. The nly exception to this is the single asterisk which matches everything.",
                "When a rule is matched, it will yield the result specified in `whitelist` as a result. To alter this",
                "add a plus (+) or a minus (-) in front of the rule. This will make it a whitelist or blacklist rule respectively.",
                "You can also add regex rules. These are objects with two keys: `allow` - a boolean that specifies whether this",
                "is a whitelist or blacklist rule and `regex` - which is a regex that must match the resource location."
        );
        
        @Override
        public Class<ResourceList> type() {
            return ResourceList.class;
        }

        @Override
        public Class<JsonObject> element() {
            return JsonObject.class;
        }

        @Override
        public ResourceList fromJSON(JsonObject json, Class<?> elementType) {
            return new ResourceList(json);
        }

        @Override
        public JsonObject toJSON(ResourceList value, Class<?> elementType) {
            return value.toJSON();
        }

        @Override
        public ResourceList read(PacketBuffer buffer, Class<?> elementType) {
            return new ResourceList(buffer);
        }

        @Override
        public void write(ResourceList value, PacketBuffer buffer, Class<?> elementType) {
            value.write(buffer);
        }

        @Override
        public List<String> comment(Class<?> elementType) {
            return this.COMMENT;
        }
    };
}
