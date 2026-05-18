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
package org.cardboardpowered.impl.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public abstract class LazyHashSet<E> implements Set<E> {

    protected Set<E> reference = null;

    @Override
    public int size() {
        return getReference().size();
    }

    @Override
    public boolean isEmpty() {
        return getReference().isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return getReference().contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return getReference().iterator();
    }

    @Override
    public Object[] toArray() {
        return getReference().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return getReference().toArray(a);
    }

    @Override
    public boolean add(E o) {
        return getReference().add(o);
    }

    @Override
    public boolean remove(Object o) {
        return getReference().remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return getReference().containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return getReference().addAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return getReference().retainAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return getReference().removeAll(c);
    }

    @Override
    public void clear() {
        getReference().clear();
    }

    public Set<E> getReference() {
        Set<E> reference = this.reference;
        if (reference != null)
            return reference;
        return this.reference = makeReference();
    }

    abstract Set<E> makeReference();

    public boolean isLazy() {
        return reference == null;
    }

    @Override
    public int hashCode() {
        return 157 * getReference().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || this.getClass() != obj.getClass()) return false;

        LazyHashSet<?> that = (LazyHashSet<?>) obj;
        return (this.isLazy() && that.isLazy()) || this.getReference().equals(that.getReference());
    }

    @Override
    public String toString() {
        return getReference().toString();
    }

}