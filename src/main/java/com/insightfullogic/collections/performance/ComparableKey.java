package com.insightfullogic.collections.performance;

public class ComparableKey implements Comparable<ComparableKey>
{
    public static final int SIZE = 1_000_000;

    private final int value;
    private final int hash;

    public ComparableKey(final int value, final int hash)
    {
        this.value = value;
        this.hash = hash;
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
        return hash;
    }
}
