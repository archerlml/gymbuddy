package com.github.archerlml.gymbuddy.activity;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.archerlml.gymbuddy.R;
import com.github.archerlml.gymbuddy.model.Event;
import com.github.archerlml.gymbuddy.model.UserPreference;
import com.github.archerlml.gymbuddy.util.GBEvent;
import com.github.archerlml.gymbuddy.util.MyEventBus;
import com.github.archerlml.gymbuddy.util.TimeUtil;
import com.github.archerlml.gymbuddy.util.Util;
import com.github.archerlml.gymbuddy.util.ViewUtil;

import butterknife.BindView;

public class EventActivity extends BaseActivity implements MyEventBus.Callback {

    @BindView(R.id.backdrop)
    ImageView backdrop;

    @BindView(R.id.title_line)
    TextView title;

    @BindView(R.id.description)
    TextView description;

    @BindView(R.id.start_time)
    TextView mStartTime;

    @BindView(R.id.end_time)
    TextView mEndTime;

    @BindView(R.id.button)
    Button button;

    Event mEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mEvent = getArgs(Event.class);

        mStartTime.setText(TimeUtil.formatTime(mEvent.startTime, "MMM/dd/yyyy HH:mm"));
        mEndTime.setText(TimeUtil.formatTime(mEvent.endTime, "MMM/dd/yyyy HH:mm"));
        title.setText(mEvent.title);
        description.setText(mEvent.description);
        Glide.with(this)
                .load(Util.or(mEvent.imageUrl, R.drawable.event))
                .into(backdrop);

        // publish button
        if (Util.isEmpty(mEvent._id)) {
            button.setVisibility(View.VISIBLE);

        }
        updateButton();
        refreshUI();
    }

    private void updateButton() {
        if (Util.isEmpty(mEvent._id)) {
            button.setText("Publish");
            button.setOnClickListener(v -> getUserData().putMy(mEvent, eventOptional -> {
                if (eventOptional.isPresent()) {
                    getUserData().requestEvents();
                    startActivityAndFinish(MainActivity.class, Util.getMap(MainActivity.KEY_TAB, R.id.action_discover));
                    return;
                }

                ViewUtil.toast("Failed to upload event");
            }));
            return;
        }

        if (mEvent.isFinished()) {
            button.setEnabled(false);
            button.setText("Event finished");
            return;
        }

        final boolean joined = getUserData().joinedEvent(mEvent);
        button.setText(joined ? "Leave" : "Join");
        button.setBackgroundResource(joined ? R.drawable.red_button_selector : R.drawable.green_button_selector);
        button.setOnClickListener(v -> {
            UserPreference userPreference = getUserData().getOrNewMy(UserPreference.class);
            boolean joined1 = getUserData().joinedEvent(mEvent);
            if (!joined1 && userPreference.balance < UserPreference.EVENT_COST) {
                new AlertDialog.Builder(this)
                        .setTitle("Join failed")
                        .setMessage("Please buy more GymBuddy coins before join the event")
                        .setPositiveButton("OK", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .show();
                return;
            }
            new AlertDialog.Builder(this)
                    .setTitle(Util.getString("Are you sure to ", joined1 ? "leave" : "join", " this event?"))
                    .setMessage(joined1
                            ? Util.getString("You can not get your ", UserPreference.EVENT_COST, " GymBuddy coins back if you leave right now")
                            : Util.getString("This will cost you ", UserPreference.EVENT_COST, " GymBuddy coins. If you complete the event, you will win a reward."))
                    .setPositiveButton("OK", (dialog, which) -> {
                        updateEnrollment(userPreference, joined1);
                        dialog.dismiss();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        });
    }

    private void updateEnrollment(UserPreference userPreference, boolean joined1) {
        if (joined1) {
            userPreference.joinedEvents.remove(mEvent._id);
        } else {
            userPreference.balance -= UserPreference.EVENT_COST;
            userPreference.joinedEvents.add(mEvent._id);
        }
        getUserData().putMy(userPreference, userPreferenceOptional -> {
            if (userPreferenceOptional.isPresent()) {
                getUserData().updateJoinedList();
                ViewUtil.toast("You've ", Util.getString(joined1 ? "left" : "joined"), " the event");
                updateButton();
                return;
            }

            ViewUtil.toast("Failed to upload event");
        });
    }

    @Override
    int getContentViewId() {
        return R.layout.activity_event;
    }

    @Override
    public boolean acceptEvent(GBEvent gbEvent) {
        return gbEvent.what == GBEvent.Type.REFRESH_SCHEDULE;
    }

    @Override
    public void onAcceptedEvent(GBEvent GBEvent) {
        refreshUI();
    }

    @Override
    public void refreshUI() {
        super.refreshUI();
        if (mEvent._id != null) {
            getUserData().updateEventIfNeed(mEvent);
        }
    }
}
