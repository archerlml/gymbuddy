package com.github.archerlml.gymbuddy.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.login.LoginManager;
import com.github.archerlml.gymbuddy.R;
import com.github.archerlml.gymbuddy.model.UserPreference;
import com.github.archerlml.gymbuddy.util.Log;
import com.github.archerlml.gymbuddy.util.ViewUtil;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.UUID;

import butterknife.BindView;

public class ProfileActivity extends BaseActivity implements
        GoogleApiClient.OnConnectionFailedListener {

    @BindView(R.id.user_name_layout)
    View nameLayout;

    @BindView(R.id.user_name)
    TextView name;

    @BindView(R.id.description)
    TextView description;

    @BindView(R.id.description_layout)
    View descriptionLayout;

    @BindView(R.id.avatar)
    ImageView avatar;

    @BindView(R.id.avatar_layout)
    View avatar_layout;

    @BindView(R.id.button)
    Button button;

    public GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UserPreference userPreference = getUserData().getOrNewMy(UserPreference.class);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        Glide.with(this).load(getUserData().avatar()).into(avatar);
        avatar_layout.setOnClickListener(v -> {
            Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImage(pickPhoto);
        });

        name.setText(getUserData().name());
        nameLayout.setOnClickListener(v -> {
            final EditText input = new EditText(this);
            input.setText(name.getText());
            new AlertDialog.Builder(this)
                    .setTitle("User name")
                    .setView(input)
                    .setPositiveButton("OK", (dialog, which) -> {
                        name.setText(input.getText());
                        UserPreference preference = getUserData().getMy(UserPreference.class, new UserPreference());
                        preference.userName = input.getText().toString();
                        getUserData().putMy(preference, optional -> {
                            refreshUI();
                            if (!optional.isPresent()) {
                                ViewUtil.toast("update name failed");
                            }
                        });
                        refreshUI();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                    .show();
        });

        description.setText(userPreference.description);
        descriptionLayout.setOnClickListener(v -> {
            final EditText input = new EditText(this);
            input.setMinLines(6);
            input.setText(description.getText());
            new AlertDialog.Builder(this)
                    .setTitle("Description")
                    .setView(input)
                    .setPositiveButton("OK", (dialog, which) -> {
                        description.setText(input.getText());
                        UserPreference preference = getUserData().getOrNewMy(UserPreference.class);
                        preference.description = input.getText().toString();
                        getUserData().putMy(preference, optional -> {
                            refreshUI();
                            if (!optional.isPresent()) {
                                ViewUtil.toast("update description failed");
                            }
                        });
                        refreshUI();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                    .show();
        });
        button.setOnClickListener(view -> {
            mFirebaseAuth.signOut();
            Auth.GoogleSignInApi.signOut(mGoogleApiClient);
            LoginManager.getInstance().logOut();

            startActivityAndFinish(SignInActivity.class);
        });

        refreshUI();
    }

    private void pickImage(Intent intent) {
        startActivityForResult(intent, returnedIntent -> {
            Uri image = returnedIntent.getData();
            if (image == null) {
                Log.d("image is null");
                return;
            }
            showProgressDialog();
            mStorageReference
                    .child("images/" + UUID.randomUUID() + ".jpg")
                    .putFile(image)
                    .addOnSuccessListener(taskSnapshot -> {
                        UserPreference userPreference = getUserData().getMy(UserPreference.class, new UserPreference());
                        userPreference.avatar = taskSnapshot.getDownloadUrl().toString();
                        getUserData().putMy(userPreference, optional -> {
                            if (!optional.isPresent()) {
                                ViewUtil.toast("update profile failed");
                            } else {
                                Glide.with(this)
                                        .load(image)
                                        .into(avatar);
                            }
                            refreshUI();
                        });
                    });
        });
    }

    @Override
    int getContentViewId() {
        return R.layout.activity_profile;
    }

    @Override
    public void refreshUI() {
        super.refreshUI();
        hideProgressDialog();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("onConnectionFailed:", connectionResult);
    }
}
