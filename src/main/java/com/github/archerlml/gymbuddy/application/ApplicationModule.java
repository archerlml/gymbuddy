package com.github.archerlml.gymbuddy.application;

import android.content.Context;
import android.content.SharedPreferences;

import com.github.archerlml.gymbuddy.util.MyEventBus;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

/**
 * Created by archerlml on 10/10/17.
 */

@Module
public class ApplicationModule {

    private final GymBuddyApplication mApplication;

    public ApplicationModule(GymBuddyApplication app) {
        mApplication = app;
    }

    @Provides
    @ApplicationContext
    Context provideContext() {
        return mApplication;
    }

    @Provides
    GymBuddyApplication provideApplication() {
        return mApplication;
    }

    @Singleton
    @Provides
    DatabaseReference provideDatabaseReference() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabase.setPersistenceEnabled(true);
        return firebaseDatabase.getReference();
    }

    @Singleton
    @Provides
    FirebaseAuth provideFirebaseAuth() {
        return FirebaseAuth.getInstance();
    }

    @Singleton
    @Provides
    MyEventBus provideMyEventBus() {
        return new MyEventBus(EventBus.getDefault());
    }

    @Singleton
    @Provides
    public StorageReference provideStorageReference() {
        return FirebaseStorage.getInstance().getReferenceFromUrl("gs://gymbudd-af458.appspot.com");
    }

    @Provides
    public FirebaseUser provideFirebaseUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    @Provides
    SharedPreferences provideSharedPreferences() {
        return mApplication.getSharedPreferences("app_sp", Context.MODE_PRIVATE);
    }

    @Singleton
    @Provides
    OkHttpClient provideOkHttpClient() {
        return new OkHttpClient();
    }

}