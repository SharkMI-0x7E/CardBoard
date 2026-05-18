/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2026 CardboardPowered.org and contributors
 * Copyright (C) 2026 SharkMI and contributors
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
package org.cardboardpowered.conflict;

import java.util.Set;

/**
 * Centralized Mixin/MixinExtras annotation descriptor constants.
 * All descriptors use full JVM internal format to avoid scattered hardcoding.
 */
public final class MixinAnnotationDescriptor {

    private MixinAnnotationDescriptor() {}

    // Core Mixin annotations
    public static final String MIXIN = "Lorg/spongepowered/asm/mixin/Mixin;";
    public static final String OVERWRITE = "Lorg/spongepowered/asm/mixin/Overwrite;";
    public static final String INJECT = "Lorg/spongepowered/asm/mixin/injection/Inject;";
    public static final String REDIRECT = "Lorg/spongepowered/asm/mixin/injection/Redirect;";
    public static final String MODIFY_ARG = "Lorg/spongepowered/asm/mixin/injection/ModifyArg;";
    public static final String MODIFY_VARIABLE = "Lorg/spongepowered/asm/mixin/injection/ModifyVariable;";
    public static final String MODIFY_RETURN_VALUE = "Lorg/spongepowered/asm/mixin/injection/ModifyReturnValue;";
    public static final String SHADOW = "Lorg/spongepowered/asm/mixin/Shadow;";
    public static final String UNIQUE = "Lorg/spongepowered/asm/mixin/Unique;";
    public static final String WRAP_WITH_CONDITION = "Lorg/spongepowered/asm/mixin/injection/WrapWithCondition;";

    // MixinExtras annotations
    public static final String ME_WRAP_WITH_CONDITION = "Lcom/llamalad7/mixinextras/spongepowered/WrapWithCondition;";
    public static final String ME_WRAP_OPERATION = "Lcom/llamalad7/mixinextras/spongepowered/WrapOperation;";
    public static final String ME_MODIFY_EXPRESSION_VALUE = "Lcom/llamalad7/mixinextras/spongepowered/ModifyExpressionValue;";

    // At annotation
    public static final String AT = "Lorg/spongepowered/asm/mixin/injection/At;";

    // Set of all known method-level injection annotations (excludes @Mixin, @Shadow, @Unique)
    public static final Set<String> KNOWN_INJECT_ANNOTATIONS = Set.of(
        OVERWRITE,
        INJECT,
        REDIRECT,
        MODIFY_ARG,
        MODIFY_VARIABLE,
        MODIFY_RETURN_VALUE,
        WRAP_WITH_CONDITION,
        ME_WRAP_WITH_CONDITION,
        ME_WRAP_OPERATION,
        ME_MODIFY_EXPRESSION_VALUE
    );
}
