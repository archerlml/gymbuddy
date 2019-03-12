package com.github.archerlml.gymbuddy.model;

import com.google.firebase.database.Exclude;

public class ReportImage {
    @Exclude
    public String path;
    public String uri;
    public int resid;
    public long time;

    public ReportImage path(String path) {
        this.path = path;
        return this;
    }

    public ReportImage uri(String uri) {
        this.uri = uri;
        return this;
    }

    public ReportImage resid(int resid) {
        this.resid = resid;
        return this;
    }

    public ReportImage time(long time) {
        this.time = time;
        return this;
    }

    public boolean isAddButtion() {
        return this.time == Long.MAX_VALUE;
    }
}
