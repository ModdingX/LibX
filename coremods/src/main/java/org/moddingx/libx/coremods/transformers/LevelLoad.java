package org.moddingx.libx.coremods.transformers;

import net.neoforged.neoforgespi.transformation.ProcessorName;
import net.neoforged.neoforgespi.transformation.SimpleClassProcessor;
import net.neoforged.neoforgespi.transformation.SimpleTransformationContext;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Set;

public class LevelLoad extends SimpleClassProcessor {

    @Override
    public ProcessorName name() {
        return new ProcessorName("libx", "level_load");
    }

    @Override
    public Set<Target> targets() {
        return Set.of(new Target("net.minecraft.server.level.ServerLevel"));
    }

    @Override
    public void transform(ClassNode cn, SimpleTransformationContext ctx) {
        for (MethodNode mn : cn.methods) {
            if (!"<init>".equals(mn.name)) continue;

            for (int i = 0; i < mn.instructions.size(); i++) {
                AbstractInsnNode node = mn.instructions.get(i);
                if (node.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                    MethodInsnNode methodInsnNode = (MethodInsnNode) node;
                    if (methodInsnNode.owner.equals("net/minecraft/world/level/dimension/LevelStem")
                            && methodInsnNode.name.equals("generator")
                            && methodInsnNode.desc.equals("()Lnet/minecraft/world/level/chunk/ChunkGenerator;")) {

                        InsnList injection = new InsnList();
                        injection.add(new InsnNode(Opcodes.DUP));
                        injection.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        injection.add(new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                "org/moddingx/libx/impl/libxcore/CoreLevelLoad",
                                "startLevelLoad",
                                "(Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/server/MinecraftServer;)V",
                                false
                        ));
                        mn.instructions.insert(node, injection);
                        return;
                    }
                }
            }
        }
        throw new IllegalStateException("Failed to patch ServerLevel.class");
    }
}
