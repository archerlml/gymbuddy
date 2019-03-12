package com.github.archerlml.gymbuddy.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by archerlml on 11/2/17.
 */

public class Event extends UserEntity {

    public long startTime;

    public long endTime;

    public String title;

    public String imageUrl;

    public String description;

    public String location;

    public String locationName;

    public List<Rule> rules;

    public Integer winners;

    public Integer joinners;

    public Integer reward;

    public Set<String> winnerIds = new HashSet<>();

    public boolean isWinner(String uid) {
        return winnerIds.contains(uid);
    }

    public boolean isFinished() {
        return System.currentTimeMillis() > endTime;
    }
}
