package com.iteratrlearning.collections.memory;

import com.github.andrewoma.dexx.collection.Vector;
import com.github.krukow.clj_lang.PersistentVector;
import org.openjdk.jol.info.GraphLayout;

import java.util.*;
import java.util.ArrayList;
import java.util.function.Supplier;

public class PersistentCollections {
    public static final int SIZE = 1_000;

    public static void main(String[] args) {

        // boxed array
        Integer[] dataArray = new Integer[SIZE];
        calculateSizeArray(dataArray, SIZE);

        // java.util.ArrayList
        calculateSizeCollection(ArrayList::new, SIZE);

        // Clojure-ds
        PersistentVector<Integer> persistentVector = PersistentVector.emptyVector();
        calculateSizePersistentVector(persistentVector, SIZE);

        // Scala-ds
        com.github.andrewoma.dexx.collection.Vector<Integer> scalaVector = Vector.empty();
        calculateSizeScalaVector(scalaVector, SIZE);

    }

    public static void calculateSizeArray(Integer[] dataArray, int size) {
        for(int i = 0; i < size; i++) {
            dataArray[i] = i;
        }
        System.out.println(GraphLayout.parseInstance(dataArray).toFootprint());
    }

    public static void calculateSizeCollection(Supplier<Collection<Integer>> supplier, int size) {
        Collection<Integer> collection = supplier.get();
        for(int i = 0; i < size; i++) {
            collection.add(i);
        }
        System.out.println(GraphLayout.parseInstance(collection).toFootprint());
    }

    public static void calculateSizePersistentVector(com.github.krukow.clj_ds.PersistentVector<Integer> data, int size) {
        for(int i = 0; i < size; i++) {
            data = data.plus(i);
        }
        System.out.println(GraphLayout.parseInstance(data).toFootprint());
    }

    public static void calculateSizeScalaVector(com.github.andrewoma.dexx.collection.Vector<Integer> data, int size) {
        for(int i = 0; i < size; i++) {
            data = data.append(i);
        }
        System.out.println(GraphLayout.parseInstance(data).toFootprint());
    }
}
