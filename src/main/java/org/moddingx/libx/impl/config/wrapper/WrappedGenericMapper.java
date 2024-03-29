package org.moddingx.libx.impl.config.wrapper;

import com.google.gson.JsonElement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.moddingx.libx.config.correct.ConfigCorrection;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.mapper.GenericValueMapper;
import org.moddingx.libx.config.mapper.ValueMapper;
import org.moddingx.libx.config.validator.ValidatorInfo;

import java.util.List;
import java.util.Optional;

public class WrappedGenericMapper<T, E extends JsonElement, C> implements ValueMapper<T, E> {

    private final GenericValueMapper<T, E, C> parent;
    private final ValueMapper<C, JsonElement> mapper;

    public WrappedGenericMapper(GenericValueMapper<T, E, C> parent, ValueMapper<C, JsonElement> mapper) {
        this.parent = parent;
        this.mapper = mapper;
    }

    @Override
    public Class<T> type() {
        return this.parent.type();
    }

    @Override
    public Class<E> element() {
        return this.parent.element();
    }

    @Override
    public T fromJson(E json) {
        return this.parent.fromJson(json, this.mapper);
    }

    @Override
    public E toJson(T value) {
        return this.parent.toJson(value, this.mapper);
    }

    @Override
    public T fromNetwork(FriendlyByteBuf buffer) {
        return this.parent.fromNetwork(buffer, this.mapper);
    }

    @Override
    public void toNetwork(T value, FriendlyByteBuf buffer) {
        this.parent.toNetwork(value, buffer, this.mapper);
    }

    @Override
    public Optional<T> correct(JsonElement json, ConfigCorrection<T> correction) {
        return this.parent.correct(json, this.mapper, correction);
    }

    @Override
    public List<String> comment() {
        return this.parent.comment();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ConfigEditor<T> createEditor(ValidatorInfo<?> validator) {
        return this.parent.createEditor(this.mapper, validator);
    }
}
