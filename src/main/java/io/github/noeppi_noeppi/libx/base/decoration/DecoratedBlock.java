package io.github.noeppi_noeppi.libx.base.decoration;

import com.google.common.collect.ImmutableMap;
import io.github.noeppi_noeppi.libx.base.BlockBase;
import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.Map;
import java.util.NoSuchElementException;

public class DecoratedBlock extends BlockBase {

    private final DecorationContext context;
    private final Map<DecorationType<?>, Object> elements;
    private final Map<String, Object> registerMap;
    
    public DecoratedBlock(ModX mod, DecorationContext context, Properties properties) {
        this(mod, context, properties, new Item.Properties());
    }

    public DecoratedBlock(ModX mod, DecorationContext context, Properties properties, Item.Properties itemProperties) {
        super(mod, properties, itemProperties);
        this.context = context;
        DecorationContext.RegistrationInfo info = context.register(this.mod, this);
        this.elements = info.elementMap();
        this.registerMap = info.registerMap();
    }
    
    @Override
    public Map<String, Object> getNamedAdditionalRegisters(ResourceLocation id) {
        return ImmutableMap.<String, Object>builder()
                .putAll(super.getNamedAdditionalRegisters(id))
                .putAll(this.registerMap)
                .build();
    }
    
    public boolean has(DecorationType<?> type) {
        return this.context.has(type) && this.elements.containsKey(type);
    }
    
    public <T> T get(DecorationType<T> type) {
        if (this.has(type)) {
            //noinspection unchecked
            return (T) this.elements.get(type);
        } else {
            throw new NoSuchElementException("Decoration context " + this.context + " has no element of type " + type.name() + ": " + type);
        }
    }
}
