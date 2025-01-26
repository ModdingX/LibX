function initializeCoreMod() {
    var ASMAPI = Java.type('net.neoforged.coremod.api.ASMAPI');
    var Opcodes = Java.type('org.objectweb.asm.Opcodes');
    var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
    var LabelNode = Java.type('org.objectweb.asm.tree.LabelNode');
    var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
    var JumpInsnNode = Java.type('org.objectweb.asm.tree.JumpInsnNode');
    var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
    
    return {
        'interact': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.server.level.ServerPlayerGameMode',
                'methodName': 'useItemOn',
                'methodDesc': '(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;'
            },
            'transformer': function (method) {
                var label = new LabelNode();
                var target = new InsnList();
                target.add(new VarInsnNode(Opcodes.ALOAD, 1));
                target.add(new VarInsnNode(Opcodes.ALOAD, 2));
                target.add(new VarInsnNode(Opcodes.ALOAD, 3));
                target.add(new VarInsnNode(Opcodes.ALOAD, 4));
                target.add(new VarInsnNode(Opcodes.ALOAD, 5));
                target.add(ASMAPI.buildMethodCall(
                    'org/moddingx/libx/impl/libxcore/CoreInteract', 'useItemOn',
                    '(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;',
                    ASMAPI.MethodType.STATIC
                ));
                target.add(new InsnNode(Opcodes.DUP));
                target.add(new JumpInsnNode(Opcodes.IFNULL, label));
                target.add(new InsnNode(Opcodes.ARETURN));
                target.add(label);
                target.add(new InsnNode(Opcodes.POP));
                for (var i = method.instructions.size() - 1; i >= 0; i--) {
                    var inst = method.instructions.get(i);
                    if (inst != null && inst.getOpcode() == Opcodes.ARETURN) {
                        method.instructions.insertBefore(inst, target);
                        return method;
                    }
                }
                throw new Error("Failed to patch ServerPlayerGameMode.class");
            }
        }
    };
}
