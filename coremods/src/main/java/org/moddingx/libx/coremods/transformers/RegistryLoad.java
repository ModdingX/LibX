package org.moddingx.libx.coremods.transformers;

import net.neoforged.neoforgespi.transformation.ProcessorName;
import net.neoforged.neoforgespi.transformation.SimpleClassProcessor;
import net.neoforged.neoforgespi.transformation.SimpleTransformationContext;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Set;

public class RegistryLoad extends SimpleClassProcessor {

    private static final String LAYERED_ACCESS = "net/minecraft/core/LayeredRegistryAccess";
    private static final String REPLACE_FROM = "replaceFrom";
    private static final String REPLACE_FROM_DESC = "(Ljava/lang/Object;[Lnet/minecraft/core/RegistryAccess$Frozen;)Lnet/minecraft/core/LayeredRegistryAccess;";
    private static final String REGISTRY_LAYER = "net/minecraft/server/RegistryLayer";
    private static final String WORLDGEN_FIELD = "WORLDGEN";

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
        // Registry loading in WorldLoader#load is asynchronous, so the layer replacement for the worldgen
        // layer does not live in the load method itself but in one of the lambda bodies it creates.
        // Therefore every method of the class is searched for the anchor instead of only the load method.
        for (MethodNode mn : cn.methods) {
            if (RegistryLoad.patch(mn)) {
                return;
            }
        }
        throw new IllegalStateException("Failed to patch WorldLoader.class: worldgen layer replacement not found");
    }

    private static boolean patch(MethodNode mn) {
        boolean foundWorldGenField = false;
        for (int i = 0; i < mn.instructions.size(); i++) {
            AbstractInsnNode node = mn.instructions.get(i);
            if (node.getOpcode() == Opcodes.GETSTATIC && node instanceof FieldInsnNode fieldInsnNode) {
                if (fieldInsnNode.owner.equals(REGISTRY_LAYER) && fieldInsnNode.name.equals(WORLDGEN_FIELD)) {
                    foundWorldGenField = true;
                }
            } else if (node.getOpcode() == Opcodes.INVOKEVIRTUAL && foundWorldGenField && node instanceof MethodInsnNode methodInsnNode) {
                if (methodInsnNode.owner.equals(LAYERED_ACCESS)
                        && methodInsnNode.name.equals(REPLACE_FROM)
                        && methodInsnNode.desc.equals(REPLACE_FROM_DESC)) {

                    InsnList injection = new InsnList();
                    injection.add(new InsnNode(Opcodes.DUP));
                    injection.add(new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "org/moddingx/libx/impl/libxcore/CoreRegistryLoad",
                            "afterWorldGenLayerLoad",
                            "(Lnet/minecraft/core/LayeredRegistryAccess;)V",
                            false
                    ));
                    mn.instructions.insert(node, injection);
                    return true;
                }
            }
        }
        return false;
    }
}
