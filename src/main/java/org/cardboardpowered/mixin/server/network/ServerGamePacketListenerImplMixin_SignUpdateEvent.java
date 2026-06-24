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
package org.cardboardpowered.mixin.server.network;

import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.block.SignChangeEvent;
import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.cardboardpowered.impl.world.CraftWorld;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Bridges vanilla {@code ServerboundSignUpdatePacket} handling to Bukkit's
 * {@link SignChangeEvent}.
 *
 * <p>Strategy: fire SignChangeEvent <strong>synchronously on the packet handling
 * thread</strong> at method HEAD, then write the (possibly modified) lines back
 * into the packet's internal String[] array. Vanilla code downstream calls
 * {@code Stream.of(packet.getLines()).map(...).collect(toList())}; since the
 * terminal {@code collect} reads elements lazily, our mutated strings are
 * picked up and turned into FilteredText as if the client had sent them.</p>
 *
 * <p>This lets vanilla's own {@code sign.updateSignText()} run, which
 * creates a new immutable SignText, swaps it into the front-text field,
 * marks the BlockEntity dirty, and broadcasts
 * {@code playBlockEvent(3) (SIGN_SET_TEXT)} to nearby players. No race
 * condition, no orphaned Cardboard tasks, no missing notifications.</p>
 *
 * <p>If the event is cancelled, we {@code ci.cancel()} the vanilla method
 * entirely — the sign keeps whatever text it had before.</p>
 *
 * @see .trae/specs/fix-sign-text-disappearing
 */
@MixinInfo(events = {"SignChangeEvent"})
@Mixin(value = ServerGamePacketListenerImpl.class, priority = 800)
public class ServerGamePacketListenerImplMixin_SignUpdateEvent {

    @Shadow
    public ServerPlayer player;

    @Inject(method = "handleSignUpdate", at = @At("HEAD"), cancellable = true)
    public void cardboard$fireSignChangeEvent(ServerboundSignUpdatePacket packet, CallbackInfo ci) {
        try {
            // packet.getLines() returns the internal String[4] by reference (verified in 1.21.11
            // via javap on ServerboundSignUpdatePacket.f()). We can mutate elements in place.
            String[] packetLines = packet.getLines();
            if (packetLines == null || packetLines.length < 4) {
                return; // malformed packet; let vanilla reject it
            }

            // Build the Bukkit event. SignChangeEvent's lines array is the one the
            // plugin can read/write via getLine/setLine; we copy packetLines by value
            // so the event holds its own snapshot.
            String[] eventLines = new String[4];
            for (int i = 0; i < 4; i++) {
                eventLines[i] = packetLines[i] == null ? "" : packetLines[i];
            }

            CraftPlayer craftPlayer = (CraftPlayer) ((EntityBridge) this.player).getBukkitEntity();
            CraftWorld craftWorld = (CraftWorld) craftPlayer.getWorld();
            Block bukkitBlock = CraftBlock.at(craftWorld.getHandle(), packet.getPos());

            SignChangeEvent event = new SignChangeEvent(
                    bukkitBlock,
                    craftPlayer,
                    eventLines);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                ci.cancel();
                return;
            }

            // Write event lines back into the packet's String[]. Vanilla's downstream
            // Stream pipeline will pick them up when it collects the List<FilteredText>.
            String[] modified = event.getLines();
            for (int i = 0; i < 4; i++) {
                packetLines[i] = (i < modified.length && modified[i] != null) ? modified[i] : "";
            }
            // Let vanilla continue: it will call sign.updateSignText(...) which
            // builds a new SignText, sets it, marks dirty, and broadcasts
            // playBlockEvent(3) to all tracking players.
        } catch (Throwable t) {
            // On any unexpected error, fail open: let vanilla handle the packet
            // with its original lines rather than blocking the player.
        }
    }

}
