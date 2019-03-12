package com.github.archerlml.gymbuddy.fragment;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.archerlml.gymbuddy.R;
import com.github.archerlml.gymbuddy.activity.EventActivity;
import com.github.archerlml.gymbuddy.activity.FigureActivity;
import com.github.archerlml.gymbuddy.activity.ScheduleActivity;
import com.github.archerlml.gymbuddy.adapter.BaseListAdapter;
import com.github.archerlml.gymbuddy.application.UserData;
import com.github.archerlml.gymbuddy.model.Event;
import com.github.archerlml.gymbuddy.model.Figure;
import com.github.archerlml.gymbuddy.model.Schedule;
import com.github.archerlml.gymbuddy.model.UserPreference;
import com.github.archerlml.gymbuddy.util.Chain;
import com.github.archerlml.gymbuddy.util.GBEvent;
import com.github.archerlml.gymbuddy.util.Log;
import com.github.archerlml.gymbuddy.util.MyEventBus;
import com.github.archerlml.gymbuddy.util.Util;
import com.github.archerlml.gymbuddy.util.ViewUtil;
import com.github.archerlml.gymbuddy.views.MyFigureView;
import com.github.archerlml.gymbuddy.views.TodayScheduleView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.github.archerlml.gymbuddy.util.GBEvent.Type.REFRESH_EVENT;

public class GymFragment extends BaseFragment implements MyEventBus.Callback {

    // schedule
    @BindView(R.id.add)
    View addScheduleButton;

    @BindView(R.id.to_add)
    View addView;

    @BindView(R.id.today_schedule)
    TodayScheduleView todayScheduleView;

    @BindView(R.id.schedule_layout)
    View mScheduleLayout;

    // hello
    @BindView(R.id.profile_image)
    CircleImageView mCircleImageView;

    @BindView(R.id.hello)
    TextView helloText;

    @BindView(R.id.description)
    TextView description;

    // Figure
    @BindView(R.id.add_figure_button)
    View addFigureButton;

    @BindView(R.id.add_figure_view)
    View addFigureView;

    @BindView(R.id.my_figure)
    MyFigureView myFigure;

    @BindView(R.id.figure_layout)
    View mFigureLayout;

    @BindView(R.id.my_events)
    RecyclerView mRecyclerView;

    @BindView(R.id.my_event_layout)
    View mMyEventLayout;

    List<Event> events = new ArrayList<>();

    public GymFragment() {
        super();
    }

    @Override
    void onViewCreated(View view) {
        super.onViewCreated(view);

        addScheduleButton.setOnClickListener(view1 -> enterSubActivity(ScheduleActivity.class));
        todayScheduleView.setOnClickListener(view1 -> enterSubActivity(ScheduleActivity.class));
        mScheduleLayout.setOnClickListener(view1 -> enterSubActivity(ScheduleActivity.class));

        addFigureButton.setOnClickListener(view1 -> enterSubActivity(FigureActivity.class));
        myFigure.setOnClickListener(view1 -> enterSubActivity(FigureActivity.class));
        mFigureLayout.setOnClickListener(view1 -> enterSubActivity(FigureActivity.class));

        mRecyclerView.setAdapter(new BaseListAdapter<Event>() {

            @Override
            public void applyData(Event event, BaseViewHolder holder, int position) {
                Glide.with(getContext())
                        .load(Util.or(event.imageUrl, R.drawable.event))
                        .into(holder.getView(R.id.backdrop, ImageView.class));
                TextView result = holder.getView(R.id.reward, TextView.class);
                result.setVisibility(event.isFinished() ? View.VISIBLE : View.GONE);
                if (event.isWinner(getUserData().currentUser().getUid()) && event.reward != null && event.reward > 0) {
                    result.setCompoundDrawables(getResources().getDrawable(R.drawable.ic_attach_money_black_48px), null, null, null);
                    result.setText(Util.getString("You got ", event.reward, " GymBuddy coins!"));
                } else {
                    result.setCompoundDrawables(null, null, null, null);
                    result.setText(Util.getString("Event finished"));
                }
                holder.getView(R.id.title, TextView.class).setText(event.title);
                holder.getView(R.id.description, TextView.class).setText(event.description);
                holder.root.setOnClickListener(v -> {
                    enterSubActivity(EventActivity.class, event);
                });
                getUserData().updateEventIfNeed(event);
            }

            @Override
            public int itemLayout() {
                return R.layout.event_summary;
            }

            @Override
            public List<Event> getItems() {
                return events;
            }

        });
        refreshUI();
    }

    @Override
    int layoutId() {
        return R.layout.fragment_gym;
    }

    public void refreshUI() {
        Glide.with(getBaseActivity()).load(getUserData().avatar()).into(mCircleImageView);
        helloText.setText(Util.getString(getHelloString(), ", ", getUserData().name()));
        UserPreference userPreference = getUserData().getMy(UserPreference.class, new UserPreference());
        Chain.from(userPreference.description)
                .then(description::setText);

        Schedule schedule = getUserData().getMy(Schedule.class);
        Log.i(Util.objToJson(schedule));
        addView.setVisibility(schedule == null ? View.VISIBLE : View.GONE);
        todayScheduleView.setVisibility(schedule == null ? View.GONE : View.VISIBLE);
        todayScheduleView.applyData(schedule);

        Figure figure = getUserData().getMy(Figure.class);
        addFigureView.setVisibility(figure == null ? View.VISIBLE : View.GONE);
        myFigure.setVisibility(figure == null ? View.GONE : View.VISIBLE);
        myFigure.applyData(figure);

        Chain.from(getUserData().getList(Event.class, UserData.TAG_JOINED))
                .then((myEvents) -> {
                    if (myEvents.isEmpty()) {
                        ViewUtil.gone(mMyEventLayout);
                        return;
                    }

                    events.clear();
                    events.addAll(myEvents);
                    mRecyclerView.post(() -> mRecyclerView.getAdapter().notifyDataSetChanged());
                    ViewUtil.show(mMyEventLayout);
                })
                .or(() -> getUserData().requestEvents());

    }

    private String getHelloString() {
        Calendar rightNow = Calendar.getInstance();
        int currentHour = rightNow.get(Calendar.HOUR_OF_DAY);
        if (currentHour >= 5 && currentHour < 12) {
            return "Good morning";
        }
        if (currentHour >= 12 && currentHour < 17) {
            return "Good afternoon";
        }
        if (currentHour >= 17 && currentHour < 21) {
            return "Good evening";
        }
        return "Goood night";
    }

    @Override
    public boolean acceptEvent(GBEvent gbEvent) {
        return gbEvent.what == REFRESH_EVENT;
    }

    @Override
    public void onAcceptedEvent(GBEvent gbEvent) {
        getView().post(this::refreshUI);
    }
}
