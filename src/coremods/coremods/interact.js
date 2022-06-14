"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var coremods_1 = require("coremods");
function initializeCoreMod() {
    return {
        'interact': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.server.level.ServerPlayerGameMode',
                'methodName': 'm_7179_',
                'methodDesc': '(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;'
            },
            'transformer': function (method) {
                var label = new coremods_1.LabelNode();
                var target = new coremods_1.InsnList();
                target.add(new coremods_1.VarInsnNode(coremods_1.Opcodes.ALOAD, 1));
                target.add(new coremods_1.VarInsnNode(coremods_1.Opcodes.ALOAD, 2));
                target.add(new coremods_1.VarInsnNode(coremods_1.Opcodes.ALOAD, 3));
                target.add(new coremods_1.VarInsnNode(coremods_1.Opcodes.ALOAD, 4));
                target.add(new coremods_1.VarInsnNode(coremods_1.Opcodes.ALOAD, 5));
                target.add(coremods_1.ASMAPI.buildMethodCall('org/moddingx/libx/impl/libxcore/CoreInteract', 'useItemOn', '(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;', coremods_1.ASMAPI.MethodType.STATIC));
                target.add(new coremods_1.InsnNode(coremods_1.Opcodes.DUP));
                target.add(new coremods_1.JumpInsnNode(coremods_1.Opcodes.IFNULL, label));
                target.add(new coremods_1.InsnNode(coremods_1.Opcodes.ARETURN));
                target.add(label);
                target.add(new coremods_1.InsnNode(coremods_1.Opcodes.POP));
                for (var i = method.instructions.size() - 1; i >= 0; i--) {
                    var inst = method.instructions.get(i);
                    if (inst != null && inst.getOpcode() == coremods_1.Opcodes.ARETURN) {
                        method.instructions.insertBefore(inst, target);
                        return method;
                    }
                }
                throw new Error("Failed to patch ServerPlayerGameMode.class");
            }
        }
    };
}
