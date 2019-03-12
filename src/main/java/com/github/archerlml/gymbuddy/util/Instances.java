package com.github.archerlml.gymbuddy.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by archerlml on 10/24/17.
 */


public class Instances<T> {

    private Map<Class, Object> instances = new HashMap<>();


    public T getOrNew(Class<? extends T> tClass) {
        Object o = instances.get(tClass);
        if (o == null) {
            try {
                o = tClass.newInstance();
                instances.put(tClass, o);
            } catch (InstantiationException | IllegalAccessException e) {
                Log.e(e);
            }
        }
        return (T) o;
    }

    public T getOrPut(Class<? extends T> tClass, T instance) {
        Object o = instances.get(tClass);
        if (o == null) {
            o = instance;
            instances.put(tClass, instance);
        }
        return (T) o;
    }

    public void put(T instance) {
        instances.put(instance.getClass(), instance);
    }

    public void put(Class<? extends T> tClass, Object instance) {
        instances.put(tClass, instance);
    }

    public boolean exist(Class<? extends T> tClass) {
        return instances.containsKey(tClass);
    }

    public void remove(Class<?>... classes) {
        for (Class<?> cls : classes) {
            instances.remove(cls);
        }
    }

    public void clear() {
        instances.clear();
    }
}
