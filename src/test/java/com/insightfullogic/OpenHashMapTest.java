package com.insightfullogic;/*
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

import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.Map.Entry;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.junit.Assert.*;

public class OpenHashMapTest
{
    private final OpenHashMap<Integer, String> classToTest = new OpenHashMap<>();

    @Test
    public void shouldDoPutAndThenGet()
    {
        final String value = "Seven";
        classToTest.put(7, value);

        assertThat(classToTest.get(7), is(value));
    }

    @Test
    public void shouldReplaceExistingValueForTheSameKey()
    {
        final int key = 7;
        final String value = "Seven";
        classToTest.put(key, value);

        final String newValue = "New Seven";
        final String oldValue = classToTest.put(key, newValue);

        assertThat(classToTest.get(key), is(newValue));
        assertThat(oldValue, is(value));
        assertThat(classToTest.size(), is(1));
    }

    @Test
    public void shouldGrowWhenThresholdExceeded()
    {
        final double loadFactor = 0.5d;
        final OpenHashMap<Integer, String> map = new OpenHashMap<>(32, loadFactor);
        for (int i = 0; i < 16; i++)
        {
            map.put(i, Integer.toString(i));
        }

        assertThat(map.resizeThreshold(), is(16));
        assertThat(map.capacity(), is(32));
        assertThat(map.size(), is(16));

        map.put(16, "16");

        assertThat(map.resizeThreshold(), is(32));
        assertThat(map.capacity(), is(64));
        assertThat(map.size(), is(17));

        assertThat(map.get(16), equalTo("16"));
        assertThat(loadFactor, closeTo(map.loadFactor(), 0.0));
    }

    @Test
    public void shouldHandleCollisionAndThenLinearProbe()
    {
        final double loadFactor = 0.5d;
        final OpenHashMap<Integer, String> map = new OpenHashMap<>(32, loadFactor);
        final int key = 7;
        final String value = "Seven";
        map.put(key, value);

        final int collisionKey = key + map.capacity();
        final String collisionValue = Integer.toString(collisionKey);
        map.put(collisionKey, collisionValue);

        assertThat(map.get(key), is(value));
        assertThat(map.get(collisionKey), is(collisionValue));
        assertThat(loadFactor, closeTo(map.loadFactor(), 0.0));
    }

    @Test
    public void shouldClearCollection()
    {
        for (int i = 0; i < 15; i++)
        {
            classToTest.put(i, Integer.toString(i));
        }

        assertThat(classToTest.size(), is(15));
        assertThat(classToTest.get(1), is("1"));

        classToTest.clear();

        assertThat(classToTest.size(), is(0));
        Assert.assertNull(classToTest.get(1));
    }

    @Test
    public void shouldCompactCollection()
    {
        final int totalItems = 50;
        for (int i = 0; i < totalItems; i++)
        {
            classToTest.put(i, Integer.toString(i));
        }

        for (int i = 0, limit = totalItems - 4; i < limit; i++)
        {
            classToTest.remove(i);
        }

        final int capacityBeforeCompaction = classToTest.capacity();
        classToTest.compact();

        assertThat(classToTest.capacity(), lessThan(capacityBeforeCompaction));
    }

    @Test
    public void shouldContainValue()
    {
        final int key = 7;
        final String value = "Seven";

        classToTest.put(key, value);

        Assert.assertTrue(classToTest.containsValue(value));
        Assert.assertFalse(classToTest.containsValue("NoKey"));
    }

    @Test
    public void shouldContainKey()
    {
        final int key = 7;
        final String value = "Seven";

        classToTest.put(key, value);

        Assert.assertTrue(classToTest.containsKey(key));
        Assert.assertFalse(classToTest.containsKey(0));
    }

    @Test
    public void shouldRemoveEntry()
    {
        final int key = 7;
        final String value = "Seven";

        classToTest.put(key, value);

        Assert.assertTrue(classToTest.containsKey(key));

        classToTest.remove(key);

        Assert.assertFalse(classToTest.containsKey(key));
    }

    @Test
    public void shouldRemoveEntryAndCompactCollisionChain()
    {
        final int key = 12;
        final String value = "12";

        classToTest.put(key, value);
        classToTest.put(13, "13");

        final int collisionKey = key + classToTest.capacity();
        final String collisionValue = Integer.toString(collisionKey);

        classToTest.put(collisionKey, collisionValue);
        classToTest.put(14, "14");

        assertThat(classToTest.remove(key), is(value));
    }

    @Test
    public void shouldIterateValues()
    {
        final Collection<String> initialSet = new HashSet<>();

        for (int i = 0; i < 11; i++)
        {
            final String value = Integer.toString(i);
            classToTest.put(i, value);
            initialSet.add(value);
        }

        final Collection<String> copyToSet = new HashSet<>();

        for (final String s : classToTest.values())
        {
            copyToSet.add(s);
        }

        assertThat(copyToSet, is(initialSet));
    }

    @Test
    public void shouldIterateKeysGettingIntAsPrimitive()
    {
        final Collection<Integer> initialSet = new HashSet<>();

        for (int i = 0; i < 11; i++)
        {
            final String value = Integer.toString(i);
            classToTest.put(i, value);
            initialSet.add(i);
        }

        final Collection<Integer> copyToSet = new HashSet<>();

        for (final Iterator<Integer> iter = classToTest.keySet().iterator(); iter.hasNext();)
        {
            copyToSet.add(iter.next());
        }

        assertThat(copyToSet, is(initialSet));
    }

    @Test
    public void shouldIterateKeys()
    {
        final Collection<Integer> initialSet = new HashSet<>();

        for (int i = 0; i < 11; i++)
        {
            final String value = Integer.toString(i);
            classToTest.put(i, value);
            initialSet.add(i);
        }

        assertIterateKeys(initialSet);
        assertIterateKeys(initialSet);
        assertIterateKeys(initialSet);
    }

    private void assertIterateKeys(final Collection<Integer> initialSet)
    {
        final Collection<Integer> copyToSet = new HashSet<>();
        for (final Integer aInteger : classToTest.keySet())
        {
            copyToSet.add(aInteger);
        }
        assertThat(copyToSet, is(initialSet));
    }

    @Test
    public void shouldIterateAndHandleRemove()
    {
        final Collection<Integer> initialSet = new HashSet<>();

        final int count = 11;
        for (int i = 0; i < count; i++)
        {
            final String value = Integer.toString(i);
            classToTest.put(i, value);
            initialSet.add(i);
        }

        final Collection<Integer> copyOfSet = new HashSet<>();

        int i = 0;
        for (final Iterator<Integer> iter = classToTest.keySet().iterator(); iter.hasNext();)
        {
            final Integer item = iter.next();
            if (i++ == 7)
            {
                iter.remove();
            }
            else
            {
                copyOfSet.add(item);
            }
        }

        final int reducedSetSize = count - 1;
        assertThat(initialSet.size(), is(count));
        assertThat(classToTest.size(), is(reducedSetSize));
        assertThat(copyOfSet.size(), is(reducedSetSize));
    }

    @Test
    public void shouldIterateEntries()
    {
        final int count = 11;
        for (int i = 0; i < count; i++)
        {
            final String value = Integer.toString(i);
            classToTest.put(i, value);
        }

        iterateEntries();
        iterateEntries();
        iterateEntries();

        final String testValue = "Wibble";
        for (final Entry<Integer, String> entry : classToTest.entrySet())
        {
            assertThat(String.valueOf(entry.getKey()), equalTo(entry.getValue()));

            if (entry.getKey() == 7)
            {
                entry.setValue(testValue);
            }
        }

        assertThat(classToTest.get(7), equalTo(testValue));
    }

    private void iterateEntries()
    {
        for (final Entry<Integer, String> entry : classToTest.entrySet())
        {
            assertThat(String.valueOf(entry.getKey()), equalTo(entry.getValue()));
        }
    }

    @Test
    public void shouldGenerateStringRepresentation()
    {
        final int[] testEntries = {3, 1, 19, 7, 11, 12, 7};

        for (final int testEntry : testEntries)
        {
            classToTest.put(testEntry, String.valueOf(testEntry));
        }

        final String mapAsAString = "{12=12, 11=11, 7=7, 19=19, 3=3, 1=1}";
        assertThat(classToTest.toString(), equalTo(mapAsAString));
    }

    @Test
    public void testPutAllFromAMapWithSameValueType()
    {
        classToTest.put(1, "string 1");
        classToTest.put(2, "string 2");
        final Map<Integer, String> anotherOpenHashMap = new HashMap<>();
        anotherOpenHashMap.put(3, "string 3");
        anotherOpenHashMap.put(4, "string 4");

        classToTest.putAll(anotherOpenHashMap);

        assertEquals("Wrong map size", 4, classToTest.size());
        assertThat(classToTest, hasEntry(1, "string 1"));
        assertThat(classToTest, hasEntry(2, "string 2"));
        assertThat(classToTest, hasEntry(3, "string 3"));
        assertThat(classToTest, hasEntry(4, "string 4"));
    }

    @Test
    public void testHashMapCastError()
    {
        classToTest.put(1, "string");
        assertNull("key of incorrect type does not return null", classToTest.get(new Date()));
    }

    @Test
    public void testEntrySet()
    {
        classToTest.put(1, "abc");
        classToTest.put(2, "def");

        final Iterator<Entry<Integer, String>> newiter = classToTest.entrySet().iterator();

        final Entry<Integer, String> first = newiter.next();
        final Integer expectedKey = first.getKey();
        final String expectedValue = first.getValue();

        newiter.next();

        assertEquals(expectedKey, first.getKey());
        assertEquals(expectedValue, first.getValue());
    }

}
