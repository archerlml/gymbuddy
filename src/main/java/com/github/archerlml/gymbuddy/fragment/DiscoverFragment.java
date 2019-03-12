package com.github.archerlml.gymbuddy.fragment;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.bumptech.glide.Glide;
import com.github.archerlml.gymbuddy.R;
import com.github.archerlml.gymbuddy.activity.CreateEventActivity;
import com.github.archerlml.gymbuddy.activity.EventActivity;
import com.github.archerlml.gymbuddy.adapter.BaseListAdapter;
import com.github.archerlml.gymbuddy.application.UserData;
import com.github.archerlml.gymbuddy.model.Event;
import com.github.archerlml.gymbuddy.util.Chain;
import com.github.archerlml.gymbuddy.util.GBEvent;
import com.github.archerlml.gymbuddy.util.MyEventBus;
import com.github.archerlml.gymbuddy.util.Util;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

import static com.github.archerlml.gymbuddy.util.GBEvent.Type.REFRESH_EVENT;

public class DiscoverFragment extends BaseFragment implements MyEventBus.Callback {
    @BindView(R.id.messageRecyclerView)
    RecyclerView mRecyclerView;

    public DiscoverFragment() {
        super();
    }

    List<Event> events = new ArrayList<>();

    @Override
    void onViewCreated(View view) {
        super.onViewCreated(view);

        getTitleBar().mTitleRightIcon.setOnClickListener(v -> {
            enterSubActivity(CreateEventActivity.class);
        });

        mRecyclerView.setAdapter(new BaseListAdapter<Event>() {

            @Override
            public void applyData(Event event, BaseViewHolder holder, int position) {
                Glide.with(getContext())
                        .load(Util.or(event.imageUrl, R.drawable.event))
                        .into(holder.getView(R.id.backdrop, ImageView.class));
                holder.getView(R.id.title, TextView.class).setText(event.title);
                holder.getView(R.id.description, TextView.class).setText(event.description);
                holder.root.setOnClickListener(v -> enterSubActivity(EventActivity.class, event));
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
    public void onStart() {
        super.onStart();
        if (getUserData().isAdmin()) {
            getTitleBar().mTitleRightIcon.setVisibility(View.VISIBLE);
            getTitleBar().mTitleRightIcon.setImageResource(R.drawable.ic_add_black_48px);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        getTitleBar().mTitleRightIcon.setVisibility(View.INVISIBLE);
    }

    @Override
    int layoutId() {
        return R.layout.fragment_event_list;
    }

    @Override
    void refreshUI() {
        super.refreshUI();

        Chain.from(getUserData().getList(Event.class, UserData.TAG_UNJOINED))
                .then((myEvents) -> {
                    events.clear();
                    events.addAll(Stream.of(myEvents).filter(event -> !event.isFinished()).toList());
                    mRecyclerView.post(() -> mRecyclerView.getAdapter().notifyDataSetChanged());
                })
                .or(() -> getUserData().requestEvents());
    }

    @Override
    public boolean acceptEvent(GBEvent gbEvent) {
        return gbEvent.what == REFRESH_EVENT;
    }

    @Override
    public void onAcceptedEvent(GBEvent gbEvent) {
        refreshUI();
    }
}
