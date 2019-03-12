package com.github.archerlml.gymbuddy.util;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.PopupWindow;

import com.github.archerlml.gymbuddy.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by archerlml on 4/5/17.
 */

public class LoadingView {
    private static Map<View, PopupWindow> windows = new HashMap<>();


    public static void showAt(final View target) {
        target.post(() -> {
            PopupWindow window = windows.get(target);
            if (window == null) {
                window = createWindow(target);
                windows.put(target, window);
            }
            int location[] = new int[2];
            target.getLocationOnScreen(location);
            window.showAtLocation(target, Gravity.NO_GRAVITY, location[0], location[1]);
        });
    }

    public static void remove(final View target) {
        PopupWindow window = windows.remove(target);
        if (window != null) {
            window.dismiss();
        }
    }

    public static void removeAll() {
        for (Map.Entry<View, PopupWindow> entry : windows.entrySet()) {
            entry.getValue().dismiss();
        }
        windows.clear();
    }

    private static PopupWindow createWindow(View parent) {
        View popupView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_loading_window, null);
        PopupWindow popupWindow = new PopupWindow(popupView,
                parent.getWidth(), parent.getHeight(), false);
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(false);
        return popupWindow;
    }
}
