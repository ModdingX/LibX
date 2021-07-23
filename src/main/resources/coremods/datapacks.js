function initializeCoreMod() {
  return {
    'datapacks': {
      'target': {
        'type': 'METHOD',
        'class': 'net.minecraft.server.management.PlayerList',
        'methodName': 'func_193244_w',
        'methodDesc': '()V'
      },
      'transformer': function(method) {
        var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
        var Opcodes = Java.type('org.objectweb.asm.Opcodes');
        var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
        var InsnList = Java.type('org.objectweb.asm.tree.InsnList');

        var target = new InsnList();
        target.add(new VarInsnNode(Opcodes.ALOAD, 0));
        target.add(ASMAPI.buildMethodCall(
            'io/github/noeppi_noeppi/libx/impl/libxcore/CoreDatapacks',
            'fireReload', '(Lnet/minecraft/server/management/PlayerList;)V',
            ASMAPI.MethodType.STATIC
        ));

        for (var i = 0; i < method.instructions.size(); i++) {
          var inst = method.instructions.get(i);
          if (inst.getOpcode() == Opcodes.INVOKEVIRTUAL
              && inst.owner == 'net/minecraft/server/management/PlayerList'
              && inst.name == ASMAPI.mapMethod('func_148540_a')) {
            method.instructions.insert(inst, target);
            return method;
          }
        }
        throw new Error("Failed to patch PlayerList.class");
      }
    }
  }
}
