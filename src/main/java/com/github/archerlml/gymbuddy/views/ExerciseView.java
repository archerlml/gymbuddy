package com.github.archerlml.gymbuddy.views;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.github.archerlml.gymbuddy.R;
import com.github.archerlml.gymbuddy.activity.BaseActivity;
import com.github.archerlml.gymbuddy.model.Exercise;
import com.github.archerlml.gymbuddy.model.UserPreference;
import com.github.archerlml.gymbuddy.util.Log;
import com.github.archerlml.gymbuddy.util.Util;
import com.github.archerlml.gymbuddy.util.ViewUtil;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.util.List;

import butterknife.BindView;

/**
 * Created by archerlml on 10/28/17.
 */

public class ExerciseView extends TypedBaseView<Exercise> {

    @BindView(R.id.delete_layout)
    public View mDeleteLayout;

    @BindView(R.id.save_layout)
    public View mSaveLayout;

    @BindView(R.id.location_layout)
    public View mlocationLayout;

    @BindView(R.id.location)
    public TextView mLocation;

    public ExerciseView(@NonNull Context context) {
        super(context);
    }

    public ExerciseView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(AttributeSet attrs) {
        super.init(attrs);

        mlocationLayout.setOnClickListener(v -> {
            if (getContext() instanceof BaseActivity) {
                BaseActivity activity = (BaseActivity) getContext();
                Intent intent = null;
                try {
                    intent = new PlacePicker.IntentBuilder().build(activity);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    Log.e(e);
                }

                if (intent != null) {
                    activity.startActivityForResult(intent, data -> {
                        Place place = PlaceAutocomplete.getPlace(getContext(), data);
                        if (place == null) {
                            Log.i("nothing picked");
                            return;
                        }

                        getData().location = place.getId();
                        getData().locationName = place.getName().toString();
                        mLocation.setText(place.getName());
                    });
                }
            }
        });
    }

    final String[] QUARTERS = {"00", "15", "30", "45"};
    final int MIN_PER_QUARTER = 15;

    @Override
    public ExerciseView applyData(Exercise exercise) {
        super.applyData(exercise);

        UserPreference userPreference = getUserData().getOrNewMy(UserPreference.class);
        int minHour = Util.or(userPreference.scheduleMinHour, 6);
        int maxHour = Util.or(userPreference.scheduleMaxHour, 24);

        setNumberSpinner(R.id.from_hour, minHour, maxHour, exercise.getStartHour(), null);
        setNumberSpinner(R.id.from_min, 0, QUARTERS.length - 1, exercise.getStartMinute() / MIN_PER_QUARTER, QUARTERS);
        setNumberSpinner(R.id.to_hour, minHour, maxHour, exercise.getEndHour(), null);
        setNumberSpinner(R.id.to_min, 0, QUARTERS.length - 1, exercise.getEndMinute() / MIN_PER_QUARTER, QUARTERS);

        LinearLayout exerciseSection = findViewById(R.id.exercise_section);
        List<TextView> parts = ViewUtil.getAllChildren(exerciseSection, TextView.class);
        for (TextView textView : parts) {
            if (textView.isClickable()) {
                String target = textView.getText().toString().toLowerCase();
                textView.setSelected(exercise.workouts.contains(target));
                textView.setOnClickListener(v1 -> {
                    if (v1.isSelected()) {
                        getData().workouts.remove(target);
                    } else {
                        getData().workouts.add(target);
                    }
                    v1.setSelected(!v1.isSelected());
                });
            }
        }

        if (exercise.location != null) {
            mLocation.setText(exercise.locationName);
        } else if (userPreference.gymId != null) {
            exercise.location = userPreference.gymId;
            exercise.locationName = userPreference.gymName;
            mLocation.setText(userPreference.gymName);
        }
        return this;
    }

    @Override
    public int layoutId() {
        return R.layout.add_exercise_view;
    }


    private void setNumberSpinner(int id, int min, int max, int value, String[] displayedValues) {
        NumberPicker numberSpinner = findViewById(id);
        numberSpinner.setMinValue(min);
        numberSpinner.setMaxValue(max);
        numberSpinner.setValue(value);
        numberSpinner.setWrapSelectorWheel(false);
        numberSpinner.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        numberSpinner.setOnValueChangedListener(((picker, oldVal, newVal) -> {
            Exercise exercise = getData();
            switch (picker.getId()) {
                case R.id.from_hour:
                    exercise.setStartTime(exercise.getStartDay(), newVal, exercise.getStartMinute());
                    break;
                case R.id.from_min:
                    exercise.setStartTime(exercise.getStartDay(), exercise.getStartHour(), newVal * MIN_PER_QUARTER);
                    break;
                case R.id.to_hour:
                    exercise.setEndTime(exercise.getEndDay(), newVal, exercise.getEndMinute());
                    break;
                case R.id.to_min:
                    exercise.setEndTime(exercise.getEndDay(), exercise.getEndHour(), newVal * MIN_PER_QUARTER);
                    break;
            }
        }));
        if (displayedValues != null) {
            numberSpinner.setDisplayedValues(displayedValues);
        }
    }


}
