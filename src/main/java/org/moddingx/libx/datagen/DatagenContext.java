package org.moddingx.libx.datagen;

import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.moddingx.libx.mod.ModX;

import java.util.function.Function;

/**
 * Context for creation of data and registry providers.
 */
public abstract class DatagenContext {

    private final DatagenStage stage;
    private final DatagenSystem system;
    private final PackTarget target;

    protected DatagenContext(DatagenStage stage, DatagenSystem system, PackTarget target) {
        this.stage = stage;
        this.system = system;
        this.target = target;
    }

    /**
     * Gets the {@link DatagenStage} the provider runs in.
     */
    public DatagenStage stage() {
        return this.stage;
    }

    /**
     * Gets the mod, the provider is generating data for.
     */
    public ModX mod() {
        return this.system.mod();
    }

    /**
     * Gets the associated {@link DatagenSystem}.
     */
    public DatagenSystem system() {
        return this.system;
    }

    /**
     * Gets the {@link ExistingFileHelper}.
     */
    public ExistingFileHelper fileHelper() {
        return this.system.fileHelper();
    }

    /**
     * Gets the {@link PackTarget} for output. <b>This may only be used during
     * {@link DatagenStage#DATAGEN datagen stage}.</b>
     */
    public PackTarget target() {
        if (this.stage != DatagenStage.DATAGEN) {
            throw new UnsupportedOperationException("Can't access the pack target in " + this.stage + " stage");
        }
        return this.target;
    }
    
    /**
     * Gets the {@link PackOutput} for output. <b>This may only be used during
     * {@link DatagenStage#DATAGEN datagen stage}.</b>
     */
    public PackOutput output() {
        return this.target().packOutput();
    }

    /**
     * Gets the {@link RegistrySet} used for this provider.
     */
    public RegistrySet registries() {
        return this.target.registries();
    }

    /**
     * Queries a {@link RegistryProvider} by class. This provider must have been registered before and there may only
     * be a single provider of that class. It's impossible to lookup providers from later stages.
     */
    public abstract <T extends RegistryProvider> T findRegistryProvider(Class<T> cls);
    
    /**
     * Queries a {@link DataProvider} by class. This provider must have been registered before and there may only
     * be a single provider of that class. It's impossible to lookup providers from later stages.
     */
    public abstract <T extends DataProvider> T findDataProvider(Class<T> cls);
    
    /**
     * Adds an additional provider for the {@link DatagenStage#DATAGEN datagen stage} that runs on the same
     * {@link PackTarget} as this provider.
     */
    public abstract void addAdditionalProvider(Function<DatagenContext, DataProvider> provider);
}
