package com.iteratrlearning.collections.performance;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Test the collision rates from the benchmark
 */
public class CollisionTest
{
    public static void main(String[] args)
    {
        final double collisionProb = 1.0;
        final int size = 1_000_000;
        Random random = new Random();
        random.setSeed(666);
        final int numberOfHashes = (int) (size * collisionProb);
        final Set<Integer> hashes = new HashSet<>();
        for (int i = 0; i < numberOfHashes;)
        {
            if(hashes.add(random.nextInt(size)))
            {
                i++;
            }
        }
        System.out.println(hashes.size());
    }
}
