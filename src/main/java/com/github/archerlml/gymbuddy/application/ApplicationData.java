package com.github.archerlml.gymbuddy.application;

import android.location.Location;

import com.github.archerlml.gymbuddy.util.Log;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by archerlml on 10/12/17.
 */
@Singleton
public class ApplicationData extends Data {
    private UserData mUserData;

    Location mLastLocation;

    @Inject
    public ApplicationData() {
        Log.i(new Random().nextInt());
    }

    private Map<Class<?>, Object> caches = new HashMap<>();

    public void setCurrentLocation(Location location) {
        if (location != null) {
            this.mLastLocation = new Location(location);
        }
    }

    public Location getCurrentLocation() {
        return mLastLocation;
    }


    public UserData getUserData() {
        return mUserData;
    }

    @Override
    protected String getSharedPreferencesName() {
        return ApplicationData.class.getSimpleName();
    }

    public void initUserData() {
        mUserData = new UserData();
    }

    public boolean signedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

}

