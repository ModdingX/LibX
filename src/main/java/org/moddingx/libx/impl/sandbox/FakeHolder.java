package org.moddingx.libx.impl.sandbox;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

// When we know, how a holder is used.
// Mimics a direct holder, but delegates to some other holder.
// Value can be changed over time.
public class FakeHolder<T> implements Holder<T> {

    private Holder<T> holder;

    public FakeHolder(Holder<T> initial) {
        this.set(initial);
    }

    public void set(Holder<T> value) {
        try {
            value.get();
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException("Unbound holder: " + value, e);
        }
        this.holder = value;
    }

    @Nonnull
    @Override
    public T value() {
        return this.holder.value();
    }

    @Override
    public boolean isBound() {
        return true;
    }

    @Override
    public boolean is(@Nonnull ResourceLocation id) {
        return false;
    }

    @Override
    public boolean is(@Nonnull ResourceKey<T> key) {
        return false;
    }

    @Override
    public boolean is(@Nonnull Predicate<ResourceKey<T>> predicate) {
        return false;
    }

    @Override
    public boolean is(@Nonnull TagKey<T> key) {
        return false;
    }

    @Nonnull
    @Override
    public Stream<TagKey<T>> tags() {
        return Stream.empty();
    }

    @Nonnull
    @Override
    public Either<ResourceKey<T>, T> unwrap() {
        return Either.right(this.value());
    }

    @Nonnull
    @Override
    public Optional<ResourceKey<T>> unwrapKey() {
        return Optional.empty();
    }

    @Nonnull
    @Override
    public Holder.Kind kind() {
        return Holder.Kind.DIRECT;
    }

    @Override
    public boolean canSerializeIn(@Nonnull HolderOwner<T> owner) {
        return true;
    }
}
