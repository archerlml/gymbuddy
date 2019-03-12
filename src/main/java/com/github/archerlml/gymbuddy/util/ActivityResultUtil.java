package com.github.archerlml.gymbuddy.util;

import android.app.Activity;
import android.content.Intent;
import android.util.SparseArray;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by archerlml on 11/22/17.
 */

@Singleton
public class ActivityResultUtil {

    public interface ActivityResultListener {
        void onResultOk(Intent returnedIntent);

        void onResultNotOk(Intent returnedIntent);
    }

    public interface ResultOkListen {
        void onResultOk(Intent returnedIntent);
    }

    SparseArray<ActivityResultListener> listeners = new SparseArray<>();
    SparseArray<ResultOkListen> okListeners = new SparseArray<>();

    @Inject
    public ActivityResultUtil() {
    }

    public void startActivityForResult(Activity activity, Intent intent, ActivityResultListener listener) {
        int requestCode = Util.nextInt();
        listeners.put(requestCode, listener);
        activity.startActivityForResult(intent, requestCode);
    }

    public void startActivityForResult(Activity activity, Intent intent, ResultOkListen listener) {
        int requestCode = Util.nextInt();
        okListeners.put(requestCode, listener);
        activity.startActivityForResult(intent, requestCode);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        ActivityResultListener listener = listeners.get(requestCode);
        if (listener != null) {
            if (resultCode == Activity.RESULT_OK) {
                listener.onResultOk(intent);
            } else {
                listener.onResultNotOk(intent);
            }
            return;
        }

        ResultOkListen okListener = okListeners.get(requestCode);
        if (okListener != null) {
            if (resultCode == Activity.RESULT_OK) {
                okListener.onResultOk(intent);
            }
        }
    }
}
