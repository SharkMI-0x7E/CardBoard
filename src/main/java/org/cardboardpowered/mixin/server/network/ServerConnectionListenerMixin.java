/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2024 CardboardPowered.org and contributors
 * Copyright (C) 2025-2026 SharkMI and contributors
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
package org.cardboardpowered.mixin.server.network;

import java.net.InetAddress;
import java.util.List;
import net.minecraft.server.network.ServerConnectionListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.cardboardpowered.bridge.server.network.ServerConnectionListenerBridge;

import io.netty.channel.ChannelFuture;

@Mixin(ServerConnectionListener.class)
public class ServerConnectionListenerMixin implements ServerConnectionListenerBridge {

    @Shadow
    @Final
    public List<ChannelFuture> channels;

    @Override
    public void acceptConnections() {
        synchronized (channels) {
            for (ChannelFuture future : channels)
                future.channel().config().setAutoRead(true);
        }
    }

    @Inject(at = @At("TAIL"), method = "startTcpServerListener")
    public void cardboard_setAutoreadFalse(InetAddress ina, int i, CallbackInfo ci) {
        synchronized (channels) {
            for (ChannelFuture future : channels)
                future.channel().config().setAutoRead(false);
        }
    }

}