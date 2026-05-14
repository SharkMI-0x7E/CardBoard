/**
 * The Bukkit for Fabric Project
 * Copyright (C) 2020 Javazilla Software and contributors
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.cardboardpowered.mixin.stats;

import org.bukkit.event.Cancellable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.player.Player;
import org.bukkit.craftbukkit.event.CraftEventFactory;

@Mixin(value = StatsCounter.class, priority = 900)
public class StatsCounterMixin {

    /**
     * @reason handleStatisticsIncrease
     * @author .
     */
    @Overwrite
    public void increment(Player player, Stat<?> statistic, int i) {
        int j = (int) Math.min((long) this.getValue(statistic) + (long) i, 2147483647L);

        Cancellable cancellable = CraftEventFactory.handleStatisticsIncrease(player, statistic, this.getValue(statistic), j);
        if (cancellable != null && cancellable.isCancelled()) return;
        this.setValue(player, statistic, j);
    }

    @Shadow
    public void setValue(Player player, Stat<?> statistic, int i) {
    }

    @Shadow
    public int getValue(Stat<?> statistic) {
        return 0;
    }

}