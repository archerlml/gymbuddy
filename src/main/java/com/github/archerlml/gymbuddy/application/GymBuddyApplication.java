package com.github.archerlml.gymbuddy.application;

import android.app.Application;

/**
 * Created by archerlml on 12/1/16.
 */

public class GymBuddyApplication extends Application {
    static GymBuddyApplication sApp;

    private ApplicationComponent applicationComponent;


    @Override
    public void onCreate() {
        super.onCreate();
        sApp = this;

        // DI
        applicationComponent = DaggerApplicationComponent
                .builder()
                .applicationModule(new ApplicationModule(this))
                .build();
//        FacebookSdk.sdkInitialize(getApplicationContext());
//        AppEventsLogger.activateApp(this);
    }

    public static ApplicationComponent getComponent() {
        return getApp().applicationComponent;
    }

    public static GymBuddyApplication getApp() {
        return sApp;
    }
}
