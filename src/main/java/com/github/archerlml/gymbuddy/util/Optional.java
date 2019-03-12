package com.github.archerlml.gymbuddy.util;

/**
 * Created by archerlml on 11/9/17.
 */

public class Optional<T> {
    private T t;

    private Optional() {

    }

    public static <T> Optional<T> of(T t) {
        Optional<T> optional = new Optional<>();
        optional.t = t;
        return optional;
    }

    public T get() {
        return t;
    }

    public boolean isPresent() {
        return t != null;
    }
}
