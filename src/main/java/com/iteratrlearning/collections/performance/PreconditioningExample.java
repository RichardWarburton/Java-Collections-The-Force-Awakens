package com.iteratrlearning.collections.performance;

import java.util.HashSet;
import java.util.Set;

public class PreconditioningExample
{
    public static void main(String[] args)
    {
        System.out.println("Han".hashCode());
        System.out.println("Chewie".hashCode());
        final int elements = 1000;
        final int bucketCount = 200;
        final Set<Integer> hashes = new HashSet<>();
        final Set<Integer> preconditioned = new HashSet<>();
        for (int x = 0; x < elements; x++)
        {
            for (int y = 0; y < elements; y++)
            {
                final int hash = x * 31 + y;
                hashes.add(hash % bucketCount);
                preconditioned.add(precondition(hash) % bucketCount);
            }
        }
        System.out.println(hashes.size());
        System.out.println(preconditioned.size());
    }

    private static int precondition(int hash)
    {
        return hash ^ (hash >>> 16);
    }
}
