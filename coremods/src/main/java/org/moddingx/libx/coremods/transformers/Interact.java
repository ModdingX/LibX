package org.moddingx.libx.coremods.transformers;

import net.neoforged.neoforgespi.transformation.ProcessorName;
import net.neoforged.neoforgespi.transformation.SimpleClassProcessor;
import net.neoforged.neoforgespi.transformation.SimpleTransformationContext;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Set;

public class Interact extends SimpleClassProcessor {

    private static final String TARGET_METHOD = "useItemOn";
    private static final String TARGET_DESC = "(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;";

    @Override
    public ProcessorName name() {
        return new ProcessorName("libx", "interact");
    }

    @Override
    public Set<Target> targets() {
        return Set.of(new Target("net.minecraft.server.level.ServerPlayerGameMode"));
    }

    @Override
    public void transform(ClassNode cn, SimpleTransformationContext ctx) {
        for (MethodNode mn : cn.methods) {
            if (TARGET_METHOD.equals(mn.name) && TARGET_DESC.equals(mn.desc)) {
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
                        TARGET_DESC,
                        false
                ));
                target.add(new InsnNode(Opcodes.DUP));
                target.add(new JumpInsnNode(Opcodes.IFNULL, label));
                target.add(new InsnNode(Opcodes.ARETURN));
                target.add(label);
                target.add(new InsnNode(Opcodes.POP));

                for (int i = mn.instructions.size() - 1; i >= 0; i--) {
                    AbstractInsnNode inst = mn.instructions.get(i);
                    if (inst != null && inst.getOpcode() == Opcodes.ARETURN) {
                        mn.instructions.insertBefore(inst, target);
                        return;
                    }
                }

                throw new IllegalStateException("Failed to patch ServerPlayerGameMode.class");
            }
        }
        throw new IllegalStateException("Failed to patch ServerPlayerGameMode.class: method not found");
    }
}
