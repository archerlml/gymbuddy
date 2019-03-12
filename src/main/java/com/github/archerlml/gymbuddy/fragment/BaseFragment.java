package com.github.archerlml.gymbuddy.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.archerlml.gymbuddy.activity.BaseActivity;
import com.github.archerlml.gymbuddy.application.ApplicationData;
import com.github.archerlml.gymbuddy.application.GymBuddyApplication;
import com.github.archerlml.gymbuddy.application.UserData;
import com.github.archerlml.gymbuddy.util.Chain;
import com.github.archerlml.gymbuddy.util.GBEvent;
import com.github.archerlml.gymbuddy.util.FitnessUtil;
import com.github.archerlml.gymbuddy.util.MyEventBus;
import com.github.archerlml.gymbuddy.util.NetworkUtil;
import com.github.archerlml.gymbuddy.views.TitlebarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.StorageReference;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import butterknife.ButterKnife;

public abstract class BaseFragment extends Fragment {

    @Inject
    StorageReference mStorageReference;

    @Inject
    ApplicationData mApplicationData;

    UserData getUserData() {
        return mApplicationData.getUserData();
    }

    @Inject
    NetworkUtil mNetworkUtil;

    @Inject
    MyEventBus mEventBus;

    @Inject
    FirebaseAuth mFirebaseAuth;

    @Inject
    FitnessUtil mFitnessUtil;

    public BaseFragment() {
        GymBuddyApplication.getComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(layoutId(), container, false);
        ButterKnife.bind(this, view);
        onViewCreated(view);
        return view;
    }

    void onViewCreated(View view) {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mEventBus.register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
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

    abstract int layoutId();

    BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
    }

    <T extends BaseActivity> T getActivity(Class<T> tClass) {
        return (T) getBaseActivity();
    }

    protected void enterSubActivity(Class<? extends BaseActivity> clz) {
        enterSubActivity(clz, null);
    }

    protected void enterSubActivity(Class<? extends BaseActivity> clz, Object args) {
        getBaseActivity().enterSubActivity(clz, args);
    }

    @Override
    public void onStart() {
        super.onStart();
        Chain.from(getView())
                .then(view -> view.postDelayed(() -> {
                    if (isVisible()) {
                        refreshUI();
                    }
                }, 500));
    }

    void refreshUI() {
    }

    public TitlebarView getTitleBar() {
        return getBaseActivity().mTitlebarView;
    }
}
