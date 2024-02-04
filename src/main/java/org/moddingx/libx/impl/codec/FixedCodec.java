package org.moddingx.libx.impl.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.util.Unit;

import java.util.Objects;

public class FixedCodec<A> implements Codec<Unit> {
    
    private final Dynamic<A> serializedValue;

    public FixedCodec(Dynamic<A> serializedValue) {
        this.serializedValue = serializedValue;
    }


    @Override
    public <T> DataResult<T> encode(Unit unit, DynamicOps<T> ops, T prefix) {
        return ops.mergeToPrimitive(prefix, this.serializedValue.convert(ops).getValue());
    }

    @Override
    public <T> DataResult<Pair<Unit, T>> decode(DynamicOps<T> ops, T input) {
        Dynamic<A> dynamic = new Dynamic<>(ops, input).convert(this.serializedValue.getOps());
        if (Objects.equals(this.serializedValue, dynamic)) {
            return DataResult.success(Pair.of(Unit.INSTANCE, ops.empty()));
        } else {
            return DataResult.error(() -> "Wrong value in fixed codec.");
        }
    }

    @Override
    public String toString() {
        return "FixedCodec[" + this.serializedValue + "]";
    }
}
