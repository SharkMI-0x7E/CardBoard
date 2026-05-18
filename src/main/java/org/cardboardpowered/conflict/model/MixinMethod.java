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
package org.cardboardpowered.conflict.model;

import java.util.ArrayList;
import java.util.List;

public class MixinMethod {

    private String name;
    private String descriptor;
    private String annotationType;
    private List<String> targetMethods = new ArrayList<>();
    private List<String> atValues = new ArrayList<>();
    private List<String> atTargets = new ArrayList<>();
    private boolean cancellable;
    private int priority = 1000;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescriptor() { return descriptor; }
    public void setDescriptor(String descriptor) { this.descriptor = descriptor; }

    public String getAnnotationType() { return annotationType; }
    public void setAnnotationType(String annotationType) { this.annotationType = annotationType; }

    public List<String> getTargetMethods() { return targetMethods; }
    public void setTargetMethods(List<String> targetMethods) { this.targetMethods = targetMethods; }

    public List<String> getAtValues() { return atValues; }
    public void setAtValues(List<String> atValues) { this.atValues = atValues; }

    public List<String> getAtTargets() { return atTargets; }
    public void setAtTargets(List<String> atTargets) { this.atTargets = atTargets; }

    public boolean isCancellable() { return cancellable; }
    public void setCancellable(boolean cancellable) { this.cancellable = cancellable; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public boolean isOverwrite() {
        return "Overwrite".equals(annotationType);
    }

    public boolean isInject() {
        return "Inject".equals(annotationType);
    }

    public boolean isRedirect() {
        return "Redirect".equals(annotationType);
    }

    public String getAtTargetKey() {
        if (atTargets == null || atTargets.isEmpty()) {
            return "";
        }
        return atTargets.get(0);
    }

    @Override
    public String toString() {
        return "MixinMethod{" +
                "name='" + name + '\'' +
                ", descriptor='" + descriptor + '\'' +
                ", annotationType='" + annotationType + '\'' +
                ", targetMethods=" + targetMethods +
                ", atValues=" + atValues +
                ", atTargets=" + atTargets +
                ", cancellable=" + cancellable +
                ", priority=" + priority +
                '}';
    }
}
