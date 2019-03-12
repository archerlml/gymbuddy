package com.github.archerlml.gymbuddy.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserPreference extends UserEntity {
    public String userName;
    public String description;
    public String avatar;
    public Boolean trackActivities;
    public Integer scheduleMinHour;
    public Integer scheduleMaxHour;
    public String gymName;
    public String gymId;

    public static final int EVENT_COST = 10;
    public int balance = 100;
    public List<String> dataTypesToDisplay;
    public Set<String> joinedEvents = new HashSet<>();
    public Set<String> finishedEvents = new HashSet<>();
}
