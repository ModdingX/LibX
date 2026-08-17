package org.moddingx.libx.coremods.transformers;

import net.neoforged.neoforgespi.transformation.ProcessorName;
import net.neoforged.neoforgespi.transformation.SimpleClassProcessor;
import net.neoforged.neoforgespi.transformation.SimpleTransformationContext;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Set;

public class TagLoad extends SimpleClassProcessor {

    private static final String RESOURCE_MANAGER = "Lnet/minecraft/server/packs/resources/ResourceManager;";

    @Override
    public ProcessorName name() {
        return new ProcessorName("libx", "tag_load");
    }

    @Override
    public Set<Target> targets() {
        return Set.of(new Target("net.minecraft.tags.TagLoader"));
    }

    @Override
    public void transform(ClassNode cn, SimpleTransformationContext ctx) {
        // TagLoader#loadTagsForRegistry is the single point where the tags of a registry that is currently being
        // constructed are loaded. The raw tag entries it reads are passed to a LibX hook, which may turn them into
        // optional entries when the resource manager marks a LibX datagen registry bootstrap.
        for (MethodNode mn : cn.methods) {
            if (mn.name.equals("loadTagsForRegistry") && mn.desc.equals("(" + RESOURCE_MANAGER + "Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/tags/TagLoader$ElementLookup;)Ljava/util/Map;") && TagLoad.patch(mn)) {
                return;
            }
        }
        throw new IllegalStateException("Failed to patch TagLoader.class: tag entry loading in loadTagsForRegistry not found");
    }

    private static boolean patch(MethodNode mn) {
        for (int i = 0; i < mn.instructions.size(); i++) {
            AbstractInsnNode node = mn.instructions.get(i);
            if (node.getOpcode() == Opcodes.INVOKEVIRTUAL && node instanceof MethodInsnNode methodInsnNode) {
                if (methodInsnNode.owner.equals("net/minecraft/tags/TagLoader")
                        && methodInsnNode.name.equals("load")
                        && methodInsnNode.desc.equals("(" + RESOURCE_MANAGER + ")Ljava/util/Map;")) {

                    InsnList injection = new InsnList();
                    injection.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    injection.add(new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "org/moddingx/libx/impl/libxcore/CoreTagLoad",
                            "processRegistryTagEntries",
                            "(Ljava/util/Map;" + RESOURCE_MANAGER + ")Ljava/util/Map;",
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
