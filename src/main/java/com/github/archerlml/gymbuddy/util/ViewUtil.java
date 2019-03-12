package com.github.archerlml.gymbuddy.util;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.archerlml.gymbuddy.application.GymBuddyApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by archerlml on 11/14/17.
 */

public class ViewUtil {
    static public void gone(View... views) {
        for (View view : views) {
            view.setVisibility(View.GONE);
        }
    }

    static public void hide(View... views) {
        for (View view : views) {
            view.setVisibility(View.INVISIBLE);
        }
    }

    static public void show(View... views) {
        for (View view : views) {
            view.setVisibility(View.VISIBLE);
        }
    }

    static public <T extends View> T findViewById(View view, int id, Class<T> tClass) {
        return (T) view.findViewById(id);
    }

    public static <T extends View> List<T> getAllChildren(View v, Class<T> tClass) {
        ArrayList<T> views = new ArrayList<>();

        if (tClass.isInstance(v)) {
            views.add((T) v);
        }

        if (!(v instanceof ViewGroup)) {
            return views;
        }

        ViewGroup viewGroup = (ViewGroup) v;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            views.addAll(getAllChildren(child, tClass));
        }
        return views;
    }

    public static void toast(Object... msg) {
        Log.i(msg);
        Toast.makeText(GymBuddyApplication.getApp(), Util.getString(msg), Toast.LENGTH_LONG).show();
    }
}
