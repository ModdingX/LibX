package org.moddingx.libx.annotation.impl;

import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProcessorInterfaceClient {

    private static final Map<ResourceLocation, StandaloneModelKey<QuadCollection>> specialModels = new ConcurrentHashMap<>();

    private static StandaloneModelKey<QuadCollection> specialModelKey(ResourceLocation id) {
        return specialModels.computeIfAbsent(id, key -> new StandaloneModelKey<>(key::toString));
    }

    public static void addSpecialModel(ModelEvent.RegisterStandalone event, ResourceLocation id) {
        event.register(specialModelKey(id), SimpleUnbakedStandaloneModel.quadCollection(id));
    }

    public static QuadCollection getSpecialModel(ModelEvent.BakingCompleted event, ResourceLocation id) {
        StandaloneModelKey<QuadCollection> key = specialModels.get(id);
        if (key != null) {
            QuadCollection model = event.getBakingResult().standaloneModels().get(key);
            if (model != null) {
                return model;
            }
        }

        throw new IllegalStateException("Model not loaded: " + id);
    }
}
