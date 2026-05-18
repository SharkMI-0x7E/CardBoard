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
package org.cardboardpowered.mixin.world.entity.ai.behavior;

import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;

@MixinInfo(events = {"EntityTargetEvent", "EntityTargetLivingEntityEvent"})
@Mixin(StopAttackingIfTargetInvalid.class)
public class StopAttackingIfTargetInvalidMixin<E extends Mob> {

    // TODO 1.19.4
	
	/*@Inject(at = @At("HEAD"), method = "forgetAttackTarget", cancellable = true)
    public void callTargetEvent(E e0, CallbackInfo ci) {
        LivingEntity old = e0.getBrain().getOptionalMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        EntityTargetEvent event = CraftEventFactory.callEntityTargetLivingEvent(e0, old, (old != null && !old.isAlive()) ? EntityTargetEvent.TargetReason.TARGET_DIED : EntityTargetEvent.TargetReason.FORGOT_TARGET);
        if (event.isCancelled()) return;

        if (event.getTarget() != null) {
            e0.getBrain().remember(MemoryModuleType.ATTACK_TARGET, ((LivingEntityImpl) event.getTarget()).getHandle());
            ci.cancel();
            return;
        }
        e0.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
    }*/

}