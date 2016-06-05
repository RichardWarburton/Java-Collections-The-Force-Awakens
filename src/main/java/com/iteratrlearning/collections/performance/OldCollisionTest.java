package com.iteratrlearning.collections.performance;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Test the collision rates from the benchmark
 */
public class OldCollisionTest
{
    public static void main(String[] args)
    {
        for (double collisionProb : Arrays.asList(0.1, 0.5, 0.9))
        {
            final int size = 1_000_000;
            Random random = new Random();
            random.setSeed(666);
            final int numberOfHashes = (int) (size * collisionProb);
            final Set<Integer> hashes = new HashSet<>();
            for (int i = 0; i < numberOfHashes; i++)
            {
                hashes.add(random.nextInt(size));
            }
            System.out.println(hashes.size());
        }

        // 0.1 = 95117  10% collisions
        // 0.5 = 393178 40% collisions
        // 0.9 = 593068 60% collisions
    }
}
