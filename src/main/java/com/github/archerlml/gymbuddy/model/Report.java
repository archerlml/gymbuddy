package com.github.archerlml.gymbuddy.model;

import android.location.Location;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.archerlml.gymbuddy.R;
import com.github.archerlml.gymbuddy.application.ApplicationData;
import com.github.archerlml.gymbuddy.application.GymBuddyApplication;
import com.github.archerlml.gymbuddy.util.LocationUtil;
import com.github.archerlml.gymbuddy.util.Util;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Report {

    public String id;
    public String description;
    public String name;
    public String email;
    public List<String> images = new ArrayList<>();
    public String address;
    public String uid;
    public long time;
    public int scale;
    public int severity;
    public String status;
    public String key;
    public Double lat;
    public Double lng;
    public Boolean confirmed;
    public String lastModifiedBy;

    @Inject
    ApplicationData applicationData;

    public boolean isMyReport() {
        return true;
    }

    public boolean isConfirmed() {
        return confirmed != null && confirmed;
    }

    @Override
    public boolean equals(Object obj) {
        return Util.objToJson(this).equals(Util.objToJson(obj));
    }

    @Override
    public String toString() {
        return Util.objToJson(this);
    }

    public boolean within30feetFromMe() {
        if (lat == null || lng == null) {
            return false;
        }
        Location myLocation = applicationData.getCurrentLocation();
        return myLocation != null && LocationUtil.with30Feet(new LatLng(lat, lng), new LatLng(myLocation.getLatitude(), myLocation.getLongitude()));
    }

    public boolean modifiedByMe() {
        return true;
    }

    public boolean statusChanged(Report reportNew) {
        return reportNew.status != null && !reportNew.status.equals(this.status);
    }

    public boolean isRemoveClaimed() {
        return GymBuddyApplication.getApp().getString(R.string.remove_claimed).equals(status);
    }
}
