"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var coremods_1 = require("coremods");
function initializeCoreMod() {
    return {
        'random_tick_block': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.level.block.state.BlockBehaviour$BlockStateBase',
                'methodName': 'm_60735_',
                'methodDesc': '(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Ljava/util/Random;)V'
            },
            'transformer': function (method) {
                var label = new coremods_1.LabelNode();
                var target = new coremods_1.InsnList();
                target.add(new coremods_1.VarInsnNode(coremods_1.Opcodes.ALOAD, 0));
                target.add(new coremods_1.VarInsnNode(coremods_1.Opcodes.ALOAD, 1));
                target.add(new coremods_1.VarInsnNode(coremods_1.Opcodes.ALOAD, 2));
                target.add(new coremods_1.VarInsnNode(coremods_1.Opcodes.ALOAD, 3));
                target.add(coremods_1.ASMAPI.buildMethodCall('io/github/noeppi_noeppi/libx/impl/libxcore/CoreRandomTick', 'processBlockTick', '(Lnet/minecraft/world/level/block/state/BlockBehaviour$BlockStateBase;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Ljava/util/Random;)Z', coremods_1.ASMAPI.MethodType.STATIC));
                target.add(new coremods_1.JumpInsnNode(coremods_1.Opcodes.IFEQ, label));
                target.add(new coremods_1.InsnNode(coremods_1.Opcodes.RETURN));
                target.add(label);
                method.instructions.insert(target);
                return method;
            }
        },
        'random_tick_fluid': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.level.material.FluidState',
                'methodName': 'm_76174_',
                'methodDesc': '(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Ljava/util/Random;)V'
            },
            'transformer': function (method) {
                var label = new coremods_1.LabelNode();
                var target = new coremods_1.InsnList();
                target.add(new coremods_1.VarInsnNode(coremods_1.Opcodes.ALOAD, 0));
                target.add(new coremods_1.VarInsnNode(coremods_1.Opcodes.ALOAD, 1));
                target.add(new coremods_1.VarInsnNode(coremods_1.Opcodes.ALOAD, 2));
                target.add(new coremods_1.VarInsnNode(coremods_1.Opcodes.ALOAD, 3));
                target.add(coremods_1.ASMAPI.buildMethodCall('io/github/noeppi_noeppi/libx/impl/libxcore/CoreRandomTick', 'processFluidTick', '(Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Ljava/util/Random;)Z', coremods_1.ASMAPI.MethodType.STATIC));
                target.add(new coremods_1.JumpInsnNode(coremods_1.Opcodes.IFEQ, label));
                target.add(new coremods_1.InsnNode(coremods_1.Opcodes.RETURN));
                target.add(label);
                method.instructions.insert(target);
                return method;
            }
        }
    };
}
