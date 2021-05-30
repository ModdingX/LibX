package io.github.noeppi_noeppi.libx.mod.registration;

import com.google.common.collect.ImmutableList;
import io.github.noeppi_noeppi.libx.impl.registration.BuiltinTransformers;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.List;

/**
 * A registration builder is used to configure the registration system for a mod.
 * You must set the version of the registration system that is used for your mod here.
 * This is because LibX keeps adding new registry transformers in newer versions. However
 * these my break mods that are not made for it. Thats why you'll need to set the version.
 * LibX will then only load its transformers made for that version.
 * 
 * You can also add custom conditions and transformers that are used for you registration.
 * 
 * <b>The current LibX registration version is 1.</b>
 * 
 * By setting the version to 0, you disable all registry transformers by LibX.
 * 
 * The transformers added by LibX may also replace the object registered. If they do so, the
 * original object is passed to the conditions (as every other object) but not to custom
 * transformers. If a LibX transformer replaced objects, it printed <i>italic</i> in the list
 * below.
 * 
 * Registry transformers added in version <b>1</b>:
 * <ul>
 *     <li><i>A transformer that translates every {@code IDataSerializer} into a DataSerializerEntry
 *     which is a subclass of ForgeRegistryEntry which means it can be registered directly to a forge
 *     registry.</i></li>
 * </ul>
 */
public class RegistrationBuilder {
    
    private int version = -1;
    private final List<RegistryCondition> customConditions = new ArrayList<>();
    private final List<RegistryTransformer> customTransformers = new ArrayList<>();

    RegistrationBuilder() {
        
    }

    public void setVersion(int version) {
        if (this.version >= 0) {
            throw new IllegalStateException("LibX registration version set twice.");
        }
        this.version = version;
    }
    
    public void addCondition(RegistryCondition condition) {
        this.customConditions.add(condition);
    }
    
    public void addTransformer(RegistryTransformer transformer) {
        this.customTransformers.add(transformer);
    }
    
    public Triple<List<RegistryCondition>, List<RegistryTransformer>, List<RegistryTransformer>> build() {
        if (this.version < 0) {
            throw new IllegalStateException("LibX registration version not set.");
        }
        ImmutableList.Builder<RegistryCondition> conditions = ImmutableList.builder();
        ImmutableList.Builder<RegistryTransformer> replacers = ImmutableList.builder();
        ImmutableList.Builder<RegistryTransformer> transformers = ImmutableList.builder();
        // No break, 
        switch (this.version) {
            case 1:
                replacers.add(BuiltinTransformers.DATA_SERIALIZER);
            case 0:
                break;
            default:
                throw new IllegalStateException("Unknown LibX registration version: " + this.version);
        }
        conditions.addAll(this.customConditions);
        transformers.addAll(this.customTransformers);
        return Triple.of(conditions.build(), replacers.build(), transformers.build());
    }
}
