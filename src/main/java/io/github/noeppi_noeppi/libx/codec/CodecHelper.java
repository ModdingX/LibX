package io.github.noeppi_noeppi.libx.codec;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import io.github.noeppi_noeppi.libx.annotation.meta.Experimental;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Some utilities to deal with {@link Codec codecs}.
 */
@Experimental
public class CodecHelper {

    /**
     * {@link CodecOps} for {@link JsonOps json}.
     */
    public static final CodecOps<JsonElement> JSON = new CodecOps<>(JsonElement.class, JsonOps.INSTANCE);

    /**
     * {@link CodecOps} for {@link NbtOps nbt}.
     */
    public static final CodecOps<Tag> NBT = new CodecOps<>(Tag.class, NbtOps.INSTANCE);

    /**
     * Wraps a value into a {@link DataResult}. If the value is non-{@code null}, the result will be
     * successful and contain the value. If the value is {@code null}, the result will be a failure
     * with the given error message.
     */
    public static <T> DataResult<T> nonNull(@Nullable T value, String error) {
        return value == null ? DataResult.error(error) : DataResult.success(value);
    }

    /**
     * Wraps a value into a {@link DataResult}. If the {@link Supplier} does not throw, the result
     * will be successful and contain the value. If the {@link Supplier} throws an exception, the
     * result will be a failure with the error message of the exception.
     */
    public static <T> DataResult<T> doesNotThrow(Supplier<T> value) {
        try {
            return DataResult.success(value.get());
        } catch (Exception e) {
            return DataResult.error(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    /**
     * Takes a number of {@link DataResult}s and returns the first result that is a
     * success. If there is no success in all the parameters, returns the first 
     * {@link DataResult} that has a partial success. If all {@link DataResult}s are
     * failures, the last failure is returned.
     */
    @SafeVarargs
    public static <T> DataResult<T> or(Supplier<DataResult<T>>... results) {
        //noinspection unchecked
        Supplier<DataResult<T>>[] resultsEvaluated = new Supplier[results.length];
        for (int i = 0; i < results.length; i++) {
            DataResult<T> result = results[i].get();
            resultsEvaluated[i] = () -> result;
            if (result.result().isPresent()) {
                return result;
            }
        }
        return orPartial(resultsEvaluated);
    }

    /**
     * Takes a number of {@link DataResult}s and returns the first result that is
     * either a success or a partial success. If there is no success in all the
     * parameters, the last failure is returned.
     */
    @SafeVarargs
    public static <T> DataResult<T> orPartial(Supplier<DataResult<T>>... results) {
        DataResult<T> current = DataResult.error("Empty OR-chain.");
        for (Supplier<DataResult<T>> resultSupplier : results) {
            current = resultSupplier.get();
            if (current.resultOrPartial(err -> {}).isPresent()) {
                return current;
            }
        }
        return current;
    }
}
