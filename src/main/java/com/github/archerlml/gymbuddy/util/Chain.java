package com.github.archerlml.gymbuddy.util;

import com.github.archerlml.gymbuddy.application.UserData;

/**
 * Created by archerlml on 11/25/17.
 */

public class Chain<From> {
    Object lastElement = null;
    private UserData userData;

    private Chain() {

    }

    public UserData getUserData() {
        return userData;
    }

    public interface Converter<From, To> {
        To to(From from);
    }

    public interface Plugin<From> {
        void onGet(From from);
    }

    public interface Filter<From> {
        boolean filter(From from);
    }

    public static <T> Chain<T> from(T o) {
        Chain<T> chain = new Chain<>();
        chain.lastElement = o;
        return chain;
    }

    public <To> Chain<To> of(Converter<From, To> t) {
        Chain<To> chain = new Chain<>();
        if (isPresent()) {
            chain.lastElement = t.to(get());
        }
        return chain;
    }

    public Chain<From> then(Plugin<From> t) {
        if (isPresent()) {
            t.onGet(get());
        }
        return this;
    }

    public Chain<From> filter(Filter<From> t) {
        if (isPresent() && !t.filter(get())) {
            lastElement = null;
        }
        return this;
    }

    public Chain<From> or(Runnable runnable) {
        if (!isPresent()) {
            runnable.run();
        }
        return this;
    }

    public boolean isPresent() {
        return lastElement != null;
    }

    public From get() {
        return (From) lastElement;
    }
}
