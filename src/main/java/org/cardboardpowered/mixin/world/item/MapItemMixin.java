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
package org.cardboardpowered.mixin.world.item;

import net.minecraft.world.item.MapItem;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;

@MixinInfo(events = {"MapInitializeEvent"})
@Mixin(MapItem.class)
public class MapItemMixin {

    /**
     * @reason .
     * @author .
     */// TODO 1.17ify
    /*@Overwrite
    private static MapState createMapState(ItemStack itemstack, World world, int i, int j, int k, boolean flag, boolean flag1, RegistryKey<World> resourcekey) {
        int l = world.getNextMapId();
        MapState worldmap = new MapState("map_" + l);

        worldmap.init(i, j, k, flag, flag1, resourcekey);
        world.putMapState(worldmap);
        itemstack.getOrCreateTag().putInt("map", l);

        MapInitializeEvent event = new MapInitializeEvent(((IMixinMapState)worldmap).getMapViewBF());
        Bukkit.getServer().getPluginManager().callEvent(event);
        return worldmap;
    }*/

}
