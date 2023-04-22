package org.moddingx.libx.datagen.provider;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SoundDefinition;
import net.minecraftforge.common.data.SoundDefinitionsProvider;
import net.minecraftforge.registries.ForgeRegistries;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.mod.ModX;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * A base class for sound definition providers.
 */
public abstract class SoundDefinitionProviderBase implements DataProvider {

    protected final ModX mod;
    
    // Keep the real provider as a field instead of extending it
    // because of conflicting methods.
    private final ParentProvider provider;

    private final Set<ResourceLocation> ignored = new HashSet<>();
    private final Map<ResourceLocation, SoundDefinitionBuilder> sounds = new HashMap<>();

    public SoundDefinitionProviderBase(DatagenContext ctx) {
        this.mod = ctx.mod();
        this.provider = new ParentProvider(ctx.output(), ctx.mod().modid, ctx.fileHelper()) {
            
            @Override
            public void registerSounds() {
                SoundDefinitionProviderBase.this.registerSounds();
            }
        };
    }

    @Nonnull
    @Override
    public String getName() {
        return this.mod.modid + " sound definitions";
    }

    /**
     * This sound will not be processed by the default generator
     */
    protected void ignore(SoundEvent sound) {
        this.ignore(Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getKey(sound)));
    }
    
    /**
     * This sound will not be processed by the default generator
     */
    protected void ignore(ResourceLocation sound) {
        this.ignored.add(sound);
    }

    protected abstract void setup();

    /**
     * Default behaviour for sound events. Override to change.
     */
    protected void defaultSound(ResourceLocation id, SoundEvent sound) {
        this.sound(sound)
                .subtitle("subtitle." + id.getNamespace() + "." + id.getPath().replace("/", "."))
                .with(id);
    }

    /**
     * Creates some empty sound settings
     */
    protected SoundSettingsBuilder settings() {
        return new SoundSettingsBuilder();
    }

    /**
     * Creates a new sound definition for the given sound event.
     */
    protected SoundDefinitionBuilder sound(SoundEvent sound) {
        return this.sound(Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getKey(sound)), this.settings());
    }

    /**
     * Creates a new sound definition for the given sound event.
     */
    protected SoundDefinitionBuilder sound(ResourceLocation sound) {
        return this.sound(sound, this.settings());
    }

    /**
     * Creates a new sound definition for the given sound event and default sound settings.
     */
    protected SoundDefinitionBuilder sound(SoundEvent sound, SoundSettingsBuilder settings) {
        return this.sound(Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getKey(sound)), settings);
    }

    /**
     * Creates a new sound definition for the given sound event and default sound settings.
     */
    protected SoundDefinitionBuilder sound(ResourceLocation sound, SoundSettingsBuilder settings) {
        this.ignore(sound);
        if (this.sounds.containsKey(sound)) throw new IllegalArgumentException("Sound processed twice: " + sound);
        SoundDefinitionBuilder builder = new SoundDefinitionBuilder(settings);
        this.sounds.put(sound, builder);
        return builder;
    }
    
    private void registerSounds() {
        this.setup();

        for (ResourceLocation id : ForgeRegistries.SOUND_EVENTS.getKeys().stream().sorted().toList()) {
            if (this.mod.modid.equals(id.getNamespace()) && !this.ignored.contains(id)) {
                SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(id);
                if (sound != null) {
                    this.defaultSound(id, sound);
                }
            }
        }
        
        for (Map.Entry<ResourceLocation, SoundDefinitionBuilder> entry : this.sounds.entrySet()) {
            this.provider.add(entry.getKey(), entry.getValue().definition);
        }
    }

    @Nonnull
    @Override
    public CompletableFuture<?> run(@Nonnull CachedOutput cache) {
        return this.provider.run(cache);
    }

    protected static class SoundSettingsBuilder {

        private float volume = 1;
        private float pitch = 1;
        private int weight = 1;
        private boolean stream = false;
        private int attenuationDistance = 16;
        private boolean preload = false;

        private SoundSettingsBuilder() {

        }

        /**
         * Sets the volume for the sound settings.
         * 
         * @see SoundDefinition.Sound#volume(float)
         */
        public SoundSettingsBuilder volume(float volume) {
            this.volume = volume;
            return this;
        }

        /**
         * Sets the pitch for the sound settings.
         *
         * @see SoundDefinition.Sound#pitch(float)
         */
        public SoundSettingsBuilder pitch(float pitch) {
            this.pitch = pitch;
            return this;
        }

        /**
         * Sets the weight for the sound settings.
         *
         * @see SoundDefinition.Sound#weight(int)
         */
        public SoundSettingsBuilder weight(int weight) {
            this.weight = weight;
            return this;
        }

        /**
         * Marks the sound settings as stream sound.
         *
         * @see SoundDefinition.Sound#stream()
         */
        public SoundSettingsBuilder stream() {
            return this.stream(true);
        }
        
        /**
         * Sets the stream state of the sound settings.
         *
         * @see SoundDefinition.Sound#stream(boolean)
         */
        public SoundSettingsBuilder stream(boolean stream) {
            this.stream = stream;
            return this;
        }

        /**
         * Sets the attenuation distance for the sound settings.
         *
         * @see SoundDefinition.Sound#attenuationDistance(int)
         */
        public SoundSettingsBuilder attenuationDistance(int attenuationDistance) {
            this.attenuationDistance = attenuationDistance;
            return this;
        }

        /**
         * Marks the sound settings as preloaded.
         *
         * @see SoundDefinition.Sound#preload()
         */
        public SoundSettingsBuilder preload() {
            return this.preload(true);
        }

        /**
         * Sets the preload state of the sound settings.
         *
         * @see SoundDefinition.Sound#preload(boolean)
         */
        public SoundSettingsBuilder preload(boolean preload) {
            this.preload = preload;
            return this;
        }
        
        private void applyTo(SoundDefinition.Sound sound) {
            sound.volume(this.volume);
            sound.pitch(this.pitch);
            sound.weight(this.weight);
            sound.stream(this.stream);
            sound.attenuationDistance(this.attenuationDistance);
            sound.preload(this.preload);
        }
    }

    /**
     * A builder for a sound definition.
     */
    protected class SoundDefinitionBuilder {

        private final SoundSettingsBuilder settings;
        private final SoundDefinition definition;
        
        private SoundDefinitionBuilder(SoundSettingsBuilder settings) {
            this.settings = settings;
            this.definition = SoundDefinition.definition();
        }

        /**
         * Sets the sound definition as a replacement sound.
         * 
         * @see SoundDefinition#replace(boolean)
         */
        public SoundDefinitionBuilder replace() {
            return this.replace(true);
        }

        /**
         * Sets the replace state of the sound definition.
         *
         * @see SoundDefinition#replace(boolean)
         */
        public SoundDefinitionBuilder replace(boolean replace) {
            this.definition.replace(replace);
            return this;
        }

        /**
         * Sets the language key for the sounds subtitle.
         * 
         * @see SoundDefinition#subtitle(String)
         */
        public SoundDefinitionBuilder subtitle(@Nullable String subtitle) {
            this.definition.subtitle(subtitle);
            return this;
        }

        /**
         * Adds a sound from this mods namespace to this sound definition.
         */
        public SoundDefinitionBuilder with(String path) {
            return this.with(SoundDefinitionProviderBase.this.mod.resource(path), sound -> {});
        }

        /**
         * Adds a sound to this sound definition.
         */
        public SoundDefinitionBuilder with(ResourceLocation soundId) {
            return this.with(soundId, sound -> {});
        }

        /**
         * Adds a sound from this mods namespace to this sound definition. Also allows to then further
         * customise the sound.
         */
        public SoundDefinitionBuilder with(String path, Consumer<SoundDefinition.Sound> configure) {
            return this.with(SoundDefinitionProviderBase.this.mod.resource(path), configure);
        }

        /**
         * Adds a sound to this sound definition. Also allows to then further customise the sound.
         */
        public SoundDefinitionBuilder with(ResourceLocation soundId, Consumer<SoundDefinition.Sound> configure) {
            SoundDefinition.Sound sound = SoundDefinition.Sound.sound(soundId, SoundDefinition.SoundType.SOUND);
            this.settings.applyTo(sound);
            configure.accept(sound);
            this.definition.with(sound);
            return this;
        }

        /**
         * Adds {@code amount} sounds to the definition. They are constructed by appending the numbers from
         * {@code 0} (inclusive) to {@code amount} (exclusive) to the given id.
         */
        public SoundDefinitionBuilder withRange(String path, int amount) {
            return this.withRange(SoundDefinitionProviderBase.this.mod.resource(path), amount, sound -> {});
        }

        /**
         * Adds {@code amount} sounds to the definition. They are constructed by appending the numbers from
         * {@code 0} (inclusive) to {@code amount} (exclusive) to the given id.
         */
        public SoundDefinitionBuilder withRange(ResourceLocation soundId, int amount) {
            return this.withRange(soundId, amount, sound -> {});
        }

        /**
         * Adds {@code amount} sounds to the definition. They are constructed by appending the numbers from
         * {@code 0} (inclusive) to {@code amount} (exclusive) to the given id.
         */
        public SoundDefinitionBuilder withRange(String path, int amount, Consumer<SoundDefinition.Sound> configure) {
            return this.withRange(SoundDefinitionProviderBase.this.mod.resource(path), amount, configure);
        }

        /**
         * Adds {@code amount} sounds to the definition. They are constructed by appending the numbers from
         * {@code 0} (inclusive) to {@code amount} (exclusive) to the given id.
         */
        public SoundDefinitionBuilder withRange(ResourceLocation soundId, int amount, Consumer<SoundDefinition.Sound> configure) {
            for (int i = 0; i < amount; i++) {
                this.with(new ResourceLocation(soundId.getNamespace(), soundId.getPath() + i), configure);
            }
            return this;
        }
        
        /**
         * Adds another sound event as a sound for this definition.
         */
        public SoundDefinitionBuilder event(SoundEvent event) {
            return this.event(event, sound -> {});
        }
        
        /**
         * Adds another sound event as a sound for this definition. Also allows to then further customise the sound.
         */
        public SoundDefinitionBuilder event(SoundEvent event, Consumer<SoundDefinition.Sound> configure) {
            SoundDefinition.Sound sound = SoundDefinition.Sound.sound(Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getKey(event)), SoundDefinition.SoundType.EVENT);
            this.settings.applyTo(sound);
            configure.accept(sound);
            this.definition.with(sound);
            return this;
        }
    }
    
    // Required to make a method public
    private static abstract class ParentProvider extends SoundDefinitionsProvider {
        
        protected ParentProvider(PackOutput packOutput, String modId, ExistingFileHelper helper) {
            super(packOutput, modId, helper);
        }

        @Override
        public void add(@Nonnull ResourceLocation sound, @Nonnull SoundDefinition definition) {
            super.add(sound, definition);
        }
    }
}
