package io.github.noeppi_noeppi.libx.impl.config.mappers.advanced;

import com.google.gson.JsonPrimitive;
import io.github.noeppi_noeppi.libx.config.ValidatorInfo;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import io.github.noeppi_noeppi.libx.config.gui.ConfigEditor;
import io.github.noeppi_noeppi.libx.config.gui.InputProperties;
import io.github.noeppi_noeppi.libx.util.Misc;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
                //noinspection ResultOfMethodCallIgnored
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
    public Class<java.util.UUID> type() {
        return UUID.class;
    }

    @Override
    public Class<JsonPrimitive> element() {
        return JsonPrimitive.class;
    }

    @Override
    public java.util.UUID fromJson(JsonPrimitive json) {
        return UUID.fromString(json.getAsString());
    }

    @Override
    public JsonPrimitive toJson(java.util.UUID value) {
        return new JsonPrimitive(value.toString());
    }

    @Override
    public UUID fromNetwork(FriendlyByteBuf buffer) {
        long mostSignificantBits = buffer.readLong();
        long leastSignificantBits = buffer.readLong();
        return new UUID(mostSignificantBits, leastSignificantBits);
    }

    @Override
    public void toNetwork(UUID value, FriendlyByteBuf buffer) {
        buffer.writeLong(value.getMostSignificantBits());
        buffer.writeLong(value.getLeastSignificantBits());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ConfigEditor<UUID> createEditor(ValidatorInfo<?> validator) {
        return ConfigEditor.input(INPUT, validator);
    }
}
