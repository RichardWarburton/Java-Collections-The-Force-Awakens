package com.insightfullogic.collections.performance;

public enum KeyFactory
{
    Comparable
        {
            @Override
            public Object make(final int value, final int hash)
            {
                return new ComparableKey(value, hash);
            }
        },
    InComparable
        {
            @Override
            public Object make(final int value, final int hash)
            {
                return new InComparableKey(value, hash);
            }
        };

    public abstract Object make(final int value, final int hash);
}
