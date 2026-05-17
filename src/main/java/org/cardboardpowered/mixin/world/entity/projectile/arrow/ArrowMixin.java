/**
 * Copyright (C) 2026 SharkMI and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 */
package org.cardboardpowered.mixin.world.entity.projectile.arrow;

import net.minecraft.world.entity.projectile.arrow.Arrow;
import org.spongepowered.asm.mixin.Mixin;

import org.cardboardpowered.bridge.world.entity.projectile.arrow.ArrowBridge;

@Mixin(Arrow.class)
public class ArrowMixin implements ArrowBridge {

    //@Shadow
    //public Potion potion;

    //@Shadow
    //public Set<StatusEffectInstance> effects;

    //@Shadow
    //private static TrackedData<Integer> COLOR;

    @Override
    public void setType(String string) {
        // TODO: 1.20.5
    	// this.potion = Registries.POTION.get(new Identifier(string));
        // (((Entity)(Object)this).getDataTracker()).set(COLOR, PotionUtil.getColor((Collection<StatusEffectInstance>) PotionUtil.getPotionEffects(this.potion, (Collection<StatusEffectInstance>) this.effects)));
    }

}
