package com.github.archerlml.gymbuddy.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by archerlml on 11/7/17.
 */

public class TimeUtil {

    public static final int MIN_PER_DAY = 60 * 24;
    public static final int MIN_PER_HOUR = 60;

    public static String formatTime(long time, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.US);
        return simpleDateFormat.format(new Date(time));
    }

    public static String formatTime(Calendar time, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.US);
        return simpleDateFormat.format(time.getTime());
    }

    public static Calendar getCalendar(long time) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        return c;
    }

    public static int getMinuteOfWeek(long time) {
        Calendar c = getCalendar(time);
        return c.get(Calendar.DAY_OF_WEEK) * MIN_PER_DAY
                + c.get(Calendar.HOUR_OF_DAY) * MIN_PER_HOUR
                + c.get(Calendar.MINUTE);
    }

    public static int toDay(int minOfWeek) {
        return minOfWeek / MIN_PER_DAY;
    }

    public static int toHour(int minOfWeek) {
        return minOfWeek % MIN_PER_DAY / MIN_PER_HOUR;
    }

    public static int toMinute(int minOfWeek) {
        return minOfWeek % MIN_PER_HOUR;
    }
}
