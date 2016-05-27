package com.insightfullogic.collections.performance;

public class InComparableKey
{

    private final int value;
    private final int hash;

    public InComparableKey(final int value, final int hash)
    {
        this.value = value;
        this.hash = hash;
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
        return hash;
    }
}
