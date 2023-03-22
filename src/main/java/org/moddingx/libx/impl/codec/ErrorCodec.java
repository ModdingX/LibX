package org.moddingx.libx.impl.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

public class ErrorCodec<A> implements Codec<A> {
    
    private final String encodeMsg;
    private final String decodeMsg;

    public ErrorCodec(String encodeMsg, String decodeMsg) {
        this.encodeMsg = encodeMsg;
        this.decodeMsg = decodeMsg;
    }

    @Override
    public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
        return DataResult.error(() -> this.encodeMsg);
    }

    @Override
    public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
        return DataResult.error(() -> this.decodeMsg);
    }
}
