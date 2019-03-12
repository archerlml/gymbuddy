package com.github.archerlml.gymbuddy.fragment;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.archerlml.gymbuddy.R;
import com.github.archerlml.gymbuddy.activity.BaseActivity;
import com.github.archerlml.gymbuddy.activity.ProfileActivity;
import com.github.archerlml.gymbuddy.model.UserPreference;
import com.github.archerlml.gymbuddy.util.Chain;
import com.github.archerlml.gymbuddy.util.Log;
import com.github.archerlml.gymbuddy.util.PermissionUtil;
import com.github.archerlml.gymbuddy.util.Util;
import com.github.archerlml.gymbuddy.util.ViewUtil;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlacePicker;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;

public class MeFragment extends BaseFragment {

    @BindView(R.id.profile_image)
    CircleImageView mCircleImageView;

    @BindView(R.id.user_name)
    TextView userName;

    @BindView(R.id.description)
    TextView description;

    @BindView(R.id.coins)
    TextView coins;

    @BindView(R.id.profile_layout)
    View profileLayout;

    @BindView(R.id.gym_layout)
    View gymLayout;

    @BindView(R.id.track_activities_layout)
    View trackActivitiesLayout;

    @BindView(R.id.track_activities_enabled)
    CheckBox trackActivitiesEnabled;

    @BindView(R.id.my_gym)
    TextView myGym;

    public MeFragment() {
        super();
    }

    @Override
    void onViewCreated(View view) {
        super.onViewCreated(view);

        profileLayout.setOnClickListener(v -> getBaseActivity().enterSubActivity(ProfileActivity.class));

        gymLayout.setOnClickListener(v -> {
            BaseActivity activity = getBaseActivity();
            Intent intent = null;
            try {
                intent = new PlacePicker.IntentBuilder().build(activity);
            } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                Log.e(e);
            }

            if (intent != null) {
                activity.startActivityForResult(intent, data -> {
                    Place place = PlaceAutocomplete.getPlace(getContext(), data);
                    if (place == null) {
                        Log.i("nothing picked");
                        return;
                    }

                    UserPreference userPreference1 = getUserData().getOrNewMy(UserPreference.class);
                    userPreference1.gymName = place.getName().toString();
                    userPreference1.gymId = place.getId();
                    getUserData().putMy(userPreference1, userPreferenceOptional -> {
                        if (userPreferenceOptional.isPresent()) {
                            myGym.setText(place.getName());
                        } else {
                            ViewUtil.toast("Failed to update location");
                        }
                    });
                });
            }
        });

        trackActivitiesLayout.setOnClickListener(v -> {
            UserPreference userPreference = getUserData().getMy(UserPreference.class);
            if (Util.or(userPreference.trackActivities)) {
                switchTrackActivities();
            } else {
                mFitnessUtil.requestFitnessPermissionIfNeed(getBaseActivity(), this::switchTrackActivities);
            }
        });
    }

    @Override
    void refreshUI() {
        super.refreshUI();

        UserPreference userPreference = getUserData().getMy(UserPreference.class, new UserPreference());
        Glide.with(this).load(getUserData().avatar()).into(mCircleImageView);

        userName.setText(getUserData().name());
        coins.setText(String.valueOf(userPreference.balance));

        Chain.from(userPreference.description)
                .then(description::setText);

        Chain.from(userPreference.gymName)
                .then(myGym::setText);

        trackActivitiesEnabled.setChecked(Util.or(userPreference.trackActivities));
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshUI();
    }

    @Override
    int layoutId() {
        return R.layout.fragment_me;
    }

    public void switchTrackActivities() {
        UserPreference userPreference1 = getUserData().getOrNewMy(UserPreference.class);
        userPreference1.trackActivities = !Util.or(userPreference1.trackActivities);
        getUserData().putMy(userPreference1, userPreferenceOptional -> {
            if (userPreferenceOptional.isPresent()) {
                trackActivitiesEnabled.setChecked(userPreference1.trackActivities);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtil.handlePermissionResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mFitnessUtil.onActivityResult(requestCode, resultCode, data);
    }
}
