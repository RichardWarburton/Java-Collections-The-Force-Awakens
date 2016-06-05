package com.iteratrlearning.collections.performance;

import org.openjdk.jmh.annotations.*;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.openjdk.jmh.annotations.Mode.AverageTime;

@Fork(1)
@Warmup(iterations = 3)
@Measurement(iterations = 3)
@State(Scope.Thread)
@BenchmarkMode(AverageTime)
@OutputTimeUnit(NANOSECONDS)
public class HashMapGetBenchmark
{

    @Param({"10", "10000", /*"1000000"*/})
    //@Param({"10000"})
    int size;

    // Actually means the inverse of the collision probability
    @Param({/*"0.1", "0.5",*/ "1.0"})
    double collisionProb;

    @Param({"JdkMap", "Koloboke" })
    String mapType;

    //@Param({"Comparable", "InComparable"})
    @Param({"Comparable"})
    String keyType;

    Map<Object, String> map;

    int successIndex = 0;
    int failIndex = 0;

    Object[] failKeys;
    Object[] successfulKeys;
    List<String> values = new ArrayList<>();

    @Setup
    public void setup()
    {
        final MapFactory mapFactory = MapFactory.valueOf(mapType);
        final KeyFactory keyFactory = KeyFactory.valueOf(keyType);

        map = mapFactory.make();
        System.out.println(map.getClass());
        failKeys = new Object[size];
        successfulKeys = new Object[size];

        // Generate hashes that may collide, but are evenly distributed throughput the space of hashes
        final int size = this.size;
        final Random random = new Random(666);
        final int numberOfHashes = (int) (size * collisionProb);
        final int[] hashes = new int[numberOfHashes];
        final Set<Integer> collisions = new HashSet<>();
        for (int i = 0; i < numberOfHashes;)
        {
            final int value = random.nextInt(size);
            if (collisions.add(value))
            {
                hashes[i] = value;
                i++;
            }
        }

        // Setup keys and values
        for (int i = 0; i < size; i++)
        {
            final int hash = hashes[random.nextInt(numberOfHashes)];
            final String value = String.valueOf(i);

            successfulKeys[i] = keyFactory.make(i, hash);
            failKeys[i] = keyFactory.make(-i, hash);
            values.add(value);
            map.put(successfulKeys[i], value);
        }

        // Randomise layout of keys in memory
        System.gc();
        Collections.shuffle(asList(successfulKeys));
        Collections.shuffle(asList(failKeys));
        System.gc();

        // Add type pollution
        pollute("a", "a");
        pollute(Integer.valueOf(1), "a");
        pollute(Float.valueOf(1.0f), "a");
    }

    private void pollute(final Object key, final String value)
    {
        map.put(key, value);
        if (!Objects.equals(map.get(key), value))
        {
            throw new IllegalStateException();
        }
        map.remove(key);
    }

    // Baseline to be able to remove overhead of nextKey() operation
    /*@Benchmark
    public Object baseline()
    {
        return nextSuccessfulKey();
    }*/

    @Benchmark
    public String getSuccess()
    {
        final Object key = nextSuccessfulKey();
        return map.get(key);
    }

    /*@Benchmark
    public String getFail()
    {
        final Object key = nextFailKey();
        return map.get(key);
    }*/

    private Object nextSuccessfulKey()
    {
        return successfulKeys[successIndex++ & (successfulKeys.length - 1)];
    }

    private Object nextFailKey()
    {
        return failKeys[failIndex++ & (failKeys.length - 1)];
    }

    // Done:
        // hash collisions
        // Different keys
        // Different Size
        // shuffle keys
        // Comparable vs incomparable
}
