package org.moddingx.libx.impl.config.mappers.generic;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.moddingx.libx.config.correct.ConfigCorrection;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.mapper.GenericValueMapper;
import org.moddingx.libx.config.mapper.ValueMapper;
import org.moddingx.libx.config.validator.ValidatorInfo;
import org.moddingx.libx.impl.config.gui.screen.content.CollectionContent;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SetValueMapper<T> implements GenericValueMapper<Set<T>, JsonArray, T> {

    public static final SetValueMapper<?> INSTANCE = new SetValueMapper<>();
    
    private static final Comparator<Object> COMPARATOR = (o1, o2) -> {
        try {
            if (o1 instanceof Comparable<?> cmp && o1.getClass().isAssignableFrom(o2.getClass())) {
                //noinspection unchecked
                return ((Comparable<Object>) cmp).compareTo(o2);
            } else if (o2 instanceof Comparable<?> cmp && o2.getClass().isAssignableFrom(o1.getClass())) {
                //noinspection unchecked
                return -((Comparable<Object>) cmp).compareTo(o1);
            }
        } catch (Exception | NoClassDefFoundError e) {
            //
        }
        return o1.toString().compareTo(o2.toString());
    };

    private SetValueMapper() {
        
    }
    
    @Override
    public Class<Set<T>> type() {
        //noinspection unchecked
        return (Class<Set<T>>) (Class<?>) Set.class;
    }

    @Override
    public Class<JsonArray> element() {
        return JsonArray.class;
    }

    @Override
    public int getGenericElementPosition() {
        return 0;
    }

    @Override
    public Set<T> fromJson(JsonArray json, ValueMapper<T, JsonElement> mapper) {
        ImmutableSet.Builder<T> builder = ImmutableSet.builder();
        for (int i = 0; i < json.size(); i++) {
            JsonElement element = json.get(i);
            builder.add(mapper.fromJson(element));
        }
        return builder.build();
    }

    @Override
    public JsonArray toJson(Set<T> value, ValueMapper<T, JsonElement> mapper) {
        JsonArray array = new JsonArray();
        for (T element : this.getSortedElements(value)) {
            array.add(mapper.toJson(element));
        }
        return array;
    }

    @Override
    public Set<T> fromNetwork(FriendlyByteBuf buffer, ValueMapper<T, JsonElement> mapper) {
        int size = buffer.readVarInt();
        ImmutableSet.Builder<T> builder = ImmutableSet.builder();
        for (int i = 0; i < size; i++) {
            builder.add(mapper.fromNetwork(buffer));
        }
        return builder.build();
    }

    @Override
    public void toNetwork(Set<T> value, FriendlyByteBuf buffer, ValueMapper<T, JsonElement> mapper) {
        buffer.writeVarInt(value.size());
        for (T element : value) {
            mapper.toNetwork(element, buffer);
        }
    }

    @Override
    public Optional<Set<T>> correct(JsonElement json, ValueMapper<T, JsonElement> mapper, ConfigCorrection<Set<T>> correction) {
        if (json.isJsonArray()) {
            // Keep everything that can load in the set
            ImmutableSet.Builder<T> set = ImmutableSet.builder();
            for (int i = 0; i < json.getAsJsonArray().size(); i++) {
                correction.tryCorrect(json.getAsJsonArray().get(i), mapper, value -> Optional.empty()).ifPresent(set::add);
            }
            Set<T> result = set.build();
            if (result.isEmpty() && json.getAsJsonArray().size() > 0) {
                // Nothing matched. Might be better to return the default value here
                return Optional.empty();
            } else {
                return Optional.of(result);
            }
        } else {
            // Maybe someone forgot to add a list for a single item.
            // We just try to pass the entire json to the child mapper.
            return correction.tryCorrect(json, mapper, ConfigCorrection.check(value -> value.size() == 1, value -> value.iterator().next())).map(Set::of);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ConfigEditor<Set<T>> createEditor(ValueMapper<T, JsonElement> mapper, ValidatorInfo<?> validator) {
        return ConfigEditor.custom(Set.of(), set -> new CollectionContent<>(this.getSortedElements(set), mapper.createEditor(ValidatorInfo.empty()), Set::copyOf, false));
    }
    
    private List<T> getSortedElements(Set<T> set) {
        return set.stream().sorted(COMPARATOR).toList();
    }
}
