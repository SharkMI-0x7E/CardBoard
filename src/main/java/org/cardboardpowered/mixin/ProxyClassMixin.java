/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2026 CardboardPowered.org and contributors*
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
package org.cardboardpowered.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.cardboardpowered.mohistremap.RemapUtilProvider;
import org.cardboardpowered.mohistremap.proxy.ProxyClass;

/**
 * Plugins like Denizen/Citizens reflectfully load NMS classes by their OLD
 * intermediate inner names (e.g. net.minecraft.class_7225$a). Cardboard's
 * reflection rewriter routes those Class.forName calls through ProxyClass,
 * whose forName() forwards the requested name to Class.forName() unchanged.
 *
 * The JVM then REQUIRES the class returned by the loader to carry exactly the
 * requested binary name, so translating inside PluginClassLoader.findClass()
 * is not enough - it fails with NoClassDefFoundError(originalName) on the name
 * mismatch. The name must be translated BEFORE Class.forName() is called. We
 * redirect the static Class.forName call inside ProxyClass.forName() and run
 * the name through RemapUtils.map() first (see RemapUtils patch entries for
 * class_7225$a etc.).
 */
@Mixin(ProxyClass.class)
public abstract class ProxyClassMixin {

    @Inject(
        method = "forName(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void cardboard$overrideForName(String name, boolean initialize, ClassLoader loader,
                                                  CallbackInfoReturnable<Class<?>> cir) throws ClassNotFoundException {
        String mapped;
        try {
            mapped = RemapUtilProvider.get().map(name.replace('.', '/'));
        } catch (RuntimeException ignored) {
            mapped = null; // unknown intermediary names fall back to the original name
        }
        String target = (mapped != null && mapped.indexOf('/') != -1) ? mapped.replace('/', '.') : name;
        cir.setReturnValue(Class.forName(target, initialize, loader));
    }
}