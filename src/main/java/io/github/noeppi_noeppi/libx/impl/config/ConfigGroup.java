package io.github.noeppi_noeppi.libx.impl.config;

import com.google.common.collect.ImmutableList;
import io.github.noeppi_noeppi.libx.config.Group;

import javax.annotation.Nullable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ConfigGroup {
    
    public final Class<?> type;
    public final List<String> path;
    public final List<String> comment;

    public ConfigGroup(Class<?> type, ImmutableList<String> path, ImmutableList<String> comment) {
        this.type = type;
        this.path = path;
        this.comment = comment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        ConfigGroup that = (ConfigGroup) o;
        return this.type.equals(that.type) && this.path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.type, this.path);
    }
    
    @Nullable
    public static ConfigGroup create(Class<?> type, Class<?> configBaseClass) {
        try {
            if (!Modifier.isStatic(type.getModifiers())) {
                return null;
            }
            if (type.equals(configBaseClass)) {
                return new ConfigGroup(type, ImmutableList.of(), ImmutableList.of());
            }
            @Nullable
            Group group = type.getAnnotation(Group.class);
            List<String> path = new ArrayList<>();
            path.add(0, type.getSimpleName());
            Class<?> currentStep = type.getDeclaringClass();
            while (currentStep != configBaseClass) {
                if (currentStep == null || currentStep == Object.class) {
                    throw new IllegalStateException("LibX config internal error: Can't create config group for class that is not part of config base class.");
                }
                path.add(0, currentStep.getSimpleName());
                currentStep = currentStep.getDeclaringClass();
            }
            return new ConfigGroup(type, ImmutableList.copyOf(path), group == null ? ImmutableList.of() : ImmutableList.copyOf(group.value()));
        } catch (SecurityException e) {
            throw new IllegalStateException("Failed to create config group for type " + type, e);
        }
    }
    
    public static final Comparator<ConfigGroup> BY_PATH = (o1, o2) -> {
        int minLength = Math.min(o1.path.size(), o2.path.size());
        for (int i = 0; i < minLength; i++) {
            int result = o1.path.get(i).compareTo(o2.path.get(i));
            if (result != 0) {
                return result;
            }
        }
        return Integer.compare(o1.path.size(), o2.path.size());
    };
}
