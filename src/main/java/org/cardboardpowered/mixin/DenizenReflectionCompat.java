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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.cardboardpowered.util.nms.MemberNameResolver;

/**
 * ⚠️ INEFFECTIVE - DO NOT RELY ON THIS, AND DO NOT TRY TO "FIX" THESE MIXINS. See BUG-012.
 *
 * These mixins target Denizen classes ("com.denizenscript.denizencore..."), but Mixin can
 * only patch classes on the SERVER classpath. Denizen's classes are loaded later by the
 * plugin's own PluginClassLoader, so the targets are never found and Mixin silently skips
 * them ("@Mixin target ... was not found"). This code therefore NEVER RUNS, and has been
 * UNREGISTERED from bukkitfabric.mixins.json.
 *
 * The original intent - translating pre-reobf member names (named / officially obfuscated)
 * to runtime intermediary names (field_xxx / method_xxx) - is still a real gap, but it must
 * be solved generically, not with a per-plugin mixin. Track it in
 * plans/runtime-member-name-translation.md (same problem as BUG-005 / BUG-009).
 * MemberNameResolver (the tiny-map resolver this used) is kept as reusable groundwork.
 */
public final class DenizenReflectionCompat {

    /** com.denizenscript.denizencore.utilities.ReflectionHelper */
    @Mixin(targets = "com.denizenscript.denizencore.utilities.ReflectionHelper")
    public abstract static class ReflectionHelperMixin {

        @Inject(method = "getMethod(Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;",
                at = @At("HEAD"), cancellable = true)
        private static void cardboard$getMethod(Class<?> clazz, String method, Class<?>[] params,
                                                CallbackInfoReturnable<Method> cir) {
            if (clazz == null || method == null) return;
            Method m = null;
            try {
                m = clazz.getDeclaredMethod(method, params);
            } catch (Throwable ignored) {
            }
            if (m == null) {
                try {
                    for (Method dm : clazz.getDeclaredMethods()) {
                        if (dm.getParameterCount() != params.length) continue;
                        if (dm.getName().equals(method)) { m = dm; break; }
                        if (MemberNameResolver.methodAliases(clazz, dm.getName(), null).contains(method)) { m = dm; break; }
                    }
                } catch (Throwable ignored) {
                }
            }
            if (m != null) {
                m.setAccessible(true);
                cir.setReturnValue(m);
            }
        }

        @Inject(method = "getClass(Ljava/lang/String;)Ljava/lang/Class;",
                at = @At("HEAD"), cancellable = true)
        private static void cardboard$getClass(String className, CallbackInfoReturnable<Class<?>> cir)
                throws ClassNotFoundException {
            String rt = MemberNameResolver.translateClass(className);
            if (rt != null) {
                cir.setReturnValue(Class.forName(rt));
            }
        }

        @Inject(method = "getClassOrThrow(Ljava/lang/String;)Ljava/lang/Class;",
                at = @At("HEAD"), cancellable = true)
        private static void cardboard$getClassOrThrow(String className, CallbackInfoReturnable<Class<?>> cir)
                throws ClassNotFoundException {
            String rt = MemberNameResolver.translateClass(className);
            if (rt != null) {
                cir.setReturnValue(Class.forName(rt));
            }
        }

        @Inject(method = "classExists(Ljava/lang/String;)Z",
                at = @At("HEAD"), cancellable = true)
        private static void cardboard$classExists(String className, CallbackInfoReturnable<Boolean> cir) {
            String rt = MemberNameResolver.translateClass(className);
            if (rt != null) {
                try {
                    Class.forName(rt);
                    cir.setReturnValue(true);
                } catch (Throwable ignored) {
                    cir.setReturnValue(false);
                }
            }
        }
    }

    /** com.denizenscript.denizencore.utilities.ReflectionHelper$FieldCache */
    @Mixin(targets = "com.denizenscript.denizencore.utilities.ReflectionHelper$FieldCache")
    public abstract static class FieldCacheMixin {

        @Shadow
        public Class<?> clazz;

        @Inject(method = "getNoCheck(Ljava/lang/String;)Ljava/lang/reflect/Field;",
                at = @At("HEAD"), cancellable = true)
        private void cardboard$getNoCheck(String name, CallbackInfoReturnable<Field> cir) {
            if (clazz == null || name == null) return;
            Field f = null;
            try {
                f = clazz.getDeclaredField(name);
            } catch (Throwable ignored) {
            }
            if (f == null) {
                try {
                    for (Field all : clazz.getDeclaredFields()) {
                        if (all.getName().equals(name)) { f = all; break; }
                        if (MemberNameResolver.fieldAliases(clazz, all.getName()).contains(name)) { f = all; break; }
                    }
                } catch (Throwable ignored) {
                }
            }
            if (f != null) {
                f.setAccessible(true);
                cir.setReturnValue(f);
            }
        }

        @Inject(method = "getFirstOfType(Ljava/lang/Class;)Ljava/lang/reflect/Field;",
                at = @At("HEAD"), cancellable = true)
        private void cardboard$getFirstOfType(Class<?> fieldClazz, CallbackInfoReturnable<Field> cir) {
            if (clazz == null || fieldClazz == null) return;
            try {
                for (Field all : clazz.getDeclaredFields()) {
                    all.setAccessible(true);
                    if (all.getType().equals(fieldClazz)) {
                        cir.setReturnValue(all);
                        return;
                    }
                }
            } catch (Throwable ignored) {
            }
            // not found: fall through to the original (keeps its error reporting)
        }
    }
}