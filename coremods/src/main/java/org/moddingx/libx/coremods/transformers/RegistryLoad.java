package org.moddingx.libx.coremods.transformers;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TargetType;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.Set;

public class RegistryLoad implements ITransformer<MethodNode> {

    private static final int FIELD_INDEX = 2;

    @Nonnull
    @Override
    public MethodNode transform(MethodNode methodNode, ITransformerVotingContext iTransformerVotingContext) {
        InsnList target = new InsnList();

        target.add(new InsnNode(Opcodes.DUP));
        target.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "org/moddingx/libx/impl/libxcore/CoreRegistryLoad",
                "afterWorldGenLayerLoad",
                "(Lnet/minecraft/core/LayeredRegistryAccess;)V",
                false
        ));

        int foundWorldGenFieldCounter = 0;
        for (int i = 0; i < methodNode.instructions.size(); i++) {
            AbstractInsnNode node = methodNode.instructions.get(i);
            if (node.getOpcode() == Opcodes.GETSTATIC && foundWorldGenFieldCounter < FIELD_INDEX) {
                FieldInsnNode fieldInsnNode = (FieldInsnNode) node;
                if (fieldInsnNode.owner.equals("net/minecraft/server/RegistryLayer") && fieldInsnNode.name.equals("WORLDGEN")) {
                    foundWorldGenFieldCounter++;
                }
            } else if (node.getOpcode() == Opcodes.INVOKEVIRTUAL && foundWorldGenFieldCounter == FIELD_INDEX) {
                MethodInsnNode methodInsnNode = (MethodInsnNode) node;
                if (methodInsnNode.owner.equals("net/minecraft/core/LayeredRegistryAccess") && methodInsnNode.name.equals("replaceFrom") && methodInsnNode.desc.equals("(Ljava/lang/Object;[Lnet/minecraft/core/RegistryAccess$Frozen;)Lnet/minecraft/core/LayeredRegistryAccess;")) {
                    methodNode.instructions.insert(node, target);
                    return methodNode;
                }
            }
        }

        throw new IllegalStateException("Failed to patch WorldLoader.class");
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
                        "net.minecraft.server.WorldLoader",
                        "load",
                        "(Lnet/minecraft/server/WorldLoader$InitConfig;Lnet/minecraft/server/WorldLoader$WorldDataSupplier;Lnet/minecraft/server/WorldLoader$ResultFactory;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"
                )
        );
    }

    @Nonnull
    @Override
    public TargetType<MethodNode> getTargetType() {
        return TargetType.METHOD;
    }
}
