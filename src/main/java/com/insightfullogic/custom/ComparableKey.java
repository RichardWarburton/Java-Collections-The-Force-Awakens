package com.insightfullogic.custom;

import java.util.stream.IntStream;

public class ComparableKey implements Comparable<ComparableKey>
{
    public static final int SIZE = 10_000;

    public static final ComparableKey[] keys;

    static
    {
        keys = IntStream.range(0, SIZE).mapToObj(ComparableKey::new).toArray(ComparableKey[]::new);
    }

    private final int value;

    public ComparableKey(final int value)
    {
        this.value = value;
    }

    public int compareTo(final ComparableKey o)
    {
        return Integer.compare(value, o.value);
    }

    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ComparableKey that = (ComparableKey) o;

        return value == that.value;
    }

    public int hashCode()
    {
        return value;
    }
}
