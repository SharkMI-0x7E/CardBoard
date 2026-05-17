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
package org.cardboardpowered.impl.entity;

import org.bukkit.craftbukkit.entity.CraftEntity;

import net.minecraft.world.entity.Entity;

public class UnknownEntity extends CraftEntity {
    public UnknownEntity(Entity entity) {
        super(entity);
    }
}
