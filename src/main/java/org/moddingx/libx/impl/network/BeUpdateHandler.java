package org.moddingx.libx.impl.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.TagValueInput;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.moddingx.libx.LibX;
import org.moddingx.libx.codec.MoreStreamCodecs;
import org.moddingx.libx.network.PacketHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BeUpdateHandler extends PacketHandler<BeUpdateHandler.Message> {

    public static final CustomPacketPayload.Type<Message> TYPE = new CustomPacketPayload.Type<>(LibX.getInstance().resource("be_update"));

    protected BeUpdateHandler() {
        super(TYPE, PacketFlow.CLIENTBOUND, StreamCodec.composite(
                BlockPos.STREAM_CODEC, Message::pos,
                Identifier.STREAM_CODEC, Message::id,
                MoreStreamCodecs.COMPOUND_TAG, Message::nbt,
                Message::new
        ), HandlerThread.MAIN);
    }

    @Override
    public void handle(Message msg, IPayloadContext ctx) {
        Level level = ClientCallbacks.getClientLevel();
        if (level != null) {
            BlockEntity be = level.getBlockEntity(msg.pos());
            if (be != null && msg.id().equals(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(be.getType()))) {
                be.handleUpdateTag(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), msg.nbt()));
            }
        }
    }

    private static class ClientCallbacks {

        @Nullable
        public static Level getClientLevel() {
            return Minecraft.getInstance().level;
        }
    }

    public record Message(BlockPos pos, Identifier id, CompoundTag nbt) implements CustomPacketPayload {

        @Nonnull
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return BeUpdateHandler.TYPE;
        }
    }
}
