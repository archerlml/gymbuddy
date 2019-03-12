package com.github.archerlml.gymbuddy.activity;

import android.os.Bundle;

import com.github.archerlml.gymbuddy.R;
import com.github.archerlml.gymbuddy.model.Schedule;
import com.github.archerlml.gymbuddy.util.GBEvent;
import com.github.archerlml.gymbuddy.util.Log;
import com.github.archerlml.gymbuddy.util.MyEventBus;
import com.github.archerlml.gymbuddy.views.TimeSheetView;
import com.github.archerlml.gymbuddy.views.TodayScheduleView;
import com.github.archerlml.gymbuddy.views.WeekCalendarView;

import butterknife.BindView;

public class ScheduleActivity extends BaseActivity implements MyEventBus.Callback {

    @BindView(R.id.today_schedule)
    TodayScheduleView mTodayScheduleView;

    @BindView(R.id.week_calendar_view)
    WeekCalendarView mWeekCalendarView;

    @BindView(R.id.time_sheet)
    TimeSheetView mTimeSheetView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i();
        refreshUI();
    }

    @Override
    int getContentViewId() {
        return R.layout.activity_schedule;
    }

    @Override
    public boolean acceptEvent(GBEvent event) {
        return event.what == GBEvent.Type.REFRESH_SCHEDULE;
    }

    @Override
    public void onAcceptedEvent(GBEvent GBEvent) {
        refreshUI();
    }

    @Override
    public void refreshUI() {
        super.refreshUI();
        Schedule schedule = getUserData().getMy(Schedule.class, new Schedule());
        mTodayScheduleView.applyData(schedule);
        mWeekCalendarView.applyData(schedule);
        mTimeSheetView.setDay(mWeekCalendarView.getSelectedDay());
    }
}
