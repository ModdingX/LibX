function initializeCoreMod() {
  return {
    'interact': {
      'target': {
        'type': 'METHOD',
        'class': 'net.minecraft.server.management.PlayerInteractionManager',
        'methodName': 'func_219441_a',
        'methodDesc': '(Lnet/minecraft/entity/player/ServerPlayerEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/Hand;Lnet/minecraft/util/math/BlockRayTraceResult;)Lnet/minecraft/util/ActionResultType;'
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
        target.add(new VarInsnNode(Opcodes.ALOAD, 1));
        target.add(new VarInsnNode(Opcodes.ALOAD, 2));
        target.add(new VarInsnNode(Opcodes.ALOAD, 3));
        target.add(new VarInsnNode(Opcodes.ALOAD, 4));
        target.add(new VarInsnNode(Opcodes.ALOAD, 5));
        target.add(ASMAPI.buildMethodCall(
            'io/github/noeppi_noeppi/libx/impl/libxcore/CoreInteract',
            'processItemUsage', '(Lnet/minecraft/entity/player/ServerPlayerEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/Hand;Lnet/minecraft/util/math/BlockRayTraceResult;)Lnet/minecraft/util/ActionResultType;',
            ASMAPI.MethodType.STATIC
        ));
        target.add(new InsnNode(Opcodes.DUP));
        target.add(new JumpInsnNode(Opcodes.IFNULL, label));
        target.add(new InsnNode(Opcodes.ARETURN));
        target.add(label);
        target.add(new InsnNode(Opcodes.POP));
        
        var hasReturn = false;
        for (var i = method.instructions.size() - 1; i >= 0; i--) {
          var inst = method.instructions.get(i);
          if (!hasReturn && inst.getOpcode() == Opcodes.ARETURN) {
            hasReturn = true;
          } else if (hasReturn && inst.getOpcode() == Opcodes.GETSTATIC
              && inst.owner == 'net/minecraft/util/ActionResultType') {
            // Need to insert before the getstatic
            method.instructions.insertBefore(inst, target)
            return method;
          }
        }
        throw new Error("Failed to patch PlayerInteractionManager.class");
      }
    }
  }
}
