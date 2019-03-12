package com.github.archerlml.gymbuddy.application;

import android.content.Context;
import android.content.SharedPreferences;

import com.github.archerlml.gymbuddy.util.Log;
import com.github.archerlml.gymbuddy.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by archerlml on 11/11/17.
 */

public abstract class Data {
    SharedPreferences sharedPreferences;

    private Map<Object, Object> caches = new HashMap<>();

    protected abstract String getSharedPreferencesName();

    private SharedPreferences getSharedPreferences() {
        if (sharedPreferences == null) {
            sharedPreferences = GymBuddyApplication.getApp().getSharedPreferences(getSharedPreferencesName(), Context.MODE_PRIVATE);
        }
        return sharedPreferences;
    }

    public <T> void put(T t) {
        put(t.getClass().getSimpleName(), t);
    }

    public <T> void putList(String key, List<T> list, Class<?> cls) {
        getSharedPreferences().edit().putString(key, Util.objToJson(list)).apply();
        caches.put(key, Util.clone(list, cls));
    }

    public void put(String key, Object object) {
        getSharedPreferences().edit().putString(key, Util.objToJson(object)).apply();
        caches.put(key, Util.clone(object));
    }

    public <T> T get(Class<T> tClass) {
        T t = (T) caches.get(tClass);
        if (t != null) {
            return Util.clone(t);
        }
        t = Util.jsonToObj(getSharedPreferences().getString(tClass.getSimpleName(), null), tClass);
        if (t != null) {
            put(t);
        }
        return t;
    }

    private String getListKey(Class<?> cls, String tag) {
        return Util.getString("List.", cls.getSimpleName(), tag == null ? "" : Util.getString(".", tag.toLowerCase()));
    }

    public <T> void putList(List<T> list, Class<?> tClass) {
        putList(list, tClass, null);
    }

    public <T> void putList(List<T> list, Class<?> tClass, String tag) {
        putList(getListKey(tClass, tag), list, tClass);
    }

    public <T> List<T> getList(Class<T> tClass) {
        return getList(tClass, null);
    }

    public <T> List<T> getList(Class<T> tClass, String tag) {
        String key = getListKey(tClass, tag);
        List<T> t = (List<T>) caches.get(key);
        if (t != null) {
            Log.i(tag, " ", Util.objToJson(t));
            return t;
        }
        t = Util.jsonToList(getSharedPreferences().getString(key, null), tClass);
        if (t != null) {
            putList(t, tClass);
        }
        Log.i(tag, " ", Util.objToJson(t));
        return t;
    }

    public <T> List<T> getOrNewList(Class<T> tClass) {
        return getOrNewList(tClass, null);
    }

    public <T> List<T> getOrNewList(Class<T> tClass, String tag) {
        List<T> ts = getList(tClass, tag);
        return ts == null ? new ArrayList<T>() : ts;
    }
}
