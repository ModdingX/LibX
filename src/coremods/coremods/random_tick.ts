import {
  ASMAPI,
  CoreMods,
  InsnList,
  InsnNode,
  JumpInsnNode,
  LabelNode,
  MethodNode,
  Opcodes,
  VarInsnNode
} from "coremods";

function initializeCoreMod(): CoreMods {
  return {
    'random_tick_block': {
      'target': {
        'type': 'METHOD',
        'class': 'net.minecraft.world.level.block.state.BlockBehaviour$BlockStateBase',
        'methodName': 'm_60735_',
        'methodDesc': '(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Ljava/util/Random;)V'
      },
      'transformer': function(method: MethodNode) {
        const label = new LabelNode();
        const target = new InsnList();
        target.add(new VarInsnNode(Opcodes.ALOAD, 0));
        target.add(new VarInsnNode(Opcodes.ALOAD, 1));
        target.add(new VarInsnNode(Opcodes.ALOAD, 2));
        target.add(new VarInsnNode(Opcodes.ALOAD, 3));
        target.add(ASMAPI.buildMethodCall(
            'io/github/noeppi_noeppi/libx/impl/libxcore/CoreRandomTick',
            'processBlockTick', '(Lnet/minecraft/world/level/block/state/BlockBehaviour$BlockStateBase;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Ljava/util/Random;)Z',
            ASMAPI.MethodType.STATIC
        ));
        target.add(new JumpInsnNode(Opcodes.IFEQ, label));
        target.add(new InsnNode(Opcodes.RETURN));
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
      'transformer': function(method: MethodNode) {
        const label = new LabelNode();
        const target = new InsnList();
        target.add(new VarInsnNode(Opcodes.ALOAD, 0));
        target.add(new VarInsnNode(Opcodes.ALOAD, 1));
        target.add(new VarInsnNode(Opcodes.ALOAD, 2));
        target.add(new VarInsnNode(Opcodes.ALOAD, 3));
        target.add(ASMAPI.buildMethodCall(
            'io/github/noeppi_noeppi/libx/impl/libxcore/CoreRandomTick',
            'processFluidTick', '(Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Ljava/util/Random;)Z',
            ASMAPI.MethodType.STATIC
        ));
        target.add(new JumpInsnNode(Opcodes.IFEQ, label));
        target.add(new InsnNode(Opcodes.RETURN));
        target.add(label);
        
        method.instructions.insert(target);
        
        return method;
      }
    }
  }
}
