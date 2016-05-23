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
    float loadFactor;

    Map<ComparableKey, String> customEmptyMap;
    Map<ComparableKey, String> customFullMap;
    Map<ComparableKey, String> defaultEmptyMap;

    int position = -1;

    // Used to simulate misses (gets that return null) with keys that aren't part of the map
    List<ComparableKey> nonKeys = new ArrayList<>(size);
    List<ComparableKey> keys = new ArrayList<>(size);
    List<String> values = new ArrayList<>(size);

    @Setup
    public void setup()
    {
        customEmptyMap = new OpenHashMapV1<>(8, loadFactor);
        customFullMap = new OpenHashMapV1<>(8, loadFactor);
        defaultEmptyMap = new HashMap<>(8, loadFactor);

        final Random random = new Random();
        final int size = this.size;
        int last = 0;
        for (int i = 0; i < size; i++)
        {
            final int number;
            if (random.nextDouble() < collisionProb)
            {
                number = last;
            }
            else
            {
                last = number = i;
            }

            final ComparableKey key = new ComparableKey(number);
            final String value = String.valueOf(number);

            keys.add(key);
            values.add(value);
            customFullMap.put(key, value);
        }

        Collections.shuffle(keys);

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
        return customEmptyMap.put(key, value);
    }

    @Benchmark
    public String customHitGet()
    {
        final ComparableKey key = nextKey();
        return customFullMap.get(key);
    }

    @Benchmark
    public String customMissGet()
    {
        nextPosition();
        final ComparableKey key = nonKeys.get(position);
        return customFullMap.get(key);
    }

    private ComparableKey nextKey()
    {
        nextPosition();
        return keys.get(position);
    }

    private void nextPosition()
    {
        position = (position + 1) % size;
    }

    // TODO:
        // Miss vs hit
        // capacity vs population
        // Koloboke

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
