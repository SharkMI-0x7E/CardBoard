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
package org.cardboardpowered.mixin.world.level.border;

import java.util.List;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import org.cardboardpowered.bridge.world.level.border.WorldBorderBridge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldBorder.class)
public abstract class WorldBorderMixin implements WorldBorderBridge {
    @Final
    @Shadow
    private List<BorderChangeListener> listeners;

    @Shadow
    public abstract void setCenter(double d, double e);

    @Shadow
    public abstract void setDamagePerBlock(double d);

    @Shadow
    public abstract void setSafeZone(double d);

    @Shadow
    public abstract void setWarningBlocks(int i);

    @Shadow
    public abstract void setWarningTime(int i);

    @Shadow
    public abstract void lerpSizeBetween(double d, double e, long l, long m);

    @Shadow
    public abstract void setSize(double d);

    @Inject(at = @At("HEAD"), method = "addListener", cancellable = true)
    public void addListenerBF(BorderChangeListener listener, CallbackInfo ci) {
        if (listeners.contains(listener)) {
            ci.cancel();
            return;
        }
    }

    // Paper start - add back applySettings
    @Override
    public void cardboard$applySettings(net.minecraft.world.level.border.WorldBorder.Settings settings) {
        this.setCenter(settings.centerX(), settings.centerZ());
        this.setDamagePerBlock(settings.damagePerBlock());
        this.setSafeZone(settings.safeZone());
        this.setWarningBlocks(settings.warningBlocks());
        this.setWarningTime(settings.warningTime());
        if (settings.lerpTime() > 0L) {
            // TODO
            //final long startTime = (this.world != null) ? this.world.getGameTime() : 0; // Virtual Borders don't have a World
            this.lerpSizeBetween(settings.size(), settings.lerpTarget(), settings.lerpTime(), 0);//startTime);
        } else {
            this.setSize(settings.size());
        }
    }
    // Paper end - add back applySettings
}
