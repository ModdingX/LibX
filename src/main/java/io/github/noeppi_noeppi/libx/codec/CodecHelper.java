package io.github.noeppi_noeppi.libx.codec;

import com.mojang.serialization.DataResult;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class CodecHelper {
    
    public static <T> DataResult<T> nonNull(@Nullable T value, String error) {
        return value == null ? DataResult.error(error) : DataResult.success(value);
    }
    
    public static <T> DataResult<T> doesNotThrow(Supplier<T> value) {
        try {
            return DataResult.success(value.get());
        } catch (Exception e) {
            return DataResult.error(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}
