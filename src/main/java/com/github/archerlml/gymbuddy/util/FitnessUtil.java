package com.github.archerlml.gymbuddy.util;

import android.app.Activity;
import android.content.Intent;
import android.util.LongSparseArray;
import android.util.SparseArray;

import com.github.archerlml.gymbuddy.activity.BaseActivity;
import com.github.archerlml.gymbuddy.application.ApplicationData;
import com.github.archerlml.gymbuddy.model.Figure;
import com.github.archerlml.gymbuddy.model.Record;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * Created by archerlml on 11/1/17.
 */

@Singleton
public class FitnessUtil {
    private SparseArray<Callback> callbacks = new SparseArray<>();

    public interface Callback {
        void onGranted();
    }

    @Inject
    ApplicationData applicationData;

    @Inject
    public FitnessUtil() {
        this.applicationData = applicationData;
    }

    private boolean showDataSet(DataSet dataSet, Figure figure) {
        boolean changed = false;
        for (DataPoint dp : dataSet.getDataPoints()) {
            for (Field field : dp.getDataType().getFields()) {
                Figure.Type type = Figure.Type.forValue(field.getName());
                if (type == null) {
                    continue;
                }
                Float value = null;
                try {
                    value = dp.getValue(field).asFloat();
                } catch (Exception e) {
                    Log.v(e);
                }
                if (value == null) {
                    try {
                        value = (float) dp.getValue(field).asInt();
                    } catch (Exception e) {
                        Log.v(e);
                    }
                }
                if (value != null) {
                    long startTime = dp.getStartTime(TimeUnit.MILLISECONDS);
                    LongSparseArray<Record> records = figure.figureAsSparseArray(type);
                    if (records == null) {
                        records = new LongSparseArray<>();
                        figure.setFigure(type, records);
                    }
                    records.put(startTime, new Record(startTime, value));
                    changed = true;
                }
            }
        }

        return changed;
    }

    long lastUpdateTime = 0;
    boolean parsing = false;
    private static final long MS_PER_DAY = 24 * 60 * 60 * 1000;

    private void doReadData(BaseActivity activity) {
        long now = System.currentTimeMillis();
        if (now - lastUpdateTime < MS_PER_DAY) {
            Log.i("skip lastUpdateTime = ", TimeUtil.formatTime(lastUpdateTime, "HHmm"));
            return;
        }
        if (parsing) {
            Log.i("skip parsing");
            return;
        }
        parsing = true;

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.YEAR, -1);
        long startTime = cal.getTimeInMillis();

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        Fitness.getHistoryClient(activity,
                GoogleSignIn.getLastSignedInAccount(activity))
                .readData(readRequest)
                .addOnSuccessListener(dataReadResponse -> {
                    Figure figure = applicationData.getUserData().getOrNewMy(Figure.class);
                    boolean changed = false;
                    for (Bucket bucket : dataReadResponse.getBuckets()) {
                        List<DataSet> dataSets = bucket.getDataSets();
                        for (DataSet dataSet : dataSets) {
                            if (showDataSet(dataSet, figure)) {
                                changed = true;
                            }
                        }
                    }
                    if (changed) {
                        applicationData.getUserData().putMy(figure, figureOptional -> {
                            lastUpdateTime = now;
                        });
                    }
                })
                .addOnFailureListener(e -> {
                })
                .addOnCompleteListener(task -> {
                });
    }

    public void readData(BaseActivity activity) {
        PermissionUtil.requestPermissions(activity, permissions -> {
            if (!hasPermission(activity)) {
                requestFitnessPermissionIfNeed(activity, () -> doReadData(activity));
                return;
            }

            doReadData(activity);
        }, ACCESS_FINE_LOCATION);
    }

    static FitnessOptions fitnessOptions;

    private static FitnessOptions getFitnessOptions() {
        if (fitnessOptions == null) {
            fitnessOptions = FitnessOptions.builder()
                    .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                    .addDataType(DataType.TYPE_DISTANCE_DELTA)
                    .addDataType(DataType.TYPE_NUTRITION)
                    .build();
        }
        return fitnessOptions;
    }

    public boolean hasPermission(BaseActivity activity) {
        return GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(activity), getFitnessOptions());
    }

    public void requestFitnessPermissionIfNeed(BaseActivity activity, Callback callback) {
        PermissionUtil.requestPermissions(activity, permissions -> {
            if (hasPermission(activity)) {
                callback.onGranted();
                return;
            }

            int requestCode = Util.nextInt();
            callbacks.put(requestCode, callback);
            GoogleSignIn.requestPermissions(
                    activity,
                    requestCode,
                    GoogleSignIn.getLastSignedInAccount(activity),
                    getFitnessOptions());
        }, ACCESS_FINE_LOCATION);

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Callback callback = callbacks.get(requestCode);
        if (callback != null && resultCode == Activity.RESULT_OK) {
            callback.onGranted();
        }
    }
}
