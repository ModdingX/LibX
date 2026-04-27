package org.moddingx.libx.coremods.transformers;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TargetType;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.Set;

public class Interact implements ITransformer<MethodNode> {

    @Nonnull
    @Override
    public MethodNode transform(MethodNode methodNode, ITransformerVotingContext iTransformerVotingContext) {
        LabelNode label = new LabelNode();
        InsnList target = new InsnList();

        target.add(new VarInsnNode(Opcodes.ALOAD, 1));
        target.add(new VarInsnNode(Opcodes.ALOAD, 2));
        target.add(new VarInsnNode(Opcodes.ALOAD, 3));
        target.add(new VarInsnNode(Opcodes.ALOAD, 4));
        target.add(new VarInsnNode(Opcodes.ALOAD, 5));
        target.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "org/moddingx/libx/impl/libxcore/CoreInteract",
                "useItemOn",
                "(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;",
                false
        ));
        target.add(new InsnNode(Opcodes.DUP));
        target.add(new JumpInsnNode(Opcodes.IFNULL, label));
        target.add(new InsnNode(Opcodes.ARETURN));
        target.add(label);
        target.add(new InsnNode(Opcodes.POP));

        for (int i = methodNode.instructions.size() - 1; i >= 0; i--) {
            AbstractInsnNode inst = methodNode.instructions.get(i);
            if (inst != null && inst.getOpcode() == Opcodes.ARETURN) {
                methodNode.instructions.insertBefore(inst, target);
                return methodNode;
            }
        }

        throw new IllegalStateException("Failed to patch ServerPlayerGameMode.class");
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
                        "net.minecraft.server.level.ServerPlayerGameMode",
                        "useItemOn",
                        "(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;"
                )
        );
    }

    @Nonnull
    @Override
    public TargetType<MethodNode> getTargetType() {
        return TargetType.METHOD;
    }
}
