package com.github.archerlml.gymbuddy.util;

import android.text.TextUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Util {

    public static String getClassNameByStackIndex(int index) {
        try {
            String name = Thread.currentThread().getStackTrace()[index]
                    .getClassName();
            int dot = name.lastIndexOf('.');
            return name.substring(dot + 1);
        } catch (Exception e) {
        }
        return "";
    }

    public static String getHostFunctionName(int index) {
        try {
            return Thread.currentThread().getStackTrace()[index]
                    .getMethodName();
        } catch (Exception e) {
        }
        return "unknown method";
    }

    public static String getString(Object... objects) {
        if (objects == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Object o : objects) {
            if (o != null) {
                sb.append(o.toString());
            }
        }
        return sb.toString();
    }

    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public static boolean isEmpty(Collection c) {
        return c == null || c.isEmpty();
    }

    public static boolean or(Boolean... objs) {
        for (Boolean b : objs) {
            if (b != null) {
                return b;
            }
        }
        return false;
    }

    public static <T> T or(T... objs) {
        for (T t : objs) {
            if (t != null) {
                return t;
            }
        }
        return null;
    }

    private static ObjectMapper objectMapper;

    public static ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }
        return objectMapper;
    }


    public static Map<String, Object> objToMap(Object object) {
        return jsonToMap(objToJson(object));
    }

    public static String objToJson(Object object) {
        try {
            return getObjectMapper().writeValueAsString(object);
        } catch (Exception e) {
        }
        return null;
    }

    public static <T> T jsonToObj(String json, Class<?> cls) {
        try {
            return (T) getObjectMapper().readValue(json, cls);
        } catch (Exception e) {
        }
        return null;
    }

    public static <T> List<T> jsonToList(String json, Class<?> cls) {
        try {
            return getObjectMapper().readValue(json, getObjectMapper().getTypeFactory().constructCollectionType(ArrayList.class, cls));
        } catch (Exception e) {
            Log.e(e);
        }
        return null;
    }

    public static Map<String, Object> jsonToMap(String json) {
        try {
            return getObjectMapper().readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
        }
        return null;
    }

    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static String join(String s, Collection<String> objects) {
        if (isEmpty(objects)) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        Iterator<String> iterator = objects.iterator();
        stringBuilder.append(iterator.next());
        while (iterator.hasNext()) {
            stringBuilder.append(s);
            stringBuilder.append(iterator.next());
        }
        return stringBuilder.toString();
    }

    public static Map<Object, Object> getMap(Object... objects) {
        Map<Object, Object> map = new HashMap<>();
        boolean isKey = false;
        Object key = null;
        for (Object object : objects) {
            isKey = !isKey;

            if (isKey) {
                key = object;
                continue;
            }

            map.put(key, object);
        }
        return map;
    }

    public interface Function<T, R> {
        R apply(T t);
    }

    public static <From, To> List<To> map(List<From> src, Function<? super From, ? extends To> mapper) {
        List<To> result = new ArrayList<>();
        for (From from : src) {
            result.add(mapper.apply(from));
        }
        return result;
    }

    private static AtomicInteger mAtomicInteger;

    public static int nextInt() {
        if (mAtomicInteger == null) {
            mAtomicInteger = new AtomicInteger(0);
        }
        return mAtomicInteger.incrementAndGet();
    }

    public static <T> T clone(T t) {
        return t == null ? null : Util.jsonToObj(Util.objToJson(t), t.getClass());
    }

    public static <T> List<T> clone(List<T> t, Class<?> cls) {
        if (t == null) {
            return null;
        }
        return Util.jsonToList(Util.objToJson(t), cls);
    }

    public static Float parseFloat(String s) {
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException e) {
            Log.e(e);
        }
        return null;
    }
}
