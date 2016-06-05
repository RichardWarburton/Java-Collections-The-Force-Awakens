package com.iteratrlearning.collections.cons;

public interface ConsList<T> {

    static <T> ConsList<T> of(T e) {

        return new Cons(e, new Nill<>());
    }
    static <T> ConsList<T> empty() {
        return new Nill<>();
    }

    ConsList<T> add(T e);
}
