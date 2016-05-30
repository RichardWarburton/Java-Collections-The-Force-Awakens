package com.iteratrlearning.collections.bugs;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IterateAndRemove {
    public static void main(String[] args) {
        List<String> jedis = Stream.of("Luke", "yoda").collect(Collectors.toList());

        System.out.println(jedis);

        for (String jedi: jedis) {
            if (Character.isLowerCase(jedi.charAt(0))) {
                jedis.remove(jedi);
            }
        }

        System.out.println(jedis);
    }
}
