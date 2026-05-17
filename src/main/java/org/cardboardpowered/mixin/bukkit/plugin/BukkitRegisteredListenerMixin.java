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
package org.cardboardpowered.mixin.bukkit.plugin;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.plugin.RegisteredListener;
import org.cardboardpowered.fabric.FabricHookBukkitEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author wdog5
 * an implementation of FabricHookBukkit Event, allow mods to hook bukkit events
 * {@link org.cardboardpowered.fabric.FabricHookBukkitEvent}
 */
@Mixin(value = RegisteredListener.class, remap = false)
public class BukkitRegisteredListenerMixin {

    @Inject(method = "callEvent", at = @At(value = "INVOKE",
            target = "Lorg/bukkit/plugin/EventExecutor;execute(Lorg/bukkit/event/Listener;Lorg/bukkit/event/Event;)V",
            shift = At.Shift.BEFORE))
    private void hookEvent(Event event, CallbackInfo ci) {
        if (Bukkit.getServer() != null) {
            FabricHookBukkitEvent.EVENT.invoker().hook(event);
        }
    }
}
