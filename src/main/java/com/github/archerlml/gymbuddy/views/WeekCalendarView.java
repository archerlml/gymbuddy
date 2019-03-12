package com.github.archerlml.gymbuddy.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.archerlml.gymbuddy.R;
import com.github.archerlml.gymbuddy.model.Schedule;
import com.github.archerlml.gymbuddy.util.GBEvent;
import com.github.archerlml.gymbuddy.util.TimeUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;

/**
 * Created by archerlml on 10/28/17.
 */

public class WeekCalendarView extends TypedBaseView<Schedule> {
    @BindView(R.id.days)
    LinearLayout mDaysLayout;

    List<View> mDayViews;

    public WeekCalendarView(@NonNull Context context) {
        super(context);
    }

    public WeekCalendarView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(AttributeSet attrs) {
        super.init(attrs);

        Calendar c = Calendar.getInstance();
        int dayToday = c.get(Calendar.DAY_OF_WEEK) - 1;
        c.add(Calendar.DATE, -dayToday);
        mDayViews = new ArrayList<>();
        for (int i = 0; i < mDaysLayout.getChildCount(); ++i) {
            View dayView = mDaysLayout.getChildAt(i);
            mDayViews.add(dayView);
            dayView.setOnClickListener(view -> {
                for (View v : mDayViews) {
                    setSelected(v, false);
                }
                setSelected(view, true);
                mEventBus.post(GBEvent.Type.REFRESH_SCHEDULE);
            });

            TextView day = dayView.findViewById(R.id.day);
            day.setText(TimeUtil.formatTime(c, "EEE"));
            day.setTextColor(ContextCompat.getColor(getContext(),
                    i == dayToday
                            ? R.color.green
                            : R.color.textGray));

            TextView date = dayView.findViewById(R.id.date);
            date.setText(TimeUtil.formatTime(c, "dd"));
            date.setTextColor(ContextCompat.getColor(getContext(),
                    i == dayToday
                            ? R.color.green
                            : R.color.textGray));

            c.add(Calendar.DATE, 1);

        }
        applyData(getUserData().get(Schedule.class));
        mDayViews.get(dayOfToday()).callOnClick();
    }

    private void setSelected(View view, boolean selected) {
        view.findViewById(R.id.indicator).setVisibility(selected ? View.VISIBLE : View.INVISIBLE);
    }

    public int dayOfToday() {
        return Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
    }

    @Override
    public TypedBaseView<Schedule> applyData(Schedule schedule) {
        super.applyData(schedule);

        for (int i = 0; i < mDayViews.size(); ++i) {
            View dayView = mDayViews.get(i);
            dayView.findViewById(R.id.date).setBackgroundResource(
                    schedule == null || schedule.exerciseOfDay(i).isEmpty()
                            ? 0
                            : R.drawable.gray_circle);
        }
        return this;
    }

    public int getSelectedDay() {
        int day = 0;
        for (int i = 0; i < mDayViews.size(); ++i) {
            if (mDayViews.get(i).findViewById(R.id.indicator).getVisibility() == View.VISIBLE) {
                day = i;
                break;
            }
        }
        return day;
    }

    @Override
    public int layoutId() {
        return R.layout.calendar_of_week;
    }
}
