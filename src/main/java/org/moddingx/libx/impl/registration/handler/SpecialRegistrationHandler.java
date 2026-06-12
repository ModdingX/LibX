package org.moddingx.libx.impl.registration.handler;

import net.minecraft.resources.Identifier;

import java.util.Map;

public abstract class SpecialRegistrationHandler {
    
    private final Runnable runRegistration;

    protected SpecialRegistrationHandler(Runnable runRegistration) {
        this.runRegistration = runRegistration;
    }
    
    protected final void runRegistration() {
        this.runRegistration.run();
    }

    public abstract void handle(Identifier id, Object object);

    protected <T> void addToMap(String clsName, Map<Identifier, T> map, Identifier id, T value) {
        if (map.containsKey(id)) {
            throw new IllegalStateException("Two instances of " + clsName + " registered with the same id: " + id);
        }
        map.put(id, value);
    }
}
