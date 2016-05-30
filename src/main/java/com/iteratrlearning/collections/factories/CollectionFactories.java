package com.iteratrlearning.collections.factories;

import java.util.*;

public class CollectionFactories {

    public static void main(String[] args) {

        java.util.List<String> names = List.of("Anakin", "Darth Vader");
        System.out.println(names);

        java.util.Map<String, Integer> ages = Map.of("Anakin", 19);
        System.out.println(ages);

    }


    // code below adapted from Java 9
    // see http://cr.openjdk.java.net/~smarks/reviews/8048330/webrev.20150806/
    interface List<E> extends java.util.List<E> {
        static <E> ListN<E> of(E... input) {
            return new ListN<>(input);
        }
    }

    interface Map<K, V> extends java.util.Map<K, V> {
        static <K,V> Map1<K,V> of(K k1, V v1) {
            return new Map1<>(k1, v1);
        }
    }

    static class ListN<E> extends AbstractList<E> {
        final E[] elements;

        @SafeVarargs
        @SuppressWarnings("unchecked")
        ListN(E... input) {
            Objects.requireNonNull(input);
            elements = (E[])new Object[input.length];
            for (int i = 0; i < input.length; i++) {
                elements[i] = Objects.requireNonNull(input[i]);
            }
        }

        @Override
        public int size() {
            return elements.length;
        }

        @Override
        public E get(int index) {
            return elements[index];
        }

        @Override
        public String toString() {
            return Arrays.toString(elements);
        }
    }

    static class Map1<K, V> extends AbstractMap<K,V> {
        final K k0;
        final V v0;

        Map1(K k0, V v0) {
            this.k0 = Objects.requireNonNull(k0);
            this.v0 = Objects.requireNonNull(v0);
        }

        @Override
        public Set<Map.Entry<K,V>> entrySet() {
            return new AbstractSet<Map.Entry<K,V>>() {
                boolean hasNext = true;

                @Override
                public int size() {
                    return 1;
                }

                @Override
                public Iterator<Map.Entry<K,V>> iterator() {
                    return new Iterator<Map.Entry<K,V>>() {
                        @Override
                        public boolean hasNext() {
                            return hasNext;
                        }

                        @Override
                        public Map.Entry<K,V> next() {
                            if (hasNext) {
                                hasNext = false;
                                return new AbstractMap.SimpleImmutableEntry<>(k0, v0);
                            } else {
                                throw new NoSuchElementException();
                            }
                        }
                    };
                }
            };
        }
    }
}
