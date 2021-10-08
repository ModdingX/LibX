package io.github.noeppi_noeppi.libx.impl.base.decoration.blocks;

import io.github.noeppi_noeppi.libx.base.decoration.DecoratedBlock;
import io.github.noeppi_noeppi.libx.base.decoration.SignAccess;
import io.github.noeppi_noeppi.libx.mod.ModX;
import io.github.noeppi_noeppi.libx.mod.registration.Registerable;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.core.BlockPos;
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

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
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
    public Set<Object> getAdditionalRegisters(ResourceLocation id) {
        this.init(id);
        return Set.of(this.standing, this.beType, this.item);
    }

    @Override
    public Map<String, Object> getNamedAdditionalRegisters(ResourceLocation id) {
        this.init(id);
        return Map.of("wall", this.wall);
    }

    @Override
    public void registerCommon(ResourceLocation id, Consumer<Runnable> defer) {
        this.init(id);
        defer.accept(() -> WoodType.register(this.wood));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient(ResourceLocation id, Consumer<Runnable> defer) {
        this.init(id);
        defer.accept(() -> Sheets.addWoodType(this.wood));
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
