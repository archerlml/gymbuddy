package com.github.archerlml.gymbuddy.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.archerlml.gymbuddy.R;
import com.github.archerlml.gymbuddy.model.Exercise;
import com.github.archerlml.gymbuddy.model.Schedule;
import com.github.archerlml.gymbuddy.util.TimeUtil;
import com.github.archerlml.gymbuddy.util.Util;
import com.github.archerlml.gymbuddy.util.ViewUtil;

import java.util.List;

import butterknife.BindView;

/**
 * Created by archerlml on 10/28/17.
 */

public class TodayScheduleView extends TypedBaseView<Schedule> {
    @BindView(R.id.date)
    TextView date;

    @BindView(R.id.exercise)
    TextView exercise;

    @BindView(R.id.completeness)
    TextView completeness;

    @BindView(R.id.progress_bar)
    ProgressBar progressbar;


    public TodayScheduleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(AttributeSet attrs) {
        super.init(attrs);
        applyData(getUserData().get(Schedule.class));
    }

    @Override
    public TypedBaseView<Schedule> applyData(Schedule schedule) {
        date.setText(getTodayDesc());

        List<Exercise> exerciseList = schedule == null ? null : schedule.exerciseOfToday();
        if (Util.isEmpty(exerciseList)) {
            exercise.setText("No exercises today");
            ViewUtil.hide(completeness, progressbar);
        } else {
            int minutes = 0;
            for (Exercise exercise : exerciseList) {
                minutes += exercise.duration();
            }
            exercise.setText(String.format("%d min, %d exercises", minutes, exerciseList.size()));

            ViewUtil.show(completeness, progressbar);
            int progress = (int) (schedule.todayPassed() * 1.0 / schedule.todayTotal() * 100);
            completeness.setText(String.format("%d%% done", progress));
            progressbar.setProgress(progress);
        }

        return this;
    }

    private String getTodayDesc() {
        return TimeUtil.formatTime(System.currentTimeMillis(), "EEEE, MMMM dd");
    }

    @Override
    public Schedule getData() {
        return getUserData().get(Schedule.class);
    }

    @Override
    public int layoutId() {
        return R.layout.schedule_today_summary;
    }
}
