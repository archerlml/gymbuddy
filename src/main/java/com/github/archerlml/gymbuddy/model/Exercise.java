package com.github.archerlml.gymbuddy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.archerlml.gymbuddy.util.TimeUtil;
import com.github.archerlml.gymbuddy.util.Util;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created by archerlml on 11/2/17.
 */

public class Exercise {
    public String id;

    private int startTime;

    private int endTime;

    public Set<String> workouts = new HashSet<>();

    public Match match;

    public String location;

    public String locationName;

    private static final int MIN_PER_DAY = 60 * 24;
    private static final int MIN_PER_HOUR = 60;

    public static Exercise gen() {
        Exercise exercise = new Exercise();
        exercise.id = UUID.randomUUID().toString();
        return exercise;
    }

    @JsonIgnore
    public long duration() {
        return endTime - startTime;
    }

    @JsonIgnore
    public int getStartDay() {
        return TimeUtil.toDay(startTime);
    }

    @JsonIgnore
    public int getStartHour() {
        return TimeUtil.toHour(startTime);
    }

    @JsonIgnore
    public int getStartMinute() {
        return TimeUtil.toMinute(startTime);
    }

    @JsonIgnore
    public int getEndDay() {
        return TimeUtil.toDay(endTime);
    }

    @JsonIgnore
    public int getEndHour() {
        return TimeUtil.toHour(endTime);
    }

    @JsonIgnore
    public int getEndMinute() {
        return TimeUtil.toMinute(endTime);
    }


    public Exercise setStartTime(int day, int hour, int minute) {
        startTime = day * MIN_PER_DAY + hour * MIN_PER_HOUR + minute;
        return this;
    }

    public Exercise setEndTime(int day, int hour, int minute) {
        endTime = day * MIN_PER_DAY + hour * MIN_PER_HOUR + minute;
        return this;
    }

    private static Calendar minOfWeekToCalendar(int minOfWeek) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DATE, c.get(Calendar.DATE) - (c.get(Calendar.DAY_OF_WEEK) - 1) + (TimeUtil.toDay(minOfWeek)));
        c.set(Calendar.HOUR_OF_DAY, TimeUtil.toHour(minOfWeek));
        c.set(Calendar.MINUTE, TimeUtil.toMinute(minOfWeek));
        c.set(Calendar.SECOND, 0);
        return c;
    }

    @JsonIgnore
    public Calendar getStartTimeToday() {
        return minOfWeekToCalendar(startTime);
    }

    @JsonIgnore
    public Calendar getEndTimeToday() {
        return minOfWeekToCalendar(endTime);
    }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    @JsonIgnore
    public String getExerciseDesc() {
        return Util.join(", ", workouts);
    }

}
