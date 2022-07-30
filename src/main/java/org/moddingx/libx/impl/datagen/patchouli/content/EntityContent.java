package org.moddingx.libx.impl.datagen.patchouli.content;

import com.google.gson.JsonObject;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;
import org.moddingx.libx.datagen.provider.patchouli.content.CaptionContent;
import org.moddingx.libx.datagen.provider.patchouli.page.PageBuilder;

import javax.annotation.Nullable;
import java.util.Objects;

public class EntityContent extends CaptionContent {
    
    private final EntityType<?> entity;

    public EntityContent(EntityType<?> entity) {
        this(entity, null);
    }
    
    private EntityContent(EntityType<?> entity, @Nullable String caption) {
        super(caption);
        this.entity = entity;
    }

    @Override
    protected int lineSkip() {
        return 13;
    }

    @Override
    protected CaptionContent withCaption(String caption) {
        return new EntityContent(this.entity, caption);
    }

    @Override
    protected void specialPage(PageBuilder builder, @Nullable String caption) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "patchouli:entity");
        json.addProperty("entity", Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(this.entity), "Entity not registered: " + this.entity).toString());
        if (caption != null) {
            json.addProperty("text", builder.translate(caption));
        }
        builder.addPage(json);
    }
}
