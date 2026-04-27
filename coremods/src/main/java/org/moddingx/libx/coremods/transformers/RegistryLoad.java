package org.moddingx.libx.coremods.transformers;

import net.neoforged.neoforgespi.transformation.ProcessorName;
import net.neoforged.neoforgespi.transformation.SimpleClassProcessor;
import net.neoforged.neoforgespi.transformation.SimpleTransformationContext;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Set;

public class RegistryLoad extends SimpleClassProcessor {

    private static final int FIELD_INDEX = 2;
    private static final String TARGET_METHOD = "load";
    private static final String TARGET_DESC = "(Lnet/minecraft/server/WorldLoader$InitConfig;Lnet/minecraft/server/WorldLoader$WorldDataSupplier;Lnet/minecraft/server/WorldLoader$ResultFactory;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;";

    @Override
    public ProcessorName name() {
        return new ProcessorName("libx", "registry_load");
    }

    @Override
    public Set<Target> targets() {
        return Set.of(new Target("net.minecraft.server.WorldLoader"));
    }

    @Override
    public void transform(ClassNode cn, SimpleTransformationContext ctx) {
        for (MethodNode mn : cn.methods) {
            if (!TARGET_METHOD.equals(mn.name) || !TARGET_DESC.equals(mn.desc)) continue;

            InsnList injection = new InsnList();
            injection.add(new InsnNode(Opcodes.DUP));
            injection.add(new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "org/moddingx/libx/impl/libxcore/CoreRegistryLoad",
                    "afterWorldGenLayerLoad",
                    "(Lnet/minecraft/core/LayeredRegistryAccess;)V",
                    false
            ));

            int foundWorldGenFieldCounter = 0;
            for (int i = 0; i < mn.instructions.size(); i++) {
                AbstractInsnNode node = mn.instructions.get(i);
                if (node.getOpcode() == Opcodes.GETSTATIC && foundWorldGenFieldCounter < FIELD_INDEX) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) node;
                    if (fieldInsnNode.owner.equals("net/minecraft/server/RegistryLayer") && fieldInsnNode.name.equals("WORLDGEN")) {
                        foundWorldGenFieldCounter++;
                    }
                } else if (node.getOpcode() == Opcodes.INVOKEVIRTUAL && foundWorldGenFieldCounter == FIELD_INDEX) {
                    MethodInsnNode methodInsnNode = (MethodInsnNode) node;
                    if (methodInsnNode.owner.equals("net/minecraft/core/LayeredRegistryAccess")
                            && methodInsnNode.name.equals("replaceFrom")
                            && methodInsnNode.desc.equals("(Ljava/lang/Object;[Lnet/minecraft/core/RegistryAccess$Frozen;)Lnet/minecraft/core/LayeredRegistryAccess;")) {
                        mn.instructions.insert(node, injection);
                        return;
                    }
                }
            }
            throw new IllegalStateException("Failed to patch WorldLoader.class");
        }
        throw new IllegalStateException("Failed to patch WorldLoader.class: method not found");
    }
}
