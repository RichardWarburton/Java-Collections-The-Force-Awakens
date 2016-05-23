package com.insightfullogic.custom;

import java.util.stream.IntStream;

import static com.insightfullogic.custom.ComparableKey.SIZE;

public class InComparableKey
{

    public static final InComparableKey[] keys;

    static
    {
        keys = IntStream.range(0, SIZE).mapToObj(InComparableKey::new).toArray(InComparableKey[]::new);
    }

    private final int value;

    public InComparableKey(final int value)
    {
        this.value = value;
    }

    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final InComparableKey that = (InComparableKey) o;

        return value == that.value;
    }

    public int hashCode()
    {
        return value;
    }
}
