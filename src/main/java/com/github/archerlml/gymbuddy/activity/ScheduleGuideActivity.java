package com.github.archerlml.gymbuddy.activity;

import android.os.Bundle;

import com.github.archerlml.gymbuddy.R;
import com.github.archerlml.gymbuddy.application.GymBuddyApplication;
import com.github.archerlml.gymbuddy.application.UserData;
import com.github.archerlml.gymbuddy.util.ImageUtil;
import com.google.firebase.storage.StorageReference;

import javax.inject.Inject;

public class ScheduleGuideActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    int getContentViewId() {
        return R.layout.activity_schedule_guide;
    }

}
