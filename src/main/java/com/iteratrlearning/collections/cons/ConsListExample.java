package com.iteratrlearning.collections.cons;

public class ConsListExample {
    public static void main(String[] args) {

        ConsList<String> list = ConsList.of("Luke Skywalker");
        list.add("Anakin Skywalker");
        System.out.println(list);

        ConsList<String> listModified = list.add("Darth Maul");
        System.out.println(listModified);
    }
}
