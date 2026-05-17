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
package org.cardboardpowered.mixin.world.entity.item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import org.cardboardpowered.bridge.world.entity.item.PrimedTntBridge;

@Mixin(PrimedTnt.class)
public class PrimedTntMixin implements PrimedTntBridge {

	/**
	 * @implNote LivingEntity (1.21.4) -> LazyEntityReference<LivingEntity> (1.21.8)
	 */
	@Shadow
	public EntityReference<LivingEntity> owner;

    @Override
    public void cardboard$setSource(LivingEntity entity) {
        this.owner = entity != null ? new EntityReference<LivingEntity>(entity) : null;;
    }

}
