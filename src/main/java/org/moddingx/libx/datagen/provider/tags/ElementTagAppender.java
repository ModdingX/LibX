package org.moddingx.libx.datagen.provider.tags;

import net.minecraft.core.Registry;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;

import javax.annotation.Nonnull;

/**
 * A {@link TagAppender} that can add elements directly in addition to {@link ResourceKey resource keys}.
 * Vanilla {@link TagAppender} only accepts {@link ResourceKey resource keys}, which would require every
 * caller to look up the key for an element themselves. This appender resolves the key from the registry
 * instead, so {@code tag.add(myBlock)} keeps working.
 */
public class ElementTagAppender<T> implements TagAppender<T> {

    private final TagAppender<T> parent;
    private final Registry<T> registry;

    public ElementTagAppender(TagAppender<T> parent, Registry<T> registry) {
        this.parent = parent;
        this.registry = registry;
    }

    /**
     * Gets the {@link ResourceKey} for an element. Fails if the element is not registered.
     */
    public ResourceKey<T> key(T element) {
        return this.registry.getResourceKey(element).orElseThrow(
                () -> new IllegalArgumentException("Can't add unregistered element to tag: " + element)
        );
    }

    /**
     * Adds an element to the tag.
     */
    public ElementTagAppender<T> add(T element) {
        return this.add(this.key(element));
    }

    /**
     * Adds multiple elements to the tag.
     */
    @SafeVarargs
    public final ElementTagAppender<T> add(T... elements) {
        for (T element : elements) {
            this.add(element);
        }
        return this;
    }

    /**
     * Adds an element to the tag if it is registered.
     */
    public ElementTagAppender<T> addOptional(T element) {
        return this.addOptional(this.key(element));
    }

    /**
     * Removes an element from the tag.
     */
    public ElementTagAppender<T> remove(T element) {
        return this.remove(this.key(element));
    }

    @Override
    public ElementTagAppender<T> add(@Nonnull ResourceKey<T> element) {
        this.parent.add(element);
        return this;
    }

    @Override
    public ElementTagAppender<T> addOptional(@Nonnull ResourceKey<T> element) {
        this.parent.addOptional(element);
        return this;
    }

    @Override
    public ElementTagAppender<T> addTag(@Nonnull TagKey<T> tag) {
        this.parent.addTag(tag);
        return this;
    }

    @Override
    public ElementTagAppender<T> addOptionalTag(@Nonnull TagKey<T> tag) {
        this.parent.addOptionalTag(tag);
        return this;
    }

    @Override
    public ElementTagAppender<T> add(@Nonnull TagEntry entry) {
        this.parent.add(entry);
        return this;
    }

    @Override
    public ElementTagAppender<T> replace(boolean value) {
        this.parent.replace(value);
        return this;
    }

    @Override
    public ElementTagAppender<T> remove(@Nonnull TagKey<T> tag) {
        this.parent.remove(tag);
        return this;
    }

    @Override
    public ElementTagAppender<T> remove(@Nonnull ResourceKey<T> element) {
        this.parent.remove(element);
        return this;
    }
}
