package org.moddingx.libx.impl;

// Thrown by ItemStackRenderer.get while in datagen.
// Used by the item model provider to find out whether a mod uses ItemStackRenderer
// Hopefully this does not break...
public class RendererOnDataGenException extends RuntimeException {
    
    public RendererOnDataGenException() {
        super("Attempted to retrieve ItemStackRenderer during data generation.");
    }
}
