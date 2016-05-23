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

package com.insightfullogic;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Fork(1)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class MyBenchmark
{

    @Param({"10", "100", "10000", "1000000"})
    int size;

    Map<Integer, String> ourMap = new OpenHashMap<>();
    Map<Integer, String> defaultMap = new HashMap<>();
    List<Integer> indexes;
    List<String> values;

    @Setup
    public void setup()
    {
        for (int i = 0; i < size; i++)
        {
            ourMap.put(i, String.valueOf(i));
            defaultMap.put(i, String.valueOf(i));
        }

        indexes = new ArrayList<>();
        values = new ArrayList<>();

        Random random = new Random(100);
        for (int i = 1; i <= 100; i++)
        {
            int index = random.nextInt(size);
            indexes.add(index);
            values.add(String.valueOf(index));
        }
    }

    @Benchmark
    public void ourGet(final Blackhole blackhole)
    {
        final Map<Integer, String> ourMap = this.ourMap;
        for (int index : indexes)
        {
            blackhole.consume(ourMap.get(index));
        }
    }

    @Benchmark
    public void defaultGet(final Blackhole blackhole)
    {
        final Map<Integer, String> defaultMap = this.defaultMap;
        for (int index : indexes)
        {
            blackhole.consume(defaultMap.get(index));
        }
    }

    @Benchmark
    public void ourPut(final Blackhole blackhole)
    {
        final Map<Integer, String> ourMap = this.ourMap;
        final List<Integer> indexes = this.indexes;
        final List<String> values = this.values;
        for (int i = 0, n = indexes.size(); i < n; i++)
        {
            blackhole.consume(ourMap.put(indexes.get(i), values.get(i)));
        }
    }

    @Benchmark
    public void defaultPut(final Blackhole blackhole)
    {
        final Map<Integer, String> ourMap = this.ourMap;
        final List<Integer> indexes = this.indexes;
        final List<String> values = this.values;
        for (int i = 0, n = indexes.size(); i < n; i++)
        {
            blackhole.consume(defaultMap.put(indexes.get(i), values.get(i)));
        }
    }

}
