package com.github.archerlml.gymbuddy.model;


import com.github.archerlml.gymbuddy.util.TimeUtil;

import java.util.Locale;

/**
 * Created by archerlml on 11/2/17.
 */

public class Match {
    public String id;
    public int startTime;
    public int endTime;
    public String peerUid;
    public String peerName;
    public String peerAvatar;
    public String description;
    public String location;
    public String locationName;

    public String toHHMM_HHMM() {
        return String.format(Locale.US, "%02d:%02d ~ %02d:%02d",
                TimeUtil.toHour(startTime),
                TimeUtil.toMinute(startTime),
                TimeUtil.toHour(endTime),
                TimeUtil.toMinute(endTime));
    }
}
