package com.github.archerlml.gymbuddy.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.util.SparseArray;

import com.github.archerlml.gymbuddy.R;
import com.github.archerlml.gymbuddy.fragment.BaseFragment;
import com.github.archerlml.gymbuddy.fragment.DiscoverFragment;
import com.github.archerlml.gymbuddy.fragment.GymFragment;
import com.github.archerlml.gymbuddy.fragment.MeFragment;
import com.github.archerlml.gymbuddy.model.UserPreference;
import com.github.archerlml.gymbuddy.util.GBEvent;
import com.github.archerlml.gymbuddy.util.Instances;
import com.github.archerlml.gymbuddy.util.Log;
import com.github.archerlml.gymbuddy.util.Util;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import butterknife.BindView;

public class MainActivity extends BaseActivity implements
        GoogleApiClient.OnConnectionFailedListener {

    public static final String KEY_TAB = "key_tab";

    @BindView(R.id.navigation)
    BottomNavigationView mBottomNavigationView;

    Instances<BaseFragment> instances = new Instances<>();

    SparseArray<Long> lastClickedTime = new SparseArray<>();

    boolean doubleClicked(int id) {
        long now = System.currentTimeMillis();
        long last = lastClickedTime.get(id, 0L);
        lastClickedTime.put(id, now);
        return now - last < 600;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBottomNavigationView
                .setOnNavigationItemSelectedListener(
                        item -> {
                            switch (item.getItemId()) {
                                case R.id.action_gym:
                                    Log.i("action_gym");
                                    showFragment(GymFragment.class);
                                    break;
                                case R.id.action_discover:
                                    if (doubleClicked(R.id.action_discover)) {
                                        getUserData().requestEvents();
                                    }
                                    Log.i("action_discover");
                                    showFragment(DiscoverFragment.class);
                                    break;
                                case R.id.action_me:
                                    showFragment(MeFragment.class);
                                    break;
                            }
                            if (mTitlebarView != null) {
                                mTitlebarView.mTitleMiddleText.setText(item.getTitle());
                            }
                            return true;
                        });
        mBottomNavigationView.post(
                () -> mBottomNavigationView.setSelectedItemId(R.id.action_gym));

        UserPreference userPreference = getUserData().getOrNewMy(UserPreference.class);
        if (Util.or(userPreference.trackActivities)) {
            mFitnessUtil.readData(this);
        }
    }

    private void showFragment(Class<? extends BaseFragment> cls) {
        Fragment fragment = instances.getOrNew(cls);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, fragment)
                .commit();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("onConnectionFailed:", connectionResult);
    }

    @Override
    public void onEvent(GBEvent GBEvent) {
        super.onEvent(GBEvent);
        Log.i(GBEvent.what);
    }

    @Override
    int getContentViewId() {
        return R.layout.activity_main;
    }

    @Override
    public void refreshUI() {
        super.refreshUI();
        Integer tab = getArg(KEY_TAB);
        if (tab != null) {
            mBottomNavigationView.setSelectedItemId(tab);
        }
    }
}
