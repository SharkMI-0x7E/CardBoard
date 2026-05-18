/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2026 CardboardPowered.org and contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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