package com.iteratrlearning.collections.cons;

public class ConsListExample {
    public static void main(String[] args) {
        ConsList<String> list = ConsList.of("Hi");
        list.add("World");
        System.out.println(list);

        ConsList<String> listModified = list.add("UK");
        System.out.println(listModified);
    }
}
