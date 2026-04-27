package org.moddingx.libx.coremods.transformers;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TargetType;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.Set;

public class LevelLoad implements ITransformer<MethodNode> {

    @Nonnull
    @Override
    public MethodNode transform(MethodNode methodNode, ITransformerVotingContext iTransformerVotingContext) {
        InsnList target = new InsnList();

        target.add(new InsnNode(Opcodes.DUP));
        target.add(new VarInsnNode(Opcodes.ALOAD, 1));
        target.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "org/moddingx/libx/impl/libxcore/CoreLevelLoad",
                "startLevelLoad",
                "(Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/server/MinecraftServer;)V",
                false
        ));
        for (int i = 0; i < methodNode.instructions.size(); i++) {
            AbstractInsnNode node = methodNode.instructions.get(i);
            if (node.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                MethodInsnNode methodInsnNode = (MethodInsnNode) node;
                if (methodInsnNode.owner.equals("net/minecraft/world/level/dimension/LevelStem") && methodInsnNode.name.equals("generator") && methodInsnNode.desc.equals("()Lnet/minecraft/world/level/chunk/ChunkGenerator;")) {
                    methodNode.instructions.insert(node, target);
                    return methodNode;
                }
            }
        }


        throw new IllegalStateException("Failed to patch ServerLevel.class");
    }

    @Nonnull
    @Override
    public TransformerVoteResult castVote(ITransformerVotingContext iTransformerVotingContext) {
        return TransformerVoteResult.YES;
    }

    @Nonnull
    @Override
    public Set<Target<MethodNode>> targets() {
        return Set.of(
                Target.targetMethod(
                        "net.minecraft.server.level.ServerLevel",
                        "<init>",
                        "(Lnet/minecraft/server/MinecraftServer;Ljava/util/concurrent/Executor;Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;Lnet/minecraft/world/level/storage/ServerLevelData;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/level/dimension/LevelStem;Lnet/minecraft/server/level/progress/ChunkProgressListener;ZJLjava/util/List;ZLnet/minecraft/world/RandomSequences;)V"
                )
        );
    }

    @Nonnull
    @Override
    public TargetType<MethodNode> getTargetType() {
        return TargetType.METHOD;
    }
}
