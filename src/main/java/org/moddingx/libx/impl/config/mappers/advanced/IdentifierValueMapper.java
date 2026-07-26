package org.moddingx.libx.impl.config.mappers.advanced;

import com.google.gson.JsonPrimitive;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.gui.InputProperties;
import org.moddingx.libx.config.mapper.ValueMapper;
import org.moddingx.libx.config.validator.ValidatorInfo;
import org.moddingx.libx.util.Misc;

public class IdentifierValueMapper implements ValueMapper<Identifier, JsonPrimitive> {

    public static final IdentifierValueMapper INSTANCE = new IdentifierValueMapper();
    private static final InputProperties<Identifier> INPUT = new InputProperties<>() {

        @Override
        public Identifier defaultValue() {
            return Misc.MISSINGNO;
        }

        @Override
        public boolean canInputChar(char chr) {
            return Identifier.isAllowedInIdentifier(chr);
        }

        @Override
        public boolean isValid(String str) {
            return Identifier.tryParse(str) != null;
        }

        @Override
        public Identifier valueOf(String str) {
            return Identifier.parse(str);
        }
    };

    private IdentifierValueMapper() {

    }

    @Override
    public Class<Identifier> type() {
        return Identifier.class;
    }

    @Override
    public Class<JsonPrimitive> element() {
        return JsonPrimitive.class;
    }

    @Override
    public Identifier fromJson(JsonPrimitive json) {
        return Identifier.parse(json.getAsString());
    }

    @Override
    public JsonPrimitive toJson(Identifier value) {
        return new JsonPrimitive(value.toString());
    }

    @Override
    public StreamCodec<? super FriendlyByteBuf, Identifier> streamCodec() {
        return Identifier.STREAM_CODEC;
    }

    @Override
    public ConfigEditor<Identifier> createEditor(ValidatorInfo<?> validator) {
        return ConfigEditor.input(INPUT, validator);
    }
}
