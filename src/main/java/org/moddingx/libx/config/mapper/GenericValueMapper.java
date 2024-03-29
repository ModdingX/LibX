package org.moddingx.libx.config.mapper;

import com.google.gson.JsonElement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.moddingx.libx.config.Config;
import org.moddingx.libx.config.correct.ConfigCorrection;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.validator.ValidatorInfo;
import org.moddingx.libx.impl.config.ConfigImpl;

import java.util.List;
import java.util.Optional;

/**
 * A value mapper for a generic type. This will get the value mapper for the generic
 * that has been used to declare the config field. For example you could create a
 * value mapper for lists that would then get passed a value mapper for the elements
 * of the list so it can fully serialise it.
 * 
 * A limitation with these is that you can only have one generic parameter for
 * which you can get a value mapper. Only LibX builtin mappers can have multiple
 * of these.
 * 
 * When extending this, you should make the type argument {@code T} dependent on {@code C}.
 * For example for lists, you should do
 * {@code ListValueMapper&lt;T&gt; implements GenericValueMapper&lt;List&lt;T&gt;, JsonArray, T&gt;}.
 * Then you can register a {@code ListValueMapper&lt;?&gt;}.
 * 
 * @param <T> The type that this mapper can serialise.
 * @param <E> The JSON element type this mapper uses.
 * @param <C> The element type. Should be a type variable in most cases.
 */
public interface GenericValueMapper<T, E extends JsonElement, C> {

    /**
     * Gets the class of the type that this mapper can serialise.
     */
    Class<T> type();

    /**
     * Gets the class of the JSON element type this mapper uses.
     */
    Class<E> element();

    /**
     * The position which generic type argument should be used to retrieve a value mapper. Indices
     * start at 0.
     */
    int getGenericElementPosition();

    /**
     * @see ValueMapper#fromJson(JsonElement)
     */
    T fromJson(E json, ValueMapper<C, JsonElement> mapper);
    
    /**
     * @see ValueMapper#toJson(Object)
     */
    E toJson(T value, ValueMapper<C, JsonElement> mapper);

    /**
     * @see ValueMapper#fromNetwork(FriendlyByteBuf)
     */
    default T fromNetwork(FriendlyByteBuf buffer, ValueMapper<C, JsonElement> mapper) {
        return this.fromJson(ConfigImpl.INTERNAL.fromJson(buffer.readUtf(0x40000), this.element()), mapper);
    }
    
    /**
     * @see ValueMapper#toNetwork(Object, FriendlyByteBuf)
     */
    default void toNetwork(T value, FriendlyByteBuf buffer, ValueMapper<C, JsonElement> mapper) {
        buffer.writeUtf(ConfigImpl.INTERNAL.toJson(this.toJson(value, mapper)), 0x40000);
    }

    /**
     * Returns a list of comment lines that will be added to the values specified in {@link Config @Config}.
     */
    default List<String> comment() {
        return List.of();
    }
    
    /**
     * @see ValueMapper#correct(JsonElement, ConfigCorrection)
     */
    default Optional<T> correct(JsonElement json, ValueMapper<C, JsonElement> mapper, ConfigCorrection<T> correction) {
        return Optional.empty();
    }
    
    /**
     * @see ValueMapper#createEditor(ValidatorInfo)
     */
    @OnlyIn(Dist.CLIENT)
    ConfigEditor<T> createEditor(ValueMapper<C, JsonElement> mapper, ValidatorInfo<?> validator);
}
