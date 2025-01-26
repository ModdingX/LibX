package org.moddingx.libx.impl.config.mappers.advanced;

import com.google.gson.JsonPrimitive;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.gui.InputProperties;
import org.moddingx.libx.config.mapper.ValueMapper;
import org.moddingx.libx.config.validator.ValidatorInfo;

import java.util.UUID;

public class UidValueMapper implements ValueMapper<UUID, JsonPrimitive> {
    
    public static final UidValueMapper INSTANCE = new UidValueMapper();
    private static final InputProperties<UUID> INPUT = new InputProperties<>() {

        @Override
        public UUID defaultValue() {
            return new UUID(0, 0);
        }

        @Override
        public boolean canInputChar(char chr) {
            return (chr >= '0' && chr <= '9') || (chr >= 'a' && chr <= 'f') || (chr >= 'A' && chr <= 'F') || chr == '-';
        }

        @Override
        public boolean isValid(String str) {
            try {
                UUID.fromString(str);
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        @Override
        public UUID valueOf(String str) {
            return UUID.fromString(str);
        }
    };

    private UidValueMapper() {

    }

    @Override
    public Class<UUID> type() {
        return UUID.class;
    }

    @Override
    public Class<JsonPrimitive> element() {
        return JsonPrimitive.class;
    }

    @Override
    public UUID fromJson(JsonPrimitive json) {
        return UUID.fromString(json.getAsString());
    }

    @Override
    public JsonPrimitive toJson(UUID value) {
        return new JsonPrimitive(value.toString());
    }

    @Override
    public StreamCodec<? super FriendlyByteBuf, UUID> streamCodec() {
        return StreamCodec.of((buf, uid) -> buf.writeUUID(uid), buf -> buf.readUUID());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ConfigEditor<UUID> createEditor(ValidatorInfo<?> validator) {
        return ConfigEditor.input(INPUT, validator);
    }
}
