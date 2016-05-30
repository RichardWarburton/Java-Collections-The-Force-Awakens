package com.iteratrlearning.collections.bugs;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IterateAndRemoveFixed {
    public static void main(String[] args) {
        List<String> jedis = Stream.of("Luke", "yoda").collect(Collectors.toList());

        System.out.println(jedis);

        jedis.removeIf(jedi -> Character.isLowerCase(jedi.charAt(0)));

        System.out.println(jedis);
    }
}
