package com.insightfullogic.collections.performance;
/*
 * Copyright 2014 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * {@link java.util.Map} implementation using open addressing and
 * linear probing for cache efficient access.
 *
 * @param <K> type of the keys stored in the {@link java.util.Map}
 * @param <V> type of values stored in the {@link java.util.Map}
 */
public class OpenHashMapV1<K, V>
    implements Map<K, V>
{
    private final double loadFactor;
    private int resizeThreshold;
    private int size;

    private Object[] keys;
    private Object[] values;

    private final ValueCollection<V> valueCollection;
    private final KeySet<K> keySet;
    private final EntrySet<K, V> entrySet;

    public OpenHashMapV1()
    {
        this(8, 0.67);
    }

    /**
     * Construct a new map allowing a configuration for initial capacity and load factor.
     *
     * @param initialCapacity for the backing array
     * @param loadFactor      limit for resizing on puts
     */
    public OpenHashMapV1(
        final int initialCapacity,
        final double loadFactor)
    {
        validateLoadFactor(loadFactor);

        this.loadFactor = loadFactor;
        /* */ final int capacity = findNextPositivePowerOfTwo(initialCapacity);
        /* */ resizeThreshold = (int)(capacity * loadFactor);

        keys = new Object[capacity];
        values = new Object[capacity];

        // Cached to avoid allocation.
        valueCollection = new ValueCollection<>();
        keySet = new KeySet<>();
        entrySet = new EntrySet<>();
    }

    /**
     * Validate that a load factor is greater than 0 and less than 1.0.
     *
     * @param loadFactor to be validated.
     */
    public static void validateLoadFactor(final double loadFactor)
    {
        if (loadFactor <= 0 || loadFactor >= 1.0)
        {
            throw new IllegalArgumentException("Load factors must be > 0.0 and < 1.0");
        }
    }

    /**
     * Fast method of finding the next power of 2 greater than or equal to the supplied value.
     *
     * If the value is &lt;= 0 then 1 will be returned.
     *
     * This method is not suitable for {@link Integer#MIN_VALUE} or numbers greater than 2^30.
     *
     * @param value from which to search for next power of 2
     * @return The next power of 2 or the value itself if it is a power of 2
     */
    public static int findNextPositivePowerOfTwo(final int value)
    {
        return 1 << (32 - Integer.numberOfLeadingZeros(value - 1));
    }

    /**
     * Get the load factor beyond which the map will increase size.
     *
     * @return load factor for when the map should increase size.
     */
    public double loadFactor()
    {
        return loadFactor;
    }

    /**
     * Get the total capacity for the map to which the load factor with be a fraction of.
     *
     * @return the total capacity for the map.
     */
    public int capacity()
    {
        return values.length;
    }

    /**
     * Get the actual threshold which when reached the map resize.
     * This is a function of the current capacity and load factor.
     *
     * @return the threshold when the map will resize.
     */
    public int resizeThreshold()
    {
        return resizeThreshold;
    }

    /**
     * {@inheritDoc}
     */
    public int size()
    {
        return size;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty()
    {
        return 0 == size;
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsKey(final Object key)
    {
        final int mask = values.length - 1;
        int index = hash(key, mask);

        boolean found = false;
        while (null != values[index])
        {
            if (key.equals(keys[index]))
            {
                found = true;
                break;
            }

            index = ++index & mask;
        }

        return found;
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsValue(final Object value)
    {
        boolean found = false;
        for (final Object v : values)
        {
            if (null != v && v.equals(value))
            {
                found = true;
                break;
            }
        }

        return found;
    }

    /**
     * {@inheritDoc}
     */
    public V get(final Object key)
    {
        final int mask = values.length - 1;
        int index = hash(key, mask);

        Object value;
        while (null != (value = values[index]))
        {
            if (key.equals(keys[index]))
            {
                break;
            }

            index = ++index & mask;
        }

        return (V)value;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public V put(final K key, final V value)
    {
        requireNonNull(key, "key cannot be null");
        requireNonNull(value, "Value cannot be null");

        V oldValue = null;
        final int mask = values.length - 1;
        int index = hash(key, mask);

        while (null != values[index])
        {
            if (key.equals(keys[index]))
            {
                oldValue = (V)values[index];
                break;
            }

            index = ++index & mask;
        }

        if (null == oldValue)
        {
            ++size;
            keys[index] = key;
        }

        values[index] = value;

        if (size > resizeThreshold)
        {
            increaseCapacity();
        }

        return oldValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V remove(final Object key)
    {
        final int mask = values.length - 1;
        int index = hash(key, mask);

        Object value;
        while (null != (value = values[index]))
        {
            if (key.equals(keys[index]))
            {
                values[index] = null;
                --size;

                compactChain(index);
                break;
            }

            index = ++index & mask;
        }

        return (V)value;
    }

    /**
     * {@inheritDoc}
     */
    public void clear()
    {
        size = 0;
        Arrays.fill(values, null);
    }

    /**
     * Compact the {@link Map} backing arrays by rehashing with a capacity just larger than current size
     * and giving consideration to the load factor.
     */
    public void compact()
    {
        final int idealCapacity = (int)Math.round(size() * (1.0d / loadFactor));
        rehash(findNextPositivePowerOfTwo(idealCapacity));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putAll(final Map<? extends K, ? extends V> map)
    {
        for (final Entry<? extends K, ? extends V> entry : map.entrySet())
        {
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<K> keySet()
    {
        return keySet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<V> values()
    {
        return valueCollection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Entry<K, V>> entrySet()
    {
        return entrySet;
    }

    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append('{');

        for (final Entry<K, V> entry : entrySet())
        {
            sb.append(entry.getKey());
            sb.append('=');
            sb.append(entry.getValue());
            sb.append(", ");
        }

        if (sb.length() > 1)
        {
            sb.setLength(sb.length() - 2);
        }

        sb.append('}');

        return sb.toString();
    }

    private void increaseCapacity()
    {
        final int newCapacity = values.length << 1;
        if (newCapacity < 0)
        {
            throw new IllegalStateException("Max capacity reached at size=" + size);
        }

        rehash(newCapacity);
    }

    private void rehash(final int newCapacity)
    {
        final int mask = newCapacity - 1;
        resizeThreshold = (int)(newCapacity * loadFactor);

        final Object[] tempKeys = new Object[newCapacity];
        final Object[] tempValues = new Object[newCapacity];

        for (int i = 0, size = values.length; i < size; i++)
        {
            final Object value = values[i];
            if (null != value)
            {
                final Object key = keys[i];
                int newHash = hash(key, mask);
                while (null != tempValues[newHash])
                {
                    newHash = ++newHash & mask;
                }

                tempKeys[newHash] = key;
                tempValues[newHash] = value;
            }
        }

        keys = tempKeys;
        values = tempValues;
    }

    private void compactChain(int deleteIndex)
    {
        final int mask = values.length - 1;
        int index = deleteIndex;
        while (true)
        {
            index = ++index & mask;
            if (null == values[index])
            {
                break;
            }

            final int hash = hash(keys[index], mask);

            if ((index < hash && (hash <= deleteIndex || deleteIndex <= index)) ||
                (hash <= deleteIndex && deleteIndex <= index))
            {
                keys[deleteIndex] = keys[index];
                values[deleteIndex] = values[index];

                values[index] = null;
                deleteIndex = index;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Internal Sets and Collections
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private class KeySet<K> extends AbstractSet<K>
    {
        public int size()
        {
            return OpenHashMapV1.this.size();
        }

        public boolean contains(final Object o)
        {
            return OpenHashMapV1.this.containsKey(o);
        }

        @Override
        public Iterator<K> iterator()
        {
            return new KeyIterator<>();
        }

        public boolean remove(final Object o)
        {
            return null != OpenHashMapV1.this.remove(o);
        }

        public void clear()
        {
            OpenHashMapV1.this.clear();
        }
    }

    private class ValueCollection<V> extends AbstractCollection<V>
    {
        public int size()
        {
            return OpenHashMapV1.this.size();
        }

        public boolean contains(final Object o)
        {
            return OpenHashMapV1.this.containsValue(o);
        }

        @Override
        public Iterator<V> iterator()
        {
            return new ValueIterator<>();
        }

        public void clear()
        {
            OpenHashMapV1.this.clear();
        }
    }

    private class EntrySet<K, V> extends AbstractSet<Entry<K, V>>
    {
        @Override
        public int size()
        {
            return OpenHashMapV1.this.size();
        }

        @Override
        public Iterator<Entry<K, V>> iterator()
        {
            return new EntryIterator<>();
        }

        @Override
        public void clear()
        {
            OpenHashMapV1.this.clear();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Iterators
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private abstract class AbstractIterator<T> implements Iterator<T>
    {
        private int posCounter;
        private int stopCounter;
        private boolean isPositionValid = false;
        protected Object[] keys;
        protected Object[] values;

        protected AbstractIterator()
        {
            reset();
        }

        protected int position()
        {
            return posCounter & values.length - 1;
        }

        public boolean hasNext()
        {
            final int mask = values.length - 1;
            boolean hasNext = false;
            for (int i = posCounter - 1; i >= stopCounter; i--)
            {
                final int index = i & mask;
                if (null != values[index])
                {
                    hasNext = true;
                    break;
                }
            }

            return hasNext;
        }

        protected void findNext()
        {
            final int mask = values.length - 1;
            isPositionValid = false;

            for (int i = posCounter - 1; i >= stopCounter; i--)
            {
                final int index = i & mask;
                if (null != values[index])
                {
                    posCounter = i;
                    isPositionValid = true;
                    return;
                }
            }

            throw new NoSuchElementException();
        }

        public abstract T next();

        public void remove()
        {
            if (isPositionValid)
            {
                final int position = position();
                values[position] = null;
                --size;

                compactChain(position);

                isPositionValid = false;
            }
            else
            {
                throw new IllegalStateException();
            }
        }

        void reset()
        {
            keys = OpenHashMapV1.this.keys;
            values = OpenHashMapV1.this.values;
            final int capacity = values.length;

            int i = capacity;
            if (null != values[capacity - 1])
            {
                i = 0;
                for (int size = capacity; i < size; i++)
                {
                    if (null == values[i])
                    {
                        break;
                    }
                }
            }

            stopCounter = i;
            posCounter = i + capacity;
        }
    }

    public class ValueIterator<T> extends AbstractIterator<T>
    {
        @SuppressWarnings("unchecked")
        public T next()
        {
            findNext();

            return (T)values[position()];
        }
    }

    private class KeyIterator<K> extends AbstractIterator<K>
    {
        @Override
        public K next()
        {
            findNext();

            return (K) keys[position()];
        }
    }

    @SuppressWarnings("unchecked")
    private class EntryIterator<K, V>
        extends AbstractIterator<Entry<K, V>>
    {
        @Override
        public Entry<K, V> next()
        {
            findNext();

            return new OpenHashMapEntry<>(position());
        }

        private class OpenHashMapEntry<K, V> implements Entry<K, V>
        {
            private final int position;

            public OpenHashMapEntry(int position) {

                this.position = position;
            }

            public K getKey()
            {
                return (K) keys[position];
            }

            public V getValue()
            {
                return (V) values[position];
            }

            public V setValue(final V value)
            {
                requireNonNull(value);

                final int pos = position;
                final Object oldValue = values[pos];
                values[pos] = value;

                return (V)oldValue;
            }

            @Override
            public String toString()
            {
                return getKey() + "=" + getValue();
            }
        }
    }

    private int hash(final Object key, final int mask)
    {
        int hash = key.hashCode();
        hash = hash ^ (hash >>> 16);
        return hash & mask;
    }

}
