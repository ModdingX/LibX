function initializeCoreMod() {
    var ASMAPI = Java.type('net.neoforged.coremod.api.ASMAPI');
    var Opcodes = Java.type('org.objectweb.asm.Opcodes');
    var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
    var LabelNode = Java.type('org.objectweb.asm.tree.LabelNode');
    var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
    var JumpInsnNode = Java.type('org.objectweb.asm.tree.JumpInsnNode');
    var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
    
    return {
        'holder_serialize': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.core.Holder$Reference',
                'methodName': 'canSerializeIn',
                'methodDesc': '(Lnet/minecraft/core/HolderOwner;)Z'
            },
            'transformer': function (method) {
                var label = new LabelNode();
                var target = new InsnList();
                target.add(new VarInsnNode(Opcodes.ALOAD, 0));
                target.add(new VarInsnNode(Opcodes.ALOAD, 1));
                target.add(ASMAPI.buildMethodCall(
                    'org/moddingx/libx/impl/libxcore/CoreHolderSerialize', 'forceSerializeIn',
                    '(Lnet/minecraft/core/Holder$Reference;Lnet/minecraft/core/HolderOwner;)Z',
                    ASMAPI.MethodType.STATIC
                ));
                target.add(new JumpInsnNode(Opcodes.IFEQ, label));
                target.add(new InsnNode(Opcodes.ICONST_1));
                target.add(new InsnNode(Opcodes.IRETURN));
                target.add(label);
                method.instructions.insert(target);
                return method;
            }
        }
    };
}
