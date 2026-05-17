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
package org.cardboardpowered.impl.util;

import org.bukkit.craftbukkit.util.Waitable;

public class WaitableImpl extends Waitable<Object> {

    private Runnable runnable;
    public WaitableImpl(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    protected Object evaluate() {
        runnable.run();
        return null;
    }

}