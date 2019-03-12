package com.github.archerlml.gymbuddy.activity;

import android.os.Bundle;
import android.view.View;

import com.github.archerlml.gymbuddy.R;
import com.github.archerlml.gymbuddy.util.NetworkUtil;
import com.github.archerlml.gymbuddy.util.ViewUtil;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SplashActivity extends BaseActivity {
    final long DELAY_TIME = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View mContentView = findViewById(R.id.fullscreen_content);

        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        Completable.fromAction(() -> mNetworkUtil.autoPickServer())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    ViewUtil.toast("use server: ", NetworkUtil.sHost);
                    if (!mApplicationData.signedIn()) {
                        startActivityWithDelay(SignInActivity.class, DELAY_TIME);
                        return;
                    }
                    mApplicationData.initUserData();
                    enterMainOrSetup(DELAY_TIME);
                });
    }


    @Override
    int getContentViewId() {
        return R.layout.activity_splash;
    }
}
