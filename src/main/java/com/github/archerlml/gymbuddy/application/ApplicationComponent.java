package com.github.archerlml.gymbuddy.application;

import android.content.Context;

import com.github.archerlml.gymbuddy.activity.BaseActivity;
import com.github.archerlml.gymbuddy.fragment.BaseFragment;
import com.github.archerlml.gymbuddy.views.BaseView;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by archerlml on 10/10/17.
 */

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

    // adapters
    void inject(GymBuddyApplication o);

    void inject(BaseActivity o);

    void inject(BaseFragment o);

    void inject(BaseView o);

    void inject(UserData o);

    @ApplicationContext
    Context getContext();


}