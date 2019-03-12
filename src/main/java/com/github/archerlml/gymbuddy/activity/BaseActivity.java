package com.github.archerlml.gymbuddy.activity;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.NavUtils;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.github.archerlml.gymbuddy.R;
import com.github.archerlml.gymbuddy.application.ApplicationData;
import com.github.archerlml.gymbuddy.application.GymBuddyApplication;
import com.github.archerlml.gymbuddy.application.UserData;
import com.github.archerlml.gymbuddy.model.Figure;
import com.github.archerlml.gymbuddy.model.LocalConfig;
import com.github.archerlml.gymbuddy.model.NotificationArgs;
import com.github.archerlml.gymbuddy.model.Schedule;
import com.github.archerlml.gymbuddy.model.UserPreference;
import com.github.archerlml.gymbuddy.util.ActivityResultUtil;
import com.github.archerlml.gymbuddy.util.FitnessUtil;
import com.github.archerlml.gymbuddy.util.GBEvent;
import com.github.archerlml.gymbuddy.util.ImageUtil;
import com.github.archerlml.gymbuddy.util.Log;
import com.github.archerlml.gymbuddy.util.MyEventBus;
import com.github.archerlml.gymbuddy.util.NetworkUtil;
import com.github.archerlml.gymbuddy.util.Optional;
import com.github.archerlml.gymbuddy.util.PermissionUtil;
import com.github.archerlml.gymbuddy.util.Util;
import com.github.archerlml.gymbuddy.views.TitlebarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.StorageReference;

import org.greenrobot.eventbus.Subscribe;

import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public abstract class BaseActivity extends AppCompatActivity {
    private static String ARGS = "args";
    private ProgressDialog mProgressDialog;

    @Inject
    NetworkUtil mNetworkUtil;

    @Inject
    StorageReference mStorageReference;

    @Inject
    ImageUtil mImageUtil;

    @Inject
    ApplicationData mApplicationData;

    @Inject
    SharedPreferences mSharedPreferences;

    @Inject
    FirebaseAuth mFirebaseAuth;

    @Inject
    ActivityResultUtil mActivityResultUtil;

    @Nullable
    @BindView(R.id.title_bar)
    public TitlebarView mTitlebarView;

    @Nullable
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @Nullable
    @BindView(R.id.appbar)
    AppBarLayout mAppBarLayout;

    @Inject
    MyEventBus mEventBus;

    @Inject
    FitnessUtil mFitnessUtil;

    UserData getUserData() {
        return mApplicationData.getUserData();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(getContentViewId());
        Log.i();

        GymBuddyApplication.getComponent().inject(this);
        ButterKnife.bind(this);

        initTitleBar();
    }

    public <T> T findViewById(@IdRes int id, Class<? extends T> clz) {
        return (T) findViewById(id);
    }

    public <T> T findViewByIdAndCast(@IdRes int id) {
        return (T) findViewById(id);
    }

    protected boolean hasParent() {
        return !TextUtils.isEmpty(NavUtils.getParentActivityName(this));
    }

    protected void backToParent() {
        finish();
        if (!hasParent()) {
            return;
        }

        startActivity(NavUtils.getParentActivityIntent(this));
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    @Override
    public void onBackPressed() {
        backToParent();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);

    }

    @Deprecated
    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        throw new RuntimeException("no need to do this");
    }

    public void enterSubActivity(Class<? extends BaseActivity> clz) {
        enterSubActivity(clz, null);
    }

    public void enterSubActivity(Class<? extends BaseActivity> clz, Object args) {
        Intent intent = new Intent(this, clz);
        intent.putExtra(ARGS, Util.objToJson(args));
        startActivity(intent);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    protected <T> T getArg(String key) {
        return (T) getArgs(Map.class).get(key);
    }

    protected <T> T getArgs(Class<T> clz) {
        Intent intent = getIntent();
        if (intent == null) {
            return null;
        }
        return Util.jsonToObj(intent.getStringExtra(ARGS), clz);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mEventBus.register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mEventBus.unregister(this);
    }

    @Subscribe
    public void onEvent(GBEvent msg) {
        mEventBus.onEvent(getEventBusCallback(), msg);
    }

    protected MyEventBus.Callback getEventBusCallback() {
        return this instanceof MyEventBus.Callback
                ? (MyEventBus.Callback) this
                : null;
    }

    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    protected void requestPermission(PermissionUtil.PermissionsCallback callback, String... permissions) {
        PermissionUtil.requestPermissions(this, callback, permissions);
    }

    protected void requestPermission(PermissionUtil.PermissionsGrantedCallback callback, String... permissions) {
        PermissionUtil.requestPermissions(this, callback, permissions);
    }

    public void showProgressDialog() {
        showProgressDialog("Loading...");
    }

    public void showProgressDialog(String text) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage(text);
        }
        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    protected void set(int id, String text) {
        if (text == null) {
            return;
        }
        View view = findViewById(id);
        if (view instanceof TextView) {
            ((TextView) view).setText(text);
        }
    }

    protected void set(int id, boolean value) {
        View view = findViewById(id);
        if (view instanceof CheckBox) {
            ((CheckBox) view).setChecked(value);
        }
    }

    protected boolean getBool(int id) {
        View view = findViewById(id);
        if (view instanceof CheckBox) {
            return ((CheckBox) view).isChecked();
        }
        return false;
    }

    protected void onClicked(View view) {

    }

    protected void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    int mNotificationId = 001;

    protected void showNotification(NotificationArgs args) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle(args.title)
                        .setContentText(args.text)
                        .setAutoCancel(true);
        Intent resultIntent = new Intent(this, args.clz);
        resultIntent.putExtra(ARGS, Util.objToJson(args.args));
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(args.clz);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(mNotificationId, mBuilder.build());
    }

    abstract int getContentViewId();

    protected void initTitleBar() {
        String title = getActivityLabel();

        if (mTitlebarView != null) {
            // left
            mTitlebarView.mTitleLeftIcon.setVisibility(TextUtils.isEmpty(NavUtils.getParentActivityName(this))
                    ? View.INVISIBLE
                    : View.VISIBLE);
            mTitlebarView.mTitleLeftIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    backToParent();
                }
            });

            // mid
            if (TextUtils.isEmpty(title)) {
                return;
            }
            mTitlebarView.mTitleMiddleText.setText(title);

            // right
        }

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            if (hasParent()) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setTitle(title);
            }
        }
    }

    @NonNull
    private String getActivityLabel() {
        ActivityInfo activityInfo = null;
        try {
            activityInfo = getPackageManager().getActivityInfo(
                    getComponentName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(e);
        }
        return activityInfo.loadLabel(getPackageManager())
                .toString();
    }

    public void startActivityAndFinish(Class<? extends Activity> tcls) {
        startActivityAndFinish(tcls, null);
    }

    public void startActivityAndFinish(Class<? extends Activity> tcls, Object args) {
        finish();
        Intent intent = new Intent(this, tcls);
        intent.putExtra(ARGS, Util.objToJson(args));
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    public void refreshUI() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mActivityResultUtil.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtil.handlePermissionResult(requestCode, permissions, grantResults);

    }

    public void startActivityForResult(Intent intent, ActivityResultUtil.ActivityResultListener listener) {
        mActivityResultUtil.startActivityForResult(this, intent, listener);
    }

    public void startActivityForResult(Intent intent, ActivityResultUtil.ResultOkListen listener) {
        mActivityResultUtil.startActivityForResult(this, intent, listener);
    }

    void enterMainOrSetup(long delay) {
        if (getUserData().getOrNewMy(LocalConfig.class).isProfileSet) {
            startActivityWithDelay(MainActivity.class, delay);
            return;
        }

        Observable.fromCallable(() -> Optional.of(mNetworkUtil.getMyResources(
                getUserData().currentUser().getUid(),
                Schedule.class, UserPreference.class, Figure.class)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(optional -> {
                    if (!optional.isPresent()) {
                        new AlertDialog.Builder(this)
                                .setMessage("Failed to connect to server")
                                .setCancelable(false)
                                .setPositiveButton("Retry", (dialog, id) -> {
                                    enterMainOrSetup(delay);
                                })
                                .create()
                                .show();
                        return;
                    }

                    Log.i(Util.objToJson(optional.get()));
                    Map<Class<?>, Object> data = optional.get();
                    for (Class<?> cls : data.keySet()) {
                        getUserData().put(cls.getSimpleName(), Util.jsonToObj(Util.objToJson(data.get(cls)), cls));
                    }

                    if (data.containsKey(UserPreference.class)) {
                        startActivityWithDelay(MainActivity.class, 0);
                    } else {
                        startActivityWithDelay(SetupActivity.class, 0);
                    }
                });
    }

    void startActivityWithDelay(Class<? extends Activity> cls, long delay) {
        findViewById(android.R.id.content).postDelayed(
                () -> startActivityAndFinish(cls),
                delay);
    }

    Context getContext() {
        return this;
    }
}
