package com.insightfullogic.custom;

public class ComparableKey implements Comparable<ComparableKey>
{
    public static final int SIZE = 10_000;

    public static final ComparableKey[] keys = new ComparableKey[SIZE];

    static
    {
        for (int i = 0; i < SIZE; i++)
        {
            keys[i] = new ComparableKey(i);
        }
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
