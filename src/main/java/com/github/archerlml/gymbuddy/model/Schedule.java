package com.github.archerlml.gymbuddy.model;


import com.annimon.stream.Stream;
import com.github.archerlml.gymbuddy.util.TimeUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.annotations.NonNull;

/**
 * Created by archerlml on 11/2/17.
 */

public class Schedule extends UserEntity {

    private Map<String, Exercise> exercises = new HashMap<>();

    public List<Exercise> getExercises() {
        return Stream.of(exercises.entrySet())
                .map(Map.Entry::getValue)
                .sortBy(Exercise::getStartTime)
                .collect(com.annimon.stream.Collectors.toList());
    }

    public void setExercises(List<Exercise> exercises) {
        Stream.of(exercises)
                .forEach(exercise -> this.exercises.put(exercise.id, exercise));
    }

    @NonNull
    public List<Exercise> exerciseOfDay(int day) {
        List<Exercise> exerciseList = new ArrayList<>();
        for (Map.Entry<String, Exercise> entry : exercises.entrySet()) {
            if (entry.getValue().getStartDay() == day) {
                exerciseList.add(entry.getValue());
            }
        }
        return exerciseList;
    }

    @NonNull
    public List<Exercise> exerciseOfToday() {
        return exerciseOfDay(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1);
    }

    public int todayTotal() {
        List<Exercise> exercises = exerciseOfToday();
        int total = 0;
        for (Exercise exercise : exercises) {
            total += exercise.duration();
        }
        return total;
    }

    public int todayPassed() {
        List<Exercise> exercises = exerciseOfToday();
        int now = TimeUtil.getMinuteOfWeek(System.currentTimeMillis());
        int total = 0;
        for (Exercise exercise : exercises) {
            int minute = TimeUtil.getMinuteOfWeek(exercise.getStartTimeToday().getTimeInMillis());
            if (now > minute) {
                total += Math.min(exercise.duration(), now - minute);
            }
        }
        return total;
    }

    public Exercise getExercise(String id) {
        return exercises.get(id);
    }

    public void setExercise(Exercise exercise) {
        exercises.put(exercise.id, exercise);
    }

    public Exercise removeExercise(String eid) {
        return exercises.remove(eid);
    }

}
