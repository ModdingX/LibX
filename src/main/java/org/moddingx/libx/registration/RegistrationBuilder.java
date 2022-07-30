package org.moddingx.libx.registration;

import java.util.ArrayList;
import java.util.List;

/**
 * A registration builder is used to configure the LibX registration system and adjust the behaviour of it.
 */
public class RegistrationBuilder {
    
    private boolean tracking;
    private final List<RegistryCondition> conditions;
    private final List<RegistryTransformer> transformers;
    
    public RegistrationBuilder() {
        this.tracking = false;
        this.conditions = new ArrayList<>();
        this.transformers = new ArrayList<>();
    }

    /**
     * Enables automatic registry tracking. That means when registering objects, 
     * {@link Registerable#initTracking(RegistrationContext, Registerable.TrackingCollector)}
     * is called. In order for registry tracking to properly work, you need to manually track the fields
     * holding your values. ModInit will do this automatically if this option here is enabled.
     */
    public void enableRegistryTracking() {
        this.tracking = true;
    }

    /**
     * Adds a new {@link RegistryCondition} that must match each object that is passed to the system in order
     * to be registered.
     */
    public void condition(RegistryCondition condition) {
        this.conditions.add(condition);
    }
    
    /**
     * Adds a new {@link RegistryTransformer} that can add additional objects that are registered with each
     * object registered through the LibX registration system.
     */
    public void transformer(RegistryTransformer transformer) {
        this.transformers.add(transformer);
    }
    
    public Result build() {
        return new Result(this.tracking, List.copyOf(this.conditions), List.copyOf(this.transformers));
    }
    
    public record Result(boolean tracking, List<RegistryCondition> conditions, List<RegistryTransformer> transformers) {}
}
