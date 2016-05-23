package com.insightfullogic.collections.performance;

public class InComparableKey
{

    public static final InComparableKey[] keys = new InComparableKey[ComparableKey.SIZE];

    static
    {
        for (int i = 0; i < ComparableKey.SIZE; i++)
        {
            keys[i] = new InComparableKey(i);
        }
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
