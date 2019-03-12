package com.github.archerlml.gymbuddy.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.github.archerlml.gymbuddy.R;
import com.github.archerlml.gymbuddy.application.ApplicationData;
import com.github.archerlml.gymbuddy.application.GymBuddyApplication;
import com.github.archerlml.gymbuddy.application.UserData;
import com.github.archerlml.gymbuddy.util.GBEvent;
import com.github.archerlml.gymbuddy.util.MyEventBus;
import com.github.archerlml.gymbuddy.util.NetworkUtil;
import com.github.archerlml.gymbuddy.util.ViewUtil;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import butterknife.ButterKnife;

/**
 * Created by archerlml on 10/28/17.
 */

public abstract class BaseView extends FrameLayout {

    @Inject
    ApplicationData mApplicationData;

    UserData getUserData() {
        return mApplicationData.getUserData();
    }

    @Inject
    NetworkUtil mNetworkUtil;

    @Inject
    MyEventBus mEventBus;

    public BaseView(@NonNull Context context) {
        this(context, null);
    }

    public BaseView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    protected void init(AttributeSet attrs) {
        inflate(getContext(), layoutId(), this);

        if (GymBuddyApplication.getComponent() != null) {
            GymBuddyApplication.getComponent().inject(this);
            ButterKnife.bind(this);
        }

        if (attrs != null) {
            TypedArray a = getContext().getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.TypedBaseView,
                    0, 0);
            try {
                applyAttrs(a);
            } finally {
                a.recycle();
            }
        }
    }

    public void applyAttrs(TypedArray a) {
    }

    public abstract int layoutId();


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (getEventBusCallback() != null) {
            mEventBus.register(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (getEventBusCallback() != null) {
            mEventBus.unregister(this);
        }
    }

    @Subscribe
    public void onEvent(GBEvent msg) {
        if (msg.what == GBEvent.Type.SHOW_APP_TOAST) {
            findViewById(R.id.content).post(() -> {
                ViewUtil.toast(msg.obj(String.class));
            });
        } else {
            mEventBus.onEvent(getEventBusCallback(), msg);
        }
    }

    public MyEventBus.Callback getEventBusCallback() {
        return this instanceof MyEventBus.Callback
                ? (MyEventBus.Callback) this
                : null;
    }

    public void refreshUI() {

    }
}
