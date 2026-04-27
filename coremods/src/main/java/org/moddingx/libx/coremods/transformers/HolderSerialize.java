package org.moddingx.libx.coremods.transformers;

import net.neoforged.neoforgespi.transformation.ProcessorName;
import net.neoforged.neoforgespi.transformation.SimpleClassProcessor;
import net.neoforged.neoforgespi.transformation.SimpleTransformationContext;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Set;

public class HolderSerialize extends SimpleClassProcessor {

    @Override
    public ProcessorName name() {
        return new ProcessorName("libx", "holder_serialize");
    }

    @Override
    public Set<Target> targets() {
        return Set.of(new Target("net.minecraft.core.Holder$Reference"));
    }

    @Override
    public void transform(ClassNode cn, SimpleTransformationContext ctx) {
        for (MethodNode mn : cn.methods) {
            if ("canSerializeIn".equals(mn.name) && "(Lnet/minecraft/core/HolderOwner;)Z".equals(mn.desc)) {
                LabelNode label = new LabelNode();
                InsnList target = new InsnList();

                target.add(new VarInsnNode(Opcodes.ALOAD, 0));
                target.add(new VarInsnNode(Opcodes.ALOAD, 1));
                target.add(new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        "org/moddingx/libx/impl/libxcore/CoreHolderSerialize",
                        "forceSerializeIn",
                        "(Lnet/minecraft/core/Holder$Reference;Lnet/minecraft/core/HolderOwner;)Z",
                        false
                ));
                target.add(new JumpInsnNode(Opcodes.IFEQ, label));
                target.add(new InsnNode(Opcodes.ICONST_1));
                target.add(new InsnNode(Opcodes.IRETURN));
                target.add(label);

                mn.instructions.insert(target);
                return;
            }
        }
        throw new IllegalStateException("Failed to patch Holder$Reference.class");
    }
}
