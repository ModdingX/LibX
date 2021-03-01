package io.github.noeppi_noeppi.libx.impl.commands;

public enum NbtOutputType {
    NBT,
    JSON;


    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
