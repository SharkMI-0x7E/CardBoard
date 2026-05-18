/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2026 CardboardPowered.org and contributors
 * Copyright (C) 2026 SharkMI and contributors
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

import org.cardboardpowered.CardboardMod;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.LegacyQueryHandler;
import org.bukkit.craftbukkit.CraftServer;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

@MixinInfo(events = "ServerListPingEvent")
@Mixin(value = LegacyQueryHandler.class, priority = 999)
public class LegacyQueryHandlerMixin {

    @Shadow private static ByteBuf createLegacyDisconnectPacket(ByteBufAllocator allocator, String string) {return null;}
    @Shadow private static void sendFlushAndClose(ChannelHandlerContext channelhandlercontext, ByteBuf bytebuf) {}

    /**
     * @reason Add ServerListPingEvent
     * @author bukkit4fabric
     *
     * TODO: Cannot replace with @Inject - this @Overwrite completely rewrites the
     * channelRead method to intercept legacy ping packets (1.3.x-1.6) and fire
     * ServerListPingEvent before responding. The original method handles raw ByteBuf
     * parsing and protocol-specific response formatting, which cannot be split into
     * multiple injection points.
     */
    @Overwrite
    public void channelRead(ChannelHandlerContext ctx, Object object) throws Exception {
        ByteBuf bytebuf = (ByteBuf) object;

        bytebuf.markReaderIndex();
        boolean flag = true;

        try {
            if (bytebuf.readUnsignedByte() != 254)
                return;
            InetSocketAddress inetsocketaddress = (InetSocketAddress) ctx.channel().remoteAddress();
            MinecraftServer minecraftserver = CraftServer.server;
            int i = bytebuf.readableBytes();
            String s;
            org.bukkit.event.server.ServerListPingEvent event = CraftEventFactory.callServerListPingEvent(CraftServer.INSTANCE, inetsocketaddress.getAddress(), minecraftserver.getMotd(), minecraftserver.getPlayerCount(), minecraftserver.getMaxPlayers()); // CraftBukkit

            switch (i) {
                case 0:
                    CardboardMod.LOGGER.config("Ping: (<1.3.x) from " + inetsocketaddress.getAddress() + ":" + inetsocketaddress.getPort());
                    s = String.format("%s\u00a7%d\u00a7%d", event.getMotd(), event.getNumPlayers(), event.getMaxPlayers()); // CraftBukkit
                    sendFlushAndClose(ctx, createLegacyDisconnectPacket(ctx.alloc(), s));
                    break;
                case 1:
                    if (bytebuf.readUnsignedByte() != 1)
                        return;

                    CardboardMod.LOGGER.config("Ping: (1.4-1.5.x) from " + inetsocketaddress.getAddress() + ":" + inetsocketaddress.getPort());
                    s = String.format("\u00a71\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d", 127, minecraftserver.getServerVersion(), event.getMotd(), event.getNumPlayers(), event.getMaxPlayers()); // CraftBukkit
                    sendFlushAndClose(ctx, createLegacyDisconnectPacket(ctx.alloc(), s));
                    break;
                default:
                    boolean flag1 = bytebuf.readUnsignedByte() == 1;

                    flag1 &= bytebuf.readUnsignedByte() == 250;
                    flag1 &= "MC|PingHost".equals(new String(bytebuf.readBytes(bytebuf.readShort() * 2).array(), StandardCharsets.UTF_16BE));
                    int j = bytebuf.readUnsignedShort();

                    flag1 &= bytebuf.readUnsignedByte() >= 73;
                    flag1 &= 3 + bytebuf.readBytes(bytebuf.readShort() * 2).array().length + 4 == j;
                    flag1 &= bytebuf.readInt() <= 65535;
                    flag1 &= bytebuf.readableBytes() == 0;
                    if (!flag1)
                        return;
                    CardboardMod.LOGGER.config("Ping: (1.6) from " + inetsocketaddress.getAddress() + ":" + inetsocketaddress.getPort());
                    String s1 = String.format("\u00a71\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d", 127, minecraftserver.getServerVersion(), event.getMotd(), event.getNumPlayers(), event.getMaxPlayers()); // CraftBukkit
                    System.out.println("DEBUG: " + s1);
                    ByteBuf bytebuf1 = createLegacyDisconnectPacket(ctx.alloc(), s1);

                    try {
                        sendFlushAndClose(ctx, bytebuf1);
                    } finally {
                        bytebuf1.release();
                    }
            }

            bytebuf.release();
            flag = false;
        } catch (RuntimeException runtimeexception) {
            runtimeexception.printStackTrace();
        } finally {
            if (flag) {
                bytebuf.resetReaderIndex();
                ctx.channel().pipeline().remove("legacy_query");
                ctx.fireChannelRead(object);
            }

        }

    }

}
