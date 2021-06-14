package io.github.noeppi_noeppi.libx.impl.commands;

import java.util.Locale;

public enum NbtOutputType {
    NBT,
    JSON;
    
    @Override
    public String toString() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
