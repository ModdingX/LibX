package org.moddingx.libx.impl.base.decoration.blocks;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.moddingx.libx.base.decoration.DecoratedBlock;
import org.moddingx.libx.base.decoration.SignAccess;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.registration.Registerable;
import org.moddingx.libx.registration.RegistrationContext;
import org.moddingx.libx.registration.SetupContext;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.Set;
import java.util.function.Supplier;

public class DecoratedSign implements Registerable, SignAccess {

    public final ModX mod;
    public final DecoratedBlock parent;
    
    private final Standing standing;
    private final Wall wall;
    private final SignItem item;
    private final BlockEntityType<Entity> beType;

    public DecoratedSign(ModX mod, DecoratedBlock parent) {
        this.mod = mod;
        this.parent = parent;
        
        this.standing = new Standing(this.parent, this::getBlockEntityType, this.parent.getMaterialProperties().woodType());
        this.wall = new Wall(this.parent, this::getBlockEntityType, this.parent.getMaterialProperties().woodType());
        //noinspection ConstantConditions
        this.beType = new BlockEntityType<>((pos, state) -> new Entity(this.getBlockEntityType(), pos, state), Set.of(this.standing, this.wall), null);
        this.item = new SignItem(new Item.Properties().stacksTo(16), this.standing, this.wall);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void registerAdditional(RegistrationContext ctx, EntryCollector builder) {
        builder.register(Registries.BLOCK, this.standing);
        builder.register(Registries.BLOCK_ENTITY_TYPE, this.beType);
        builder.register(Registries.ITEM, this.item);
        builder.registerNamed(Registries.BLOCK, "wall", this.wall);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient(SetupContext ctx) {
        BlockEntityRenderers.register(this.beType, SignRenderer::new);
        // Add sign texture to sheet.
        ctx.enqueue(() -> Sheets.addWoodType(this.parent.getMaterialProperties().woodType()));
    }

    @Nonnull
    @Override
    public Item asItem() {
        if (this.item == null) throw new IllegalStateException("Can't get sign item before registration");
        return this.item;
    }

    @Override
    public StandingSignBlock getStandingBlock() {
        if (this.standing == null) throw new IllegalStateException("Can't get standing sign before registration");
        return this.standing;
    }

    @Override
    public WallSignBlock getWallBlock() {
        if (this.wall == null) throw new IllegalStateException("Can't get wall sign before registration");
        return this.wall;
    }

    private BlockEntityType<Entity> getBlockEntityType() {
        if (this.beType == null) throw new IllegalStateException("Can't get sign block entity type before registration");
        return this.beType;
    }
    
    public static class Standing extends StandingSignBlock {

        public final DecoratedBlock parent;
        private final Supplier<BlockEntityType<Entity>> beType;
        
        public Standing(DecoratedBlock parent, Supplier<BlockEntityType<Entity>> beType, WoodType wood) {
            super(Properties.copy(parent), wood);
            this.parent = parent;
            this.beType = beType;
        }

        @Override
        public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
            return this.beType.get().create(pos, state);
        }
    }
    
    public static class Wall extends WallSignBlock {

        public final DecoratedBlock parent;
        private final Supplier<BlockEntityType<Entity>> beType;

        public Wall(DecoratedBlock parent, Supplier<BlockEntityType<Entity>> beType, WoodType wood) {
            super(Properties.copy(parent), wood);
            this.parent = parent;
            this.beType = beType;
        }

        @Override
        public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
            return this.beType.get().create(pos, state);
        }
    }
    
    public static class Entity extends SignBlockEntity {
        
        private final BlockEntityType<?> signType;
        
        public Entity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(pos, state);
            this.signType = type;
        }

        @Nonnull
        @Override
        public BlockEntityType<?> getType() {
            return this.signType;
        }
    }
}
