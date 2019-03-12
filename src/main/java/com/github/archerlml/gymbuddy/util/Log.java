package com.github.archerlml.gymbuddy.util;

public class Log {
    private static final int HOST_CLASS_INDEX = 4;

    public static void v(Object... msg) {
        android.util.Log.v(com.github.archerlml.gymbuddy.util.Util.getClassNameByStackIndex(HOST_CLASS_INDEX),
                com.github.archerlml.gymbuddy.util.Util.getString(com.github.archerlml.gymbuddy.util.Util.getHostFunctionName(HOST_CLASS_INDEX),
                        "(): ", com.github.archerlml.gymbuddy.util.Util.getString(msg)));
    }

    public static void i(Object... msg) {
        android.util.Log.i(com.github.archerlml.gymbuddy.util.Util.getClassNameByStackIndex(HOST_CLASS_INDEX),
                com.github.archerlml.gymbuddy.util.Util.getString(com.github.archerlml.gymbuddy.util.Util.getHostFunctionName(HOST_CLASS_INDEX),
                        "(): ", com.github.archerlml.gymbuddy.util.Util.getString(msg)));
    }

    public static void d(Object... msg) {
        android.util.Log.d(com.github.archerlml.gymbuddy.util.Util.getClassNameByStackIndex(HOST_CLASS_INDEX),
                com.github.archerlml.gymbuddy.util.Util.getString(com.github.archerlml.gymbuddy.util.Util.getHostFunctionName(HOST_CLASS_INDEX),
                        "(): ", com.github.archerlml.gymbuddy.util.Util.getString(msg)));
    }

    public static void e(Object... msg) {
        android.util.Log.e(com.github.archerlml.gymbuddy.util.Util.getClassNameByStackIndex(HOST_CLASS_INDEX),
                com.github.archerlml.gymbuddy.util.Util.getString(com.github.archerlml.gymbuddy.util.Util.getHostFunctionName(HOST_CLASS_INDEX),
                        "(): ", com.github.archerlml.gymbuddy.util.Util.getString(msg)));
    }
}
