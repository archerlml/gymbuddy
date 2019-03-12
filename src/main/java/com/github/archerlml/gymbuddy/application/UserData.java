package com.github.archerlml.gymbuddy.application;


import android.net.Uri;

import com.annimon.stream.Stream;
import com.github.archerlml.gymbuddy.model.Event;
import com.github.archerlml.gymbuddy.model.UserEntity;
import com.github.archerlml.gymbuddy.model.UserPreference;
import com.github.archerlml.gymbuddy.util.Chain;
import com.github.archerlml.gymbuddy.util.GBEvent;
import com.github.archerlml.gymbuddy.util.Log;
import com.github.archerlml.gymbuddy.util.MyEventBus;
import com.github.archerlml.gymbuddy.util.NetworkUtil;
import com.github.archerlml.gymbuddy.util.Optional;
import com.github.archerlml.gymbuddy.util.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by archerlml on 10/12/17.
 */

public class UserData extends Data {

    FirebaseUser currentFirebaseUser;

    @Inject
    MyEventBus mEventBus;

    @Inject
    NetworkUtil networkUtil;

    UserData() {
        Log.i();
        GymBuddyApplication.getComponent().inject(this);
        this.currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

//    public boolean signedIn() {
//        return currentFirebaseUser != null;
//    }

    public FirebaseUser currentUser() {
        return currentFirebaseUser;
    }

    public <T extends UserEntity> void deleteMy(T t, final Consumer<Optional<NetworkUtil.OperateResult>> onNext) {
        t.uid = currentUser().getUid();
        Flowable.fromCallable(() -> Optional.of(networkUtil.delete(t)))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.single())
                .subscribe(optional -> {
                    if (optional.isPresent()) {
                        put(optional.get());
                    }
                    if (onNext != null) {
                        onNext.accept(optional);
                    }
                });
    }

    public <T extends UserEntity> void putMy(T t, final Consumer<Optional<T>> onNext) {
        t.uid = currentUser().getUid();
        Flowable.fromCallable(() -> Optional.of(networkUtil.put(t)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(optional -> {
                    if (optional.isPresent()) {
                        put(optional.get());
                    } else {
                        Log.e("put failed: ", Util.objToJson(t));
                    }
                    if (onNext != null) {
                        onNext.accept(optional);
                    }
                });
    }

//    public <T extends UserEntity> List<T> getMy(Class<T> tClass, final Consumer<Optional<T>> onNext) {
//        return getMy(tClass, null, Optional::of);
//    }

    public <T extends UserEntity> T getMy(Class<T> tClass) {
        return getMy(tClass, null);
    }

    public <T extends UserEntity> T getOrNewMy(Class<T> tClass) {
        T t = getMy(tClass, null);
        if (t == null) {
            try {
                t = tClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                Log.e(e);
            }
        }
        return t;
    }

    public <T extends UserEntity> T getMy(Class<T> tClass, T defaultValue) {
        T t = get(tClass);
        Log.i("get ", tClass.getSimpleName(), ": ", Util.objToJson(t));
        return t == null ? defaultValue : t;
    }

    @Override
    protected String getSharedPreferencesName() {
        return currentFirebaseUser.getUid();
    }

    public String name() {
        return Util.or(getMy(UserPreference.class, new UserPreference()).userName, currentUser().getDisplayName());
    }

    public String avatar() {
        return Util.or(
                getMy(UserPreference.class, new UserPreference()).avatar,
                Chain.from(currentUser()).of(FirebaseUser::getPhotoUrl).of(Uri::toString).get());
    }

    public void requestEvents() {
        Observable.fromCallable(() -> Optional.of(networkUtil.query(Event.class, "{}")))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.single())
                .subscribe(optional -> {
                    if (!optional.isPresent()) {
                        Log.e("Failed to fetch events");
                        return;
                    }

                    List<Event> events = optional.get();
                    updateJoinedList(events);
                    putList(events, Event.class);

                    mEventBus.post(GBEvent.Type.REFRESH_EVENT);
                });
    }

    public void updateJoinedList() {
        List<Event> events = getOrNewList(Event.class);
        updateJoinedList(events);
        putList(events, Event.class);
    }

    private void updateJoinedList(List<Event> events) {
        UserPreference userPreference = getOrNewMy(UserPreference.class);
        List<Event> joined = new ArrayList<>();
        List<Event> unjoined = new ArrayList<>();
        Stream.of(events)
                .forEach(event -> {
                    if (userPreference.joinedEvents.contains(event._id)) {
                        joined.add(event);
                    } else {
                        unjoined.add(event);
                    }
                });
        putList(joined, Event.class, TAG_JOINED);
        putList(unjoined, Event.class, TAG_UNJOINED);
    }

    public final static String TAG_JOINED = "joined";
    public final static String TAG_UNJOINED = "unjoined";

    public boolean joinedEvent(Event event) {
        return getOrNewMy(UserPreference.class).joinedEvents.contains(event._id);
    }

    public void updateEventIfNeed(Event event) {
        UserPreference userPreference = getOrNewMy(UserPreference.class);
        if (!userPreference.joinedEvents.contains(event._id)
                || userPreference.finishedEvents.contains(event._id)
                || System.currentTimeMillis() < event.endTime) {
            return;
        }

        Flowable.fromCallable(() -> Optional.of(networkUtil.updateEvent(event)))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.single())
                .subscribe(optional -> {
                    if (!optional.isPresent()) {
                        return;
                    }
                    Event newEvent = optional.get();
                    reward(event, newEvent);
                    requestEvents();
                });
    }

    private void reward(Event event, Event result) {
        UserPreference userPreference1 = getOrNewMy(UserPreference.class);
        if (userPreference1.finishedEvents.contains(event._id) ||
                !userPreference1.joinedEvents.contains(event._id)) {
            return;
        }
        if (event.winnerIds.contains(currentUser().getUid())) {
            userPreference1.balance += result.reward;
        }
        userPreference1.finishedEvents.add(event._id);
        putMy(userPreference1, optional1 -> {
            if (!optional1.isPresent()) {
                mEventBus.post(GBEvent.Type.SHOW_APP_TOAST, "Failed to update event");
            }
        });
    }

    public boolean isAdmin() {
        return Chain.from(currentUser())
                .of(FirebaseUser::getProviders)
                .of(list -> list.contains("google.com"))
                .get();
    }
}
