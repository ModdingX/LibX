package org.moddingx.libx.impl.config.correct;

import com.google.gson.JsonElement;
import org.moddingx.libx.config.correct.ConfigCorrection;
import org.moddingx.libx.config.mapper.ValueMapper;
import org.moddingx.libx.util.lazy.LazyValue;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;

public class CorrectionInstance<T, P> implements ConfigCorrection<T> {
    
    private final LazyValue<Optional<T>> defaultValue;

    private CorrectionInstance(LazyValue<Optional<T>> defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    public static <T> CorrectionInstance<T, T> create(T defaultValue) {
        return new CorrectionInstance<>(new LazyValue<>(() -> Optional.of(defaultValue)));
    }

    public <U> Optional<U> tryGetRaw(@Nullable JsonElement json, ValueMapper<U, ?> mapper) {
        if (json == null) {
            return Optional.empty();
        }
        if (mapper.element().isAssignableFrom(json.getClass())) {
            try {
                //noinspection unchecked
                return Optional.ofNullable(((ValueMapper<U, JsonElement>) mapper).fromJson(json));
            } catch (Exception e) {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    @Override
    public <U> Optional<U> tryGet(JsonElement json, ValueMapper<U, ?> mapper) {
        return this.tryCorrect(json, mapper, o -> Optional.empty());
    }

    @Override
    public <U> Optional<U> tryCorrect(@Nullable JsonElement json, ValueMapper<U, ?> mapper, Function<T, Optional<U>> extractor) {
        return this.tryGetRaw(json, mapper).or(() -> {
            LazyValue<Optional<U>> defaultChild = this.defaultValue.map(o -> o.flatMap(extractor));
            CorrectionInstance<U, P> child = new CorrectionInstance<>(defaultChild);
            try {
                return mapper.correct(json, child).or(defaultChild::get);
            } catch (Exception e) {
                return defaultChild.get();
            }
        });
    }
}
