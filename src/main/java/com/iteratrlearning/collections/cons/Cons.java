package com.iteratrlearning.collections.cons;

public class Cons<T> implements ConsList<T> {

    final private T head;
    final private ConsList<T> tail;

    public Cons(T head, ConsList<T> tail) {
        this.head = head;
        this.tail = tail;
    }

    @Override
    public ConsList<T> add(T e) {
        return new Cons(e, this);
    }

    @Override
    public String toString() {

        return this.head.toString() + ":" + tail.toString();
    }
}
