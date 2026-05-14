package org.cardboardpowered.mixin.network.protocol.game;

import org.cardboardpowered.bridge.network.protocol.game.ClientboundSystemChatPacketBridge;
import org.spongepowered.asm.mixin.Mixin;

import net.md_5.bungee.api.chat.BaseComponent;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

@Mixin(ClientboundSystemChatPacket.class)
public class ClientboundSystemChatPacketMixin implements ClientboundSystemChatPacketBridge {

    //@Shadow private Text message;
    //@Shadow private UUID sender;

    public net.md_5.bungee.api.chat.BaseComponent[] bungeeComponents;

    @Override
    public BaseComponent[] getBungeeComponents() {
        return bungeeComponents;
    }

    /*
     * TODO
    @Inject(at = @At("HEAD"), method = "write", cancellable = true)
    public void writePacket(PacketByteBuf buf, CallbackInfo ci) {
        if (bungeeComponents != null) {
            buf.writeString(net.md_5.bungee.chat.ComponentSerializer.toString(bungeeComponents));
            buf.writeByte(MessageType.CHAT.getId());
            buf.writeUuid(this.sender);
            ci.cancel();
        }
    }
*/

    @Override
    public void setBungeeComponents(BaseComponent[] components) {
        this.bungeeComponents = components;
    }


}