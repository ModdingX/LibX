package org.moddingx.libx.impl.base.decoration.blocks;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
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
    
    private WoodType wood;
    private Standing standing;
    private Wall wall;
    private SignItem item;
    private BlockEntityType<Entity> beType;

    public DecoratedSign(ModX mod, DecoratedBlock parent) {
        this.mod = mod;
        this.parent = parent;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void registerAdditional(RegistrationContext ctx, EntryCollector builder) {
        this.init(ctx.id());
        builder.register(Registry.BLOCK_REGISTRY, this.standing);
        builder.register(Registry.BLOCK_ENTITY_TYPE_REGISTRY, this.beType);
        builder.register(Registry.ITEM_REGISTRY, this.item);
        builder.registerNamed(Registry.BLOCK_REGISTRY, "wall", this.wall);
    }

    @Override
    public void registerCommon(SetupContext ctx) {
        this.init(ctx.id());
        ctx.enqueue(() -> WoodType.register(this.wood));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient(SetupContext ctx) {
        this.init(ctx.id());
        BlockEntityRenderers.register(this.beType, SignRenderer::new);
        ctx.enqueue(() -> Sheets.addWoodType(this.wood));
    }

    private synchronized void init(ResourceLocation id) {
        if (this.wood == null) {
            this.wood = WoodType.create(id.toString());
        }
        if (this.standing == null) {
            this.standing = new Standing(this.parent, this::getBlockEntityType, this.wood);
        }
        if (this.wall == null) {
            this.wall = new Wall(this.parent, this::getBlockEntityType, this.wood);
        }
        if (this.beType == null) {
            //noinspection ConstantConditions
            this.beType = new BlockEntityType<>((pos, state) -> new Entity(this.getBlockEntityType(), pos, state), Set.of(this.standing, this.wall), null);
        }
        if (this.item == null) {
            Item.Properties itemProperties = new Item.Properties().stacksTo(16);
            if (this.mod.tab != null) itemProperties.tab(this.mod.tab);
            this.item = new SignItem(itemProperties, this.standing, this.wall);
        }
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
