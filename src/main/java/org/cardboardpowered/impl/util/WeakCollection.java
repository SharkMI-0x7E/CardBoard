/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2024 CardboardPowered.org and contributors
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.apache.commons.lang.Validate;

public final class WeakCollection<T> implements Collection<T> {

    static final Object NO_VALUE = new Object();
    private final Collection<WeakReference<T>> collection;

    public WeakCollection() {
        collection = new ArrayList<WeakReference<T>>();
    }

    @Override
    public boolean add(T value) {
        Validate.notNull(value, "Cannot add null value");
        return collection.add(new WeakReference<T>(value));
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        Collection<WeakReference<T>> values = this.collection;
        boolean ret = false;
        for (T value : collection) {
            Validate.notNull(value, "Cannot add null value");
            ret |= values.add(new WeakReference<T>(value));
        }
        return ret;
    }

    @Override
    public void clear() {
        collection.clear();
    }

    @Override
    public boolean contains(Object object) {
        if (object == null) return false;
        for (T compare : this)
            if (object.equals(compare)) return true;
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return toCollection().containsAll(collection);
    }

    @Override
    public boolean isEmpty() {
        return !iterator().hasNext();
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            Iterator<WeakReference<T>> it = collection.iterator();
            Object value = NO_VALUE;

            @Override
            public boolean hasNext() {
                Object value = this.value;
                if (value != null && value != NO_VALUE) return true;

                Iterator<WeakReference<T>> it = this.it;
                value = null;

                while (it.hasNext()) {
                    WeakReference<T> ref = it.next();
                    value = ref.get();
                    if (value == null) {
                        it.remove();
                    } else {
                        this.value = value;
                        return true;
                    }
                }
                return false;
            }

            @Override
            public T next() throws NoSuchElementException {
                if (!hasNext()) throw new NoSuchElementException("No more elements");

                @SuppressWarnings("unchecked")
                T value = (T) this.value;
                this.value = NO_VALUE;
                return value;
            }

            @Override
            public void remove() throws IllegalStateException {
                if (value != NO_VALUE) throw new IllegalStateException("No last element");

                value = null;
                it.remove();
            }
        };
    }

    @Override
    public boolean remove(Object object) {
        if (object == null)
            return false;

        Iterator<T> it = this.iterator();
        while (it.hasNext()) {
            if (object.equals(it.next())) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        Iterator<T> it = this.iterator();
        boolean ret = false;
        while (it.hasNext()) {
            if (collection.contains(it.next())) {
                ret = true;
                it.remove();
            }
        }
        return ret;
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        Iterator<T> it = this.iterator();
        boolean ret = false;
        while (it.hasNext()) {
            if (!collection.contains(it.next())) {
                ret = true;
                it.remove();
            }
        }
        return ret;
    }

    @Override
    public int size() {
        int s = 0;
        for (T value : this) s++;
        return s;
    }

    @Override
    public Object[] toArray() {
        return this.toArray(new Object[0]);
    }

    @Override
    public <T> T[] toArray(T[] array) {
        return toCollection().toArray(array);
    }

    private Collection<T> toCollection() {
        ArrayList<T> collection = new ArrayList<T>();
        for (T value : this) collection.add(value);
        return collection;
    }

}