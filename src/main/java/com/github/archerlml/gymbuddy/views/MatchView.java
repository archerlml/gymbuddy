package com.github.archerlml.gymbuddy.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.archerlml.gymbuddy.R;
import com.github.archerlml.gymbuddy.adapter.BaseListAdapter;
import com.github.archerlml.gymbuddy.model.Exercise;
import com.github.archerlml.gymbuddy.model.Match;
import com.github.archerlml.gymbuddy.util.Util;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by archerlml on 10/28/17.
 */

public class MatchView extends BaseView {

    @BindView(R.id.candidates)
    public RecyclerView mRecyclerView;

    @BindView(R.id.delete_layout)
    public View mDeleteLayout;

    @BindView(R.id.save_layout)
    public View mSaveLayout;

    @BindView(R.id.match_time)
    public TextView mMatchTime;

    @BindView(R.id.match_location)
    public TextView matchLocation;

    List<Match> matches = new ArrayList<>();
    Match mSelectedItem;

    public MatchView(@NonNull Context context) {
        super(context);
    }

    public MatchView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(AttributeSet attrs) {
        super.init(attrs);

        mRecyclerView.setAdapter(new BaseListAdapter<Match>() {

            @Override
            public void applyData(Match match, BaseViewHolder holder, int position) {
                Glide.with(getContext())
                        .load(match.peerAvatar)
                        .into(holder.getView(R.id.avatar, ImageView.class));
                holder.getView(R.id.name, TextView.class).setText(match.peerName);
                holder.getView(R.id.description, TextView.class).setText(match.description);
                mMatchTime.setText(Util.getString("Match time: ", match.toHHMM_HHMM()));
                matchLocation.setText(Util.getString("Location: ", match.locationName));
            }

            @Override
            public int itemLayout() {
                return R.layout.item_name_card;
            }

            @Override
            public List<Match> getItems() {
                return matches;
            }
        });
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean scrollTo = false;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    return;
                }

                LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
                int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();

                if (scrollTo) {
                    scrollTo = false;
                    mSelectedItem = Util.isEmpty(matches) ? null : matches.get(firstVisiblePosition);
                    return;
                }

                scrollTo = true;
                View v = layoutManager.getChildAt(0);
                int offsetRight = v.getRight();
                if (offsetRight < v.getWidth() / 2) {
                    recyclerView.smoothScrollToPosition(Math.min(firstVisiblePosition + 1,
                            recyclerView.getAdapter().getItemCount() - 1));
                } else {
                    recyclerView.smoothScrollToPosition(firstVisiblePosition);
                }
            }
        });
    }

    private void setFirstItemSelected() {
        mSelectedItem = Util.isEmpty(matches) ? null : matches.get(0);
    }

    public void showMatches(Exercise exercise, List<Match> matches) {
        if (exercise.match != null) {
            this.matches.clear();
            this.matches.add(Util.clone(exercise.match));
            setFirstItemSelected();
            mRecyclerView.getAdapter().notifyDataSetChanged();
            return;
        }

        this.matches.clear();
        this.matches.addAll(matches);
        setFirstItemSelected();
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public int layoutId() {
        return R.layout.match_view;
    }

}
