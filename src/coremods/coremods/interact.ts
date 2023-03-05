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
    'interact': {
      'target': {
        'type': 'METHOD',
        'class': 'net.minecraft.server.level.ServerPlayerGameMode',
        'methodName': 'm_7179_',
        'methodDesc': '(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;'
      },
      'transformer': (method: MethodNode) => {
        const label = new LabelNode();
        const target = new InsnList();
        target.add(new VarInsnNode(Opcodes.ALOAD, 1));
        target.add(new VarInsnNode(Opcodes.ALOAD, 2));
        target.add(new VarInsnNode(Opcodes.ALOAD, 3));
        target.add(new VarInsnNode(Opcodes.ALOAD, 4));
        target.add(new VarInsnNode(Opcodes.ALOAD, 5));
        target.add(ASMAPI.buildMethodCall(
            'org/moddingx/libx/impl/libxcore/CoreInteract',
            'useItemOn', '(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;',
            ASMAPI.MethodType.STATIC
        ));
        target.add(new InsnNode(Opcodes.DUP));
        target.add(new JumpInsnNode(Opcodes.IFNULL, label));
        target.add(new InsnNode(Opcodes.ARETURN));
        target.add(label);
        target.add(new InsnNode(Opcodes.POP));
        
        for (let i = method.instructions.size() - 1; i >= 0; i--) {
          const inst = method.instructions.get(i);
          if (inst != null && inst.getOpcode() == Opcodes.ARETURN) {
            method.instructions.insertBefore(inst, target)
            return method;
          }
        }
        throw new Error("Failed to patch ServerPlayerGameMode.class");
      }
    }
  }
}
