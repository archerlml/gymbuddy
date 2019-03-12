package com.github.archerlml.gymbuddy.views;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.framgia.library.calendardayview.CalendarDayView;
import com.framgia.library.calendardayview.DayView;
import com.framgia.library.calendardayview.EventView;
import com.framgia.library.calendardayview.PopupView;
import com.framgia.library.calendardayview.data.IEvent;
import com.framgia.library.calendardayview.data.IPopup;
import com.framgia.library.calendardayview.decoration.CdvDecoration;
import com.github.archerlml.gymbuddy.R;
import com.github.archerlml.gymbuddy.model.Exercise;
import com.github.archerlml.gymbuddy.model.Match;
import com.github.archerlml.gymbuddy.model.Schedule;
import com.github.archerlml.gymbuddy.model.UserPreference;
import com.github.archerlml.gymbuddy.util.Chain;
import com.github.archerlml.gymbuddy.util.GBEvent;
import com.github.archerlml.gymbuddy.util.LoadingView;
import com.github.archerlml.gymbuddy.util.Log;
import com.github.archerlml.gymbuddy.util.Optional;
import com.github.archerlml.gymbuddy.util.Util;
import com.github.archerlml.gymbuddy.util.ViewUtil;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by archerlml on 10/28/17.
 */

public class TimeSheetView extends TypedBaseView<List<Exercise>> {

    @BindView(R.id.day_view)
    CalendarDayView mCalendarDayView;

    int mDay;

    public TimeSheetView(@NonNull Context context) {
        super(context);
    }

    public TimeSheetView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setDay(int day) {
        this.mDay = day;
        Schedule schedule = getUserData().getOrNewMy(Schedule.class);
        applyData(schedule.exerciseOfDay(day));
    }

    @Override
    protected void init(AttributeSet attrs) {
        super.init(attrs);

        UserPreference userPreference = getUserData().getMy(UserPreference.class, new UserPreference());
        mCalendarDayView.setLimitTime(
                Util.or(userPreference.scheduleMinHour, 6),
                Util.or(userPreference.scheduleMaxHour, 24));

        mCalendarDayView.setDecorator(new CdvDecoration() {
            @Override
            public EventView getEventView(IEvent event, Rect eventBound, int hourHeight, int separateHeight) {
                ExerciseEvent exerciseEvent = (ExerciseEvent) event;
                Exercise exercise = exerciseEvent.exercise;

                EventView eventView = new EventView(getContext());
                eventView.setEvent(event);
                eventView.findViewById(R.id.item_event_header).setVisibility(View.INVISIBLE);
                eventView.setPosition(eventBound, -hourHeight, hourHeight - separateHeight * 2);
                eventView.setOnEventClickListener(new EventView.OnEventClickListener() {
                    @Override
                    public void onEventClick(EventView view, IEvent data) {

                    }

                    @Override
                    public void onEventViewClick(View view, EventView eventView, IEvent data) {
                        showExerciseView(exerciseEvent.getExercise());
                    }
                });

                LinearLayout container = eventView.findViewById(R.id.item_event_content);
                container.setGravity(Gravity.CENTER);

                TextView tvDesc = container.findViewById(R.id.item_event_name);

                TextView tvWith = new TextView(getContext());
                tvWith.setText(" with ");
                tvWith.setTextColor(ResourcesCompat.getColor(getResources(), R.color.textGray, null));
                tvWith.setOnClickListener(v -> showMatchDialog(exercise, null));
                container.addView(tvWith);

                CircleImageView imageView = new CircleImageView(getContext());
                imageView.setOnClickListener(v -> showMatchDialog(exercise, null));
                imageView.setElevation(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
                imageView.setImageResource(R.drawable.ic_help_outline_black_48px);
                imageView.setLayoutParams(new LayoutParams(60, 60));
                imageView.requestLayout();
                container.addView(imageView);

                Chain.from(exerciseEvent)
                        .of(e -> e.exercise)
                        .of(e -> e.match)
                        .then(match1 -> {
                            tvDesc.setTextColor(ResourcesCompat.getColor(getResources(), R.color.textGray, null));
                            tvDesc.setBackgroundResource(0);
                            tvDesc.setOnClickListener(null);
                            ViewUtil.show(tvWith, imageView);
                            Glide.with(getContext())
                                    .load(match1.peerAvatar)
                                    .error(R.drawable.ic_face_black_48px)
                                    .into(imageView);
                        })
                        .or(() -> {
                            tvDesc.setOnClickListener(v -> {
                                LoadingView.showAt(eventView);
                                Map<String, Object> param = new HashMap<>(Util.objToMap(exercise));
                                param.put("uid", getUserData().currentUser().getUid());
                                Flowable.fromCallable(() -> Optional.of(mNetworkUtil.matchUp(param)))
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(optional -> {
                                            Log.i(Util.objToJson(optional.get()));
                                            LoadingView.remove(eventView);
                                            if (!optional.isPresent()) {
                                                ViewUtil.toast("failed to match");
                                                return;
                                            }

                                            List<Match> matches = optional.get();
                                            if (matches.isEmpty()) {
                                                ViewUtil.toast("No matched result");
                                                return;
                                            }

                                            showMatchDialog(exercise, matches);
                                        });
                            });
                            tvDesc.setTextColor(ResourcesCompat.getColor(getResources(), R.color.textWhite, null));
                            tvDesc.setBackgroundResource(R.drawable.button_boarder_selector);
                            tvDesc.setText("Match buddy for " + exerciseEvent.exercise.getExerciseDesc());
                            ViewUtil.gone(tvWith, imageView);
                        });

                return eventView;
            }

            @Override
            public PopupView getPopupView(IPopup popup, Rect eventBound, int hourHeight, int seperateHeight) {
                return null;
            }

            @Override
            public DayView getDayView(int hour) {
                DayView dayView = new DayView(getContext());
                dayView.setText(String.format("%1$2s:00", hour));
                dayView.setOnClickListener(v1 ->
                        showExerciseView(Exercise.gen()
                                .setStartTime(mDay, hour, 0)
                                .setEndTime(mDay, Math.min(hour + 1, 24), 0)));
                return dayView;
            }
        });
    }

    @Override
    public TypedBaseView applyData(List<Exercise> exercises) {
        super.applyData(exercises);
        mCalendarDayView.setEvents(Util.map(exercises, ExerciseEvent::new));
        return this;
    }

    @Override
    public int layoutId() {
        return R.layout.time_sheet_view;
    }

    public static class ExerciseEvent implements IEvent {
        final Exercise exercise;

        public ExerciseEvent(Exercise exercise) {
            this.exercise = exercise;
        }

        public Exercise getExercise() {
            return this.exercise;
        }

        @Override
        public String getName() {
            return exercise.getExerciseDesc();
        }

        @Override
        public int getColor() {
            return R.color.eventColor;
        }

        @Override
        public Calendar getStartTime() {
            return exercise.getStartTimeToday();
        }

        @Override
        public Calendar getEndTime() {
            return exercise.getEndTimeToday();
        }
    }

    private void showExerciseView(Exercise exercise) {
        exercise = Util.clone(exercise);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext(), R.style.BottomSheetDialog);
        ExerciseView exerciseView = new ExerciseView(getContext()).applyData(exercise);
        Schedule schedule = getUserData().getMy(Schedule.class, new Schedule());
        exerciseView.mDeleteLayout.setOnClickListener(v1 -> {
            if (schedule.removeExercise(exerciseView.getData().id) != null) {
                updateSchedule(schedule, bottomSheetDialog);
            }
        });
        exerciseView.mSaveLayout.setOnClickListener(v1 -> {
            Log.i(Util.objToJson(exerciseView.getData()));
            schedule.setExercise(exerciseView.getData());
            updateSchedule(schedule, bottomSheetDialog);
        });
        bottomSheetDialog.setContentView(exerciseView);
        bottomSheetDialog.show();
    }

    private void showMatchDialog(Exercise exercise, List<Match> matches) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext(), R.style.BottomSheetDialog);
        MatchView matchView = new MatchView(getContext());
        matchView.showMatches(exercise, matches);
        matchView.mDeleteLayout.setOnClickListener(v ->
                Chain.from(matchView.mSelectedItem)
                        .of(i -> i.id)
                        .then(id -> {
                            Schedule schedule = getUserData().getOrNewMy(Schedule.class);
                            Exercise e = schedule.getExercise(exercise.id);
                            e.match = null;
                            getUserData().putMy(schedule, tOptional -> {
                                ViewUtil.toast("Remove match ", tOptional.isPresent() ? "successfully" : "failed");
                                refreshUI();
                                bottomSheetDialog.dismiss();
                            });
                        })
                        .or(bottomSheetDialog::dismiss));
        matchView.mSaveLayout.setOnClickListener(v ->
                Chain.from(matchView.mSelectedItem)
                        .of(i -> i.id)
                        .then(id -> {
                            bottomSheetDialog.dismiss();
                        })
                        .or(() -> {
                            Schedule schedule = getUserData().getOrNewMy(Schedule.class);
                            Exercise e = schedule.getExercise(exercise.id);
                            e.match = matchView.mSelectedItem;
                            e.match.id = UUID.randomUUID().toString();
                            getUserData().putMy(schedule, tOptional -> {
                                ViewUtil.toast("Add match ", tOptional.isPresent() ? "successfully" : "failed");
                                refreshUI();
                                bottomSheetDialog.dismiss();
                            });
                        }));
        bottomSheetDialog.setContentView(matchView);
        bottomSheetDialog.show();
    }

    @Override
    public void refreshUI() {
        super.refreshUI();
        setDay(mDay);
    }

    private void updateSchedule(Schedule schedule, BottomSheetDialog bottomSheetDialog) {
        getUserData().putMy(schedule, optional -> {
            post(() -> {
                ViewUtil.toast("upload ", optional.isPresent() ? "successfully" : "failed");
                bottomSheetDialog.dismiss();
                mEventBus.post(GBEvent.Type.REFRESH_SCHEDULE);
                refreshUI();
            });
        });
    }
}
