function initializeCoreMod() {
  return {
    'random_tick_block': {
      'target': {
        'type': 'METHOD',
        'class': 'net.minecraft.block.AbstractBlock$AbstractBlockState',
        'methodName': 'func_227034_b_',
        'methodDesc': '(Lnet/minecraft/world/server/ServerWorld;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)V'
      },
      'transformer': function(method) {
        var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
        var Opcodes = Java.type('org.objectweb.asm.Opcodes');
        var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
        var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
        var LabelNode = Java.type('org.objectweb.asm.tree.LabelNode');
        var JumpInsnNode = Java.type('org.objectweb.asm.tree.JumpInsnNode');
        var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
        
        var label = new LabelNode();
        var target = new InsnList();
        target.add(new VarInsnNode(Opcodes.ALOAD, 0));
        target.add(new VarInsnNode(Opcodes.ALOAD, 1));
        target.add(new VarInsnNode(Opcodes.ALOAD, 2));
        target.add(new VarInsnNode(Opcodes.ALOAD, 3));
        target.add(ASMAPI.buildMethodCall(
            'io/github/noeppi_noeppi/libx/impl/libxcore/CoreRandomTick',
            'processBlockTick', '(Lnet/minecraft/block/AbstractBlock$AbstractBlockState;Lnet/minecraft/world/server/ServerWorld;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)Z',
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
        'class': 'net.minecraft.fluid.FluidState',
        'methodName': 'func_206891_b',
        'methodDesc': '(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)V'
      },
      'transformer': function(method) {
        var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
        var Opcodes = Java.type('org.objectweb.asm.Opcodes');
        var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
        var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
        var LabelNode = Java.type('org.objectweb.asm.tree.LabelNode');
        var JumpInsnNode = Java.type('org.objectweb.asm.tree.JumpInsnNode');
        var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
        
        var label = new LabelNode();
        var target = new InsnList();
        target.add(new VarInsnNode(Opcodes.ALOAD, 0));
        target.add(new VarInsnNode(Opcodes.ALOAD, 1));
        target.add(new VarInsnNode(Opcodes.ALOAD, 2));
        target.add(new VarInsnNode(Opcodes.ALOAD, 3));
        target.add(ASMAPI.buildMethodCall(
            'io/github/noeppi_noeppi/libx/impl/libxcore/CoreRandomTick',
            'processFluidTick', '(Lnet/minecraft/fluid/FluidState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)Z',
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