/**
 * This file is a part of Cardboard & iCommonLib
 * Copyright (c) 2020-2021 by Isaiah
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
package org.cardboardpowered.mixin.server.level;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import net.minecraft.server.level.ServerLevel.EntityCallbacks;
import net.minecraft.world.entity.Entity;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityCallbacks.class)
public class EntityCallbacksMixin {

    @Inject(at = @At("TAIL"), method = "onTrackingEnd(Lnet/minecraft/world/entity/Entity;)V")
    public void unvalidateEntityBF(Entity entity, CallbackInfo ci) {
        EntityBridge bf = (EntityBridge) entity;
        bf.setValid(false);
        CraftEventFactory.callEvent( new EntityRemoveFromWorldEvent(bf.getBukkitEntity(), entity.level().cardboard$getWorld()) );
    }

    @Inject(at = @At("TAIL"), method = "onTickingStart(Lnet/minecraft/world/entity/Entity;)V")
    public void validateEntityBF(Entity entity, CallbackInfo ci) {
        EntityBridge bf = (EntityBridge) entity;
        bf.setValid(true);
        bf.cb$setInWorld(true);
        
        if (null == bf.getOriginBF() && bf.getBukkitEntity() != null)
            bf.setOriginBF(bf.getBukkitEntity().getLocation()); // Paper Entity Origin API

        CraftEventFactory.callEvent( new EntityAddToWorldEvent(bf.getBukkitEntity(), entity.level().cardboard$getWorld()) );
    } 

}
