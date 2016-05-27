/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.insightfullogic.collections.performance;

import org.openjdk.jmh.annotations.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Fork(1)
@Threads(1)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
public class HashMapBenchmark
{

    @Param({"10", "100", "10000", "100000", "1000000"})
    int size;
    int loadedSize;

    @Param({"0.0", "0.5", "0.9"})
    double collisionProb;

    @Param({"0.5", "0.75", "0.9"})
    float resizeLoadFactor;

    @Param({"OpenHashMap", "JdkMap", "Koloboke" })
    String mapType;

    Map<ComparableKey, String> comparableEmptyMap;
    Map<ComparableKey, String> comparableFullMap;
    Map<ComparableKey, String> comparableLoadedMap;
    Map<InComparableKey, String> inComparableEmptyMap;
    Map<InComparableKey, String> inComparableFullMap;
    Map<InComparableKey, String> inComparableLoadedMap;

    int position = -1;

    // Used to simulate misses (gets that return null) with keys that aren't part of the map
    List<ComparableKey> comparableNonKeys = new ArrayList<>(size);
    List<ComparableKey> comparableKeys = new ArrayList<>(size);
    List<ComparableKey> comparableLoadedKeys = new ArrayList<>();
    List<InComparableKey> inComparableNonKeys = new ArrayList<>(size);
    List<InComparableKey> inComparableKeys = new ArrayList<>(size);
    List<InComparableKey> inComparableLoadedKeys = new ArrayList<>();
    List<String> values = new ArrayList<>(size);

    @Setup
    public void setup()
    {
        final MapFactory factory = MapFactory.valueOf(mapType);
        comparableEmptyMap = factory.make(resizeLoadFactor);
        comparableFullMap = factory.make(resizeLoadFactor);
        comparableLoadedMap = factory.make(resizeLoadFactor);
        inComparableEmptyMap = factory.make(resizeLoadFactor);
        inComparableFullMap = factory.make(resizeLoadFactor);
        inComparableLoadedMap = factory.make(resizeLoadFactor);

        final Random random = new Random();
        final int size = this.size;
        loadedSize = (int) (size * resizeLoadFactor) - 1;
        int last = 0;
        for (int i = 0; i < size; i++)
        {
            final int hash;
            if (random.nextDouble() < collisionProb)
            {
                hash = last;
            }
            else
            {
                last = hash = i;
            }

            final ComparableKey comparableKey = new ComparableKey(i, hash);
            final InComparableKey inComparableKey = new InComparableKey(i, hash);
            final String value = String.valueOf(i);

            comparableKeys.add(comparableKey);
            inComparableKeys.add(inComparableKey);
            values.add(value);
            if (i < loadedSize)
            {
                comparableLoadedMap.put(comparableKey, value);
                inComparableLoadedMap.put(inComparableKey, value);
                comparableLoadedKeys.add(comparableKey);
                inComparableLoadedKeys.add(inComparableKey);
            }
            comparableFullMap.put(comparableKey, value);
            inComparableFullMap.put(inComparableKey, value);
        }

        last = size + 1;
        final int n = 2 * size + 1;
        for (int i = last; i < n; i++)
        {
            final int hash;
            if (random.nextDouble() < collisionProb)
            {
                hash = last;
            }
            else
            {
                last = hash = i;
            }

            comparableNonKeys.add(new ComparableKey(i, hash));
            inComparableNonKeys.add(new InComparableKey(i, hash));
        }

        Collections.shuffle(comparableKeys);
        Collections.shuffle(inComparableKeys);
        Collections.shuffle(comparableNonKeys);
        Collections.shuffle(inComparableNonKeys);
        Collections.shuffle(comparableLoadedKeys);
        Collections.shuffle(inComparableLoadedKeys);

        System.gc();
    }

    // Baseline to be able to remove overhead of nextKey() operation
    @Benchmark
    public ComparableKey baseline()
    {
        return nextComparableKey();
    }

    @Benchmark
    public String putMissingComparable()
    {
        final ComparableKey key = nextComparableKey();
        final String value = values.get(position);
        return comparableEmptyMap.put(key, value);
    }

    @Benchmark
    public String putReplaceComparable()
    {
        final ComparableKey key = nextComparableKey();
        final String value = values.get(position);
        return comparableFullMap.put(key, value);
    }

    @Benchmark
    public String putLoadedComparable()
    {
        final ComparableKey key = nextLoadedComparableKey();
        final String value = values.get(position);
        return comparableLoadedMap.put(key, value);
    }

    @Benchmark
    public String getHitComparable()
    {
        final ComparableKey key = nextComparableKey();
        return comparableFullMap.get(key);
    }

    @Benchmark
    public String getMissComparable()
    {
        nextPosition(size);
        final ComparableKey key = comparableNonKeys.get(position);
        return comparableFullMap.get(key);
    }

    @Benchmark
    public String loadedGetComparable()
    {
        final ComparableKey key = nextLoadedComparableKey();
        return comparableLoadedMap.get(key);
    }

    @Benchmark
    public String putMissingIncomparable()
    {
        final InComparableKey key = nextInComparableKey();
        final String value = values.get(position);
        return inComparableEmptyMap.put(key, value);
    }

    @Benchmark
    public String putReplaceInComparable()
    {
        final InComparableKey key = nextInComparableKey();
        final String value = values.get(position);
        return inComparableFullMap.put(key, value);
    }

    @Benchmark
    public String putLoadedInComparable()
    {
        final InComparableKey key = nextLoadedInComparableKey();
        final String value = values.get(position);
        return inComparableLoadedMap.put(key, value);
    }

    @Benchmark
    public String getHitInComparable()
    {
        final InComparableKey key = nextInComparableKey();
        return inComparableFullMap.get(key);
    }

    @Benchmark
    public String getMissInComparable()
    {
        nextPosition(size);
        final InComparableKey key = inComparableNonKeys.get(position);
        return inComparableFullMap.get(key);
    }

    @Benchmark
    public String loadedGetInComparable()
    {
        final InComparableKey key = nextLoadedInComparableKey();
        return comparableLoadedMap.get(key);
    }

    private ComparableKey nextComparableKey()
    {
        nextPosition(size);
        return comparableKeys.get(position);
    }

    private ComparableKey nextLoadedComparableKey()
    {
        nextPosition(loadedSize);
        return comparableLoadedKeys.get(position);
    }

    private InComparableKey nextInComparableKey()
    {
        nextPosition(size);
        return inComparableKeys.get(position);
    }

    private InComparableKey nextLoadedInComparableKey()
    {
        nextPosition(loadedSize);
        return inComparableLoadedKeys.get(position);
    }

    private void nextPosition(final int size)
    {
        position = (position + 1) % size;
    }

    // Future TODO:
        // megamorphic equals?
        // Expensive vs Cheap hashCode and Equals methods

    // Done:
        // hash collisions
        // Different keys
        // Different Load Factors
        // Different Size
        // shuffle keys
        // Very the capacity vs population ratio more
        // Comparable vs incomparable
}
