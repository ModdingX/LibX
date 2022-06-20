package org.moddingx.libx.codec;

import com.mojang.serialization.DynamicOps;

import java.util.stream.Stream;

public interface ElementFactory {
    
    <T> Stream<T> elements(DynamicOps<T> ops);
}
