package org.moddingx.libx.coremods.transformers;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TargetType;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RegisterClassIds implements ITransformer<MethodNode> {

    private static final String HELPER = "org/moddingx/libx/annotation/impl/RegistrationPropertiesHelper";
    private static final String ITEM_PROPS = "net/minecraft/world/item/Item$Properties";
    private static final String BLOCK_PROPS = "net/minecraft/world/level/block/state/BlockBehaviour$Properties";

    private final Map<String, List<FieldEntry>> entries;

    public RegisterClassIds() {
        this.entries = RegisterClassIds.loadEntries();
    }

    // -------------------------------------------------------------------------
    // ITransformer implementation
    // -------------------------------------------------------------------------

    @Nonnull
    @Override
    public TargetType<MethodNode> getTargetType() {
        return TargetType.METHOD;
    }

    @Nonnull
    @Override
    public Set<Target<MethodNode>> targets() {
        Set<Target<MethodNode>> targets = new HashSet<>();
        for (String binaryClass : this.entries.keySet()) {
            targets.add(Target.targetMethod(
                    binaryClass.replace('/', '.'),
                    "<clinit>",
                    "()V"
            ));
        }

        return targets;
    }

    @Nonnull
    @Override
    public TransformerVoteResult castVote(ITransformerVotingContext ctx) {
        return TransformerVoteResult.YES;
    }

    @Nonnull
    @Override
    public MethodNode transform(MethodNode method, ITransformerVotingContext ctx) {
        // Determine which class this <clinit> belongs to from the voting context.
        String binaryName = ctx.getClassName().replace('.', '/');
        List<FieldEntry> fields = this.entries.get(binaryName);
        if (fields == null) {
            return method;
        }

        for (FieldEntry fe : fields) {
            RegisterClassIds.injectField(method, binaryName, fe);
        }

        return method;
    }

    private static void injectField(MethodNode method, String owner, FieldEntry fe) {
        InsnList insns = method.instructions;

        // Find the PUTSTATIC for this field.
        FieldInsnNode putStatic = null;
        for (int i = 0; i < insns.size(); i++) {
            AbstractInsnNode node = insns.get(i);
            if (node.getOpcode() == Opcodes.PUTSTATIC && node instanceof FieldInsnNode fin && fin.owner.equals(owner) && fin.name.equals(fe.fieldName())) {
                putStatic = fin;
                break;
            }
        }

        if (putStatic == null) { // field not initialised in <clinit>
            return;
        }

        // Walk backward from PUTSTATIC to find the INVOKESPECIAL <init> that constructs
        // the Item / Block just before it is stored. We look for the closest <init> call
        // whose descriptor ends with either ITEM_PROPS or BLOCK_PROPS (last argument).
        int putIdx = insns.indexOf(putStatic);
        for (int i = putIdx - 1; i >= 0; i--) {
            AbstractInsnNode node = insns.get(i);
            if (node.getOpcode() != Opcodes.INVOKESPECIAL) {
                continue;
            }

            if (!(node instanceof MethodInsnNode min)) {
                continue;
            }

            if (!"<init>".equals(min.name)) {
                continue;
            }

            // Determine which Properties types appear as constructor arguments.
            boolean lastIsItem = min.desc.contains("L" + ITEM_PROPS + ";)V");
            boolean lastIsBlock = min.desc.contains("L" + BLOCK_PROPS + ";)V");

            if (!lastIsItem && !lastIsBlock) {
                continue;
            }

            // ----------------------------------------------------------------
            // Determine injection(s) needed.
            // The Properties object of interest is always the TOP of the
            // operand stack just before INVOKESPECIAL executes.
            // ----------------------------------------------------------------

            boolean hasItemArg = min.desc.contains("L" + ITEM_PROPS + ";");
            boolean hasBlockArg = min.desc.contains("L" + BLOCK_PROPS + ";");
            boolean bothPresent = hasItemArg && hasBlockArg;

            InsnList injection = new InsnList();

            if (!bothPresent) {
                // Simple case: only one Properties type in the constructor.
                // TOP of stack is the Properties object.
                injection.add(new InsnNode(Opcodes.DUP));
                injection.add(new LdcInsnNode(fe.id()));
                if (lastIsItem) {
                    injection.add(new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            HELPER, "setItemId",
                            "(Lnet/minecraft/world/item/Item$Properties;Ljava/lang/String;)V"
                    ));
                } else {
                    injection.add(new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            HELPER, "setBlockId",
                            "(Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;Ljava/lang/String;)V"
                    ));
                }
            } else if (lastIsItem) {
                // Stack: [..., blockProps, itemProps]  (item on TOP, block second)
                // Set item id (TOP):  DUP, LDC, setItemId  → stack unchanged
                injection.add(new InsnNode(Opcodes.DUP));
                injection.add(new LdcInsnNode(fe.id()));
                injection.add(new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        HELPER, "setItemId",
                        "(Lnet/minecraft/world/item/Item$Properties;Ljava/lang/String;)V"
                ));
                // Set block id (second): SWAP, DUP, LDC, setBlockId, SWAP  → stack unchanged
                injection.add(new InsnNode(Opcodes.SWAP));
                injection.add(new InsnNode(Opcodes.DUP));
                injection.add(new LdcInsnNode(fe.id()));
                injection.add(new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        HELPER, "setBlockId",
                        "(Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;Ljava/lang/String;)V"
                ));
                injection.add(new InsnNode(Opcodes.SWAP));
            } else {
                // Stack: [..., itemProps, blockProps]  (block on TOP, item second)
                // Set block id (TOP):  DUP, LDC, setBlockId  → stack unchanged
                injection.add(new InsnNode(Opcodes.DUP));
                injection.add(new LdcInsnNode(fe.id()));
                injection.add(new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        HELPER, "setBlockId",
                        "(Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;Ljava/lang/String;)V"
                ));
                // Set item id (second): SWAP, DUP, LDC, setItemId, SWAP  → stack unchanged
                injection.add(new InsnNode(Opcodes.SWAP));
                injection.add(new InsnNode(Opcodes.DUP));
                injection.add(new LdcInsnNode(fe.id()));
                injection.add(new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        HELPER, "setItemId",
                        "(Lnet/minecraft/world/item/Item$Properties;Ljava/lang/String;)V"
                ));
                injection.add(new InsnNode(Opcodes.SWAP));
            }

            insns.insertBefore(node, injection);

            return; // one injection per field is enough
        }
    }

    private static Map<String, List<FieldEntry>> loadEntries() {
        Map<String, List<FieldEntry>> map = new HashMap<>();
        Set<String> seenUrls = new HashSet<>();

        // Try multiple classloaders in priority order.
        // ClassLoader.getSystemClassLoader() is the app classloader and does NOT see mod jars
        // in NeoForge's module-based setup. RegisterClassIds.class.getClassLoader() is the
        // coremod service classloader (a URLClassLoader wrapping all game jars) and is the
        // most reliable option.
        ClassLoader[] candidates = {
                RegisterClassIds.class.getClassLoader(),
                Thread.currentThread().getContextClassLoader(),
                ClassLoader.getSystemClassLoader()
        };

        for (ClassLoader cl : candidates) {
            if (cl == null) continue;
            try {
                Enumeration<URL> resources = cl.getResources("META-INF/libx_registration.json");
                while (resources.hasMoreElements()) {
                    URL url = resources.nextElement();
                    if (seenUrls.add(url.toString())) {
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                sb.append(line);
                            }

                            RegisterClassIds.parseJson(sb.toString(), map);
                        } catch (Exception e) {
                            System.err.println("[LibX] Failed to parse libx_registration.json from " + url + ": " + e);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("[LibX] Failed to enumerate libx_registration.json via " + cl + ": " + e);
            }
        }

        if (map.isEmpty()) {
            System.err.println("[LibX] WARNING: No libx_registration.json entries found — @RegisterClass ids will not be injected!");
        } else {
            System.out.println("[LibX] Loaded @RegisterClass id entries for " + map.size() + " class(es)");
        }

        return map;
    }

    // Expected format: {@code [{"class":"a/b/C","field":"f","id":"m:n"}, ...]}
    private static void parseJson(String json, Map<String, List<FieldEntry>> map) {
        // Strip outer array brackets
        json = json.trim();
        if (json.startsWith("[")) json = json.substring(1);
        if (json.endsWith("]")) json = json.substring(0, json.length() - 1);

        // Split on "},{" to get individual objects
        for (String obj : json.split("\\},\\s*\\{")) {
            obj = obj.replace("{", "").replace("}", "").trim();
            String cls = extractValue(obj, "class");
            String field = extractValue(obj, "field");
            String id = extractValue(obj, "id");
            if (cls == null || field == null || id == null) {
                continue;
            }

            map.computeIfAbsent(cls, k -> new ArrayList<>()).add(new FieldEntry(field, id));
        }
    }

    private static String extractValue(String obj, String key) {
        String search = "\"" + key + "\":\"";
        int start = obj.indexOf(search);
        if (start < 0) {
            return null;
        }

        start += search.length();
        int end = obj.indexOf('"', start);
        if (end < 0) {
            return null;
        }

        return obj.substring(start, end);
    }

    private record FieldEntry(String fieldName, String id) {}
}
