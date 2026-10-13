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
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.cardboardpowered.mixin.server.level;

import com.mojang.datafixers.util.Either;
import io.papermc.paper.block.bed.BedEnterAction;
import io.papermc.paper.block.bed.BedEnterProblem;
import io.papermc.paper.block.bed.BedRuleResult;
import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.event.Event;
import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@MixinInfo(events = {"PlayerBedEnterEvent", "PlayerBedLeaveEvent"})
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin_BedEvents {

    // Partial semantic bridge for Paper's 1.21.11 bed-rule rework: we fire the
    // Bukkit event on the vanilla success path, right before super.startSleepInBed
    // writes the sleeping state. Vanilla checks (range/obstruction/monsters) have
    // passed at this point, but setRespawnPosition() may already have run earlier
    // in the same method, and modded BedRule outcomes are not represented. The
    // synthetic BedEnterAction therefore only expresses the "allowed" main path.
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Inject(method = "startSleepInBed",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;startSleepInBed(Lnet/minecraft/core/BlockPos;)Lcom/mojang/datafixers/util/Either;"),
            cancellable = true)
    private void cardboard$firePlayerBedEnterEvent(BlockPos pos, CallbackInfoReturnable<Either> cir) {
        if (org.bukkit.event.player.PlayerBedEnterEvent.getHandlerList().getRegisteredListeners().length == 0) {
            return;
        }
        ServerPlayer self = (ServerPlayer) (Object) this;
        Level level = self.level();
        org.bukkit.entity.Player bukkitPlayer =
                (org.bukkit.entity.Player) ((EntityBridge) (Object) this).getBukkitEntity();

        BedEnterAction action = new BedEnterAction() {
            @Override
            public BedRuleResult canSleep() {
                return BedRuleResult.ALLOWED;
            }

            @Override
            public BedRuleResult canSetSpawn() {
                return BedRuleResult.ALLOWED;
            }

            @Override
            public BedEnterProblem problem() {
                return BedEnterProblem.OTHER;
            }

            @Override
            public Component errorMessage() {
                return null;
            }
        };

        org.bukkit.event.player.PlayerBedEnterEvent event =
                new org.bukkit.event.player.PlayerBedEnterEvent(
                        bukkitPlayer,
                        CraftBlock.at(level, pos),
                        org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult.OK,
                        action);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled() || event.useBed() == Event.Result.DENY) {
            cir.setReturnValue(Either.left(Player.BedSleepingProblem.OTHER_PROBLEM));
        }
    }

    // Matches Paper's stopSleepInBed head: no event outside an actual sleep
    // (the method is also called on death/teleport/disconnect), the bed falls back
    // to the player's current position and is never null, and the third argument is
    // a constant true (the legacy spawn flag has been a no-op since MC 1.15).
    @Inject(method = "stopSleepInBed(ZZ)V", at = @At("HEAD"), cancellable = true)
    private void cardboard$firePlayerBedLeaveEvent(boolean updateSpawn, boolean updateState, CallbackInfo ci) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        if (!self.isSleeping()) {
            return;
        }
        if (org.bukkit.event.player.PlayerBedLeaveEvent.getHandlerList().getRegisteredListeners().length == 0) {
            return;
        }
        BlockPos bedPos = self.getSleepingPos().orElse(self.blockPosition());
        org.bukkit.block.Block bed = CraftBlock.at(self.level(), bedPos);
        org.bukkit.event.player.PlayerBedLeaveEvent event =
                new org.bukkit.event.player.PlayerBedLeaveEvent(
                        (org.bukkit.entity.Player) ((EntityBridge) (Object) this).getBukkitEntity(),
                        bed,
                        true);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
