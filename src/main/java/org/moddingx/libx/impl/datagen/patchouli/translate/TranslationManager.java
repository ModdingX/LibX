package org.moddingx.libx.impl.datagen.patchouli.translate;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TranslationManager {
    
    private final String prefix;
    private final Map<String, String> translations;
    
    public TranslationManager(String prefix) {
        this.prefix = prefix;
        this.translations = new HashMap<>();
    }
    
    public String add(String translated, List<String> nameElems) {
        String fullName = Stream.concat(Stream.of(this.prefix), nameElems.stream()).filter(s -> !s.isEmpty()).collect(Collectors.joining("."));
        this.translations.put(fullName, translated);
        return fullName;
    }
    
    public JsonObject build() {
        JsonObject json = new JsonObject();
        for (String key : this.translations.keySet().stream().sorted().toList()) {
            json.addProperty(key, this.translations.get(key));
        }
        return json;
    }
}
