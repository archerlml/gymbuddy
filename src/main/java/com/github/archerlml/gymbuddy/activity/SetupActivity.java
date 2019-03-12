package com.github.archerlml.gymbuddy.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.archerlml.gymbuddy.R;
import com.github.archerlml.gymbuddy.model.LocalConfig;
import com.github.archerlml.gymbuddy.model.UserPreference;
import com.github.archerlml.gymbuddy.util.Log;
import com.github.archerlml.gymbuddy.util.Util;
import com.github.archerlml.gymbuddy.util.ViewUtil;

import java.util.UUID;

import butterknife.BindView;

public class SetupActivity extends BaseActivity {

    @BindView(R.id.button)
    View button;

    @BindView(R.id.user_name_layout)
    View nameLayout;

    @BindView(R.id.user_name)
    TextView name;

    @BindView(R.id.avatar)
    ImageView avatar;

    @BindView(R.id.avatar_layout)
    View avatar_layout;

    String mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                        refreshUI();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                    .show();
        });

        button.setOnClickListener(v -> {
            UserPreference userPreference = getUserData().getMy(UserPreference.class, new UserPreference());
            userPreference.userName = name.getText().toString();
            userPreference.avatar = Util.or(mImageUri, getUserData().avatar());
            getUserData().putMy(userPreference, userPreferenceOptional -> {
                if (!userPreferenceOptional.isPresent()) {
                    ViewUtil.toast("Failed to upload profile");
                    Log.i("userPreference = ", userPreference);
                    return;
                }
                LocalConfig localConfig = getUserData().getMy(LocalConfig.class, new LocalConfig());
                localConfig.isProfileSet = true;
                getUserData().put(localConfig);
                startActivityAndFinish(MainActivity.class);
            });
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
                        mImageUri = taskSnapshot.getDownloadUrl().toString();
                        Glide.with(this)
                                .load(mImageUri)
                                .into(avatar);
                        refreshUI();
                    });
        });
    }

    @Override
    protected void backToParent() {
        Log.i();
    }

    @Override
    int getContentViewId() {
        return R.layout.activity_setup;
    }

    @Override
    public void refreshUI() {
        super.refreshUI();
        button.setEnabled(isValid());
        hideProgressDialog();
    }

    public boolean isValid() {
        return !TextUtils.isEmpty(name.getText()) &&
                !TextUtils.isEmpty(Util.or(getUserData().avatar(), mImageUri));
    }
}

