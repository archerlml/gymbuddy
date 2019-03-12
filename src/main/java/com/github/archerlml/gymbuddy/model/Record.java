package com.github.archerlml.gymbuddy.model;

import com.github.archerlml.gymbuddy.util.TimeUtil;

/**
 * Created by archerlml on 11/28/17.
 */

public class Record {
    public long time;
    public float value;

    public Record() {
    }

    public Record(long time, float value) {
        this.time = time;
        this.value = value;
    }

    public String toMMDD() {
        return TimeUtil.formatTime(time, "MM/dd");
    }
}
