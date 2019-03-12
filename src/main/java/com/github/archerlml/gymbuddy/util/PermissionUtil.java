package com.github.archerlml.gymbuddy.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by archerlml on 4/25/17.
 */

public class PermissionUtil {
    private static PermissionUtil sInstance;
    private Map<Integer, Request> mRequestMap;
    private static String TAG = PermissionUtil.class.getSimpleName();

    private PermissionUtil() {
        mRequestMap = new HashMap<>();
    }

    public static PermissionUtil getInstance() {
        if (sInstance == null) {
            sInstance = new PermissionUtil();
        }
        return sInstance;
    }

    public static void requestPermission(Activity activity, PermissionCallback callback, String permission) {
        getInstance().requestPermissionIfNeed(activity, permission, callback);
    }

    public static void requestPermissions(Activity activity, PermissionsCallback callback, String... permissions) {
        getInstance().requestPermissionsIfNeed(activity, permissions, callback);
    }

    public static void requestPermissions(Activity activity, PermissionsGrantedCallback callback, String... permissions) {
        getInstance().requestPermissionsIfNeed(activity, permissions, callback);
    }

    public boolean permissionGranted(Context context, String permission) {
        return ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissionIfNeed(Activity activity, String permission, PermissionCallback callback) {
        PermissionGroup group = groupPermissions(activity, permission);
        if (group.ungranted.isEmpty()) {
            callback.onGranted(permission);
            return;
        }

        int requestCode = Util.nextInt();
        mRequestMap.put(requestCode, new Request(group, callback));
        ActivityCompat.requestPermissions(activity,
                group.ungranted.toArray(new String[group.ungranted.size()]), requestCode);
    }

    private void requestPermissionsIfNeed(Activity activity, String[] permissions, PermissionsCallback callback) {
        PermissionGroup group = groupPermissions(activity, permissions);
        if (group.ungranted.isEmpty()) {
            callback.onGranted(permissions);
            return;
        }

        int requestCode = Util.nextInt();
        mRequestMap.put(requestCode, new Request(group, callback));
        ActivityCompat.requestPermissions(activity,
                group.ungranted.toArray(new String[group.ungranted.size()]), requestCode);
    }

    private void requestPermissionsIfNeed(Activity activity, String[] permissions, PermissionsGrantedCallback callback) {
        PermissionGroup group = groupPermissions(activity, permissions);
        if (group.ungranted.isEmpty()) {
            callback.onGranted(permissions);
            return;
        }

        int requestCode = Util.nextInt();
        mRequestMap.put(requestCode, new Request(group, callback));
        ActivityCompat.requestPermissions(activity,
                group.ungranted.toArray(new String[group.ungranted.size()]), requestCode);
    }

    private static PermissionGroup groupPermissions(@NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionGroup group = new PermissionGroup();
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                group.granted.add(permissions[i]);
            } else {
                group.ungranted.add(permissions[i]);
            }
        }
        return group;
    }

    private PermissionGroup groupPermissions(Context context, @NonNull String... permissions) {
        PermissionGroup group = new PermissionGroup();
        for (String permission : permissions) {
            if (permissionGranted(context, permission)) {
                group.granted.add(permission);
            } else {
                group.ungranted.add(permission);
            }
        }
        return group;
    }

    public static void handlePermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "handlePermissionResult, requestCdoe = " + requestCode);
        Request request = getInstance().mRequestMap.remove(requestCode);
        if (request.callback == null) {
            return;
        }
        PermissionGroup group = groupPermissions(permissions, grantResults);
        group.granted.addAll(request.group.granted);

        if (request.callback instanceof PermissionCallback) {
            PermissionCallback cb = (PermissionCallback) request.callback;
            if (!group.granted.isEmpty()) {
                cb.onGranted(group.granted.get(0));
            } else if (!group.ungranted.isEmpty()) {
                cb.onDenied(group.ungranted.get(0));
            }
            return;
        }

        if (request.callback instanceof PermissionsCallback) {
            PermissionsCallback cb = (PermissionsCallback) request.callback;
            if (!group.granted.isEmpty()) {
                cb.onGranted(group.granted.toArray(new String[group.granted.size()]));
            }
            if (!group.ungranted.isEmpty()) {
                cb.onDenied(group.ungranted.toArray(new String[group.ungranted.size()]));
            }
        }
        if (request.callback instanceof PermissionsGrantedCallback) {
            PermissionsGrantedCallback cb = (PermissionsGrantedCallback) request.callback;
            if (!group.granted.isEmpty()) {
                cb.onGranted(group.granted.toArray(new String[group.granted.size()]));
            }
        }
    }

    private static class Request {
        PermissionGroup group;
        Callback callback;

        Request(PermissionGroup group, Callback callback) {
            this.group = group;
            this.callback = callback;
        }
    }

    private static class PermissionGroup {
        List<String> granted = new ArrayList<>();
        List<String> ungranted = new ArrayList<>();
    }

    private interface Callback {

    }

    public interface PermissionCallback extends Callback {
        void onGranted(String permission);

        void onDenied(String permission);
    }

    public interface PermissionsGrantedCallback extends Callback {
        void onGranted(String[] permissions);
    }

    public interface PermissionsCallback extends Callback {
        void onGranted(String[] permissions);

        void onDenied(String[] permissions);
    }
}
