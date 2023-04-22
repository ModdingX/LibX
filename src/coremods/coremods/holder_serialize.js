"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var coremods_1 = require("coremods");
function initializeCoreMod() {
    return {
        'holder_serialize': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.core.Holder$Reference',
                'methodName': 'm_203401_',
                'methodDesc': '(Lnet/minecraft/core/HolderOwner;)Z'
            },
            'transformer': function (method) {
                var label = new coremods_1.LabelNode();
                var target = new coremods_1.InsnList();
                target.add(new coremods_1.VarInsnNode(coremods_1.Opcodes.ALOAD, 0));
                target.add(new coremods_1.VarInsnNode(coremods_1.Opcodes.ALOAD, 1));
                target.add(coremods_1.ASMAPI.buildMethodCall('org/moddingx/libx/impl/libxcore/CoreHolderSerialize', 'forceSerializeIn', '(Lnet/minecraft/core/Holder$Reference;Lnet/minecraft/core/HolderOwner;)Z', coremods_1.ASMAPI.MethodType.STATIC));
                target.add(new coremods_1.JumpInsnNode(coremods_1.Opcodes.IFEQ, label));
                target.add(new coremods_1.InsnNode(coremods_1.Opcodes.ICONST_1));
                target.add(new coremods_1.InsnNode(coremods_1.Opcodes.IRETURN));
                target.add(label);
                method.instructions.insert(target);
                return method;
            }
        }
    };
}
