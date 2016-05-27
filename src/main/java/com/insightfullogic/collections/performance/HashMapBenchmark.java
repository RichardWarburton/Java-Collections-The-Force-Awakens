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
    List<InComparableKey> inComparableNonKeys = new ArrayList<>(size);
    List<InComparableKey> inComparableKeys = new ArrayList<>(size);
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
        final int loaded = (int) (size * resizeLoadFactor) - 1;
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
            if (i < loaded)
            {
                comparableLoadedMap.put(comparableKey, value);
                inComparableLoadedMap.put(inComparableKey, value);
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

        System.gc();
    }

    // Baseline to be able to remove overhead of nextKey() operation
    @Benchmark
    public ComparableKey baseline()
    {
        return nextKey();
    }

    @Benchmark
    public String customPut()
    {
        final ComparableKey key = nextKey();
        final String value = values.get(position);
        return comparableEmptyMap.put(key, value);
    }

    @Benchmark
    public String customHitGet()
    {
        final ComparableKey key = nextKey();
        return comparableFullMap.get(key);
    }

    @Benchmark
    public String customMissGet()
    {
        nextPosition();
        final ComparableKey key = comparableNonKeys.get(position);
        return comparableFullMap.get(key);
    }

    @Benchmark
    public String customLoadedGet()
    {
        final ComparableKey key = nextKey();
        return comparableLoadedMap.get(key);
    }

    private ComparableKey nextKey()
    {
        nextPosition();
        return comparableKeys.get(position);
    }

    private void nextPosition()
    {
        position = (position + 1) % size;
    }

    // TODO:
        // Very the capacity vs population ratio more
        // comparable vs incomparable

    // Future TODO:
        // megamorphic equals?
        // Expensive vs Cheap hashCode and Equals methods

    // Done:
        // hash collisions
        // Different keys
        // Different Load Factors
        // Different Size
        // shuffle keys
}
