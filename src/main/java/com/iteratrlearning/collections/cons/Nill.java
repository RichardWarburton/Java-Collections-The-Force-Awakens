package com.iteratrlearning.collections.cons;

public class Nill<T> implements ConsList<T> {
    Nill() {}

    @Override
    public ConsList<T> add(T e) {
        return this;
    }

    @Override
    public String toString() {
        return "Nill";
    }
}
