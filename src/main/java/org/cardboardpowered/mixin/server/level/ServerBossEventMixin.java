package org.cardboardpowered.mixin.server.level;

import net.minecraft.server.level.ServerBossEvent;
import org.cardboardpowered.bridge.server.level.ServerBossEventBridge;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerBossEvent.class)
public class ServerBossEventMixin implements ServerBossEventBridge {

   /* @Override
    public void sendPacketBF(Type updateName) {
        sendPacket(updateName);
    }

    @Shadow
    public void sendPacket(Type updateName) {
    }*/
    // TODO 1.17ify

}
